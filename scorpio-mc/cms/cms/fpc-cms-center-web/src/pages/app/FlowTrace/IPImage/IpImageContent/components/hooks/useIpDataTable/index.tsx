import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { ESortDirection } from '@/pages/app/analysis/typings';
import { bytesToSize, getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import { Tooltip } from 'antd';
import type { IpTableTitle } from '../../../../typings';
import { categoryMap, IShowCategory, NetworklocationTypeFilters } from '../../../../typings';
import type { ColumnsType } from 'antd/lib/table';
import moment from 'moment';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { useSelector } from 'umi';
import { useMemo } from 'react';

export interface IIpImageMoreTable {
  category: string;
  IpAddress: string;
  networkId: string;
  filterValue?: string[];
  sortProperty: string;
  sortDirection: ESortDirection;
  windowWidth?: number;
}

export default function useIpDataTable(props: IIpImageMoreTable) {
  const globalSelectTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state: ConnectState) => state.appModel.globalSelectedTime,
  );
  const { category, IpAddress, networkId, filterValue, sortProperty, sortDirection, windowWidth } =
    props;
  const Tablecolumns: ColumnsType<IpTableTitle> = useMemo(() => {
    return [
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>{categoryMap[category]}</span>,
        dataIndex: 'ip_address',
        // align: 'center',
        width: windowWidth,
        // ellipsis: true,
        render: (_, record) => {
          let flowRecordFilter = {};
          if (category === IShowCategory.VISITEDIP) {
            //表格的内容是访问的IP说明表格中的内容是目的IP，props传过来的是源IP
            const srcIsV4 = isIpv4(IpAddress);
            const targetIsV4 = isIpv4(record.ip_address);
            flowRecordFilter = {
              operator: EFilterGroupOperatorTypes.AND,
              group: [
                {
                  field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                  operator: EFilterOperatorTypes.EQ,
                  operand: IpAddress,
                },
                {
                  field: targetIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                  operator: EFilterOperatorTypes.EQ,
                  operand: record.ip_address,
                },
              ],
            };
          }
          if (category === IShowCategory.VISITINGIP) {
            //表格的内容是来访的IP，说明props传过来的时目的IP
            const srcIsV4 = isIpv4(record.ip_address);
            const targetIsV4 = isIpv4(IpAddress);
            flowRecordFilter = {
              operator: EFilterGroupOperatorTypes.AND,
              group: [
                {
                  field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                  operator: EFilterOperatorTypes.EQ,
                  operand: record.ip_address,
                },
                {
                  field: targetIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                  operator: EFilterOperatorTypes.EQ,
                  operand: IpAddress,
                },
              ],
            };
          }

          // 可以跳转到会话详单
          return (
            <div
              style={{
                whiteSpace: 'nowrap',
                width: windowWidth,
                textOverflow: 'ellipsis',
                overflow: 'hidden',
                color: '#1890ff',
                cursor: 'pointer',
              }}
              onClick={() => {
                const url = getLinkUrl(
                  `/analysis/trace/flow-record?from=${moment(
                    globalSelectTime.originStartTime,
                  ).valueOf()}&to=${moment(globalSelectTime.originEndTime).valueOf()}&timeType=${
                    ETimeType.CUSTOM
                  }&filter=${encodeURIComponent(
                    JSON.stringify([
                      flowRecordFilter,
                      {
                        field: 'network_id',
                        operator: EFilterOperatorTypes.EQ,
                        operand: networkId,
                      },
                    ]),
                  )}`,
                );
                jumpNewPage(url);
              }}
            >
              <Tooltip title={record.ip_address}>{record.ip_address}</Tooltip>
            </div>
          );
        },
      },
      {
        title: <span style={{ whiteSpace: 'nowrap' }}>IP位置</span>,
        dataIndex: 'isIntranet',
        // align: 'center',
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
        align: 'center',
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
        // align: 'center',
        sorter: true,
        defaultSortOrder: 'descend',
        sortOrder: (sortProperty === 'totalBytes' ? `${sortDirection}end` : false) as any,
        render: (_, record) => {
          return bytesToSize(record.totalBytes);
        },
      },
    ];
  }, [
    IpAddress,
    category,
    filterValue,
    globalSelectTime.originEndTime,
    globalSelectTime.originStartTime,
    networkId,
    sortDirection,
    sortProperty,
    windowWidth,
  ]);
  return Tablecolumns;
}
