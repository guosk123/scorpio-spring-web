import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { EServiceTabs } from '@/pages/app/analysis/Service/typing';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import type { TrendChartData } from '@/utils/utils';
import { timeFormatter } from '@/utils/utils';
import { TableOutlined } from '@ant-design/icons';
import type { TableColumnProps } from 'antd';
import { Card, Menu, Select, Space, Table, Tooltip } from 'antd';
import moment from 'moment';
import React, { useCallback, useContext, useMemo, useRef, useState } from 'react';
import { useParams } from 'umi';
import type { IMarkArea } from '@/pages/app/analysis/components/MultipleSourceTrend';
import { JUMP_TO_NEW_TAB } from '../../../LinkToAnalysis';
import storage from '@/utils/frame/storage';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';

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
  onBrush?: (startTime: number, endTime: number) => void;
  markArea?: IMarkArea;
  onChangeTime?: any;
}

const MultipleSourceLine: React.FC<IMultipleSourceLineProps> = ({
  title,
  dataList,
  height = 300,
  loading = false,
  brushMenus = [],
  onBrush,
  markArea = {},
  onChangeTime = () => {},
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
  const { networkId, serviceId }: IUriParams = useParams();

  const [state, dispatch] = useContext(serviceId ? ServiceAnalysisContext : AnalysisContext);
  const handleMenuClick = useCallback(
    (info) => {
      if (onBrush) {
        if (info.key !== 'time') {
          // onBrush(brushTime[0], brushTime[1]);
          const jumpToNewPage =
            JUMP_TO_NEW_TAB ||
            (storage.get('jumpToNew') === null ? false : storage.get('jumpToNew') === 'true');
          if (!jumpToNewPage) {
            onBrush(brushTime[0], brushTime[1]);
          }
          const filterArr = [];
          if (networkId) {
            filterArr.push({
              field: 'network_id',
              operator: EFilterOperatorTypes.EQ,
              operand: networkId || '',
            });
          }
          if (serviceId) {
            filterArr.push({
              field: 'service_id',
              operator: EFilterOperatorTypes.EQ,
              operand: serviceId || '',
            });
          }
          jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.PACKET, {
            globalSelectedTime: {
              startTime: brushTime[0],
              endTime: brushTime[1],
            },
            filter: filterArr,
          });
        } else {
          onChangeTime();
          onBrush(brushTime[0], brushTime[1]);
        }
        chartRef.current?.hideMenu();
      }
    },
    [brushTime, dispatch, onBrush, state],
  );

  const menu = useMemo(() => {
    const tmpMenus = brushMenus.concat([{ text: '修改时间', key: 'time' }]);
    return (
      <Menu onClick={handleMenuClick}>
        {tmpMenus.map((item) => {
          return <Menu.Item key={item.key}>{item.text}</Menu.Item>;
        })}
      </Menu>
    );
  }, [brushMenus, handleMenuClick]);

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
  }, [dataList, dataSource]);

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
        />
      )}
    </Card>
  );
};

export default MultipleSourceLine;
