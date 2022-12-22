/* eslint-disable @typescript-eslint/no-shadow */
import { Liquid } from '@ant-design/charts';
import type { FC } from 'react';
import numeral from 'numeral';

interface LiquidChartProps {
  height?: number;
  percent: number;
}
const LiquidChart: FC<LiquidChartProps> = ({ percent, height = 200 }) => {
  const config = {
    percent,
    height,
    statistic: {
      content: {
        formatter: function formatter(_ref: any) {
          const { percent } = _ref;
          return `${numeral((percent * 100).toFixed(2)).format('0,0')}%`;
        },
        style: {
          fontSize: '18px',
          // fill: 'white',
        },
      },
    },
    liquidStyle: function liquidStyle(_ref2: any) {
      const { percent } = _ref2;
      return {
        fill: percent <= 0.8 ? '#5B8FF9' : '#FAAD14',
        stroke: percent <= 0.8 ? '#5B8FF9' : '#FAAD14',
      };
    },
    color: function color() {
      return '#5B8FF9';
    },
  };
  return <Liquid {...config} />;
};

export default LiquidChart;
