import { queryNetworkGroups } from '@/pages/app/Configuration/Network/service';
import type { INetworkGroup } from '@/pages/app/Configuration/Network/typings';
import { useEffect, useState } from 'react';

export default function useNetworkGroupList() {
  const [networkGroupList, setNetworkGroupList] = useState<INetworkGroup[]>([]);

  useEffect(() => {
    (async () => {
      const { success, result } = await queryNetworkGroups();
      if (success) {
        setNetworkGroupList(result);
      }
    })();
  }, []);
  return networkGroupList;
}
