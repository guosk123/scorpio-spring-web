import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import { useMemo } from 'react';

interface IBarChartProps {
  data: Record<string, number | string>[];
  showMetrics: { title: string; value: string; type?: 'bar' | 'line' }[];
  top?: number;
  category: string[];
  categoryMap?: Record<string, { name?: string; nameText?: string; [key: string]: any }>;
  unitConverter?: any;
  tooltipFormater?: (params: any) => string;
  height: number;
}

const BarChart: React.FC<IBarChartProps> = ({
  data,
  top = 10,
  showMetrics,
  categoryMap,
  category,
  unitConverter,
  tooltipFormater,
  height,
}) => {
  const chartData = useMemo(() => {
    return data
      .slice(0, top)
      .map((item) => {
        const obj: Record<string, string | number> = showMetrics.reduce((prev, current) => {
          return {
            ...prev,
            [current.value]: item[current.value],
          };
        }, {});
        obj.category = category.map((i) => item[i]).join('-');
        return obj;
      })
      .reverse();
  }, [category, data, showMetrics, top]);

  const option: ECOption = useMemo(() => {
    return {
      grid: {
        top: 0,
        bottom: 0,
        left: 0,
        right: 0,
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          axis: 'y',
        },
        formatter: (params: any) => {
          if (tooltipFormater) {
            return tooltipFormater(params);
          }
          let label = `${params.lastItem.axisValue}<br/>`;
          for (let i = 0; i < params.length; i += 1) {
            // label += `${params[i].marker}${params[i].seriesName}: ${
            label += `${params[i].seriesName}: ${
              unitConverter ? unitConverter(params[i].value) : params[i].value
            }<br/>`;
          }
          return label;
        },
      },
      xAxis: {
        type: 'value',
        show: false,
      },
      legend: { show: false },
      yAxis: {
        type: 'category',
        data: chartData.map((item) => {
          return (
            (categoryMap &&
              category.length === 1 &&
              (categoryMap[item.category]?.nameText || categoryMap[item.category]?.name)) ||
            item.category
          );
        }),
        axisLabel: {
          // formatter: (value: string | number) => {
          //   if ((value as string).length >= 8) {
          //     return `${(value as string).substring(0, 8)}`;
          //   }
          //   return value as string;
          // },
          inside: true,
        },
        axisTick: {
          alignWithLabel: true,
        },
        z: 99999,
      },
      series: showMetrics.map((metric) => {
        return {
          name: metric.title,
          type: metric.type || 'bar',
          stack: 'total',
          barWidth: height / 11,
          data: chartData.map((item) => {
            return {
              value: item[metric.value],
              itemStyle: { color: '#88cffe', borderColor: '#88cffe' },
            };
          }),
        };
      }),
    };
  }, [
    category.length,
    categoryMap,
    chartData,
    height,
    showMetrics,
    tooltipFormater,
    unitConverter,
  ]);

  return (
    <ReactECharts
      option={option}
      needPrettify={false}
      style={{ height: '100%' }}
      opts={{ height }}
    />
  );
};

export default BarChart;
