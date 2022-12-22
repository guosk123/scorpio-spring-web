import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { getLinkUrl, jumpNewPage, timeFormatter } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import type { TableColumnProps } from 'antd';
import { Card, Menu, Select, Space, Table, Tooltip } from 'antd';
import moment from 'moment';
import React, { useCallback, useMemo, useRef, useState } from 'react';
import type { TrendChartData } from '../AnalysisChart';
import type { IMarkArea } from '../MultipleSourceTrend';

interface Data {
  zhName: string;
  name: string;
  unitConverter?: any;
  data: TrendChartData;
}

interface IMultipleSourceLineProps {
  title: string;
  dataList: Data[];
  height?: number;
  loading?: boolean;
  networkId?: string;
  serviceId?: string;
  brushMenus?: { text: string; key: string }[];
  markArea?: IMarkArea;
  disableChangeTime?: boolean;
  isRefreshPage?: boolean;
  onBrush?: (startTime: number, endTime: number) => void;
}

const MultipleSourceLine: React.FC<IMultipleSourceLineProps> = ({
  title,
  dataList,
  height = 300,
  loading = false,
  networkId,
  serviceId,
  brushMenus = [],
  markArea = {},
  disableChangeTime = false,
  isRefreshPage,
  onBrush,
}) => {
  const [showTable, setShowTable] = useState<boolean>(false);
  const [dataSource, setDataSource] = useState<string>(dataList[0].name);

  const [brushTime, setBrushTime] = useState([0, 0]);

  const chartRef = useRef<any>();

  const selectList = useMemo(() => {
    return dataList.map((item) => {
      return {
        label: item.zhName,
        value: item.name,
      };
    });
  }, [dataList]);

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
    if (showTable === false && onBrush) {
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
  }, [onBrush, showTable]);

  const currentData = useMemo(() => {
    const current = dataList.find((data) => {
      return data.name === dataSource;
    }) as Data;

    const { unitConverter, data, zhName } = current;

    const tableColumns: TableColumnProps<any>[] = [
      {
        title: '时间',
        dataIndex: 'time',
        key: 'time',
        width: '50%',
        align: 'center' as any,
        render: (value: any) => {
          return moment(value).format('YYYY-MM-DD HH:mm:ss');
        },
      },
      {
        title: zhName,
        dataIndex: 'dataValue',
        key: 'dataValue',
        align: 'center' as any,
      },
    ];

    const lineChartSeries: any[] = [
      {
        name: zhName,
        type: 'line',
        // smooth: 'true',
        data,
      },
    ];

    lineChartSeries.push({
      type: 'line',
      data: [],
      markArea,
    });

    const option: ECOption = {
      grid: {
        bottom: 30,
      },
      xAxis: {
        ...(timeAxis as any),
        axisLabel: {
          overflow: 'break',
          ...timeAxis.axisLabel,
        },
      },

      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => (unitConverter ? unitConverter(value) : (value as any)),
        },
      },
      series: lineChartSeries,
      tooltip: {
        formatter: (params: any) => {
          let label = `${params.lastItem.axisValueLabel}<br/>`;
          for (let i = 0; i < params.length; i += 1) {
            label += `${params[i].marker}${params[i].seriesName}: ${
              unitConverter ? unitConverter(params[i].value[1]) : params[i].value[1]
            }<br/>`;
          }
          return label;
        },
      },
    };

    const tableData: any[] =
      data &&
      data.map((item) => {
        return {
          time: item[0],
          dataValue: unitConverter ? unitConverter(item[1]) : item[1],
        };
      });

    return {
      option,
      tableData,
      tableColumns,
    };
  }, [dataList, dataSource, markArea]);

  const handleTableClick = () => {
    setShowTable((prev) => {
      return !prev;
    });
  };

  const handleDisplayChange = (selectValue: string) => {
    setDataSource(selectValue);
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
          {selectList.map((item) => {
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
    <Card size="small" title={title} extra={extra}>
      {showTable ? (
        <Table
          rowKey={(record) => record.time}
          bordered
          size="small"
          loading={loading}
          columns={currentData.tableColumns}
          dataSource={currentData.tableData}
          pagination={false}
          style={{ height }}
          // 表头高度40px
          scroll={{ y: height - 40, x: 'max-content' }}
        />
      ) : (
        <ReactECharts
          loading={loading}
          option={currentData.option}
          opts={{ height }}
          needPrettify={false}
          onBrushEnd={handleBrushEnd}
          brushMenuChildren={menu}
          isRefreshPage={isRefreshPage}
        />
      )}
    </Card>
  );
};

export default MultipleSourceLine;
