import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { timeFormatter } from '@/utils/utils';
import { Menu } from 'antd';
import type { LineSeriesOption } from 'echarts/charts';
import moment from 'moment';
import React, { useCallback, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';

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
  brushMenus?: { text: string; key: string }[];
  seriesOrder?: string[];
  selectedTimeInfo?: IGlobalTime;
  selectRowToFilter?: [{ field: string; operator: any; operand: any }];
  // 是否需要下钻菜单
  isDilldownChart?: boolean;
}

const DimensionsChart: React.FC<IFlowAnalysisChartProps> = (props) => {
  const { data, unitConverter, loading, brushMenus = [], dispatch, isDilldownChart = true } = props;
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

  const jumpToPacket = useCallback(() => {}, []);

  const handleMenuClick = useCallback(
    (info) => {
      if (info.key !== 'time') {
        jumpToPacket();
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
        ...Object.keys(data).map((seriesName) => {
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
        }),
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
  }, [data, unitConverter]);

  return (
    <ReactECharts
      ref={chartRef}
      loading={loading}
      option={options}
      opts={{ height }}
      notMerge={false}
      needPrettify={false}
      onBrushEnd={handleBrushEnd}
      brushMenuChildren={menu}
    />
  );
};

export default connect()(DimensionsChart);
