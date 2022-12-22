import { queryNetWorkTree } from '@/pages/app/Network/service';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { message } from 'antd';
import { useEffect, useState } from 'react';

export default function useNetworkList() {
  const [networkList, setNetworkList] = useState<INetworkTreeItem[]>([]);

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkList(result);
      })
      .catch((err) => {
        message.error(err);
      });
  }, []);
  return networkList;
}
