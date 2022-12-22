import EChartsMessage from '@/components/Message';
import { Menu } from 'antd';
import moment from 'moment';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch } from 'umi';
import type { IGlobalTime } from '../GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '../GlobalTimeSelector';
import type { ECOption } from '../ReactECharts';
import ReactECharts from '../ReactECharts';
import type { TimeAxisChartData } from './typing';
import { fillTimeAxisPoint, TIME_AXIS_LABEL_MAX_LENGTH } from './utils';
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
  brush?: boolean;
  /**
   * 选择是否需要图例
   */
  showLegend?: boolean;
  markArea?: IMarkArea;
  loading?: boolean;
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
  chartHeight,
  loading,
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

  const timePointCounts = useMemo(() => {
    if (rect?.width) {
      return Math.floor((rect.width / TIME_AXIS_LABEL_MAX_LENGTH) * 1.5);
    }
    return 8;
  }, [rect]);

  const option = useMemo<ECOption | null>(() => {
    if (internalData.length < 1) return null;

    const findStructure = internalData.find(
      (tempData) => tempData && Object.keys(tempData).length > 1,
    );

    if (!findStructure) {
      return null;
    }

    const dimensions: string[] = [
      'timestamp',
      ...Object.keys(findStructure).filter((key) => key !== 'timestamp'),
    ];
    return {
      xAxis: {
        type: 'time',
        minInterval: (endTime - startTime) / timePointCounts,
        splitNumber: timePointCounts / 1.5,
        axisLabel: {
          showMinLabel: false,
          showMaxLabel: false,
          fontSize: 12,
          width: 12 * 6,
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
      },
      grid: {
        bottom: showLegend ? 25 : 0,
      },
      legend: {
        show: showLegend,
        bottom: 0,
        height: 12,
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
            tooltip += `${params[i].marker}${params[i].seriesName}: ${unitConverter(
              params[i].data[params[i].componentIndex + 1],
            )}<br/>`;
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
  }, [
    endTime,
    internalData,
    markArea,
    nameMap,
    showLegend,
    startTime,
    timePointCounts,
    unitConverter,
  ]);

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

  if (data.length === 0) {
    return <EChartsMessage height={chartHeight} message="暂无数据" />;
  }

  return (
    <div ref={chartContainerRef} style={{ height: chartHeight ?? '100%' }}>
      {option ? (
        <ReactECharts
          option={option}
          style={{ height: chartHeight ?? '100%' }}
          opts={{ width: 'auto' }}
          onBrushEnd={brush ? handleBrushEnd : undefined}
          brushMenuChildren={brush ? menu : undefined}
          ref={chartRef}
        />
      ) : (
        <EChartsMessage message="暂无数据" />
      )}
    </div>
  );
};

export default React.memo(TimeAxisChart);
