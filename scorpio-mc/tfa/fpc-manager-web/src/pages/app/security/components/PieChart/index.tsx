import EChartsMessage from '@/components/Message';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { Card } from 'antd';
import { CHART_HEIGHT } from '../../Dashboard';
import type { SuricataStatisticsResult } from '../../typings';

interface Props {
  data: SuricataStatisticsResult[];
  title: string;
  categoryMap?: Record<string, string>;
  loading?: boolean;
}

interface showedProps {
  name: string;
  value: number;
}

const PieChart = (props: Props) => {
  const { data, title, categoryMap, loading } = props;

  const labelColor = usePieChartLabelColor();

  const showedData: showedProps[] = [];
  data.forEach((item) => {
    if (categoryMap) {
      if ((categoryMap[item.key] ?? '') !== '') {
        showedData.push({
          name: categoryMap[item.key],
          value: item.count,
        });
      }
    } else {
      showedData.push({
        name: item.key,
        value: item.count,
      });
    }
  });

  const options: ECOption = {
    tooltip: {
      trigger: 'item',
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: showedData,
        label: {
          show: true,
          color: labelColor,
          position: 'outer',
        },
      },
    ],
  };

  if (loading) {
    return <EChartsMessage height={CHART_HEIGHT} message="loading" />;
  }

  return (
    <Card bordered title={title} size="small">
      <ReactECharts option={options} opts={{ height: CHART_HEIGHT }} />
    </Card>
  );
};

export default PieChart;
