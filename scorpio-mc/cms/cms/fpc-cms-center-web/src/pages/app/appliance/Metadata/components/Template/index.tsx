import {
  DEFAULT_PAGE_SIZE_KEY,
  DHCP_VERSION_ENUM,
  ICMP_VERSION_ENUM,
  PAGE_DEFAULT_SIZE,
} from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import ExportFile from '@/components/ExportFile';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type { IField, IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldType, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import useClearURL from '@/hooks/useClearURL';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app//analysis/typings';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import { ANALYSIS_RESULT_ID_PREFIX } from '@/pages/app/appliance/ScenarioTask/Result';
import type { INetworkGroupMap } from '@/pages/app/Configuration/Network/typings';
import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import { dimensionsUrl } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';
import type { IFlowRecordColumnProps } from '@/pages/app/Netflow/FlowRecord';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { clearShareInfo, getTabDetail } from '@/pages/app/Network/components/EditTabs';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import storage from '@/utils/frame/storage';
import { parseArrayJson, scrollTo, snakeCase } from '@/utils/utils';
import { ReloadOutlined, RollbackOutlined } from '@ant-design/icons';
import { useDeepCompareEffect } from '@ant-design/pro-utils';
import { Button, Space, Spin } from 'antd';
import _ from 'lodash';
import moment from 'moment';
import { useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useDispatch, useLocation, useParams, useSelector } from 'umi';
import { MetaDataContext } from '../..';
import useColumnForMetadata from '../../hooks/useColumnForMetadata';
import { exportMetadataLogs, queryMetadataMapfieldKeys } from '../../service';
import type { IMetadataLog, IQueryMetadataParams } from '../../typings';
import { EMailProtocol, EMetadataProtocol } from '../../typings';
import styles from './index.less';

export const METADATA_EXCLUDE_COLS = ['operate', 'dnsType'];

export const getColumnParams = getColumnParamsFunc(METADATA_EXCLUDE_COLS);

interface ILocation {
  query: {
    filter?: string;
    analysisResultId?: string;
    /**
     * 各种分析任务的开始时间
     */
    analysisStartTime?: string;
    /**
     * 各种分析任务的结束时间
     */
    analysisEndTime?: string;
  };
}

const defColArrSimple = ['index', 'startTime', 'srcIp', 'destIp', 'action'];

const defColArr = ['index', 'startTime', 'srcIp', 'srcPort', 'destIp', 'destPort', 'action'];

const defMailArr = ['subject', 'from', 'to'];

const defSqlArr = ['cmd', 'error', 'delaytime'];

const defaultShowColumnsForFlow = {
  [EMetadataProtocol.HTTP]: [...defColArr, 'uri', 'method', 'status', 'host'],
  [EMetadataProtocol.DNS]: [...defColArr, 'domain', 'domainAddress'],
  [EMetadataProtocol.FTP]: [...defColArr, 'cmd', 'reply'],
  [EMetadataProtocol.MAIL]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.POP3]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.SMTP]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.IMAP]: [...defColArr, ...defMailArr],
  [EMetadataProtocol.TELNET]: [...defColArr, 'cmd', 'reply'],
  [EMetadataProtocol.SSL]: [...defColArr, 'serverName', 'issuer', 'validity'],
  [EMetadataProtocol.SSH]: [...defColArr, 'clientSoftware', 'serverVersion', 'serverSoftware'],
  [EMetadataProtocol.MYSQL]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.POSTGRESQL]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.TNS]: [...defColArr, ...defSqlArr],
  // [EMetadataProtocol.ICMP]: [...defColArr],
  [EMetadataProtocol.ICMPV4]: [...defColArrSimple, 'result'],
  [EMetadataProtocol.ICMPV6]: [...defColArrSimple, 'result'],
  [EMetadataProtocol.SOCKS5]: [...defColArr, 'bindAddr', 'bindPort', 'cmdResult'],
  [EMetadataProtocol.DHCP]: [
    'index',
    'startTime',
    'srcIpv4',
    'destIpv4',
    'srcMac',
    'destMac',
    'messageType',
    'parameters',
    'offeredIpv4Address',
    'action',
  ],
  [EMetadataProtocol.DHCPV6]: [
    'index',
    'startTime',
    'srcIpv6',
    'destIpv6',
    'srcMac',
    'destMac',
    'messageType',
    'parameters',
    'offeredIpv6Address',
    'action',
  ],
  [EMetadataProtocol.TDS]: [...defColArr, ...defSqlArr],
  [EMetadataProtocol.ARP]: [
    'index',
    'startTime',
    'srcIp',
    'destIp',
    'srcMac',
    'destMac',
    'type',
    'action',
  ],
  [EMetadataProtocol.OSPF]: [
    ...defColArrSimple,
    'messageType',
    'sourceOspfRouter',
    'linkStateIpv4Address',
  ],
  [EMetadataProtocol.LDAP]: [...defColArr, 'opType', 'resStatus', 'resContent', 'reqContent'],
  [EMetadataProtocol.DB2]: [...defColArr, 'codePoint', 'data'],
};

export const DISABLE_FILTER_BUBBLE = ['dnsQueries', 'answer', 'parameters'];

/**
 * 表格定义
 */
export interface IColumnProps<RecordType> extends IFlowRecordColumnProps<RecordType> {}

interface IMetadataLayoutProps<MetaType> {
  // 协议
  protocol: EMetadataProtocol;
  // 不同的协议表格展示不同的列
  tableColumns: IColumnProps<MetaType>[];

  timeField?: string;

  // 入口
  entry?: string;
  // 用来表示是否是统一使用src_ip字段来表示，
  // false时: src_ipv4,src_ipv6,dest_ipv4,dest_ipv6
  // true: src_ip, dest_ip
  isNewIpFieldType?: boolean;
  allNetworkGroupMap: INetworkGroupMap;
}

function MetadataLayout<MetaType extends IMetadataLog>({
  protocol,
  tableColumns,
  entry,
  isNewIpFieldType = false,
  timeField = 'start_time',
  allNetworkGroupMap,
}: IMetadataLayoutProps<MetaType>) {
  const location = useLocation();

  const tableKey = `metadata-log-${protocol}`;

  const {
    filter: uriFilter,
    analysisResultId,
    analysisStartTime,
    analysisEndTime,
  } = (location as unknown as ILocation).query;
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);
  const [metaDataState, metaDataDispatch] = useContext(
    isDimensionsTab ? DimensionsSearchContext : MetaDataContext,
  );
  const flowAnalysisDetail = getTabDetail(metaDataState) || {};

  const [columns, setColumns] = useState<string[]>();

  useEffect(() => {
    clearShareInfo(metaDataDispatch);
  }, [metaDataDispatch]);
  const { shareInfo } = metaDataState;

  const filter = useMemo(() => {
    if (isDimensionsTab) {
      return JSON.stringify([
        {
          field: 'src_ipv4',
          operator: EFilterOperatorTypes.EQ,
          operand: flowAnalysisDetail?.searchBoxInfo?.content,
        },
      ]);
    }
    if (shareInfo) {
      return shareInfo;
    }
    return uriFilter;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shareInfo, uriFilter]);
  const { serviceId: urlServiceId, networkId: urlNetworkId } = useParams() as {
    networkId: string;
    serviceId: string;
  };
  const { serviceId, networkId } = (() => {
    if (urlNetworkId?.includes('^')) {
      return {
        serviceId: urlServiceId,
        networkId: urlNetworkId?.split('^')[1],
      };
    }
    return { serviceId: urlServiceId, networkId: urlNetworkId };
  })();

  const dispatch = useDispatch<Dispatch>();
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const metadataModel = useSelector<ConnectState, ConnectState['metadataModel']>(
    (state) => state.metadataModel,
  );
  const queryLogsLoading = useSelector<ConnectState, boolean>(
    (state) => state.loading.effects['metadataModel/queryMetadataLogs'] || false,
  );

  const [pageIsReady, setPageIsReady] = useState<boolean>(false);
  // 排序方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  // filter过滤条件
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  const [mapFieldKeys, setMapFieldKeys] = useState<Record<string, string[]>>({});

  const { pagination, protocolLogsMap } = metadataModel;
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const pageProps = useMemo(() => {
    return {
      currentPage: pagination.current || 0,
      pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
      total: pagination.total || 0,
    };
  }, [pagination]);

  const selectedTimeInfo = useMemo(() => {
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const networkGroupId = useMemo(() => {
    const networkCondition = filterCondition.find((c) =>
      (c as IFilter).field?.includes('network_id'),
    );
    if (networkCondition && (networkCondition as IFilter)?.operand) {
      return allNetworkGroupMap[(networkCondition as IFilter)?.operand as string]?.id;
    }
    return undefined;
  }, [allNetworkGroupMap, filterCondition]);

  // ====== Filter过滤 E ======

  // 表格所需要的字段
  const { fullTableColumns, fullColumns } = useColumnForMetadata({
    pageProps,
    protocol,
    tableColumns,
    isNewIpFieldType,
    onFilterClick: [filterCondition, setFilterCondition],
    sortDirection: sortDirection,
  });

  const filterField = useMemo<IField[]>(() => {
    return fullColumns
      .filter((item) => item.searchable)
      .map((item) => {
        const fieldValue = ['service_id', 'network_id'].includes(String(item.dataIndex || ''))
          ? String(item.dataIndex)
          : snakeCase(item.dataIndex as string);
        return {
          title: item.title as string,
          dataIndex: fieldValue as string,
          ...(item.fieldType ? { type: item.fieldType } : {}),
          operandType: item.operandType!,
          enumValue: item.enumValue || [],
          ...(() => {
            if ((item.dataIndex as string)?.includes('network_id')) {
              return {
                unEditable: true,
                single: true,
              };
            }
            return {};
          })(),

          ...(item.fieldType === EFieldType.Map
            ? {
                subFields: mapFieldKeys[fieldValue]?.map((key) => {
                  return {
                    dataIndex: key,
                    title: key,
                  };
                }),
              }
            : {}),
        };
      });
  }, [fullColumns, mapFieldKeys]);

  const getFilterParams = () => {
    const nextProtocol = protocol;
    // 过滤条件转SPL
    // const filterSpl = filterCondition2Spl(
    //   expendNetworkGroupConditions(filterCondition, allNetworkGroupMap),
    //   filterField,
    // );
    const filterSpl = filterCondition2Spl(
      filterCondition.filter((c) => {
        if (
          (c as IFilter)?.field?.includes('network_id') &&
          allNetworkGroupMap[(c as IFilter)?.operand as string]
        ) {
          return false;
        }
        return true;
      }),
      filterField,
    );

    const queryParams: IQueryMetadataParams = {
      protocol: nextProtocol,
      sortProperty: timeField,
      sortDirection: sortDirection || ESortDirection.DESC,
      startTime: moment(selectedTimeInfo.originStartTime).format(),
      endTime: moment(selectedTimeInfo.originEndTime).format(),
      dsl: filterSpl || '',
      serviceId,
      ...(() => {
        if (networkGroupId) {
          return {
            networkGroupId: networkGroupId || '',
          };
        }
        return {};
      })(),
    };

    // 如果是网络组，加上网络组id
    if (networkType === ENetowrkType.NETWORK_GROUP) {
      queryParams.networkGroupId = networkId;
    }
    // 这里填充上分析任务的时间
    if (analysisStartTime && analysisEndTime) {
      queryParams.startTime = moment(decodeURIComponent(analysisStartTime)).format();
      queryParams.endTime = moment(decodeURIComponent(analysisEndTime)).format();
    }

    // 拆分IMAIL为SMTP POP3 IMAP
    if (protocol === EMetadataProtocol.SMTP) {
      queryParams.protocol = EMetadataProtocol.MAIL;
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (protocol = "${EMailProtocol.SMTP}") `;
    }
    if (protocol === EMetadataProtocol.POP3) {
      queryParams.protocol = EMetadataProtocol.MAIL;
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (protocol = "${EMailProtocol.POP3}") `;
    }
    if (protocol === EMetadataProtocol.IMAP) {
      queryParams.protocol = EMetadataProtocol.MAIL;
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (protocol = "${EMailProtocol.IMAP}") `;
    }

    // 拆分ICMP为ICMPv4和ICMPv6
    if (protocol === EMetadataProtocol.ICMPV4) {
      queryParams.protocol = EMetadataProtocol.ICMP;
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (version = ${ICMP_VERSION_ENUM.ICMPv4}) `;
    }
    if (protocol === EMetadataProtocol.ICMPV6) {
      queryParams.protocol = EMetadataProtocol.ICMP;
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (version = ${ICMP_VERSION_ENUM.ICMPv6}) `;
    }

    // fix: 特殊处理DHCP协议
    if (protocol === EMetadataProtocol.DHCP) {
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (version = ${DHCP_VERSION_ENUM.DHCP}) `;
    }
    if (protocol === EMetadataProtocol.DHCPV6) {
      if (queryParams.dsl) {
        queryParams.dsl += ` AND `;
      }
      queryParams.dsl += ` (version = ${DHCP_VERSION_ENUM.DHCPv6}) `;
    }

    // 兼容各种查询任务的详情
    if (analysisResultId) {
      queryParams.id = ANALYSIS_RESULT_ID_PREFIX + analysisResultId;
    } else if (networkId) {
      if (networkType === ENetowrkType.NETWORK) {
        if (queryParams.dsl) {
          queryParams.dsl += ` AND`;
        }
        // 网络 ID 改成了数组形式
        queryParams.dsl += ` (network_id<Array> = ${networkId})`;
        if (serviceId) {
          if (queryParams.dsl) {
            queryParams.dsl += ` AND`;
          }
          // 业务 ID 改成了数组形式
          queryParams.dsl += ` (service_id<Array> = ${serviceId})`;
        }
      }
    }

    // 拼接全局的时间和
    queryParams.dsl += ` | gentimes ${timeField} start="${queryParams.startTime}" end="${queryParams.endTime}"`;

    return queryParams;
  };

  const queryMedataLogs = (params: Partial<IQueryMetadataParams>) => {
    const newParams = getFilterParams();
    const dimensionsQueryData = (() => {
      const {
        searchBoxInfo,
        // shareRow,
        // drilldown = EDRILLDOWN.NOTDRILLDOWN
      } = flowAnalysisDetail;
      const tmpIds = {
        networkId: searchBoxInfo?.networkIds
          .filter((item: string) => !item.includes('networkGroup'))
          .map((sub: string) => sub.replace('^network', ''))
          .join(','),
        networkGroupId: searchBoxInfo?.networkIds
          .filter((item: string) => item.includes('networkGroup'))
          .map((sub: string) => sub.replace('^networkGroup', ''))
          .join(','),
      };
      return tmpIds;
    })();

    const queryParams: IQueryMetadataParams = {
      ...newParams,
      page: params.page || 1,
      pageSize: params.pageSize || pageProps.pageSize,
    };

    if (
      protocol === EMetadataProtocol.SIP ||
      protocol === EMetadataProtocol.LDAP ||
      protocol === EMetadataProtocol.DB2
    ) {
      queryMetadataMapfieldKeys(protocol).then((res) => {
        const { success, result } = res;
        if (success) {
          setMapFieldKeys(result);
        }
      });
    }

    if (dispatch) {
      dispatch({
        type: 'metadataModel/queryMetadataLogs',
        payload: isDimensionsTab
          ? {
              ...queryParams,
              columns: getColumnParams({
                cols: columns,
                tableKey,
              }),
              entry,
              ...dimensionsQueryData,
            }
          : {
              ...queryParams,
              columns: getColumnParams({
                cols: columns,
                tableKey,
              }),
              entry,
            },
      });
      dispatch({
        type: 'networkModel/queryAllNetworkGroups',
      });
      dispatch({
        type: 'metadataModel/queryMetadataTotal',
        payload: isDimensionsTab
          ? {
              ...queryParams,
              columns: getColumnParams({
                cols: columns,
                tableKey,
              }),
              entry,
              ...dimensionsQueryData,
            }
          : {
              ...queryParams,
              columns: getColumnParams({
                cols: columns,
                tableKey,
              }),
              entry,
            },
      });
    }
  };

  useEffect(() => {
    if (!filter) {
      return;
    }
    // 转换过滤条件
    const filterJson: IFilterCondition = parseArrayJson(decodeURIComponent(filter));
    setFilterCondition(deduplicateCondition(filterJson, new Set()));
  }, [filter]);

  useDeepCompareEffect(() => {
    if (!pageIsReady) {
      return;
    }
    queryMedataLogs({
      page: 1,
    });
  }, [selectedTimeInfo, filterCondition, sortDirection, networkId, pageIsReady, columns]);

  useEffect(() => {
    if (!pageIsReady) {
      setPageIsReady(true);
    }
  }, [pageIsReady]);

  // ====== 表格变化 S ======
  const handleTableChange = (page: any, filters: any, sorter: any) => {
    const newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    setSortDirection(newSortDirection);
  };

  // 上下翻页
  const handlePageChange = (currentPage: number, pageSize: number) => {
    scrollTo('#metadataTable');
    queryMedataLogs({
      page: currentPage,
      pageSize,
    });
  };
  const debouncedHandlePageChange = _.debounce(handlePageChange, 500);

  // ====== 表格变化 E ======

  // ====== Filter过滤 S ======
  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  /**
   * 重置过滤条件
   */
  const handleResetFilter = () => {
    setFilterCondition([]);
  };

  useClearURL();

  const refresh = () => {
    queryMedataLogs({
      page: 1,
    });
  };

  const tableData = (() => {
    if (
      protocol === EMetadataProtocol.IMAP ||
      protocol === EMetadataProtocol.POP3 ||
      protocol === EMetadataProtocol.SMTP
    ) {
      return protocolLogsMap[EMetadataProtocol.MAIL] || [];
    }
    if (protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6) {
      return protocolLogsMap[EMetadataProtocol.ICMP] || [];
    }
    return protocolLogsMap[protocol] || [];
  })();
  return (
    <>
      <div id="metadataTable" className={styles.table}>
        <EnhancedTable<IMetadataLog>
          sortDirection={`${sortDirection}end` as any}
          tableKey={`metadata-log-${protocol}`}
          rowKey="id"
          defaultShowColumns={defaultShowColumnsForFlow[protocol]}
          loading={queryLogsLoading}
          columns={fullTableColumns}
          dataSource={tableData}
          pagination={false}
          onChange={handleTableChange}
          onColumnChange={setColumns}
          extraTool={
            <FieldFilter
              fields={filterField}
              onChange={handleFilterChange}
              condition={filterCondition}
              historyStorageKey={`tfa-metadata-${protocol.toLocaleLowerCase()}-log-filter-history`}
              extra={
                <Space size="small">
                  <Button
                    type="primary"
                    icon={<ReloadOutlined />}
                    loading={queryLogsLoading}
                    onClick={() => refresh()}
                  >
                    刷新
                  </Button>
                  <ExportFile
                    loading={queryLogsLoading}
                    totalNum={pageProps.total}
                    queryFn={(params) => {
                      return exportMetadataLogs({
                        ...params,
                        ...getFilterParams(),
                        networkId,
                        columns: getColumnParams({
                          cols: columns,
                          tableKey,
                        }),
                        protocol,
                      });
                    }}
                  />
                  <Button
                    icon={<RollbackOutlined />}
                    onClick={handleResetFilter}
                    loading={queryLogsLoading}
                  >
                    重置
                  </Button>
                </Space>
              }
            />
          }
          extraFooter={
            <Spin spinning={queryLogsLoading} size="small">
              <CustomPagination {...pageProps} onChange={debouncedHandlePageChange} />
            </Spin>
          }
        />
      </div>
    </>
  );
}

export default connect(({ networkModel: { allNetworkGroupMap } }: ConnectState) => ({
  allNetworkGroupMap,
}))(MetadataLayout);
