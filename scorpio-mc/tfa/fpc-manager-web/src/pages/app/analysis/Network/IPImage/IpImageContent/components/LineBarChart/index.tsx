import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import { bytesToSize } from '@/utils/utils';
import { useMemo } from 'react';

interface IBarLineChartConfig {}

interface IBarLineChartProps {
  height?: number;
  configDataOne: IBarLineChartConfig[];
  configDataTwo: IBarLineChartConfig[];
  configDataThree: IBarLineChartConfig[];
  configName: string[];
  topIndexName: string[];
}

const BarLineChart: React.FC<IBarLineChartProps> = ({
  configName,
  configDataOne,
  configDataTwo,
  configDataThree,
  topIndexName,
}) => {
  // const viewBarCount = 5;
  const showedOptions = useMemo<ECOption>(() => {
    return {
      tooltip: {
        trigger: 'axis',
        // axisPointer: {
        //   type: 'none',
        // },
      },
      legend: {
        data: topIndexName,
        left: 'center',
        top: 0,
      },
      xAxis: [
        {
          type: 'category',
          axisTick: {
            alignWithLabel: true,
          },
          axisLabel:{
            show: true,
            interval: 0,
            rotate: 40
          },
          // splitLine: { show: false },
          // prettier-ignore
          data: configName,
        },
      ],
      yAxis: [
        {
          type: 'value',
          name: topIndexName[0],
          position: 'right',
          alignTicks: true,
          axisLine: {
            show: true,
          },
          axisLabel: {
            formatter: '{value} 次',
          },
          splitLine: {
            show: false, //去网格线
          },
        },
        {
          type: 'value',
          name: topIndexName[1],
          position: 'right',
          alignTicks: true,
          offset: 80,
          axisLine: {
            show: false,
          },
          axisLabel: {
            show: false,
            formatter: '{value} 次',
          },
          splitLine: {
            show: false,
          },
        },
        {
          type: 'value',
          name: topIndexName[2],
          position: 'left',
          alignTicks: true,
          axisLine: {
            show: true,
          },
          axisLabel: {
            formatter: (value: number) => {
              return bytesToSize(value);
            },
          },
          splitLine: {
            show: false,
          },
        },
      ],
      series: [
        {
          name: topIndexName[0],
          type: 'bar',
          yAxisIndex: 0,
          data: configDataOne,
          barMaxWidth: 50,
        },
        {
          name: topIndexName[1],
          type: 'bar',
          yAxisIndex: 1,
          data: configDataTwo,
          barMaxWidth: 50,
        },
        {
          name: topIndexName[2],
          type: 'line',
          yAxisIndex: 2,
          data: configDataThree,
        },
      ],
      dataZoom:[
        {
          type: 'slider',
          show: true,
          // xAxisIndex:[0],
          start: 0,
          end: 10,
          filterMode: 'filter',
          handleSize: 8,
          top: '80%',
        }
      ],
    };
  }, [configDataOne, configDataThree, configDataTwo, configName, topIndexName]);

  return <ReactECharts option={showedOptions} style={{ margin: 5 }} opts={{ height: 360 }} />;
};

export default BarLineChart;
