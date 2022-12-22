import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { formatNumber, getLinkUrl, jumpNewPage, timeFormatter } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import type { TableColumnProps } from 'antd';
import { Card, Menu, Select, Space, Table, Tooltip } from 'antd';
import { isArray } from 'lodash';
import moment from 'moment';
import numeral from 'numeral';
import React, { useCallback, useMemo, useRef, useState } from 'react';
import type { TrendChartData } from '../AnalysisChart';

enum EDataSource {
  'TREND',
  'PERCENTAGE',
}

const dataSourceList = [
  {
    label: '趋势图',
    value: EDataSource.TREND,
  },
  {
    label: '占比图',
    value: EDataSource.PERCENTAGE,
  },
];
interface Data {
  barChartData: Record<string, number>;
  lineChartData: Record<string, TrendChartData>;
}

export interface IMarkArea {
  itemStyle: any;
  data: any;
}

interface IMultipleSourceTrend {
  title: string;
  percentTableLabelTitle: string;
  percentTableValueTitle: string;
  data: Data;
  unitConverter?: (value: number) => string;
  height?: number;
  loading?: boolean;
  onBrush?: (startTime: number, endTime: number) => void;
  networkId?: string;
  serviceId?: string;
  brushMenus?: { text: string; key: string }[];
  stacked?: boolean; // 是否为堆叠柱状图
  rightAxisName?: string; // 第二坐标轴名称，数据名称
  disableChangeTime?: boolean; // 关闭修改时间选项
  isRefreshPage?: boolean; // 是否为可以实时刷新的页面
  markArea?: IMarkArea;
}

const MultipleSourceTrend: React.FC<IMultipleSourceTrend> = (props) => {
  const {
    title,
    data,
    unitConverter,
    percentTableLabelTitle,
    percentTableValueTitle,
    height = 300,
    loading = false,
    networkId,
    serviceId,
    brushMenus = [],
    onBrush,
    stacked = false,
    rightAxisName = '',
    disableChangeTime = false,
    isRefreshPage,
    markArea = {},
  } = props;
  const { barChartData, lineChartData } = data;

  const [dataSource, setDataSource] = useState<EDataSource>(EDataSource.TREND);
  const [showTable, setShowTable] = useState<boolean>(false);

  const [brushTime, setBrushTime] = useState([0, 0]);

  const chartRef = useRef<any>();

  const handleTableClick = () => {
    setShowTable((prev) => {
      return !prev;
    });
  };

  const filter = useMemo(() => {
    const tmp: IFilter[] = [];
    if (networkId) {
      tmp.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: networkId,
      });
    }

    if (serviceId) {
      tmp.push({
        field: 'service_id',
        operator: EFilterOperatorTypes.EQ,
        operand: serviceId,
      });
    }
    return tmp;
  }, [networkId, serviceId]);

  const handleMenuClick = useCallback(
    (info) => {
      if (onBrush) {
        if (info.key !== 'time') {
          const url = getLinkUrl(
            `/analysis/trace/${info.key}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${
              ETimeType.CUSTOM
            }&filter=${encodeURIComponent(JSON.stringify(filter))}`,
          );
          jumpNewPage(url);
        } else {
          onBrush(brushTime[0], brushTime[1]);
        }
        chartRef.current?.hideMenu();
      }
    },
    [brushTime, onBrush, filter],
  );

  const menu = useMemo(() => {
    let tmpMenus = brushMenus;
    if (!disableChangeTime) {
      tmpMenus = brushMenus.concat([{ text: '修改时间', key: 'time' }]);
    }
    return (
      <Menu onClick={handleMenuClick}>
        {tmpMenus.map((item) => {
          return <Menu.Item key={item.key}>{item.text}</Menu.Item>;
        })}
      </Menu>
    );
  }, [brushMenus, disableChangeTime, handleMenuClick]);

  const handleBrushEnd = useMemo(() => {
    if (dataSource === EDataSource.TREND && showTable === false && onBrush) {
      const internalHandleBrushEnd = (from: number, to: number) => {
        // 时间跨度小于两分钟时;
        if ((to - from) / 1000 < 120) {
          const diffSeconds = 120 - (to - from) / 1000;
          const offset = diffSeconds / 2;
          setBrushTime([from - offset * 1000, to + offset * 1000]);
        } else {
          const { startTime, endTime } = timeFormatter(from, to);
          setBrushTime([new Date(startTime).valueOf(), new Date(endTime).valueOf() + 60 * 1000]);
        }
      };
      return internalHandleBrushEnd;
    }
    return undefined;
  }, [dataSource, onBrush, showTable]);

  const trendTableColumns: TableColumnProps<any>[] = [
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
      width: '180px',
      align: 'center' as any,
      render: (value: any) => {
        return moment(value).format('YYYY-MM-DD HH:mm:ss');
      },
    },

    ...Object.keys(lineChartData).map((key) => {
      return {
        title: key,
        dataIndex: key,
        width: key.length * 14 + 20,
        align: 'center' as any,
        key,
        render: (value: number) => {
          let tmpValue = value;
          if (!tmpValue) {
            tmpValue = 0;
          }
          if (unitConverter) {
            return unitConverter(tmpValue);
          }

          return tmpValue ? numeral(tmpValue).format('0,0') : tmpValue;
        },
      };
    }),
  ];

  const percentTableColumns: TableColumnProps<any>[] = [
    {
      title: `${percentTableLabelTitle}`,
      dataIndex: 'label',
      align: 'center',
      width: '50%',
    },
    {
      title: `${percentTableValueTitle}`,
      dataIndex: 'value',
      align: 'center',
      render: (value: any) => {
        if (unitConverter) {
          return unitConverter(value);
        }
        return value ? numeral(value).format('0,0') : value;
      },
    },
  ];

  const lineChartSeries: any[] = [];
  Object.keys(lineChartData).forEach((key) => {
    if (key !== rightAxisName) {
      if (stacked) {
        lineChartSeries.push({
          name: key,
          type: 'bar',
          stack: 'stack',
          data: lineChartData[key],
        });
      } else {
        lineChartSeries.push({
          name: key,
          type: 'line',
          // smooth: 'true',
          data: lineChartData[key],
        });
      }
    }
  });

  if (rightAxisName && lineChartData[rightAxisName] && dataSource === EDataSource.TREND) {
    lineChartSeries.push({
      name: rightAxisName,
      type: 'line',
      yAxisIndex: 1,
      data: lineChartData[rightAxisName],
    });
  }

  lineChartSeries.push({
    type: 'line',
    data: [],
    markArea,
  });

  const lineChartOption: ECOption = {
    grid: {
      bottom: 30,
      top: rightAxisName ? 30 : 10,
      containLabel: true, // grid 区域是否包含坐标轴的刻度标签，
    },
    xAxis: {
      ...timeAxis,
    },
    yAxis: [
      {
        type: 'value',
        position: 'left',
        axisLabel: {
          formatter: (value: number) =>
            unitConverter ? unitConverter(value) : formatNumber(value),
        },
      },
      rightAxisName && lineChartData[rightAxisName] && dataSource === EDataSource.TREND
        ? {
            type: 'value',
            name: rightAxisName,
            position: 'right',
            min: 0,
            // fix: 双 Y 轴刻度不对齐的问题
            // 通用的解决方案是：设置 min，max，splitnumber，计算出 interval，具体可以看下述链接：
            // @see: https://github.com/apache/echarts/issues/5722
            // 但是存在一个问题，计算出来的 interval 有时候会是 Y 轴刻度并不好看
            // 所以为了简化处理，直接把右侧的 Y 轴刻度线隐藏即可
            splitLine: { show: false },
            axisLabel: {
              formatter: (value: number) =>
                unitConverter ? unitConverter(value) : formatNumber(value),
            },
          }
        : {},
    ],
    series: lineChartSeries,
    legend: {
      show: true,
      bottom: 0,
      height: 30,
      itemWidth: 12,
    },
    tooltip: {
      formatter: (params: any) => {
        let label = `${params.lastItem.axisValueLabel}<br/>`;
        for (let i = 0; i < params.length; i += 1) {
          label += `${params[i].marker}${params[i].seriesName}: ${
            unitConverter
              ? unitConverter(params[i].value[1])
              : numeral(params[i].value[1]).format('0,0')
          }<br/>`;
        }
        return label;
      },
      // 这样其实可以时提示框好看一些
      // @see: https://echarts.apache.org/examples/zh/editor.html?c=area-rainfall
      // trigger: 'axis',
      // axisPointer: {
      //   type: 'cross',
      //   animation: false,
      //   label: {
      //     backgroundColor: '#505765',
      //   },
      // },
    },
  };

  const barChartXAxis = Object.keys(barChartData);
  const barChartSeries: any[] = [];
  barChartSeries.push({
    name: title,
    type: 'bar',
    data: Object.keys(barChartData).map((key) => {
      return barChartData[key];
    }),
  });

  const barChartOption: ECOption = {
    grid: {
      // 没有图例，底部没有必要预留空间
      bottom: 0,
    },
    xAxis: {
      type: 'category',
      data: barChartXAxis,
      axisTick: {
        show: false,
      },
      axisLine: {
        show: false,
      },
    },
    yAxis: {
      type: 'value',
      position: 'left',
      axisLabel: {
        formatter: (value: number) =>
          unitConverter ? `${unitConverter(value)}` : `${formatNumber(value)}`,
      },
    },
    series: barChartSeries,
    legend: {
      show: false,
    },
    tooltip: {
      formatter: (params: any) => {
        const point = params[0];
        let label = `<b>${point.seriesName}</b><br/>`;
        label += `${point.marker}${point.name}: `;
        label += unitConverter ? unitConverter(point.value) : numeral(point.value).format('0,0');
        return label;
      },
    },
  };

  const trendTableData = useMemo(() => {
    let res: any[] = [];
    if (showTable) {
      console.log('keys');
      const hasTrendData = Object.keys(lineChartData).length > 0;
      if (hasTrendData) {
        const firstKey = Object.keys(lineChartData)[0];
        const len = isArray(lineChartData[firstKey]) && lineChartData[firstKey].length;
        if (len) {
          res = Array(len)
            .fill(1)
            .map(() => ({}));
          // 取时间
          lineChartData[firstKey].forEach((value, index) => {
            res[index] = { time: value[0] };
          });
          Object.keys(lineChartData).forEach((key) => {
            lineChartData[key].forEach((value, index) => {
              res[index] = {
                ...res[index],
                [key]: value[1],
              };
            });
          });
        }
      }
    }
    return res;
  }, [lineChartData, showTable]);

  const percentTableData: any[] = [];
  Object.keys(barChartData).forEach((key) => {
    percentTableData.push({
      label: key,
      value: barChartData[key],
    });
  });

  // select组件，切换选项回调
  const handleDisplayChange = (value: EDataSource) => {
    setDataSource(value);
  };

  const extra = (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <Space>
        <Select
          value={dataSource}
          size="small"
          style={{ width: 120 }}
          onChange={handleDisplayChange}
        >
          {dataSourceList.map((item) => {
            return (
              <Select.Option value={item.value} key={item.value}>
                {item.label}
              </Select.Option>
            );
          })}
        </Select>
        <Tooltip title={`${showTable ? '关闭' : '打开'}表格预览`}>
          <TableOutlined
            style={{ fontSize: 16, color: showTable ? '#198ce1' : '' }}
            onClick={handleTableClick}
          />
        </Tooltip>
      </Space>
    </div>
  );

  return (
    <Card size="small" title={`${title}`} extra={extra}>
      {showTable ? (
        <Table
          rowKey={(record) => record.time || record.label}
          bordered
          size="small"
          loading={loading}
          columns={dataSource === EDataSource.TREND ? trendTableColumns : percentTableColumns}
          dataSource={dataSource === EDataSource.TREND ? trendTableData : percentTableData}
          pagination={false}
          style={{ height }}
          // 表头高度40px
          scroll={{ y: height - 40, x: 'max-content' }}
        />
      ) : (
        <>
          <ReactECharts
            key={dataSource}
            ref={chartRef}
            needPrettify={false}
            loading={loading}
            option={dataSource === EDataSource.TREND ? lineChartOption : barChartOption}
            opts={{ height }}
            onBrushEnd={handleBrushEnd}
            brushMenuChildren={menu}
            isRefreshPage={isRefreshPage}
          />
        </>
      )}
    </Card>
  );
};

export default MultipleSourceTrend;
