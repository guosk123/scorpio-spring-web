import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import {
  ETimeType,
  ETimeUnit,
  getGlobalTime,
  globalTimeFormatText,
} from '@/components/GlobalTimeSelector';
import EChartsMessage from '@/components/Message';
import type { ECOption, IReactEChartsProps } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { bytesToSize, convertBandwidth } from '@/utils/utils';
import { connect } from 'dva';
import type { ECharts } from 'echarts';
import moment from 'moment';
import numeral from 'numeral';
import type { FC } from 'react';
// import { useState } from 'react';
import type { Dispatch } from 'umi';

export enum EChartType {
  'LINE' = 'line',
  'BAR' = 'bar',
}

type IObjectString = Record<string, any>;

export interface ITimeHistogramChartProps {
  dispatch: Dispatch;
  title?: string;
  height?: number;
  chartType?: EChartType;
  seriesData: ISeries[];
  showLegend?: boolean;
  loading: boolean;
  /**
   * 完整的Y轴配置
   */
  yAxis?: IObjectString;
  /**
   * Y 轴显示文字格式化
   */
  yAxisLabels?: IObjectString;

  /**
   * 从图表上选择时间范围
   */
  onTimeSelection?: IReactEChartsProps['onBrushEnd'];
  changeGlobalTime?: boolean;

  needPrettify?: boolean;
}

export enum EFormatterType {
  /**
   * 带宽
   */
  'bps' = 'bps',
  /**
   * pps
   */
  'pps' = 'pps',
  /**
   * 每秒的数量
   */
  'perSecond' = 'perSecond',
  /**
   * 流量大小
   */
  'bytes' = 'bytes',
  /**
   * 延迟时间ms
   */
  'ms' = 'ms',
  /**
   * count
   */
  'count' = 'count',
}

export const histogramChartColors = [
  '#5B8FF9',
  '#5AD8A6',
  '#5D7092',
  '#F6BD16',
  '#E86452',
  '#6DC8EC',
  '#945FB9',
  '#FF9845',
  '#1E9493',
  '#FF99C3',
];

export interface ISeriesData {
  value: number[] | [number, null];
  startTime: number;
  endTime: number;
  smooth?: boolean;
  symbol?: string;
  formatter?: EFormatterType;
}

export interface ISeries {
  name: string;
  yAxisIndex?: number;
  type?: EChartType;
  stack?: string;
  data: ISeriesData[];
  [propName: string]: any;
}

const TimeHistogramChart: FC<ITimeHistogramChartProps> = (props) => {
  const {
    dispatch,
    title,
    height = 300,
    chartType = EChartType.LINE,
    seriesData = [],
    loading = false,
    showLegend = true,
    yAxis,
    yAxisLabels,

    changeGlobalTime = true,
    onTimeSelection,

    needPrettify = true,
  } = props;

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  // const [internalChart, setInternalChart] = useState<ECharts>();

  if (!loading && seriesData.length === 0) {
    return <EChartsMessage title={title} height={height} message="暂无统计数据" />;
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const afterChartCreated = (chart: ECharts) => {
    // setInternalChart(chart);
  };

  const handleBrushEnd = (startTime: number, endTime: number) => {
    // 修改全局时间
    if (changeGlobalTime) {
      const timeObj: IGlobalTime = {
        relative: false,
        type: ETimeType.CUSTOM,
        last: {
          range: 30,
          unit: ETimeUnit.MINUTES,
        },
        custom: undefined,
      };

      const time1 = moment(startTime).format(globalTimeFormatText);
      const time2 = moment(endTime).format(globalTimeFormatText);
      timeObj.custom = [moment(time1), moment(time2)];

      if (dispatch) {
        dispatch({
          type: 'appModel/updateGlobalTime',
          payload: getGlobalTime(timeObj),
        });
      }
    }
    if (onTimeSelection) {
      onTimeSelection(startTime, endTime);
    }
  };

  const option: ECOption = {
    title: {
      text: title,
      left: 'left',
    },
    grid: {
      top: title ? 40 : 10,
      bottom: showLegend ? 20 : 0,
    },
    xAxis: {
      ...timeAxis,
    },
    yAxis: yAxis || {
      min: 0,
      axisLabel: {
        ...yAxisLabels,
      },
    },
    color: histogramChartColors,
    legend: {
      show: showLegend,
      type: 'scroll',
    },
    tooltip: {
      formatter: (params: any) => {
        let label = `${params.lastItem.axisValueLabel}<br/>`;

        let total = 0;
        if (chartType === EChartType.BAR) {
          params.forEach((item: any) => {
            total += item.data.value[1] || 0;
          });
        }
        params.forEach((item: any) => {
          label += `${item.marker}${item.seriesName}: `;
          const { formatter } = item.data;
          const value = item && item.data.value[1];
          if (formatter === EFormatterType.bytes) {
            label += bytesToSize(value);
          } else if (formatter === EFormatterType.bps) {
            label += convertBandwidth(value);
          } else {
            label += numeral(value).format('0,0');
            if (formatter === EFormatterType.perSecond) {
              label += '次/s';
            } else if (formatter === EFormatterType.pps) {
              label += 'pps';
            } else if (formatter === EFormatterType.ms) {
              label += 'ms';
            } else if (chartType === EChartType.BAR) {
              // @ts-ignore
              if (item.componentSubType === 'bar') {
                label += `（${total ? ((value / total) * 100).toFixed(2) : 0}%）`;
              }
            }
          }
          label += '<br/>';
        });
        return label;
      },
    },
    // @ts-ignore
    series: seriesData,
  };

  return (
    <ReactECharts
      loading={loading}
      option={option}
      opts={{ height }}
      onBrushEnd={handleBrushEnd}
      onChartReadyCallback={afterChartCreated}
      needPrettify={needPrettify}
    />
  );
};

export default connect()(TimeHistogramChart);
