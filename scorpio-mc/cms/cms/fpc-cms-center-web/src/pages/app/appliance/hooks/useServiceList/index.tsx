import { queryServiceList } from '@/pages/app/Configuration/Service/service';
import { useEffect, useState } from 'react';

export interface IServiceListItem {
  title: string;
  value: string;
  key: string;
}

export interface IServiceListRes {
  id: string;
  name: string;
  createTime: string;
  networkIds: string;
  networkNames: string;
  networkGroupIds: string;
  networkGroupNames: string;
  description: string;
}

export default function useServiceList() {
  const [serviceList, setServiceList] = useState<IServiceListItem[]>([]);
  useEffect(() => {
    queryServiceList().then((res) => {
      const { success, result } = res;
      if (success) {
        setServiceList(
          (Array.isArray(result) ? result : []).map((item) => ({
            title: `${item.name}-${item.networkNames}`,
            value: item.id,
            key: item.id,
          })),
        );
      }
    });
  }, []);

  return serviceList;
}
