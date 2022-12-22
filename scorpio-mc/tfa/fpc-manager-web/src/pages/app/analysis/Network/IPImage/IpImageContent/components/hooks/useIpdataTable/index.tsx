import { useMemo } from 'react';
import type { ColumnsType } from 'antd/lib/table';
import type { IpTableTitle } from '../../../../typings';
import { categoryMap, IShowCategory, NetworklocationTypeFilters } from '../../../../typings';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import EllipsisCom from '@/components/EllipsisCom';
import LinkMenu, { EIP_DRILLDOWN_MENU_KEY } from '../../LinkMenu';
import type { ESortDirection } from '@/pages/app/Home/typings';
import { bytesToSize } from '@/utils/utils';

export interface IIpImageMoreTable {
  category: string;
  IpAddress: string;
  networkId: string;
  filterValue?: string[];
  sortProperty: string;
  sortDirection: ESortDirection;
  widowWidth?: number;
}

export default function useIpDataTable(props: IIpImageMoreTable) {
  const { category, IpAddress, networkId, filterValue, sortProperty, sortDirection, widowWidth } =
    props;
  const Tablecolumns: ColumnsType<IpTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
        dataIndex: 'ip_address',
        width: widowWidth,
        render: (_, record) => {
          let srcIp = '',
            desIp = '';
          if (category === IShowCategory.VISITEDIP) {
            //表格的内容是访问的IP说明表格中的内容是目的IP，props传过来的是源IP
            srcIp = IpAddress;
            desIp = record.ip_address;
          }
          if (category === IShowCategory.VISITINGIP) {
            //表格的内容是来访的IP，说明props传过来的时目的IP
            srcIp = record.ip_address;
            desIp = IpAddress;
          }
          // 可以跳转到会话详单
          return (
            <FilterBubble
              dataIndex={record.ip_address}
              label={<EllipsisCom style={{ width: widowWidth }}>{record.ip_address}</EllipsisCom>}
              hasFilter={false}
              DrilldownMenu={
                <LinkMenu
                  MenuItemsGroup={[
                    {
                      label: '跳转到其他页',
                      key: 'jumpToOtherPage',
                      children: [{ label: '会话详单', key: EIP_DRILLDOWN_MENU_KEY.FLOW_RECORD }],
                    },
                  ]}
                  settings={{
                    networkId: networkId,
                    ipPair: { srcIp: srcIp, desIp: desIp },
                  }}
                />
              }
            />
          );
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>IP位置</span>,
        dataIndex: 'isIntranet',
        // width: 120,
        filters: NetworklocationTypeFilters,
        filteredValue: filterValue || null,
        render: (_, record) => {
          if (record.ip_locality_responder === 0 || record.ip_locality_initiator === 0) {
            return '内网';
          }
          return '外网';
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>新建请求/失败</span>,
        dataIndex: 'tcpEstablishedCounts',
        // width: 100,
        sorter: true,
        sortOrder: (sortProperty === 'tcpEstablishedCounts' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return (
            <>
              {record.tcpEstablishedCounts}/
              <span style={{ color: 'red' }}>{record.tcpEstablishedFailCounts}</span>
            </>
          );
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>流量</span>,
        dataIndex: 'totalBytes',
        sorter: true,
        defaultSortOrder: 'descend',
        sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return bytesToSize(record.totalBytes);
        },
      },
    ];
  }, [IpAddress, category, filterValue, networkId, sortDirection, sortProperty, widowWidth]);
  return Tablecolumns;
}
