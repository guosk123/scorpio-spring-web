import type { ConnectState } from '@/models/connect';
import type { ILogicalSubnetMap } from '@/pages/app/Configuration/LogicalSubnet/typings';
import type { INetworkGroupMap, INetworkSensorMap } from '@/pages/app/Configuration/Network/typings';
import { useMemo } from 'react';
import { useSelector } from 'umi';

/** 根据id获取网络信息 */
export default function useNetwork(id: string) {
  const allNetworkSensorMap = useSelector<ConnectState, Required<INetworkSensorMap>>(
    (state) => state.networkModel.allNetworkSensorMap,
  );

  const allNetworkGroupMap = useSelector<ConnectState, Required<INetworkGroupMap>>(
    (state) => state.networkModel.allNetworkGroupMap,
  );

  const allLogicalSubnetMap = useSelector<ConnectState, Required<ILogicalSubnetMap>>(
    (state) => state.logicSubnetModel.allLogicalSubnetMap,
  );

  const allNetworksMap = useMemo(() => {
    return {
      ...allNetworkSensorMap,
      ...allNetworkGroupMap,
      ...allLogicalSubnetMap,
    };
  }, [allLogicalSubnetMap, allNetworkGroupMap, allNetworkSensorMap]);
  return allNetworksMap[id]?.name ? allNetworksMap[id]?.name : id;
}
