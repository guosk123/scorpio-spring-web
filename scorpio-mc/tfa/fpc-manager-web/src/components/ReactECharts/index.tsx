import type { ConnectState } from '@/models/connect';
import type { TTheme } from '@/models/frame/setting';
import { ETheme, SITE_THEME_KEY } from '@/models/frame/setting';
import { useClickAway } from 'ahooks';
import type { ECharts } from 'echarts';
import type EChartsReactCore from 'echarts-for-react/lib/core';
import ReactEchartsCore from 'echarts-for-react/lib/core';
import type { Opts } from 'echarts-for-react/lib/types';
import type {
  BarSeriesOption,
  GraphSeriesOption,
  HeatmapSeriesOption,
  LineSeriesOption,
  LinesSeriesOption,
  PieSeriesOption,
} from 'echarts/charts';
import {
  BarChart,
  EffectScatterChart,
  GraphChart,
  HeatmapChart,
  LineChart,
  LinesChart,
  PieChart,
  ScatterChart,
} from 'echarts/charts';
import type {
  BrushComponentOption,
  DatasetComponentOption,
  DataZoomComponentOption,
  GridComponentOption,
  LegendComponentOption,
  MarkAreaComponentOption,
  MarkLineComponentOption,
  MarkPointComponentOption,
  SingleAxisComponentOption,
  TimelineComponentOption,
  TitleComponentOption,
  ToolboxComponentOption,
  TooltipComponentOption,
} from 'echarts/components';
import {
  BrushComponent,
  DatasetComponent,
  DataZoomComponent,
  GeoComponent,
  GridComponent,
  LegendComponent,
  MarkAreaComponent,
  MarkLineComponent,
  MarkPointComponent,
  SingleAxisComponent,
  TimelineComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  TransformComponent,
} from 'echarts/components';
import * as echarts from 'echarts/core';
import 'echarts/lib/component/legend/scrollableLegendAction';
// 引入图例的横向选择
import 'echarts/lib/component/legend/ScrollableLegendModel';
import 'echarts/lib/component/legend/ScrollableLegendView';
import { CanvasRenderer } from 'echarts/renderers';
import type { XAXisOption } from 'echarts/types/dist/shared';
import moment from 'moment';
import React, { useCallback, useEffect, useImperativeHandle, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import AnyWhereContainer from '../AnyWhereContainer';
import EChartsMessage from '../Message';
import { refreshFlagFn } from '../PlayButton';

export type ECOption = echarts.ComposeOption<
  | BarSeriesOption
  | LineSeriesOption
  | TitleComponentOption
  | GridComponentOption
  | HeatmapSeriesOption
  | LinesSeriesOption
  | PieSeriesOption
  | GraphSeriesOption
  | BrushComponentOption
  | DataZoomComponentOption
  | LegendComponentOption
  | MarkAreaComponentOption
  | MarkLineComponentOption
  | MarkPointComponentOption
  | SingleAxisComponentOption
  | TimelineComponentOption
  | ToolboxComponentOption
  | TooltipComponentOption
  | DatasetComponentOption
>;

echarts.use([
  BarChart,
  HeatmapChart,
  LineChart,
  LinesChart,
  PieChart,
  GraphChart,
  EffectScatterChart,
  ScatterChart,

  BrushComponent,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  MarkAreaComponent,
  MarkLineComponent,
  MarkPointComponent,
  SingleAxisComponent,
  TimelineComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  DatasetComponent,
  TransformComponent,
  GeoComponent,

  CanvasRenderer,
]);

/** 按需加载后的 ECharts */
export const customECharts = echarts;

export const CHART_COLORS = [
  '#1890FF',
  '#41D9C7',
  '#2FC25B',
  '#FACC14',
  '#D1E65C',
  '#84E777',
  '#F8965A',
  '#FF696A',
  '#5C8EE6',
  '#13C2C2',
  '#5CA3E6',
  '#7D7FDE',
  '#B381E6',
  '#F04864',
  '#D940E4',
];

/**
 * 默认主题下的图表颜色搭配
 */
const lightColors = {
  // 标题颜色
  titleColor: '#333',
  // 分割线的颜色
  lineColor: '#f0f0f0',
  // 图例文字的颜色
  legendTextColor: '#333',
  // 图例关闭时的演示
  legendInactiveColor: '#ccc',
  // 轴标签的颜色
  axisLabelColor: '#666',
};

/**
 * 暗黑主题下的图表颜色搭配
 */
const darkColors = {
  titleColor: 'rgba(255, 255, 255, 0.8)',
  lineColor: '#545454',
  legendTextColor: 'rgba(255, 255, 255, 0.8)',
  legendInactiveColor: '#666',
  axisLabelColor: 'rgba(255, 255, 255, 0.8)',
};

// echarts.registerTheme("light", )

const currentTheme: ETheme = (localStorage.getItem(SITE_THEME_KEY) || ETheme.light) as ETheme;
const currentThemeColors = currentTheme === ETheme.dark ? darkColors : lightColors;

/**
 * 显示dataZoom时的分类个数
 */
export const SHOW_DATAZOOM_CATEGORIES_COUNT = 50;

/**
 * 清空框选状态
 * @param chart ECharts
 * @see: https://github.com/apache/incubator-echarts/issues/9617
 */
export const clearChartBrush = (chart: ECharts) => {
  chart.dispatchAction({
    type: 'brush',
    areas: [],
  });
};

export const getDataZoom = (categoryCount: number) => {
  return [
    {
      type: 'slider',
      show: categoryCount > SHOW_DATAZOOM_CATEGORIES_COUNT,
      disabled: categoryCount <= SHOW_DATAZOOM_CATEGORIES_COUNT,
      startValue: 0,
      endValue: SHOW_DATAZOOM_CATEGORIES_COUNT,
      maxValueSpan: SHOW_DATAZOOM_CATEGORIES_COUNT,
    },
    {
      type: 'inside',
      startValue: 0,
      endValue: SHOW_DATAZOOM_CATEGORIES_COUNT,
      minValueSpan: 10,
      maxValueSpan: SHOW_DATAZOOM_CATEGORIES_COUNT,
    },
  ];
};

export const timeAxis: XAXisOption = {
  type: 'time',
  // splitNumber: 20,
  minInterval: 60 * 1000,
  // 只有一个点的情况下 最大值等于最小值， 系统里的时间通常会格式化为整分钟，因此判断小于60s
  min: ({ min, max }) => {
    if (max - min < 60 * 1000) {
      return min - 30 * 1000;
    }
    return min;
  },
  max: ({ min, max }) => {
    if (max - min < 60 * 1000) {
      return max + 30 * 1000;
    }
    return max;
  },
  axisLabel: {
    showMinLabel: false,
    showMaxLabel: false,
    // @ts-ignore
    hideOverlap: true,
    formatter: (value: string | number) => {
      const time = moment(value).format('HH:mm');
      if (time === '00:00') {
        return moment(value).format('MM-DD');
      }
      if (moment(value).format('ss') === '00') {
        return moment(value).format('HH:mm');
      }
      return time;
    },
  },
  axisLine: {
    show: false,
  },
  axisTick: {
    show: false,
    interval: 'auto',
  },
};

export const defaultOptions: ECOption = {
  grid: {
    containLabel: true,
    left: 10,
    right: 10,
    top: 10,
    bottom: 30,
  },
  color: CHART_COLORS,
  title: {
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal',
      color: currentThemeColors.titleColor,
    },
  },
  textStyle: {
    fontFamily: 'sans-serif',
  },
  xAxis: {
    splitLine: {
      show: true,
      lineStyle: {
        color: currentThemeColors.lineColor,
      },
    },
    axisTick: {
      show: false,
    },
    axisLine: {
      show: true,
      lineStyle: {
        color: currentThemeColors.lineColor,
      },
    },
    axisLabel: {
      color: currentThemeColors.axisLabelColor,
      fontSize: 12,
    },
  },
  yAxis: [
    {
      minInterval: 1,
      axisLabel: {
        color: currentThemeColors.axisLabelColor,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: currentThemeColors.lineColor,
        },
      },
      axisTick: {
        show: false,
      },
      axisLine: {
        show: false,
        lineStyle: {
          color: currentThemeColors.lineColor,
        },
      },
      nameTextStyle: {
        color: currentThemeColors.axisLabelColor,
      },
    },
    {
      minInterval: 1,
      axisLabel: {
        color: currentThemeColors.axisLabelColor,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: currentThemeColors.lineColor,
        },
      },
      axisTick: {
        show: false,
      },
      axisLine: {
        show: false,
      },
      nameTextStyle: {
        color: currentThemeColors.axisLabelColor,
      },
    },
  ],
  toolbox: {
    show: false,
    right: 20,
  },
  brush: {
    geoIndex: 0,
    brushLink: 'all',
    toolbox: ['lineX'],
    xAxisIndex: 'all',
    brushType: 'lineX',
    throttleType: 'fixRate',
    throttleDelay: 300,
    // 已经选好的选框是否可以被调整形状或平移
    transformable: false,
  },
  tooltip: {
    // 紧跟着鼠标移动
    transitionDuration: 0,
    // 隐藏延时
    hideDelay: 0,
    trigger: 'axis',
    confine: true,
    backgroundColor: '#fff',
    axisPointer: {
      // 交叉线
      // type: 'cross',
      // 阴影
      // type: 'shadow',
      // 十字准星线的颜色
      lineStyle: {
        color: '#cccccc',
        type: 'dashed',
      },
      crossStyle: {
        color: '#e8e8e8',
      },
      shadowStyle: {
        color: 'rgba(150,150,150,0.1)',
      },
    },
    textStyle: {
      fontSize: 12,
      fontFamily: '"Lucida Grande", "Lucida Sans Unicode", Arial, Helvetica, sans-serif;',
      color: 'rgb(51, 51, 51)',
    },
    padding: 12,
    extraCssText: 'box-shadow: 0px 2px 8px 0px #cacaca;border-radius: 4px;opacity: 0.9;',
  },
  legend: {
    show: true,
    bottom: 0,
    type: 'scroll',
    textStyle: {
      color: currentThemeColors.legendTextColor,
    },
    inactiveColor: currentThemeColors.legendInactiveColor,
  },
  animation: false,
};

export interface IReactEChartsProps {
  //
  dispatch: Dispatch;
  theme: TTheme;
  option: ECOption;
  opts?: Opts;
  // eslint-disable-next-line no-unused-vars
  onChartReadyCallback?: (instance: ECharts) => void;
  // eslint-disable-next-line @typescript-eslint/ban-types
  onEvents?: Record<string, Function>;
  style?: React.CSSProperties;
  notMerge?: boolean;

  // 自定义附加属性
  loading?: boolean;
  needPrettify?: boolean;
  needDefaultOption?: boolean;
  /**
   * 鼠标框选事件
   */
  // eslint-disable-next-line no-unused-vars
  onBrushEnd?: (startTime: number, endTime: number) => void;
  onClick?: (params: any) => void;
  brushMenuChildren?: JSX.Element;
  isRefreshPage?: boolean;
}

const ReactECharts = React.forwardRef<any, IReactEChartsProps>((props, ref) => {
  const {
    dispatch,
    theme,
    loading = false,
    needPrettify = true,
    needDefaultOption = true,
    option,
    notMerge = false,
    onEvents = {},
    opts = {},
    style = {},
    onChartReadyCallback,
    onBrushEnd,
    onClick,
    brushMenuChildren,
    isRefreshPage,
  } = props;
  const [internalChart, setInternalChart] = useState<ECharts>();
  const echartsRef = React.useRef<EChartsReactCore>();
  const chartContainerRef = React.useRef<HTMLDivElement | null>(null);

  const [brushMenuDisplay, setBrushMenuDisplay] = useState(false);
  const [brushMenuPos, setBrushMenuPos] = useState<{ top: number; left: number }>({
    top: -1,
    left: -1,
  });

  const isRefreshFn = (flag: boolean) => {
    refreshFlagFn(flag, dispatch);
  };

  const changeTheme = useCallback(
    (nextTheme: TTheme) => {
      // 根据主题颜色动态加载图表的颜色定义
      const nextColors = nextTheme === ETheme.dark ? darkColors : lightColors;

      const axisStyle = {
        axisLabel: {
          color: nextColors.axisLabelColor,
        },
        splitLine: {
          lineStyle: { color: nextColors.lineColor },
        },
        axisLine: {
          lineStyle: { color: nextColors.lineColor },
        },
      };
      if (notMerge && needDefaultOption) {
        internalChart?.setOption(defaultOptions);
      }
      internalChart?.setOption({
        title: {
          textStyle: {
            color: nextColors.titleColor,
          },
        },
        xAxis: axisStyle,
        yAxis: [axisStyle, axisStyle],
        legend: {
          textStyle: {
            color: nextColors.legendTextColor,
          },
          inactiveColor: nextColors.legendInactiveColor,
        },
      });
    },
    [internalChart, needDefaultOption, notMerge],
  );

  useImperativeHandle<IReactEChartsProps, any>(ref, () => {
    return {
      hideMenu: () => {
        setBrushMenuDisplay(false);
      },
    };
  });

  useEffect(() => {
    if (onClick && internalChart?.dispatchAction) {
      internalChart.on('click', (params) => {
        const { event } = params;
        if (event) {
          const { offsetX, offsetY } = event;
          const pointInPixel = [offsetX, offsetY];

          const inGrid = internalChart.containPixel('grid', pointInPixel);
          if (inGrid) {
            setBrushMenuPos({ left: offsetX, top: offsetY });
            if (isRefreshPage) {
              isRefreshFn(false);
            }
            setBrushMenuDisplay(true);
            onClick(params);
          }
        }
      });
    }

    if (onBrushEnd && internalChart?.dispatchAction) {
      internalChart.dispatchAction({
        type: 'takeGlobalCursor',
        key: 'brush',
        brushOption: {
          brushType: 'lineX',
          brushMode: 'single',
        },
      });

      internalChart.on('brushEnd', (params) => {
        const { areas } = params as any;
        if (areas.length > 0) {
          const { coordRanges, range } = areas[0];

          const rect = chartContainerRef.current?.getBoundingClientRect();
          // range 里是按照大小排序的 [小的 X 坐标，大的 X 坐标]
          // 由于无法区分框选的方向感，所以这里默认使用大的 X 坐标
          const offsetX = range[1];
          // 无法获取 Y 轴位置，所以取图表中间位置
          const offsetY = (rect?.height || 0) / 2;
          const pointInPixel = [offsetX, offsetY];

          // 判断当前指针位置是否在grid区域内，
          // @see https://echarts.apache.org/zh/api.html#echartsInstance.containPixel
          const inGrid = internalChart.containPixel('grid', pointInPixel);
          if (inGrid) {
            let left = offsetX;
            let top = offsetY;
            if (offsetX + 80 > rect!.width) {
              left = offsetX - 100;
            }
            if (offsetY + 200 > rect!.height) {
              top = rect!.height - 200;
            }
            setBrushMenuPos({ left, top });
            if (isRefreshPage) {
              isRefreshFn(false);
            }
            setBrushMenuDisplay(true);
          }

          if (coordRanges && coordRanges[0].length === 2) {
            let brushEndFlag = true;
            const [from, end] = coordRanges[0];
            // 判断时间范围
            if (!from || !end) {
              brushEndFlag = false;
              onBrushEnd(from, end);
            }

            const startTime = moment(from).valueOf();
            const endTime = moment(end).valueOf();
            if (brushEndFlag) {
              onBrushEnd(startTime, endTime);
            }
          }
        }
      });
    }
    if (!onBrushEnd) {
      internalChart?.dispatchAction({
        type: 'takeGlobalCursor',
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [internalChart, onBrushEnd]);

  useClickAway(() => {
    setBrushMenuDisplay(false);
    if (internalChart) {
      clearChartBrush(internalChart);
    }
  }, [chartContainerRef]);

  useEffect(() => {
    changeTheme(theme);
  }, [changeTheme, theme]);

  useEffect(() => {
    if (internalChart) {
      const oldOptions = internalChart.getOption();
      if (loading) {
        // 清空图表
        internalChart.clear();
        if (needDefaultOption) {
          internalChart.setOption(defaultOptions);
        }

        internalChart.showLoading('default', { showSpinner: false, textColor: '#cbcbcb' });
      } else {
        internalChart.hideLoading();
        // // 重新设置参数
        if (!notMerge) {
          internalChart.setOption(oldOptions);
        } else if (needDefaultOption) {
          internalChart.setOption(defaultOptions);
        }

        internalChart.setOption(option);
      }
    }
  }, [internalChart, loading, needDefaultOption, notMerge, option]);

  const handleChartReadyCallback = (chart: ECharts) => {
    setInternalChart(chart);
    if (onBrushEnd) {
      // chart.getZr().on('dragend', (e) => {
      //   console.log('dragend e', e);
      //   const pointInPixel = [e.offsetX, e.offsetY];
      //   console.log('pointInPixel', pointInPixel);
      //   const rect = chartContainerRef.current?.getBoundingClientRect();
      //   // 判断当前指针位置是否在grid区域内，
      //   // @see https://echarts.apache.org/zh/api.html#echartsInstance.containPixel
      //   const inGrid = chart.containPixel('grid', pointInPixel);
      //   console.log('inGrid', inGrid);
      //   if (inGrid) {
      //     let left = e.offsetX;
      //     let top = e.offsetY;
      //     if (e.offsetX + 80 > rect!.width) {
      //       left = e.offsetX - 100;
      //     }
      //     if (e.offsetY + 200 > rect!.height) {
      //       top = rect!.height - 200;
      //     }
      //     setBrushMenuPos({ left, top });
      //     setBrushMenuDisplay(true);
      //   }
      // });
    }
    chart.getZr().on('click', () => {
      setBrushMenuDisplay(false);
      if (isRefreshPage) {
        isRefreshFn(true);
      }
    });

    // 深度合并 option
    if (needDefaultOption) {
      chart.setOption(defaultOptions);
    }
    // 更新用户自定义的option
    chart.setOption(option);

    if (onChartReadyCallback) {
      onChartReadyCallback(chart);
    }
  };

  const { series } = option;
  if (series) {
    for (let index = 0; index < (series as any).length; index += 1) {
      const { type } = series[index];
      // 只有一个点的情况下，打开描点
      if (type === 'line' && series[index]?.data?.length === 1) {
        continue;
      } else if (type === 'line') {
        (series[index] as ECOption).symbol = 'none';
      } else if (type === 'bar') {
        (series[index] as ECOption).barMaxWidth = '30px';
      }
      continue;
    }
  }

  // 替换内容
  // 防止最近的时间点掉低的情况
  // 查找最后一个不为0的点，这个点以后的点全部置为 null
  // 防止掉底，美化数据
  if (needPrettify) {
    if (series) {
      for (let i = 0; i < (series as any).length; i += 1) {
        const { data: serieData, type } = series[i];
        if (!serieData) {
          continue;
        }
        if (type !== 'line') {
          continue;
        }
        for (let j = serieData.length - 1; j >= 0; j -= 1) {
          // 可能是个2维数组
          const point = serieData[j];
          if (!point) {
            continue;
          }
          if (Array.isArray(point) && point.length === 2) {
            // 第一个值是时间
            // 第二个值是具体的值
            // const [time, value] = point;
            if (point[1] !== 0 && !point[1]) {
              // 置为 null
              // @ts-ignore
              point[1] = null;
              continue;
            } else {
              // 从最后一个开始找，碰到一个是不为0的就跳出
              break;
            }
          }
          if (typeof point === 'number') {
            if (!point) {
              // @ts-ignore
              point = null;
            } else {
              break;
            }
          }
          if (Object.prototype.toString.call(point) === '[object Object]') {
            const { value } = point as { value: any };
            if (Array.isArray(value) && value.length === 2) {
              // 第一个值是时间
              // 第二个值是具体的值
              // const [time, value] = point;
              if (!value[1]) {
                // 置为 null
                value[1] = null;
                continue;
              } else {
                // 从最后一个开始找，碰到一个是不为0的就跳出
                break;
              }
            }
            if (typeof value === 'number') {
              if (!value) {
                // @ts-ignore
                point.value = null;
              } else {
                break;
              }
            }
          }
        }
      }
    }
  }

  const hadData = useMemo(() => {
    let hasSerieData = false;
    let hasDatasetData = false;
    if (option.series instanceof Array) {
      hasSerieData = option.series.some((serie) => serie.data?.length && serie.data.length > 0);
    } else {
      hasSerieData = !!(option.series?.data?.length && option.series?.data?.length > 0);
    }

    if (option.dataset instanceof Array) {
      hasDatasetData = option.dataset.some(
        (data) => data.source?.length && data.source?.length > 0,
      );
    } else {
      hasDatasetData = !!(option.dataset?.source?.length && option.dataset?.source?.length > 0);
    }

    return hasSerieData || hasDatasetData;
  }, [option.dataset, option.series]);

  if (loading) {
    return <EChartsMessage height={opts?.height as number} message="loading" />;
  }

  return (
    <div ref={chartContainerRef} style={{ position: 'relative', height: '100%' }}>
      {hadData ? (
        <ReactEchartsCore
          // theme="customTheme"
          echarts={echarts}
          // 图表的配置项和数据
          option={option}
          // 可选，是否不跟之前设置的 option 进行合并，默认为 false，即合并。
          notMerge={notMerge}
          // 可选，在设置完 option 后是否不立即更新图表，默认为 false，即立即更新。
          lazyUpdate={false}
          onChartReady={handleChartReadyCallback}
          onEvents={onEvents}
          opts={opts}
          ref={(e: EChartsReactCore) => {
            echartsRef.current = e;
          }}
          style={{
            height: opts?.height || 'auto',
            ...style,
          }}
        />
      ) : (
        <EChartsMessage height={opts?.height as number} />
      )}
      <AnyWhereContainer
        style={{ padding: 0 }}
        {...brushMenuPos}
        children={brushMenuChildren}
        display={brushMenuDisplay}
      />
    </div>
  );
});

export default connect(
  ({ settings }: ConnectState) => ({
    theme: settings?.theme,
  }),
  null,
  null,
  { forwardRef: true },
)(ReactECharts);
