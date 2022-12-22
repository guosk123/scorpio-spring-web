import TimeAxisChart from '@/components/TimeAxisChart';
import useCallbackState from '@/hooks/useCallbackState';
import type { IMarkArea } from '@/pages/app/analysis/components/MultipleSourceTrend';
import { Card } from 'antd';
import moment from 'moment';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { queryNetyworkLineChart, queryUsageRate } from '../../services';
import type { TimeAxisChartData } from '../../utils/converter';
import { lineConverter } from '../../utils/converter';

interface Props {
  title: string;
  startTime: string;
  endTime: string;
  interval: number;
  fpcDevices: any[];
  metric: string;
  topNumber: number;
  unitConverter?: (value: number) => string;
  type: 'network' | 'device';
  /** 需要使用的字段名称，不传默认使用metric */
  valueName?: string;
  markArea?: IMarkArea;
}

export default function ProTimeAxisChart({
  title,
  startTime,
  endTime,
  interval,
  fpcDevices,
  topNumber,
  metric,
  unitConverter,
  type = 'network',
  valueName,
  markArea,
}: Props) {
  const [data, setData] = useCallbackState<TimeAxisChartData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  /** 展示开始时间 */
  const displayStartTime = useMemo(() => {
    return moment(startTime)
      .add(interval / 60, 'm')
      .valueOf();
  }, [interval, startTime]);

  const displayEndTime = useMemo(() => {
    return moment(endTime).valueOf();
  }, [endTime]);

  /** 获取折线图数据 */
  const queryDeviceLineChartData = useCallback(async () => {
    setLoading(true);
    const { success, result } = await queryUsageRate({
      metric: metric as any,
      topNumber,
      startTime,
      endTime,
      interval,
    });
    if (!success) {
      setLoading(false);
      return;
    }
    setData(lineConverter(result || [], fpcDevices, metric) || [], () => {
      setLoading(false);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endTime, fpcDevices, interval, metric, startTime, topNumber]);

  const queryNetworkLineChartData = useCallback(async () => {
    setLoading(true);
    const { success, result } = await queryNetyworkLineChart({
      metric: metric as any,
      topNumber,
      startTime,
      endTime,
      interval,
    });
    setLoading(false);
    if (!success) {
      return;
    }
    setData(
      lineConverter(
        result || [],
        fpcDevices,
        valueName || metric,
        'serialNumber',
        undefined,
        'timestamp',
        // eslint-disable-next-line no-nested-ternary
        metric === 'established_sessions'
          ? (value) => value
          : metric === 'total_bytes'
          ? (value) => (value * 8) / interval
          : undefined,
      ) || [],
      () => {
        setLoading(false);
      },
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endTime, fpcDevices, interval, metric, startTime, topNumber, valueName]);

  useEffect(() => {
    if (!fpcDevices || fpcDevices?.length === 0) {
      return;
    }
    if (type === 'network') {
      queryNetworkLineChartData();
    } else if (type === 'device') {
      queryDeviceLineChartData();
    }
  }, [
    endTime,
    fpcDevices,
    metric,
    queryDeviceLineChartData,
    queryNetworkLineChartData,
    startTime,
    topNumber,
    type,
    valueName,
  ]);

  return (
    <>
      <Card
        size="small"
        title={title}
        style={{ height: 360 }}
        bodyStyle={{ height: 320, padding: 10 }}
      >
        <TimeAxisChart
          brush={true}
          loading={loading}
          data={data}
          startTime={displayStartTime}
          endTime={displayEndTime}
          interval={interval}
          markArea={markArea}
          // markArea={getMissingTimeArea(sensorMemoryMetric || [], '采集缺失') as any}
          unitConverter={
            unitConverter
              ? unitConverter
              : (value: number) => {
                  if (value === undefined) {
                    return '采集缺失';
                  }
                  return `${value}%`;
                }
          }
        />
      </Card>
    </>
  );
}
