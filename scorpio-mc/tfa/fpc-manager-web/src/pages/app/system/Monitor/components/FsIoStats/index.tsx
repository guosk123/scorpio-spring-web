import type { ITimeParams } from '@/common/typings';
import Message from '@/components/Message';
import type { ECOption } from '@/components/ReactECharts';
import ReactECharts, { timeAxis } from '@/components/ReactECharts';
import { convertBandwidth, timeFormatter } from '@/utils/utils';
import { Card, Select } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { queryFsIoHistogram } from './service';
import type { IFsIoMetric, TPartitionName } from './typings';
import { PARTITION_NAME_MAP } from './typings';

interface IProps {
  from: string;
  to: string;
}

type TSeriesData = [number, number][];

const FsIoStats = ({ from, to }: IProps) => {
  const [partitionName, setPartitionName] = useState<TPartitionName>('fs_system_io');
  const [loading, setLoading] = useState<boolean>(false);
  const [histogramData, setHistogramData] = useState<IFsIoMetric[]>([]);

  const timeInfo: ITimeParams = useMemo(() => {
    return timeFormatter(from, to);
  }, [from, to]);

  const getData = useCallback(async () => {
    setLoading(true);
    // 获取统计
    const { success, result } = await queryFsIoHistogram({
      ...timeInfo,
      partitionName,
    } as any);

    setHistogramData(success ? result : []);
    setLoading(false);
  }, [timeInfo, partitionName]);

  useEffect(() => {
    getData();
  }, [getData]);

  const chartOptions = useMemo(() => {
    const readBytepsArr: TSeriesData = [];
    const readBytepsPeakArr: TSeriesData = [];
    const writeBytepsArr: TSeriesData = [];
    const writeBytepsPeakArr: TSeriesData = [];

    histogramData.forEach(
      ({ timestamp, readByteps = 0, readBytepsPeak = 0, writeByteps = 0, writeBytepsPeak = 0 }) => {
        const time = new Date(timestamp).valueOf();
        readBytepsArr.push([time, readByteps]);
        readBytepsPeakArr.push([time, readBytepsPeak]);
        writeBytepsArr.push([time, writeByteps]);
        writeBytepsPeakArr.push([time, writeBytepsPeak]);
      },
    );

    return {
      xAxis: {
        ...timeAxis,
      },
      yAxis: {
        min: 0,
        axisLabel: {
          formatter: (value: number) => convertBandwidth(value * 8),
        },
      },
      tooltip: {
        formatter: (params: any[]) => {
          let label = '';
          params.forEach((v, index) => {
            if (index === 0) {
              label += `${v.axisValueLabel}<br/>`;
            }
            label += v.marker;
            label += `<span style="dispay: inline-block">${
              v.seriesName
            }：</span><span">${convertBandwidth((v.value[1] || 0) * 8)}</span>`;
            label += '<br/>';
          });
          return label;
        },
      },
      series: [
        {
          name: '读速率',
          type: 'line',
          symbol: 'none',
          data: readBytepsArr,
        },
        {
          name: '读峰值速率',
          type: 'line',
          symbol: 'none',
          data: readBytepsPeakArr,
        },
        {
          name: '写速率',
          type: 'line',
          symbol: 'none',
          data: writeBytepsArr,
        },
        {
          name: '写峰值速率',
          type: 'line',
          symbol: 'none',
          data: writeBytepsPeakArr,
        },
      ],
    } as ECOption;
  }, [histogramData]);

  return (
    <Card
      size="small"
      title="分区IO"
      bodyStyle={{ padding: '10px 10px 0' }}
      style={{ marginBottom: 10 }}
      extra={
        <Select
          size="small"
          style={{ width: 140 }}
          value={partitionName}
          onChange={(value) => setPartitionName(value)}
        >
          {Object.keys(PARTITION_NAME_MAP).map((key) => (
            <Select.Option key={key} value={key}>
              {PARTITION_NAME_MAP[key]}
            </Select.Option>
          ))}
        </Select>
      }
    >
      {loading ? (
        <Message height={300} message={'Loading'} />
      ) : (
        <ReactECharts option={chartOptions} opts={{ height: 300 }} />
      )}
    </Card>
  );
};

export default FsIoStats;
