import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import { refreshFlagFn } from '@/components/PlayButton';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import type { ConnectState } from '@/models/connect';
import {
  bytesToSize,
  convertBandwidth,
  formatNumber,
  getLinkUrl,
  jumpNewPage,
  timeFormatter,
} from '@/utils/utils';
import { FullscreenExitOutlined, FullscreenOutlined, TableOutlined } from '@ant-design/icons';
import { useFullscreen } from 'ahooks';
import { Card, Menu, Space, Table, Tooltip } from 'antd';
import type { ColumnProps, TableProps } from 'antd/lib/table';
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import numeral from 'numeral';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, ERealTimeStatisticsFlag, useParams } from 'umi';
import type { IUriParams } from '../../typings';
import type { TrendChartData } from '../AnalysisChart';
import { completeTimePoint } from '../FlowAnalysis';

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
    key: 'flow-record',
  },
  {
    text: '建连分析',
    key: 'tcp/connection/error',
  },
  {
    text: '重传分析',
    key: 'tcp/retransmission',
  },
  {
    text: '流量分析',
    key: 'flow/location',
  },
  {
    text: '数据包',
    key: 'packet',
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
  markArea = {},
  isRefreshPage,
  disableChangeTime = false,
  dispatch,
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
  markArea?: any;
  isRefreshPage?: boolean;
  // 保留字段，如果非实时状态并且暂停更新仍然可以更新时间，应传入 realTimeStatisticsFlag && is isStopLoading
  disableChangeTime?: boolean;
  dispatch: Dispatch;
}) {
  const wrapRef = useRef<HTMLDivElement>();
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [showTable, setShowTable] = useState<boolean>(false);
  const [wrapHeight, setWrapHeight] = useState<number>(DEFAULT_HEIGHT);
  const [isFullscreen, { toggleFullscreen: toggleFull }] = useFullscreen(wrapRef);
  const [brushTime, setBrushTime] = useState([0, 0]);
  const chartRef = useRef<any>();

  const urlFragment = useMemo(() => {
    let res = '';
    if (pcapFileId) {
      res = `offline/${pcapFileId}`;
    } else if (serviceId) {
      res = `service/${serviceId}/${networkId}`;
    } else if (networkId) {
      res = `network/${networkId}`;
    }
    return res;
  }, [networkId, serviceId, pcapFileId]);

  const changeGlobalTime = useCallback(
    (from: number, to: number) => {
      const timeObj: IGlobalTime = {
        relative: false,
        type: ETimeType.CUSTOM,

        custom: [moment(from), moment(to)],
      };
      refreshFlagFn(true, dispatch);
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(timeObj),
      });
    },
    [dispatch],
  );

  const handleMenuClick = useCallback(
    (info: any) => {
      if (info.key !== 'time') {
        const url = getLinkUrl(
          `/analysis/${urlFragment}/${info.key}?from=${brushTime[0]}&to=${brushTime[1]}&timeType=${ETimeType.CUSTOM}`,
        );
        jumpNewPage(url);
      } else {
        changeGlobalTime(brushTime[0], brushTime[1]);
      }

      chartRef.current?.hideMenu();
    },
    [urlFragment, changeGlobalTime, brushTime],
  );

  const menu = useMemo(() => {
    return (
      <Menu onClick={handleMenuClick} style={{ fontSize: '12px' }}>
        {drillMenuItemObj
          .filter((ele) => {
            if (disableChangeTime) {
              return ele.key !== 'time';
            }
            return pcapFileId ? ele.key !== 'packet' : true;
          })
          .map((obj) => {
            return (
              <Menu.Item key={obj.key} style={{ margin: 0, height: '3em', lineHeight: '3em' }}>
                {obj.text}
              </Menu.Item>
            );
          })}
      </Menu>
    );
  }, [disableChangeTime, handleMenuClick, pcapFileId]);

  const handleBrushEnd = useMemo(() => {
    // if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.OPEN && !isStopLoading) {
    //   return undefined;
    // }

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
  }, []);

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
        // top: 30,
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
            let value = 0;
            if (item && item?.data.length >= 2) {
              value = item.data[1];
            }
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
            isRefreshPage={isRefreshPage}
          />
        )}
      </Card>
    </div>
  );
}

export default connect(({ appModel: { realTimeStatisticsFlag } }: ConnectState) => {
  return {
    realTimeStatisticsFlag,
  };
})(WidgetChart);
