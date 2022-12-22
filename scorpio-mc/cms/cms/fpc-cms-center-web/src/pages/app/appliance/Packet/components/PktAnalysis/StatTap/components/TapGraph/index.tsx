import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { getDataZoom } from '@/components/ReactECharts';
import { bytesToSize } from '@/utils/utils';
import React from 'react';

export interface ISerieItemData {
  type: 'bar';
  name: string;
  data: any[];
}

interface TapGraphProps {
  type?: 'bytes' | 'frames';
  title: string;
  categories: string[];
  data: ISerieItemData[];
}

const TapGraph = React.memo(({ type = 'frames', title, categories, data }: TapGraphProps) => {
  const option: ECOption = {
    title: {
      text: title,
      left: 'center',
    },
    xAxis: {
      type: 'category',
      data: categories,
    },
    color: ['#4FA9FF', '#CBAAFF'],
    grid: {
      top: 40,
      bottom: 40,
    },
    yAxis: {
      minInterval: 1,
      axisLabel: {
        formatter(value: number) {
          if (type === 'bytes') {
            return bytesToSize(Math.abs(value), 0);
          }
          return value as unknown as string;
        },
      },
    },
    dataZoom: getDataZoom(categories.length),
    tooltip: {
      formatter(params: any) {
        if (!Array.isArray(params)) {
          return '';
        }
        const pointName = params[0].name;
        let s = `<b>${pointName}</b><br/>`;
        params.forEach((point) => {
          s += '<div>';
          s += point.marker;
          s += `${point.seriesName}：${
            type === 'frames' ? point.value : bytesToSize(point.value as number)
          }`;
          s += `</div><br/>`;
        });
        return s;
      },
    },
    series: data.map((point) => ({ ...point, barMaxWidth: 20 })),
  };

  return (
    <div>
      <ReactECharts option={option} opts={{ height: 300 }} />
    </div>
  );
});

export default TapGraph;
