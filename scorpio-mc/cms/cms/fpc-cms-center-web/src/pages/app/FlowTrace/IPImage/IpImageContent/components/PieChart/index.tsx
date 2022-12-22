import type { ECOption } from '@/components/ReactECharts';
import ReactECharts from '@/components/ReactECharts';
import { useMemo, useState } from 'react';
import { bytesToSize } from '@/utils/utils';
import usePieChartLabelColor from '@/utils/hooks/usePieChartLabelColor';
import AutoHeightContainer from '@/components/AutoHeightContainer';

interface IPieChartConfig {}

interface IPieChartProps {
  height?: number;
  configDataOne: IPieChartConfig[];
  configDataTwo: IPieChartConfig[];
  configName: string[];
  isFull: boolean;
}

const PieChart: React.FC<IPieChartProps> = ({
  height = 400,
  configName,
  configDataOne,
  configDataTwo,
  isFull = false,
}) => {
  const labelColor = usePieChartLabelColor();
  const [pieHeight, setPieHeight] = useState(height);
  const handleHeightChange = (h: number) => {
    setPieHeight(h);
  };
  const showedOptions = useMemo<ECOption>(() => {
    let hasLabel = true;
    if (configDataTwo.length > 10) {
      hasLabel = false;
    }
    return {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          if (params.seriesName == '流量数') {
            return `${params.seriesName}<br/>${params.name}: ${bytesToSize(params.value)}`;
          }
          // if (params.seriesName == '会话数') {
          return `${params.seriesName}<br/>${params.name}: ${params.value}`;
          // }
        },
      },
      legend: {
        show: isFull,
        data: configName,
        orient: 'vertical',
        left: 'right',
        top: 'middle',
      },
      xAxis: {
        show: false,
      },
      series: [
        {
          name: '会话数',
          type: 'pie',
          selectedMode: 'single',
          radius: [0, '30%'],
          label: {
            position: 'inner',
            show: false,
            // fontSize: 14,
          },
          avoidLabelOverlap: true,
          // right: '15%',
          data: configDataOne,
        },
        {
          name: '流量数',
          type: 'pie',
          // right: '15%',
          radius: ['45%', '60%'],
          labelLine: {
            show: true,
          },
          avoidLabelOverlap: true,
          label: {
            formatter: (params: any) => {
              return `${params.name}: ${params.percent.toFixed(2)}%`;
            },
            show: true,
            color: labelColor,
            fontWeight: 'bold',
            position: 'outer',
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          data: configDataTwo,
        },
      ],
    };
  }, [configDataOne, configDataTwo, configName, isFull, labelColor]);

  const currentPie = useMemo(() => {
    return (
      <ReactECharts
        option={showedOptions}
        style={{ margin: 10 }}
        opts={{ height: isFull ? pieHeight - 20 : height }}
      />
    );
  }, [height, isFull, pieHeight, showedOptions]);

  return (
    <>
      {isFull ? (
        <AutoHeightContainer autoHeight={true} onHeightChange={handleHeightChange}>
          {currentPie}
        </AutoHeightContainer>
      ) : (
        currentPie
      )}
    </>
  );
};

export default PieChart;
