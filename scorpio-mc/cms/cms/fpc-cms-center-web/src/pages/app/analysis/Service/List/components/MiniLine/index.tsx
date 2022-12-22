import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { convertBandwidth } from '@/utils/utils';
import { Spin } from 'antd';
import type { EChartsOption } from 'echarts';
import moment from 'moment';
import React from 'react';

interface IMiniLineProps {
  data: any[];
  loading?: boolean;
  height?: number;
}
const MiniLine: React.FC<IMiniLineProps> = ({ data = [], loading = false, height = 40 }) => {
  const chartOption: EChartsOption = {
    grid: {
      bottom: 0,
      top: 0,
      left: 0,
      right: 0,
      containLabel: false,
    },
    xAxis: {
      ...timeAxis,
      show: false,
      boundaryGap: false,
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => {
          return convertBandwidth(value);
        },
      },
      show: false,
    },
    series: [
      {
        name: '带宽',
        type: 'line',
        areaStyle: {
          opacity: 1,
        },
        showSymbol: false,
        smooth: true,
        data,
      },
    ],
    legend: { show: false },
    tooltip: {
      formatter(params: any) {
        if (!Array.isArray(params)) {
          return '';
        }

        let label = '';
        const { axisValue } = params[0];
        label += `<b>${moment(axisValue).format('YYYY-MM-DD HH:mm:ss')}</b><br/>`;
        params.forEach((item) => {
          label += `${item.marker}${item.seriesName}: `;
          const value = item && item.data[1];
          label += convertBandwidth(value);
          label += '<br/>';
        });

        return label;
      },
    },
  };

  return (
    <Spin spinning={loading} size="small">
      <ReactECharts option={chartOption as any} opts={{ height }} notMerge={false} />
    </Spin>
  );
};

export default MiniLine;
