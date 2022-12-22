import { ONE_KILO_1024 } from '@/common/dict';
import type { IMonitorMetricMap } from '@/models/app/monitor';
import { queryStorageSpaceSettings } from '@/pages/app/configuration/StorageSpace/service';
import { queryMetrics } from '@/services/app/monitor';
import { bytesToSize } from '@/utils/utils';
import { Loading3QuartersOutlined } from '@ant-design/icons';
import React, { useEffect, useMemo, useState } from 'react';

export default function ServerDiskInfo() {
  const [info, setInfo] = useState<IMonitorMetricMap>({} as any);
  const [loading, setLoading] = useState(true);
  const [storageInfo, setStorageInfo] = useState({} as any);

  // 查询缓存大小
  const fsCacheTotalByte = useMemo(() => {
    if (loading) {
      return <Loading3QuartersOutlined style={{ margin: '0 4px' }} spin />;
    }
    const value = info.fs_cache_total_byte?.metricValue || 0;
    return bytesToSize(+value, 3, ONE_KILO_1024);
  }, [info.fs_cache_total_byte?.metricValue, loading]);

  useEffect(() => {
    queryStorageSpaceSettings().then((res) => {
      const { success, result } = res;
      const storageInfoRes: any = {};
      if (success) {
        result.forEach((sub: any) => {
          storageInfoRes[sub.spaceType] = sub;
        });
        setStorageInfo(storageInfoRes);
      }
    });
  }, []);

  useEffect(() => {
    setLoading(true);
    queryMetrics().then((res) => {
      const { success, result } = res;
      const serverState: any = {};
      if (success) {
        result.forEach((ele: any) => {
          serverState[ele.metricName] = ele;
        });
        setInfo(serverState);
      }
      setLoading(false);
    });
  }, []);
  // console.log('storageInfo', storageInfo, info);
  return (
    <div>
      流量查询缓存空间共 {fsCacheTotalByte}
      ，系统会自动清理；每次任务查询最大支持
      {bytesToSize(storageInfo?.transmit_task_file_limit?.capacity || 0, 3, ONE_KILO_1024)}
      PCAP文件导出；相同转发接口下，同一时刻只能执行一个重放任务。
    </div>
  );
}
