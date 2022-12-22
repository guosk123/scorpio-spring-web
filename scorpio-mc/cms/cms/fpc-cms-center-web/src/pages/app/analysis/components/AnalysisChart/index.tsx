import type { IFilterCondition } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { camelCase, getLinkUrl, jumpNewPage, timeFormatter } from '@/utils/utils';
import { Menu } from 'antd';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import React, { useCallback, useContext, useMemo, useRef, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { useParams } from 'umi';
import { connect } from 'umi';
import { ServiceAnalysisContext } from '../../Service/index';
import { jumpToSericeAnalysisTab } from '../../Service/constant';
import type { IUriParams } from '../../typings';
import { JUMP_TO_NEW_TAB } from '@/pages/app/Network/components/LinkToAnalysis';
import storage from '@/utils/frame/storage';

const height = 260;

type Time = string | number;
type Value = number;
type SerieName = string;
export type TrendChartData = [Time, Value][];

type Data = Record<SerieName, TrendChartData>;

interface IFlowAnalysisChartProps {
  data: Data;
  unitConverter?: (value: number) => string;
  loading?: boolean;
  dispatch: Dispatch;
  networkId?: string;
  serviceId?: string;
  brushMenus?: { text: string; key: string }[];
  filterCondition?: IFilterCondition;
  seriesOrder?: string[];
  selectedTimeInfo?:
    | IGlobalSelectedTime
    | { startTime: string; endTime: string; interval: number; totalSeconds: number };
  selectRowToFilter?: [{ field: string; operator: any; operand: any }];
  isDilldownChart?: boolean;
}

const AnalysisChart: React.FC<IFlowAnalysisChartProps> = (props) => {
  const {
    data,
    unitConverter,
    loading,
    filterCondition = [],
    selectedTimeInfo,
    brushMenus = [],
    selectRowToFilter,
    seriesOrder: tmpSeriesOrder,
    dispatch,
    isDilldownChart = true,
  } = props;
  const seriesOrder = tmpSeriesOrder?.length ? [...new Set(tmpSeriesOrder)] : undefined;
  const { networkId, serviceId }: IUriParams = useParams();

  const [brushTime, setBrushTime] = useState([0, 0]);
  const chartRef = useRef<any>();

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
  const [state, widgetChartDispatch] = useContext(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );
  const originStartTime = useMemo(() => {
    return (selectedTimeInfo as IGlobalTime)?.originStartTime || selectedTimeInfo?.startTime;
  }, [selectedTimeInfo]);

  const originEndTime = useMemo(() => {
    return (selectedTimeInfo as IGlobalTime)?.originEndTime || selectedTimeInfo?.endTime;
  }, [selectedTimeInfo]);

  const jumpToPacket = useCallback(() => {
    // 数据包页面支持的过滤类型
    const packetSupportFilterType = [
      'country_id',
      'province_id',
      'application_id',
      'l7_protocol_id',
      'port',
      // 'ip_protocol',
      'mac_address',
      'ip_address',
    ];
    // 对过滤条件进行过滤
    const packetSupportFilter = filterCondition
      .map((sub: any) => {
        if (!sub.hasOwnProperty('operand') && sub.group.length < 2) {
          return sub.group[0];
        }
        return sub;
      })
      .filter(
        (item: any) =>
          item.hasOwnProperty('operand') &&
          ((item.field === 'ip_protocol' &&
            ['TCP', 'UDP', 'ICMP', 'SCTP'].includes(item.operandText) &&
            item.operator === '=') ||
            (packetSupportFilterType.includes(item.field) && item.operator === '=')),
      )
      // 整理过滤条件结构
      .map((ele: any) => ({
        field: camelCase(ele.field),
        operator: ele.operator,
        operand: ele.field === 'ip_protocol' ? ele.operand.toLocaleUpperCase() : ele.operand,
      }));
    if (selectRowToFilter?.length) {
      selectRowToFilter.forEach((sub) => {
        packetSupportFilter.push(sub);
      });
    }
  }, [filterCondition, networkId, originEndTime, originStartTime, selectRowToFilter, serviceId]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const handleMenuClick = (info: any) => {
    if (info.key !== 'time') {
      const jumpToNewPage =
        JUMP_TO_NEW_TAB ||
        (storage.get('jumpToNew') === null ? false : storage.get('jumpToNew') === 'true');
      if (!jumpToNewPage) {
        changeGlobalTime(brushTime[0], brushTime[1]);
      }
      jumpToAnalysisTabNew(state, widgetChartDispatch, info.key, {
        globalSelectedTime: { startTime: brushTime[0], endTime: brushTime[1] },
      });
    } else {
      changeGlobalTime(brushTime[0], brushTime[1]);
    }
    chartRef.current?.hideMenu();
  };

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

  const options = useMemo<ECOption>(() => {
    return {
      grid: {
        bottom: 30,
      },
      xAxis: {
        ...timeAxis,
        minInterval: 20 * 1000,
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => (unitConverter ? unitConverter(value) : value.toString()),
        },
      },
      series: [
        ...(seriesOrder
          ? seriesOrder.map((seriesName) => {
              return {
                type: 'line',
                emphasis: {
                  focus: 'series',
                },
                stack: '总量',
                areaStyle: {},
                // smooth: true,
                name: seriesName,
                data: data[seriesName],
              } as LineSeriesOption;
            })
          : Object.keys(data).map((seriesName) => {
              return {
                type: 'line',
                emphasis: {
                  focus: 'series',
                },
                stack: '总量',
                areaStyle: {},
                // smooth: true,
                name: seriesName,
                data: data[seriesName],
              } as LineSeriesOption;
            })),
      ],
      legend: {
        show: true,
        height: 30,
        bottom: 0,
      },
      tooltip: unitConverter
        ? {
            formatter: (params: any) => {
              let label = `${params.lastItem.axisValueLabel}<br/>`;
              for (let i = 0; i < params.length; i += 1) {
                label += `${params[i].marker}${params[i].seriesName}: ${unitConverter(
                  params[i].value[1],
                )}<br/>`;
              }
              return label;
            },
          }
        : {},
    };
  }, [data, seriesOrder, unitConverter]);

  return (
    <ReactECharts
      ref={chartRef}
      loading={loading}
      option={options}
      opts={{ height }}
      notMerge={false}
      needPrettify={false}
      onBrushEnd={handleBrushEnd}
      brushMenuChildren={isDilldownChart ? menu : <div style={{ display: 'none' }} />}
    />
  );
};

export default connect()(AnalysisChart);
