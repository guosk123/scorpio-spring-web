import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { convertBandwidth } from '@/utils/utils';
import type { LineSeriesOption } from 'echarts';
import moment from 'moment';

interface Props {
  style?: React.CSSProperties;
  data: LineSeriesOption['data'];
}

const FlowChat = ({ data, style }: Props) => {
  const option: ECOption = {
    grid: {
      containLabel: true,
      left: 10,
      right: 10,
      top: 20,
      bottom: 0,
    },
    xAxis: {
      ...timeAxis,
    },
    yAxis: {
      min: 0,
      axisLabel: {
        formatter(value: number) {
          return convertBandwidth(value);
        },
      },
    },
    tooltip: {
      formatter(params: any) {
        if (!Array.isArray(params)) {
          return '';
        }
        let label = '';
        const time = params[0].axisValue;
        label += `<b>${moment(time).format('YYYY-MM-DD HH:mm:ss')}</b><br/>`;
        params.forEach((item) => {
          label += item.marker;
          label += `${item.seriesName}：`;
          label += convertBandwidth(item.data[1]);
          label += '<br/>';
        });
        return label;
      },
    },
    legend: {
      show: false,
    },
    series: [
      {
        type: 'line',
        connectNulls: false,
        symbol: 'none',
        name: '总带宽',
        data,
      },
    ],
  };

  return <ReactECharts option={option} needPrettify={false} style={style} />;
};

export default FlowChat;
