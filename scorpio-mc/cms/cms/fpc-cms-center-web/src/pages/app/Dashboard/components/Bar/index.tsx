/* eslint-disable no-nested-ternary */
import { Card } from 'antd';
import React, { useCallback, useEffect, useState } from 'react';
import Bar from '@/pages/app/analysis/components/Bar';
import { bytesToSize } from '@/utils/utils';
import { queryBarChart } from '../../services';

interface IMultipeSourceBarProps {
  title: string;
  metric: 'system_fs_free' | 'index_fs_free' | 'metadata_fs_free' | 'metadata_hot_fs_free';
  height?: number;
  fpcDevices: any[];
  startTime: string;
  endTime: string;
}

interface IBarData {
  label: string;
  value: number;
}

const MultipeSourceBar: React.FC<IMultipeSourceBarProps> = ({
  title,
  metric,
  height = 300,
  fpcDevices,
  startTime,
  endTime,
}) => {
  const [barData, setBarData] = useState<IBarData[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  /** 获取柱状图数据 */
  const queryBarChartData = useCallback(
    async (
      metric: 'system_fs_free' | 'index_fs_free' | 'metadata_fs_free' | 'metadata_hot_fs_free',
      topNumber: number,
      startTime: string,
      endTime: string,
    ) => {
      setLoading(true);
      const { success, result } = await queryBarChart({ metric, topNumber, startTime, endTime });
      setLoading(false);
      if (!success) {
        return;
      }
      const formData: { label: string; value: any }[] = [];
      result.forEach((item: any) => {
        if (item.monitored_serial_number && item[metric] !== undefined) {
          const serialNumber = item.monitored_serial_number;
          const label = fpcDevices.find((device) => device.serialNumber === serialNumber)?.name;
          formData.push({
            label: label || serialNumber,
            value: item[metric],
          });
        }
      });
      setBarData(formData);
    },
    [fpcDevices],
  );

  useEffect(() => {
    queryBarChartData(metric, 10, startTime, endTime);
  }, [queryBarChartData]);

  return (
    <Card size="small" title={title}>
      {
        <Bar
          loading={loading}
          height={height}
          data={barData}
          valueTextFormatterFn={(value: any) => {
            return bytesToSize(value, 3, 1024);
          }}
        />
      }
    </Card>
  );
};

export default MultipeSourceBar;
