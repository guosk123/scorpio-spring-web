import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import { useSafeState } from 'ahooks';
import { Spin } from 'antd';
import { useEffect, useMemo } from 'react';
import { queryIpLabelStat } from '../../service';
import type { EIpLabelCatagory } from '../../typings';
import { IpLabelCategoryText } from '../../typings';

const CategoryPie = () => {
  const [data, setData] = useSafeState<Record<EIpLabelCatagory, number>>();

  const labelColor = usePieChartLabelColor();

  useEffect(() => {
    queryIpLabelStat().then((res) => {
      const { success, result } = res;
      if (success) {
        setData(result);
      }
    });
  }, [setData]);

  const option: ECOption | null = useMemo(() => {
    if (data === undefined) {
      return null;
    }
    return {
      title: { text: '标签分类占比' },
      tooltip: {
        trigger: 'item',
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: Object.keys(data).map((key) => {
            return {
              name: IpLabelCategoryText[key],
              value: data[key],
            };
          }),
          label: {
            show: true,
            color: labelColor,
            position: 'outer',
          },
        },
      ],
    };
  }, [data, labelColor]);

  if (option === null) {
    return <Spin />;
  }

  return <ReactECharts option={option} opts={{ height: 400 }} />;
};

export default CategoryPie;
