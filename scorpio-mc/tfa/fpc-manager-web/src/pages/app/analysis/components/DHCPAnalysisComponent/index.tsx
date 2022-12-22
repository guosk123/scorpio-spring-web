import { EMetricApiType } from '@/common/api/analysis';
import {
  DHCP_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_LIST,
  DHCP_VERSION_ENUM,
} from '@/common/app';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type { IEnumValue, IField, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { MetaDataContext } from '@/pages/app/appliance/Metadata/Analysis';
import { jumpToMetadataTab } from '@/pages/app/appliance/Metadata/Analysis/constant';
import { EMetadataTabType } from '@/pages/app/appliance/Metadata/Analysis/typings';
import { getLinkUrl, isIpv4, isIpv6, jumpNewPage, snakeCase } from '@/utils/utils';
import { CloseSquareOutlined, ReloadOutlined } from '@ant-design/icons';
import type { TableColumnProps, TablePaginationConfig } from 'antd';
import { Button, Card } from 'antd';
import React, { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useParams } from 'umi';
import styles from '../../components/FlowAnalysis/index.less';
import type { IDHCPStatFields, IUriParams } from '../../typings';
import { ESortDirection } from '../../typings';
import type { TrendChartData } from '../AnalysisChart';
import AnalysisChart from '../AnalysisChart';
import {
  EFormatterType,
  fieldFormatterFuncMap,
  fieldsMapping,
  SortedTypes,
} from '../fieldsManager';
import { EDHCPFields } from '../fieldsManager/fieldsGroup/dhcpAnalysisFieldsGroup';
import { completeTimePoint } from '../FlowAnalysis';
import type { ITableMenuListProps } from '../WithMenuTable';
import WithMenuTable from '../WithMenuTable';

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
  networkId?: string,
  pcapFileId?: string,
  from?: number,
  to?: number,
  filterCondition?: IFilterCondition,
) => {
  let uriPrefix = `/analysis/trace`;
  if (pcapFileId) {
    uriPrefix = `/analysis/offline/${pcapFileId}`;
  }

  const { clientIpAddress, clientMacAddress, serverIpAddress, serverMacAddress, messageType } =
    record;
  let filterJson: IFilterCondition = [];
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
      filterJson = [
        { field: 'message_type', operator: EFilterOperatorTypes.EQ, operand: String(messageType) },
      ];
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

  if (networkId) {
    filterJson.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId,
    });
  }

  if (menuKey !== TabKey.MESSAGE_TYPE) {
    jumpNewPage(
      `${getLinkUrl(uriPrefix)}/flow-record?filter=${encodeURIComponent(
        JSON.stringify(filterJson),
      )}&from=${from}&to=${to}&timeType=${ETimeType.CUSTOM}`,
    );

    return 0;
  }
  history.replace({
    pathname: history.location.pathname,
    query: { ...history.location.query, from: String(from), to: String(to) },
  });

  return `${encodeURIComponent(JSON.stringify(filterJson))}`;
};

const renderLinkText = (menuKey: TabKey, dhcpType: DHCP_VERSION_ENUM) => {
  if (menuKey !== TabKey.MESSAGE_TYPE) {
    return '会话详单';
  }
  return `${dhcpType === DHCP_VERSION_ENUM.DHCP ? 'DHCP' : 'DHCPv6'}详单`;
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
  flowTableData: Record<EMetricApiType, IDHCPStatFields[]>;
  flowHistogramData: Record<EMetricApiType, IDHCPStatFields[]>;
  queryLoading: boolean | undefined;
}

const DHCPAnalysisComponent: React.FC<IDHCPAnalysisProps> = (props) => {
  const { dhcpType, dispatch, globalSelectedTime, flowHistogramData, flowTableData, queryLoading } =
    props;
  const { networkId, pcapFileId }: IUriParams = useParams();
  const [currentMenu, setCurrentMenu] = useState<TabKey>(TabKey.CLIENT);
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);
  const [sortProperty, setSortProperty] = useState<string>(DEFAULT_SORT_PROPERTY);
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [selectedRow, setSelectedRow] = useState<withIndexColumnProps | null>(null);

  const dhcpMessageEnum = useMemo(() => {
    return dhcpType === DHCP_VERSION_ENUM.DHCP ? DHCP_MESSAGE_TYPE_ENUM : DHCP_V6_MESSAGE_TYPE_ENUM;
  }, [dhcpType]);
  const [state, metaDataDispatch] = useContext(MetaDataContext);

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
        return (
          <span
            className="link"
            onClick={() => {
              const shareInfo = renderLink(
                currentMenu,
                record,
                dhcpType,
                networkId,
                pcapFileId,
                new Date(globalSelectedTime.originStartTime).valueOf(),
                new Date(globalSelectedTime.originEndTime).valueOf(),
                filterCondition,
              );
              if (shareInfo) {
                jumpToMetadataTab(
                  state,
                  metaDataDispatch,
                  dhcpType ? EMetadataTabType.DHCPV6 : EMetadataTabType.DHCP,
                  { filter: shareInfo },
                );
              }
            }}
          >
            {renderLinkText(currentMenu, dhcpType)}
          </span>
        );
      },
    });
    return fullColumns;
  }, [
    dhcpMessageEnum,
    currentMenu,
    dhcpType,
    state,
    metaDataDispatch,
    networkId,
    pcapFileId,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
    filterCondition,
  ]);

  const tableColumns = useMemo(() => {
    return columns.map((col) => ({
      ...col,
      sortOrder: (sortProperty === col.dataIndex ? `${sortDirection}end` : false) as any,
    }));
  }, [columns, sortProperty, sortDirection]);

  const menuItemList: ITableMenuListProps<TabKey>[] = [
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
      ].map((field) => {
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
      }),
    [dhcpType],
  );

  const dsl = useMemo(() => {
    let nextDsl = filterCondition2Spl(filterCondition, filterFields);

    if (networkId) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(${`network_id="${networkId}"`}`;
    }
    if (pcapFileId) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(${`network_id="${pcapFileId}"`}`;
    }
    if (nextDsl) {
      nextDsl += ' AND ';
    }
    nextDsl += `(dhcp_version=${dhcpType}) | gentimes timestamp start="${globalSelectedTime.startTime}" end="${globalSelectedTime.endTime}"`;
    return nextDsl;
  }, [
    filterCondition,
    filterFields,
    networkId,
    pcapFileId,
    dhcpType,
    globalSelectedTime.startTime,
    globalSelectedTime.endTime,
  ]);

  const tableData = useMemo(() => {
    if (flowTableData.dhcps) {
      return flowTableData.dhcps.map((data, index) => {
        return {
          ...data,
          index: index + 1,
        };
      });
    }
    return [];
  }, [flowTableData]);

  const queryParams = useMemo(() => {
    return {
      networkId,
      packetFileId: pcapFileId,
      metricApi: EMetricApiType.DHCP,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      interval: globalSelectedTime.interval,
      sortDirection,
      type: currentMenu,
      dsl,
    };
  }, [
    currentMenu,
    dsl,
    networkId,
    pcapFileId,
    globalSelectedTime.endTime,
    globalSelectedTime.interval,
    globalSelectedTime.startTime,
    sortDirection,
  ]);

  const queryData = useCallback(() => {
    const payload: Record<string, number | string | undefined> = { ...queryParams };

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
      dispatch({
        type: 'npmdModel/queryNetworkFlow',
        payload,
      });
      dispatch({
        type: 'npmdModel/queryNetworkFlowTable',
        payload,
      });
    }
  }, [currentMenu, dispatch, getSeriesName, queryParams, selectedRow, sortProperty]);

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
    if (flowHistogramData.dhcps) {
      flowHistogramData.dhcps.forEach((item) => {
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
  }, [flowHistogramData.dhcps, getSeriesName, globalSelectedTime, sortProperty]);

  const topTenChart = (
    <AnalysisChart data={chartData} loading={queryLoading} unitConverter={currentFormatter} />
  );

  const table = (
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
        <Button type="primary" icon={<ReloadOutlined />} loading={queryLoading} onClick={queryData}>
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
  );

  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          title={`${getSeriesName(selectedRow)}:${fieldsMapping[sortProperty]?.name}`}
          extra={
            <span onClick={cancelSelectedRow} className={styles.closeBtn}>
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
      {table}
    </>
  );
};

const mapStateToProps = ({
  npmdModel: { flowTableData, flowHistogramData },
  appModel: { globalSelectedTime },

  loading: { effects },
}: ConnectState) => ({
  flowTableData: flowTableData as Record<EMetricApiType, IDHCPStatFields[]>,
  flowHistogramData: flowHistogramData as Record<EMetricApiType, IDHCPStatFields[]>,
  globalSelectedTime,
  queryLoading: effects['npmdModel/queryNetworkFlow'],
});

export default connect(mapStateToProps)(DHCPAnalysisComponent);
