import type { ConnectState } from '@/models/connect';
import type { ILogicalSubnetMap } from '@/pages/app/configuration/LogicalSubnet/typings';
import type { INetworkMap } from '@/pages/app/configuration/Network/typings';
import { useMemo } from 'react';
import { useSelector } from 'umi';

/** 根据id获取网络信息 */
export default function useNetwork(id: string) {
  const allNetworkMap = useSelector<ConnectState, Required<INetworkMap>>(
    (state) => state.networkModel.allNetworkMap,
  );

  const allLogicalSubnetMap = useSelector<ConnectState, Required<ILogicalSubnetMap>>(
    (state) => state.logicSubnetModel.allLogicalSubnetMap,
  );

  const allNetworksMap = useMemo(() => {
    return {
      ...allNetworkMap,
      ...allLogicalSubnetMap,
    };
  }, [allLogicalSubnetMap, allNetworkMap]);
  return allNetworksMap[id]?.name ? allNetworksMap[id]?.name : id;
}
