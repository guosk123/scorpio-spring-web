import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import type { TrendChartData } from '@/pages/app/analysis/components/AnalysisChart';
import { jumpToSericeAnalysisTab } from '@/pages/app/analysis/Service/constant';
import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import storage from '@/utils/frame/storage';
import {
  bytesToSize,
  completeTimePoint,
  convertBandwidth,
  formatNumber,
  timeFormatter,
} from '@/utils/utils';
import { FullscreenExitOutlined, FullscreenOutlined, TableOutlined } from '@ant-design/icons';
import { useFullscreen } from 'ahooks';
import { Card, Menu, Space, Table, Tooltip } from 'antd';
import type { ColumnProps, TableProps } from 'antd/lib/table';
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { useParams } from 'umi';
import { connect, ERealTimeStatisticsFlag } from 'umi';
import { AnalysisContext } from '../../Analysis';
import { jumpToAnalysisTabNew } from '../../Analysis/constant';
import { ENetworkTabs } from '../../typing';
import { JUMP_TO_NEW_TAB } from '../LinkToAnalysis';

const DEFAULT_HEIGHT = 300;

export enum EFormatterType {
  /** 数量 */
  'count',
  /** 字节数 */
  'bytes',
  /** 带宽 */
  'bps',
  /** 包速率 */
  'pps',
  /** 网络延迟 */
  'ms',
  /** 每秒/个 */
  'perSecond',
  /** 百分比 */
  'percentage',
}

const drillMenuItemObj = [
  {
    text: '会话详单',
    key: ENetworkTabs.FLOWRECORD,
  },
  {
    text: '建连分析',
    key: ENetworkTabs.CONNECTION,
  },
  {
    text: '重传分析',
    key: ENetworkTabs.RETRANSMISSION,
  },
  {
    text: '流量分析',
    key: ENetworkTabs.FLOW,
  },
  {
    text: '数据包',
    key: ENetworkTabs.PACKET,
  },
  {
    text: '修改时间',
    key: 'time',
  },
];

function WidgetChart<RecordType extends Record<string, any>>({
  title,
  tableColumns = [],
  tableData = [],
  chartSeries = [],
  formatterType = EFormatterType.count,
  loading = false,
  realTimeStatisticsFlag,
  selectedTime,
  dispatch,
  markArea,
  onChangeTime = () => {},
}: {
  title: string;
  tableColumns: ColumnProps<RecordType>[];
  tableData: TableProps<RecordType>['dataSource'];
  // TODO: 确定类型
  chartSeries: (LineSeriesOption | BarSeriesOption)[];
  formatterType: EFormatterType;
  loading?: boolean;
  /** 实时刷新的标志 */
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
  selectedTime: { startTime: string; endTime: string; interval: number };
  dispatch: Dispatch;
  markArea?: any;
  onChangeTime?: any;
}) {
  const wrapRef = useRef<HTMLDivElement>();
  const [showTable, setShowTable] = useState<boolean>(false);
  const [wrapHeight, setWrapHeight] = useState<number>(DEFAULT_HEIGHT);
  const [isFullscreen, { toggleFullscreen: toggleFull }] = useFullscreen(wrapRef);
  const [brushTime, setBrushTime] = useState([0, 0]);
  const chartRef = useRef<any>();
  const param: IUriParams = useParams();

  const changeGlobalTime = useCallback(
    (from: number, to: number) => {
      const timeObj: IGlobalTime = {
        relative: false,
        type: ETimeType.CUSTOM,

        custom: [moment(from), moment(to)],
      };
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(timeObj),
      });
    },
    [dispatch],
  );
  const { serviceId }: IUriParams = useParams();
  const [state, widgetChartDispatch] = useContext(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );

  const handleMenuClick = useCallback(
    (info: any) => {
      if (info.key !== 'time') {
        const jumpToNewPage =
          JUMP_TO_NEW_TAB ||
          (storage.get('jumpToNew') === null ? false : storage.get('jumpToNew') === 'true');
        if (!jumpToNewPage) {
          changeGlobalTime(brushTime[0], brushTime[1]);
        }
        const filterArr = [
          {
            field: 'network_id',
            operator: EFilterOperatorTypes.EQ,
            operand: param.networkId || '',
          },
        ];
        if (param.serviceId) {
          filterArr.push({
            field: 'service_id',
            operator: EFilterOperatorTypes.EQ,
            operand: param.serviceId || '',
          });
        }
        console.log('brushTime', moment(brushTime[0]).format(), moment(brushTime[1]).format());
        jumpToAnalysisTabNew(state, widgetChartDispatch, info.key, {
          globalSelectedTime: {
            startTime: brushTime[0],
            endTime: brushTime[1],
          },
          filter: filterArr,
        });
      } else {
        onChangeTime();
        changeGlobalTime(brushTime[0], brushTime[1]);
      }

      chartRef.current?.hideMenu();
    },
    [changeGlobalTime, brushTime, state, widgetChartDispatch, param.serviceId, param.networkId],
  );

  const menu = useMemo(() => {
    return (
      <Menu onClick={handleMenuClick} style={{ fontSize: '12px' }}>
        {drillMenuItemObj.map((obj) => {
          return (
            <Menu.Item key={obj.key} style={{ margin: 0, height: '3em', lineHeight: '3em' }}>
              {obj.text}
            </Menu.Item>
          );
        })}
      </Menu>
    );
  }, [handleMenuClick]);

  const handleBrushEnd = useMemo(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) return undefined;

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
  }, [realTimeStatisticsFlag]);

  // 全屏情况下，调整高度
  useEffect(() => {
    // 全屏有个动画，不加setTimeout会无法获取正确的高度
    window.setTimeout(() => {
      const height = wrapRef.current?.clientHeight || DEFAULT_HEIGHT;
      setWrapHeight(height);
    }, 0);
  }, [wrapRef, isFullscreen]);

  const innerContentHeight = useMemo(() => {
    // 图表的高度 = 容器高度 - card头部高度 - card内容区域的 padding
    return wrapHeight - 40 - 12 * 2;
  }, [wrapHeight]);

  const columns = useMemo(() => {
    return tableColumns
      .filter((col) => {
        // 未开启实时刷新的话显示全部的
        if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
          return true;
        }
        const colTitle = (col.title || '') as string;
        // 排除掉上周同期
        // 排除掉峰值
        // 排除掉基线
        return (
          colTitle.indexOf('上周同期') === -1 &&
          colTitle.indexOf('峰值') === -1 &&
          colTitle.indexOf('基线') === -1
        );
      })
      .map((col) => {
        if (col.dataIndex === 'timestamp') {
          return {
            ...col,
            width: 180,
            align: 'center',
          };
        }
        return {
          ...col,
          ellipsis: true,
          width: 120,
          align: 'center',
        };
      }) as ColumnProps<any>[];
  }, [tableColumns, realTimeStatisticsFlag]);

  const handleTableClick = () => {
    setShowTable((prev) => {
      return !prev;
    });
  };

  const chartOption: ECOption = useMemo(() => {
    let seriesData = [...chartSeries];
    // 实时统计时，过滤掉特殊的统计
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN) {
      seriesData = seriesData.filter((row) => {
        const seriesName = row.name! as string;
        return (
          seriesName.indexOf('上周同期') === -1 &&
          seriesName.indexOf('峰值') === -1 &&
          seriesName.indexOf('基线') === -1
        );
      });
    }

    return {
      legend: {
        show: true,
        data: seriesData.map((data) => data.name) as string[],
      },
      grid: {
        // right: 20,
      },
      xAxis: {
        ...timeAxis,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => {
            let result: any = value;
            switch (formatterType) {
              case EFormatterType.bytes:
                result = bytesToSize(value);
                break;
              case EFormatterType.bps:
                result = convertBandwidth(value);
                break;
              case EFormatterType.percentage:
                result = `${value}%`;
                break;
              case EFormatterType.ms:
                result = `${value}ms`;
                break;
              // TODO: 其他情况
              default:
                result = formatNumber(value);
                break;
            }
            return result;
          },
        },
      },
      series: seriesData
        .map((series) => {
          const { data, ...rest } = series;
          return {
            ...rest,
            data:
              realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED
                ? completeTimePoint(
                    data as TrendChartData,
                    selectedTime.startTime,
                    selectedTime.endTime,
                    selectedTime.interval,
                  )
                : (data as TrendChartData),
          };
        })
        .concat([
          {
            type: 'line',
            data: [],
            markArea,
          },
        ]),
      tooltip: {
        formatter(params: any) {
          if (!Array.isArray(params)) {
            return '';
          }
          let label = '';
          const { axisValue } = params[0];
          label += `<b>${moment(axisValue).format('YYYY-MM-DD HH:mm:ss')}</b><br/>`;
          // 比较日期
          // if (moment(axisValue).format('YYYY-MM-DD') === moment(endTime).format('YYYY-MM-DD')) {
          //   label += `<b>${moment(startTime).format('YYYY-MM-DD HH:mm:ss')}-${moment(endTime).format(
          //     'HH:mm:ss',
          //   )}</b><br/>`;
          // } else {
          //   label += `<b>${moment(startTime).format('YYYY-MM-DD HH:mm:ss')}-${moment(endTime).format(
          //     'YYYY-MM-DD HH:mm:ss',
          //   )}</b><br/>`;
          // }
          params.forEach((item) => {
            label += `${item.marker}${item.seriesName}: `;
            const value = item && item.data[1];
            if (formatterType === EFormatterType.count) {
              label += numeral(value).format('0,0');
            } else if (formatterType === EFormatterType.bytes) {
              label += bytesToSize(value);
            } else if (formatterType === EFormatterType.bps) {
              label += convertBandwidth(value);
            } else if (formatterType === EFormatterType.percentage) {
              label += `${value}%`;
            } else {
              label += numeral(value).format('0,0');
              if (formatterType === EFormatterType.perSecond) {
                label += '个/s';
              } else if (formatterType === EFormatterType.pps) {
                label += 'pps';
              } else if (formatterType === EFormatterType.ms) {
                label += 'ms';
              }
            }
            label += '<br/>';
          });

          return label;
        },
      },
    };
  }, [
    chartSeries,
    formatterType,
    markArea,
    realTimeStatisticsFlag,
    selectedTime.endTime,
    selectedTime.interval,
    selectedTime.startTime,
  ]);

  return (
    // @ts-ignore
    <div ref={wrapRef}>
      <Card
        size="small"
        title={title}
        bodyStyle={{ height: isFullscreen ? `calc(100vh - 40px)` : DEFAULT_HEIGHT }}
        extra={
          <Space>
            <Tooltip title={`${showTable ? '关闭' : '打开'}表格预览`}>
              <span onClick={handleTableClick}>
                <TableOutlined style={{ fontSize: 16, color: showTable ? '#198ce1' : '' }} />
              </span>
            </Tooltip>

            <Tooltip title={isFullscreen ? '还原' : '全屏'}>
              <span onClick={() => toggleFull()}>
                {isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
              </span>
            </Tooltip>
          </Space>
        }
      >
        {showTable ? (
          //  TODO: 虚拟表格
          <Table
            rowKey="timestamp"
            bordered
            size="small"
            loading={loading}
            columns={columns}
            dataSource={tableData}
            pagination={false}
            style={{ height: innerContentHeight }}
            // 表头高度40px
            scroll={{ y: innerContentHeight - 40, x: 'max-content' }}
          />
        ) : (
          <ReactECharts
            ref={chartRef}
            key={`${realTimeStatisticsFlag}`}
            option={chartOption}
            loading={loading}
            style={{ height: '100%' }}
            onBrushEnd={handleBrushEnd}
            brushMenuChildren={menu}
          />
        )}
      </Card>
    </div>
  );
}

export default connect(({ appModel: { realTimeStatisticsFlag } }: ConnectState) => ({
  realTimeStatisticsFlag,
}))(WidgetChart);
