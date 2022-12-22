import { useMemo } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import type { DomainNameTableTitle } from '../../../../typings';
import { categoryMap } from '../../../../typings';
import type { IIpImageMoreTable } from '../useIpdataTable';
import EllipsisCom from '@/components/EllipsisCom';

export default function useDomainTable(props: IIpImageMoreTable) {
  const { category, sortProperty, sortDirection, widowWidth } = props;
  const Tablecolumns: ColumnsType<DomainNameTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
        dataIndex: 'domain',
        width: widowWidth,
        render: (_, record) => {
          return <EllipsisCom style={{ width: widowWidth }}>{record.domain}</EllipsisCom>;
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
        dataIndex: 'totalCounts',
        // width: 100,
        sorter: true,
        sortOrder: (sortProperty === 'totalCounts' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return `${record.totalCounts}/${record.failCounts}`;
        },
      },
    ];
  }, [category, sortDirection, sortProperty, widowWidth]);
  return Tablecolumns;
}
