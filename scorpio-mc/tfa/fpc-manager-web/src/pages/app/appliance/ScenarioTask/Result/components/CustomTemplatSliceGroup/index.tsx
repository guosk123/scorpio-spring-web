import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { getDataZoom } from '@/components/ReactECharts';
import { connect } from 'umi';

interface CustomTemplatSliceGroupProps {
  title: string;
  data: number[];
}

function CustomTemplatSliceGroup(props: CustomTemplatSliceGroupProps) {
  const { data } = props;
  const categories: string[] = data.map((item, index) => `Slice_${index}${1}`);

  const option: ECOption = {
    xAxis: {
      type: 'category',
      data: categories,
    },
    color: ['#4FA9FF', '#CBAAFF'],
    yAxis: {},
    dataZoom: getDataZoom(categories.length),
    tooltip: {
      formatter(params: any) {
        const point = params[0];
        return `<b>${point.name}: ${point.value}</b>`;
      },
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: 20,
        data,
      },
    ],
  };

  return (
    <div>
      <ReactECharts option={option} opts={{ height: 300 }} />
    </div>
  );
}

export default connect()(CustomTemplatSliceGroup);
