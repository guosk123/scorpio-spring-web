import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';

export interface IPieChartData {
  name: string;
  value: number;
}
export default (props: { datas: IPieChartData[] }) => {
  const option: ECOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/> 次数: {c} <br/> 占比: {d}%',
    },
    legend: {
      data: [],
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: props.datas,
      },
    ],
    xAxis: {
      show: false,
    },
  };
  return (
    <>
      <ReactECharts option={option} style={{ height: '100%' }} />
    </>
  );
};
