import { useMemo } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import type { DomainNameTableTitle } from '../../../../typings';
import { categoryMap } from '../../../../typings';
import EllipsisCom from '@/components/EllipsisCom';
import type { IIpImageMoreTable } from '../useIpDataTable';

export default function useDomainNameDataTable(props: IIpImageMoreTable) {
  const { category, sortProperty, sortDirection, windowWidth } = props;
  const Tablecolumns: ColumnsType<DomainNameTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
        dataIndex: 'domain',
        align: 'center',
        ellipsis: true,
        render: (_, record) => {
          return <EllipsisCom style={{ width: windowWidth, color: '#1890ff' }}>{record.domain}</EllipsisCom>;
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>请求总数</span>,
        dataIndex: 'totalCounts',
        align: 'center',
        sorter: true,
        sortOrder: (sortProperty === 'totalCounts' ? `${sortDirection}end` : false) as any,
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>请求失败</span>,
        dataIndex: 'failCounts',
        align: 'center',
        sorter: true,
        sortOrder: (sortProperty === 'failCounts' ? `${sortDirection}end` : false) as any,
      },
    ];
  }, [category, sortDirection, sortProperty, windowWidth]);
  return Tablecolumns;
}
