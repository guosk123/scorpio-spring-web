import AutoHeightContainer from '@/components/AutoHeightContainer';
import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { packetUrl } from '@/pages/app/appliance/Packet';
import { camelCase, getLinkUrl, jumpNewPage, timeFormatter } from '@/utils/utils';
import { Menu } from 'antd';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import React, { useCallback, useMemo, useRef, useState } from 'react';
import type { Dispatch, IGlobalSelectedTime } from 'umi';
import { connect, useParams } from 'umi';
import type { IUriParams } from '../../typings';
import type { IMarkArea } from '../MultipleSourceTrend';

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
  markArea?: IMarkArea;
}

const AnalysisChart: React.FC<IFlowAnalysisChartProps> = (props) => {
  const {
    data,
    unitConverter,
    loading,
    filterCondition = [],
    brushMenus = [],
    selectRowToFilter,
    seriesOrder: tmpSeriesOrder,
    dispatch,
    markArea = {},
    networkId,
  } = props;
  const seriesOrder = tmpSeriesOrder?.length ? [...new Set(tmpSeriesOrder)] : undefined;
  const { networkId: urlNetworkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [currentNetworkId] = useState(() => {
    if (urlNetworkId) {
      return urlNetworkId;
    }
    return networkId;
  });

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

  const jumpToPacket = useCallback(
    (startTime, endTime) => {
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
      if (serviceId) {
        packetSupportFilter.push({
          field: 'service_id',
          operator: EFilterOperatorTypes.EQ,
          operand: serviceId,
        });
      }
      if (currentNetworkId) {
        packetSupportFilter.push({
          field: 'network_id',
          operator: EFilterOperatorTypes.EQ,
          operand: currentNetworkId,
        });
      }
      jumpNewPage(
        getLinkUrl(
          `${packetUrl}?&filter=${encodeURIComponent(
            JSON.stringify(packetSupportFilter),
          )}&from=${new Date(startTime!).valueOf()}&to=${new Date(endTime!).valueOf()}&timeType=${
            ETimeType.CUSTOM
          }`,
        ),
      );
    },
    [filterCondition, currentNetworkId, selectRowToFilter, serviceId],
  );

  const handleMenuClick = useCallback(
    (info) => {
      if (info.key !== 'time') {
        jumpToPacket(brushTime[0], brushTime[1]);
      } else {
        changeGlobalTime(brushTime[0], brushTime[1]);
      }
      chartRef.current?.hideMenu();
    },
    [brushTime, changeGlobalTime, jumpToPacket],
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
      ].concat([
        {
          type: 'line',
          data: [],
          markArea,
        },
      ]),
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
  }, [data, markArea, seriesOrder, unitConverter]);

  return (
    <ReactECharts
      ref={chartRef}
      loading={loading}
      option={options}
      opts={{ height }}
      notMerge={false}
      needPrettify={false}
      onBrushEnd={pcapFileId ? undefined : handleBrushEnd}
      brushMenuChildren={menu}
    />
  );
};

export default connect()(AnalysisChart);
