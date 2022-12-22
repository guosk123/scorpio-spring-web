import { useMemo } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import type { PortTabelTitle } from '../../../../typings';
import { categoryMap } from '../../../../typings';
import { bytesToSize } from '@/utils/utils';
import type { IIpImageMoreTable } from '../useIpdataTable';

export default function usePortdataTable(props: IIpImageMoreTable) {
  const { category, sortProperty, sortDirection } = props;
  const Tablecolumns: ColumnsType<PortTabelTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
        dataIndex: 'port',
        render: (_, record) => {
          return record.port_responder;
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
        dataIndex: 'tcpEstablishedCounts',
        sorter: true,
        sortOrder: (sortProperty === 'tcpEstablishedCounts' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return `${record.tcpEstablishedCounts}/${record.tcpEstablishedFailCounts}`;
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>流量</span>,
        dataIndex: 'totalBytes',
        sorter: true,
        sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return bytesToSize(record.totalBytes);
        },
      },
    ];
  }, [category, sortDirection, sortProperty]);

  return Tablecolumns;
}
