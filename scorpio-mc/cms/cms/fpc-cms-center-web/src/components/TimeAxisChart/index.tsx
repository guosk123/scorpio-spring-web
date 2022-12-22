import EChartsMessage from '@/components/Message';
import { Menu } from 'antd';
import moment from 'moment';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch } from 'umi';
import type { IGlobalTime } from '../GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '../GlobalTimeSelector';
import type { ECOption } from '../ReactECharts';
import { timeAxis } from '../ReactECharts';
import ReactECharts from '../ReactECharts';
import type { TimeAxisChartData } from './typing';
import { fillTimeAxisPoint } from './utils';
import { debounce } from 'lodash';
import type { IMarkArea } from '@/pages/app/analysis/components/MultipleSourceTrend';

export interface Props {
  startTime: number;
  endTime: number;
  interval: number;
  data: TimeAxisChartData[];
  nameMap?: Record<string, string>;
  unitConverter?: (value: number) => string;
  chartHeight?: number;

  loading?: boolean;
  brush?: boolean;
  /**
   * 选择是否需要图例
   */
  showLegend?: boolean;
  markArea?: IMarkArea;
}

const TimeAxisChart: React.FC<Props> = ({
  startTime,
  endTime,
  data,
  interval,
  unitConverter = (value: number) => `${value}`,
  nameMap,
  brush = true,
  showLegend = true,
  markArea = {},
  loading = false,
  chartHeight,
}) => {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const [rect, setRect] = useState<DOMRect | undefined>();
  const [brushTime, setBrushTime] = useState([0, 0]);
  const chartRef = useRef<any>();
  const dispatch = useDispatch<Dispatch>();

  const internalData = useMemo(() => {
    const result = fillTimeAxisPoint(data, startTime, endTime, interval);
    return result;
  }, [data, endTime, interval, startTime]);

  useEffect(() => {
    const target = chartContainerRef.current;
    const handleResize = debounce(() => {
      setRect(target?.getBoundingClientRect());
    }, 200);

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const option = useMemo<ECOption | null>(() => {
    if (internalData.length < 1) return null;

    const dimensions: string[] = [
      'timestamp',
      ...Object.keys(internalData[0])
        .filter((key) => key !== 'timestamp')
        .sort((a, b) => {
          return internalData[0][b] - internalData[0][a];
        }),
    ];

    return {
      xAxis: {
        ...timeAxis,
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
      },
      grid: {
        bottom: showLegend ? 25 : 0,
      },
      legend: {
        show: showLegend,
        bottom: 0,
        height: 12,
        data: dimensions.filter(
          (dimension) => dimension && dimension !== 'undefined' && dimension !== 'timestamp',
        ),
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number | string) =>
            unitConverter ? (unitConverter(value as number) as string) : `${value}`,
        },
      },
      tooltip: {
        formatter: (params: any) => {
          let tooltip = '';
          if (params.length > 0) {
            tooltip += `${params[0].axisValueLabel}<br />`;
          }
          for (let i = 0; i < params.length; i += 1) {
            const value =
              params[i].data[
                (params[i].dimensionNames as []).findIndex((seriesName) => {
                  if (nameMap) {
                    return (
                      seriesName ===
                      Object.keys(nameMap).find((key) => nameMap[key] === params[i].seriesName)
                    );
                  }
                  return seriesName === params[i].seriesName;
                }) || 0
              ];
            tooltip += `${params[i].marker}${params[i].seriesName}: ${
              unitConverter ? unitConverter(value) : `${value}`
            }<br/>`;
          }
          return tooltip;
        },
      },
      dataset: [
        {
          dimensions,
          source: [
            ...internalData.map((item) => {
              return dimensions.map((dimension) => item[dimension]);
            }),
          ],
        },
      ],
      series: dimensions
        .filter((dimension) => dimension !== 'timestamp')
        .map((dimension) => {
          return {
            type: 'line',
            name: (nameMap && nameMap[dimension]) || dimension,
            encode: {
              x: 'timestamp',
              y: dimension,
            },
          };
        })
        .concat([
          {
            type: 'line',
            data: [],
            markArea,
          } as any,
        ]) as any,
    };
  }, [internalData, markArea, nameMap, showLegend, unitConverter]);

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

  const handleMenuClick = useCallback(() => {
    changeGlobalTime(brushTime[0], brushTime[1]);

    chartRef.current?.hideMenu();
  }, [brushTime, changeGlobalTime]);

  const menu = useMemo(() => {
    return (
      <>
        <Menu onClick={handleMenuClick}>
          <Menu.Item key="time">修改时间</Menu.Item>
        </Menu>
      </>
    );
  }, [handleMenuClick]);

  const handleBrushEnd = useMemo(() => {
    const internalHandleBrushEnd = (from: number, to: number) => {
      // 时间跨度小于两分钟时;
      if ((to - from) / 1000 < 120) {
        const diffSeconds = 120 - (to - from) / 1000;
        const offset = diffSeconds / 2;
        setBrushTime([from - offset * 1000, to + offset * 1000]);
      } else {
        setBrushTime([new Date(from).valueOf(), new Date(to).valueOf() + 60 * 1000]);
      }
    };

    return internalHandleBrushEnd;
  }, []);

  if (loading) {
    return <EChartsMessage height={chartHeight} message="loading" />;
  }

  if (!option) {
    return <EChartsMessage height={chartHeight} message="暂无数据" />;
  }

  return (
    <div ref={chartContainerRef} style={{ height: '100%' }}>
      {
        <ReactECharts
          key={JSON.stringify(option)}
          option={option}
          style={{ height: chartHeight ?? '100%' }}
          opts={{ width: 'auto' }}
          onBrushEnd={brush ? handleBrushEnd : undefined}
          brushMenuChildren={brush ? menu : undefined}
          ref={chartRef}
        />
      }
    </div>
  );
};

export default React.memo(TimeAxisChart);
