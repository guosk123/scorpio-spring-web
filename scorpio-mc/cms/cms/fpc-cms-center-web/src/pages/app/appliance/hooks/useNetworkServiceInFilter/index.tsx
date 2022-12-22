import type { IField } from '@/components/FieldFilter/typings';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import useNetworkList from '@/pages/app/appliance/hooks/useNetworkList';
import useServiceList from '@/pages/app/appliance/hooks/useServiceList';
import { useMemo } from 'react';

export default function useNetworkServiceInFilter() {
  const networkList = useNetworkList();
  const serviceList = useServiceList();
  const networkAndServiceCol: IField[] = useMemo(() => {
    return [
      {
        title: '网络',
        dataIndex: 'network_id',
        width: 150,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        fieldType: EFieldType.ARRAY,
        enumValue: networkList.map((network) => ({
          text: network.title,
          value: network.value,
        })),
        render: (text: string, record: any) => {
          const networkMap = {};
          networkList.forEach((network) => {
            networkMap[network.value] = network.title;
          });
          return record.network_id?.map((item: string) => {
            return {
              text: <span className="show-text">{networkMap[item] || ''}</span>,
              value: item,
            };
          });
        },
      },
      {
        title: '业务',
        dataIndex: 'service_id',
        width: 150,
        show: true,
        searchable: true,
        operandType: EFieldOperandType.ENUM,
        fieldType: EFieldType.ARRAY,
        enumValue: serviceList.map((server) => ({
          text: server.title,
          value: server.value,
        })),
        render: (text: string, record: any) => {
          const serviceMap = {};
          serviceList.forEach((server) => {
            serviceMap[server.value] = server.title;
          });
          return record.service_id?.map((item: string) => {
            return {
              text: <span className="show-text">{serviceMap[item] || ''}</span>,
              value: item,
            };
          });
        },
      },
    ];
  }, [networkList, serviceList]);
  return networkAndServiceCol;
}
