import { EMetricApiType } from '@/common/api/analysis';
import {
  DHCP_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_LIST,
  DHCP_VERSION_ENUM,
} from '@/common/app';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type {
  IEnumValue,
  IField,
  IFilter,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import AnalysisChart from '@/pages/app/analysis/components/AnalysisChart';
import {
  EDHCPFields,
  EFormatterType,
  fieldFormatterFuncMap,
  fieldsMapping,
  SortedTypes,
} from '@/pages/app/analysis/components/fieldsManager';
import type { ITableMenuListProps } from '@/pages/app/analysis/components/WithMenuTable';
import WithMenuTable from '@/pages/app/analysis/components/WithMenuTable';
import { queryNetworkFlow, queryNetworkFlowHistogram } from '@/pages/app/analysis/Flow/service';
import { ServiceAnalysisContext, ServiceContext } from '@/pages/app/analysis/Service/index';
import type { IDHCPStatFields, IUriParams } from '@/pages/app/analysis/typings';
import { ESortDirection } from '@/pages/app/analysis/typings';
import type { INetworkGroupMap } from '@/pages/app/Configuration/Network/typings';
import { AnalysisContext, NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType, ENetworkTabs } from '@/pages/app/Network/typing';
import type { TrendChartData } from '@/utils/utils';
import { completeTimePoint, isIpv4, isIpv6, snakeCase } from '@/utils/utils';
import { CloseSquareOutlined, ReloadOutlined } from '@ant-design/icons';
import type { TableColumnProps, TablePaginationConfig } from 'antd';
import { Button, Card } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import { jumpToMetadataTab, MetaDataContext } from '../..';
import { useNetworkServiceInFilter } from '../../../hooks';
import { EMetadataTabType } from '../../typings';

export const fixedColumns: Record<EMetricApiType.DHCP, string[]> = {
  [EMetricApiType.DHCP]: [
    'clientIpAddress',
    'clientMacAddress',
    'serverIpAddress',
    'serverMacAddress',
    'messageType',
  ],
};

type DHCPAnalysisTableColumnProps = IDHCPStatFields & {
  index: number;
};

export enum TabKey {
  'CLIENT' = 'client',
  'SERVER' = 'server',
  'MESSAGE_TYPE' = 'messageType',
}

const getJumpFilterJson = (ipAddress: string, macAddress: string) => {
  const filterJson = [
    {
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: 'ethernet_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: macAddress,
        },
        {
          field: 'ethernet_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: macAddress,
        },
      ],
    },
  ];

  if (isIpv4(ipAddress)) {
    filterJson.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: 'ipv4_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
        {
          field: 'ipv4_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
      ],
    });
  }
  if (isIpv6(ipAddress)) {
    filterJson.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
        {
          field: 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAddress,
        },
      ],
    });
  }

  return filterJson;
};

const renderLink = (
  menuKey: TabKey,
  record: DHCPAnalysisTableColumnProps,
  dhcpType: DHCP_VERSION_ENUM,
  filterCondition?: IFilterCondition,
) => {
  const { clientIpAddress, clientMacAddress, serverIpAddress, serverMacAddress, messageType } =
    record;
  let filterJson;
  switch (menuKey) {
    case TabKey.CLIENT: {
      filterJson = getJumpFilterJson(clientIpAddress as string, clientMacAddress as string);
      break;
    }
    case TabKey.SERVER: {
      filterJson = getJumpFilterJson(serverIpAddress as string, serverMacAddress as string);
      break;
    }
    case TabKey.MESSAGE_TYPE: {
      filterJson = [{ field: 'message_type', operator: '=', operand: String(messageType) }];
      filterCondition
        ?.filter((ele: any) => !ele?.group)
        .forEach((item: any) => {
          filterJson.push({
            field:
              (item.field === 'client_ip_address' &&
                `${dhcpType === DHCP_VERSION_ENUM.DHCP ? 'src_ipv4' : 'src_ipv6'}`) ||
              (item.field === 'client_mac_address' && 'src_mac') ||
              (item.field === 'server_ip_address' &&
                `${dhcpType === DHCP_VERSION_ENUM.DHCP ? 'dest_ipv4' : 'dest_ipv6'}`) ||
              (item.field === 'server_mac_address' && 'dest_mac') ||
              item.field,
            operator: item.operator,
            operand: item.operand,
          });
        });
      break;
    }
    default:
      break;
  }
  console.log('filterJson', filterJson);
  return filterJson;
};

type withIndexColumnProps = IDHCPStatFields & {
  index: number;
};

const dhcpStatFields = Object.keys(EDHCPFields);
export const DEFAULT_SORT_PROPERTY = 'totalBytes';

export interface IDHCPAnalysisProps {
  dhcpType: DHCP_VERSION_ENUM;
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  allNetworkGroupMap: INetworkGroupMap;
}

const DHCPAnalysisComponent: React.FC<IDHCPAnalysisProps> = (props) => {
  const { allNetworkGroupMap, dhcpType, globalSelectedTime } = props;
  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    if (tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);
  const [currentMenu, setCurrentMenu] = useState<TabKey>(TabKey.CLIENT);
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);
  const [sortProperty, setSortProperty] = useState<string>(DEFAULT_SORT_PROPERTY);
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [selectedRow, setSelectedRow] = useState<withIndexColumnProps | null>(null);
  const [flowTableData, setFlowTableData] = useState<IDHCPStatFields[]>([]);
  const [flowHistogramData, setFlowHistogramData] = useState<IDHCPStatFields[]>([]);
  const [queryLoading, setQueryLoading] = useState(false);

  const dhcpMessageEnum = useMemo(() => {
    return dhcpType === DHCP_VERSION_ENUM.DHCP ? DHCP_MESSAGE_TYPE_ENUM : DHCP_V6_MESSAGE_TYPE_ENUM;
  }, [dhcpType]);

  const [state, analysisDispatch] = useContext(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );
  const [metaDataState, metaDataDispatch] = useContext(MetaDataContext);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const columns = useMemo(() => {
    const fullColumns: TableColumnProps<DHCPAnalysisTableColumnProps>[] = [];
    const fieldsColumns = dhcpStatFields
      .filter((field) => field !== 'timestamp')
      .map((field) => {
        const { name, formatterType } = fieldsMapping[field];
        const renderFunc = fieldFormatterFuncMap[formatterType];
        const column = {
          title: name,
          dataIndex: field,
          fixed: fixedColumns[EMetricApiType.DHCP].includes(field) ? ('left' as any) : false,
          width: name.length * 18 + 40,
          align: 'center' as any,
          ellipsis: true,
          sorter: SortedTypes.includes(formatterType),
          ...(renderFunc ? { render: (value: any) => renderFunc(value) } : {}),
        };

        if (field === 'messageType') {
          column.render = (value) => dhcpMessageEnum[value] || value;
        }

        return column;
      });
    fullColumns.push({
      title: '#',
      align: 'center',
      dataIndex: 'index',
      width: 60,
      fixed: 'left',
    });
    fullColumns.push(...fieldsColumns);
    fullColumns.push({
      title: '操作',
      dataIndex: 'operate',
      align: 'center',
      width: 100,
      fixed: 'right',
      render: (text, record) => {
        let tmpDom = <Button type="link" size="small" />;
        if (currentMenu !== TabKey.MESSAGE_TYPE) {
          tmpDom = (
            <Button
              type="link"
              size="small"
              onClick={() => {
                jumpToAnalysisTabNew(state, analysisDispatch, ENetworkTabs.FLOWRECORD, {
                  filter: renderLink(currentMenu, record, dhcpType, filterCondition) as any,
                  networkId: (
                    filterCondition.find((item: any) => item?.field?.includes('network')) as any
                  )?.operand,
                  serviceId: (
                    filterCondition.find((item: any) => item?.field?.includes('service')) as any
                  )?.operand,
                });
              }}
            >
              会话详单
            </Button>
          );
        } else if (dhcpType === DHCP_VERSION_ENUM.DHCP) {
          tmpDom = (
            <Button
              type="link"
              size="small"
              onClick={() => {
                jumpToMetadataTab(
                  metaDataState,
                  metaDataDispatch,
                  EMetadataTabType.DHCP,
                  renderLink(currentMenu, record, dhcpType, filterCondition),
                );
              }}
            >
              DHCP详单
            </Button>
          );
        } else {
          tmpDom = (
            <Button
              type="link"
              size="small"
              onClick={() => {
                jumpToMetadataTab(
                  metaDataState,
                  metaDataDispatch,
                  EMetadataTabType.DHCPV6,
                  renderLink(currentMenu, record, dhcpType, filterCondition),
                );
              }}
            >
              DHCPv6详单
            </Button>
          );
        }
        return tmpDom;
      },
    });
    return fullColumns;
  }, [
    dhcpMessageEnum,
    currentMenu,
    dhcpType,
    state,
    analysisDispatch,
    filterCondition,
    metaDataState,
    metaDataDispatch,
  ]);

  const NSFilter = useNetworkServiceInFilter();

  const tableColumns = useMemo(() => {
    return columns.map((col) => ({
      ...col,
      sortOrder: (sortProperty === col.dataIndex ? `${sortDirection}end` : false) as any,
    }));
  }, [columns, sortProperty, sortDirection]);

  const menuItemList: ITableMenuListProps<TabKey>[] = useMemo(() => {
    return [
      {
        title: '客户端',
        key: TabKey.CLIENT,
        excludeColumn: [
          EDHCPFields.messageType,
          EDHCPFields.serverIpAddress,
          EDHCPFields.serverMacAddress,
        ],
      },
      {
        title: '服务端',
        key: TabKey.SERVER,
        excludeColumn: [
          EDHCPFields.messageType,
          EDHCPFields.clientIpAddress,
          EDHCPFields.clientMacAddress,
        ],
        overrideColumns: {
          [EDHCPFields.sendBytes]: {
            name: '接收字节数',
            formatterType: EFormatterType.BYTE,
          },
          [EDHCPFields.receiveBytes]: {
            name: '发送字节数',
            formatterType: EFormatterType.BYTE,
          },
          [EDHCPFields.sendPackets]: {
            name: '接收包数',
            formatterType: EFormatterType.COUNT,
          },
          [EDHCPFields.receivePackets]: {
            name: '发送包数',
            formatterType: EFormatterType.COUNT,
          },
        },
      },
      {
        title: '消息类型',
        key: TabKey.MESSAGE_TYPE,
        excludeColumn: [
          EDHCPFields.clientIpAddress,
          EDHCPFields.serverIpAddress,
          EDHCPFields.clientMacAddress,
          EDHCPFields.serverMacAddress,
          EDHCPFields.sendBytes,
          EDHCPFields.sendPackets,
          EDHCPFields.receiveBytes,
          EDHCPFields.receivePackets,
        ],
      },
    ];
  }, []);

  // TODO: 确定图表的系列名称;
  const getSeriesName = useCallback(
    (record: IDHCPStatFields) => {
      let seriesName = '[--]';
      const { clientIpAddress, clientMacAddress, serverIpAddress, serverMacAddress, messageType } =
        record;
      switch (currentMenu) {
        case TabKey.CLIENT: {
          seriesName = `${clientIpAddress}_${clientMacAddress}`;
          break;
        }
        case TabKey.SERVER: {
          seriesName = `${serverIpAddress}_${serverMacAddress}`;
          break;
        }
        case TabKey.MESSAGE_TYPE: {
          seriesName = `${dhcpMessageEnum[messageType] || messageType}`;
          break;
        }
        default: {
          break;
        }
      }
      return seriesName;
    },
    [currentMenu, dhcpMessageEnum],
  );

  const filterFields: IField[] = useMemo(
    () =>
      [
        EDHCPFields.clientIpAddress,
        EDHCPFields.clientMacAddress,
        EDHCPFields.serverIpAddress,
        EDHCPFields.serverMacAddress,
        EDHCPFields.messageType,
      ]
        .map((field) => {
          const { name, formatterType, filterOperandType } = fieldsMapping[field];
          let enumList = fieldsMapping[field].enumValue as IEnumValue[];
          const isEnum = formatterType === EFormatterType.ENUM;
          /// messageType字段仅保存了dhcp的枚举值，dhcpv6的枚举值需要手动覆盖
          if (field === EDHCPFields.messageType && dhcpType === DHCP_VERSION_ENUM.DHCPv6) {
            enumList = DHCP_V6_MESSAGE_TYPE_LIST;
          }
          return {
            title: name,
            dataIndex: snakeCase(field),
            ...(filterOperandType ? { operandType: filterOperandType } : {}),
            // operandType: filterOperandType,
            ...(isEnum ? { enumValue: enumList } : {}),
          };
        })
        .concat(NSFilter)
        .map((f) => ({
          ...f,
          ...(() => {
            if ((f.dataIndex as string)?.includes('network_id')) {
              return {
                unEditable: true,
                single: true,
              };
            }
            return {};
          })(),
        })),
    [NSFilter, dhcpType],
  );

  const dsl = useMemo(() => {
    let nextDsl = filterCondition2Spl(
      filterCondition.filter((c) => {
        if (
          (c as IFilter)?.field?.includes('network_id') &&
          allNetworkGroupMap[(c as IFilter)?.operand as string]
        ) {
          return false;
        }
        return true;
      }),
      filterFields,
    );
    if (nextDsl) {
      nextDsl += ' AND ';
    }
    nextDsl += `(${
      networkId && networkType === ENetowrkType.NETWORK ? `network_id="${networkId}" AND ` : ``
    }dhcp_version=${dhcpType}) | gentimes timestamp start="${globalSelectedTime.startTime}" end="${
      globalSelectedTime.endTime
    }"`;
    return nextDsl;
  }, [
    filterCondition,
    filterFields,
    networkId,
    networkType,
    dhcpType,
    globalSelectedTime.startTime,
    globalSelectedTime.endTime,
    allNetworkGroupMap,
  ]);

  const networkGroupId = useMemo(() => {
    const networkCondition = filterCondition.find((c) =>
      (c as IFilter).field?.includes('network_id'),
    );
    if (networkCondition && (networkCondition as IFilter)?.operand) {
      return allNetworkGroupMap[(networkCondition as IFilter)?.operand as string]?.id;
    }
    return undefined;
  }, [allNetworkGroupMap, filterCondition]);

  const tableData = useMemo(() => {
    if (!Array.isArray(flowTableData)) {
      return [];
    }
    return flowTableData.map((data, index) => {
      return {
        ...data,
        index: index + 1,
      };
    });
  }, [flowTableData]);

  const queryParams = useMemo(() => {
    const queryData = {
      metricApi: EMetricApiType.DHCP,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortDirection,
      type: currentMenu,
      dsl,
      ...(() => {
        if (networkGroupId) {
          return {
            networkGroupId: networkGroupId || '',
          };
        }
        return {};
      })(),
    };
    queryData[networkType === ENetowrkType.NETWORK ? 'networkId' : 'networkGroupId'] = networkId;
    return queryData;
  }, [
    globalSelectedTime.startTime,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    sortDirection,
    currentMenu,
    dsl,
    networkType,
    networkId,
    networkGroupId,
  ]);

  const queryData = useCallback(() => {
    const payload: any = { ...queryParams };

    if (selectedRow) {
      const id = getSeriesName(selectedRow);
      if (currentMenu !== TabKey.MESSAGE_TYPE) {
        payload.id = id;
      } else {
        payload.messageType = id;
      }
    }

    if (sortProperty) {
      payload.sortProperty = snakeCase(sortProperty);
      setQueryLoading(true);
      Promise.all([queryNetworkFlow(payload), queryNetworkFlowHistogram(payload)]).then((res) => {
        const [flow, flowHistogram] = res;
        if (flow.success || flowHistogram.success) {
          setFlowTableData(flow.result);
          setFlowHistogramData(flowHistogram.result);
        }
        setQueryLoading(false);
      });
    }
  }, [currentMenu, getSeriesName, queryParams, selectedRow, sortProperty]);

  useEffect(() => {
    queryData();
  }, [queryData]);

  const currentFormatter = useMemo(() => {
    const valueType = fieldsMapping[sortProperty]?.formatterType;
    const formatterType = fieldFormatterFuncMap[valueType];
    return formatterType;
  }, [sortProperty]);

  const cancelSelectedRow = () => {
    setSelectedRow(null);
  };

  const handleMenuChange = (menuKey: TabKey) => {
    setCurrentMenu(menuKey);
    setSortProperty(DEFAULT_SORT_PROPERTY);
    setSortDirection(ESortDirection.DESC);
    setSelectedRow(null);
  };

  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
    if (sorter.field !== sortProperty) {
      setSortProperty(sorter.field);
      setSortDirection(ESortDirection.DESC);
    } else {
      setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
    }
  };

  const handleRowClick = (e: any, record: withIndexColumnProps) => {
    setSelectedRow(record);
  };

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  const chartData = useMemo(() => {
    const formater = fieldFormatterFuncMap[fieldsMapping[sortProperty].formatterType];
    const tmp: Record<string, TrendChartData> = {};
    if (Array.isArray(flowHistogramData)) {
      flowHistogramData.forEach((item) => {
        const seriesName: string = getSeriesName(item);
        if (seriesName) {
          if (!tmp[seriesName]) {
            tmp[seriesName] = [];
          }
          if (formater) {
            tmp[seriesName].push([
              item.timestamp as string,
              parseFloat(formater(item[sortProperty])),
            ]);
          } else {
            tmp[seriesName].push([item.timestamp, item[sortProperty] as number]);
          }
        }
      });
    }
    Object.keys(tmp).forEach((seriesName) => {
      tmp[seriesName] = completeTimePoint(
        tmp[seriesName],
        globalSelectedTime.startTime!,
        globalSelectedTime.endTime!,
        globalSelectedTime.interval,
      );
    });
    return tmp;
  }, [flowHistogramData, getSeriesName, globalSelectedTime, sortProperty]);

  const topTenChart = (
    <AnalysisChart data={chartData} loading={queryLoading} unitConverter={currentFormatter} />
  );

  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          title={`${getSeriesName(selectedRow)}:${fieldsMapping[sortProperty]?.name}`}
          extra={
            <span onClick={cancelSelectedRow}>
              <CloseSquareOutlined />
            </span>
          }
          style={{ marginBottom: '10px' }}
        >
          <AnalysisChart loading={queryLoading} data={chartData} unitConverter={currentFormatter} />
        </Card>
      ) : (
        <Card
          size="small"
          title={fieldsMapping[sortProperty]?.name}
          style={{ marginBottom: '10px' }}
        >
          {topTenChart}
        </Card>
      )}
      <FieldFilter
        fields={filterFields}
        historyStorageKey={`metadata-${dhcpType}-filter-history`}
        condition={filterCondition}
        onChange={handleFilterChange}
      />
      <WithMenuTable<DHCPAnalysisTableColumnProps, TabKey>
        tableKey={`metadata-${dhcpType}-table`}
        columns={tableColumns}
        currentMenu={currentMenu}
        menuItemList={menuItemList}
        onMenuChange={handleMenuChange}
        size="small"
        rowKey={(record) => record.index}
        onChange={handleTableChange}
        bordered={true}
        loading={queryLoading}
        dataSource={tableData}
        extraTool={
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            loading={queryLoading}
            onClick={queryData}
          >
            刷新
          </Button>
        }
        onRow={(record) => {
          return {
            onClick: (event) => {
              handleRowClick(event, record);
            },
          };
        }}
      />
    </>
  );
};

const mapStateToProps = ({
  appModel: { globalSelectedTime },
  networkModel: { allNetworkGroupMap },
  loading: { effects },
}: ConnectState) => ({
  globalSelectedTime,
  allNetworkGroupMap,
  queryLoading: effects['npmdModel/queryNetworkFlow'],
});

export default connect(mapStateToProps)(DHCPAnalysisComponent);
