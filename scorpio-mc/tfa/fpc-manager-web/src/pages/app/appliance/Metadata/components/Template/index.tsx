import {
  DEFAULT_PAGE_SIZE_KEY,
  DHCP_VERSION_ENUM,
  ICMP_VERSION_ENUM,
  PAGE_DEFAULT_SIZE
} from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import type { IField, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldType } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app//analysis/typings';
import type { IOfflinePcapData } from '@/pages/app/analysis/OfflinePcapAnalysis/typing';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecord/Record';
import storage from '@/utils/frame/storage';
import { parseArrayJson, scrollTo, snakeCase } from '@/utils/utils';
import { ReloadOutlined, RollbackOutlined } from '@ant-design/icons';
import { useSafeState } from 'ahooks';
import { Button, Space, Spin } from 'antd';
import { debounce } from 'lodash';
import moment from 'moment';
import { useContext, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch, useLocation, useParams, useSelector } from 'umi';
import { v1 } from 'uuid';
import DownLoadPktBtn, { EQueryLogToPkt, queryPkt } from '../../../components/DownLoadPktBtn';
import ExportFile, { queryExportFile } from '../../../components/ExportFile';
import { ANALYSIS_RESULT_ID_PREFIX } from '../../../ScenarioTask/Result';
import { MetaDataContext } from '../../Analysis';
import { clearShareInfo } from '../../Analysis/components/EditTabs';
import useColumnForMetadata from '../../hooks/useColumnForMetadata';
import { queryMetadataMapfieldKeys } from '../../service';
import type { IMetadataLog, IQueryMetadataParams } from '../../typings';
import { EMailProtocol, EMetadataProtocol } from '../../typings';
import { defaultShowColumnsForFlow, METADATA_EXCLUDE_COLS } from './constant';
import styles from './index.less';

export const getColumnParams = getColumnParamsFunc(METADATA_EXCLUDE_COLS);

export interface IMetadataLayoutProps<MetaLog> {
  // 用来表示是否是统一使用src_ip字段来表示，
  // false时: src_ipv4,src_ipv6,dest_ipv4,dest_ipv6
  // true: src_ip, dest_ip
  isNewIpFieldType?: boolean;
  // 协议
  protocol: EMetadataProtocol;
  // 不同的协议表格展示不同的列
  tableColumns: IColumnProps<MetaLog>[];

  timeField?: string;

  // 入口
  entry?: string;
}

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

/**
 * 表格定义
 */
export interface IColumnProps<RecordType> extends IFlowRecordColumnProps<RecordType> {}

// export interface MetaLog extends IMetadataLog{}

function MetadataLayout<MetaLog extends IMetadataLog>({
  protocol,
  tableColumns,
  entry,
  timeField = 'start_time',
  isNewIpFieldType = false,
}: IMetadataLayoutProps<MetaLog>) {
  const location = useLocation();

  const tableKey = `metadata-log-${protocol}`;

  const dispatch = useDispatch<Dispatch>();
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const metadataModel = useSelector<ConnectState, ConnectState['metadataModel']>(
    (state) => state.metadataModel,
  );
  const currentPcpInfo = useSelector<ConnectState, IOfflinePcapData>(
    (state) => state.npmdModel.currentPcpInfo,
  );
  const queryLogsLoading = useSelector<ConnectState, boolean>(
    (state) => state.loading.effects['metadataModel/queryMetadataLogs'] || false,
  );

  const { filter, analysisResultId, analysisStartTime, analysisEndTime } = (
    location as unknown as ILocation
  ).query;
  const [tabState, analysisDispatch] = useContext(MetaDataContext);

  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  // filter过滤条件
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(() => {
    let resFilter: any = [];
    if (filter) {
      // 转换过滤条件
      resFilter = deduplicateCondition(parseArrayJson(decodeURIComponent(filter)), new Set());
    } else if (tabState.shareInfo?.filter) {
      // 转换过滤条件
      resFilter = deduplicateCondition(
        parseArrayJson(decodeURIComponent(tabState.shareInfo?.filter)),
        new Set(),
      );
      clearShareInfo(analysisDispatch);
    }
    return resFilter;
  });

  const [mapFieldKeys, setMapFieldKeys] = useSafeState<Record<string, string[]>>({});

  const [columns, setColumns] = useState<string[]>();

  const { pagination, protocolLogsMap } = metadataModel;

  const pageProps = useMemo(() => {
    return {
      currentPage: pagination.current || 0,
      pageSize: parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE,
      total: pagination.total || 0,
    };
  }, [pagination]);

  const selectedTimeInfo = useMemo(() => {
    if (pcapFileId) {
      return {
        startTime: currentPcpInfo?.filterStartTime,
        endTime: currentPcpInfo?.filterEndTime,
        originStartTime: currentPcpInfo?.filterStartTime,
        originEndTime: currentPcpInfo?.filterEndTime,
      };
    }

    return globalSelectedTime;
  }, [globalSelectedTime, pcapFileId, currentPcpInfo]);

  // 表格所需要的字段
  const { fullTableColumns, fullColumns } = useColumnForMetadata({
    protocol,
    tableColumns,
    isNewIpFieldType,
    onFilterClick: setFilterCondition,
    selectedTimeInfo,
    sortDirection,
  });

  const filterField = useMemo<IField[]>(() => {
    return fullColumns
      .filter((item) => item.searchable)
      .map((item) => {
        const fieldValue = snakeCase(item.dataIndex as string);
        return {
          title: item.title as string,
          dataIndex: fieldValue as string,
          ...(item.fieldType ? { type: item.fieldType } : {}),
          operandType: item.operandType!,
          enumValue: item.enumValue || [],
          directionConfig: item.directionConfig,
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
    const filterSpl = filterCondition2Spl(filterCondition, filterField);

    const queryParams: IQueryMetadataParams = {
      protocol: nextProtocol,
      packetFileId: pcapFileId,
      sortProperty: timeField,
      sortDirection: sortDirection || ESortDirection.DESC,
      startTime: moment(selectedTimeInfo.originStartTime).format(),
      endTime: moment(selectedTimeInfo.originEndTime).format(),
      dsl: filterSpl || '',
    };
    // 这里填充上分析任务的时间
    if (analysisStartTime && analysisEndTime) {
      queryParams.startTime = moment(decodeURIComponent(analysisStartTime)).format();
      queryParams.endTime = moment(decodeURIComponent(analysisEndTime)).format();
    }

    // 离线文件 添加参数
    if (pcapFileId) {
      queryParams.sourceType = 'packetFile';
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
    } else if (pcapFileId) {
      if (queryParams.dsl) {
        queryParams.dsl += ` AND`;
      }
      // 网络 ID 改成了数组形式
      queryParams.dsl += ` (network_id<Array> = ${pcapFileId})`;
    }

    // 拼接全局的时间和
    queryParams.dsl += ` | gentimes ${timeField} start="${queryParams.startTime}" end="${queryParams.endTime}"`;

    return queryParams;
  };

  const queryMedataLogs = (params: Partial<IQueryMetadataParams>) => {
    const newParams = getFilterParams();
    const queryParams: IQueryMetadataParams = {
      ...newParams,
      page: params.page || 1,
      pageSize: params.pageSize || pageProps.pageSize,
    };

    if (pcapFileId) {
      queryParams.sourceType = 'packetFile';
    }

    if (
      protocol === EMetadataProtocol.SIP ||
      protocol === EMetadataProtocol.LDAP ||
      protocol === EMetadataProtocol.DB2
    ) {
      queryMetadataMapfieldKeys(protocol).then((res) => {
        console.log(res, 'res');
        const { success, result } = res;
        if (success) {
          setMapFieldKeys(result);
        }
      });
    }

    if (dispatch) {
      dispatch({
        type: 'metadataModel/queryMetadataLogs',
        payload: {
          ...queryParams,
          columns: getColumnParams({
            cols: columns,
            tableKey,
          }),
          entry,
        },
      });
      dispatch({
        type: 'metadataModel/queryMetadataTotal',
        payload: {
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

  // useEffect(() => {
  //   if (filter) {
  //     // 转换过滤条件
  //     setFilterCondition(
  //       deduplicateCondition(parseArrayJson(decodeURIComponent(filter)), new Set()),
  //     );
  //   } else if (tabState.shareInfo?.filter) {
  //     // 转换过滤条件
  //     setFilterCondition(
  //       deduplicateCondition(
  //         parseArrayJson(decodeURIComponent(tabState.shareInfo?.filter)),
  //         new Set(),
  //       ),
  //     );
  //     clearShareInfo(analysisDispatch);
  //   }
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, []);

  useEffect(() => {
    queryMedataLogs({
      page: 1,
    });

    // 监听这些条件，触发请求
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTimeInfo, filterCondition, sortDirection, networkId, pcapFileId, protocol, columns]);

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
  const debouncedHandlePageChange = debounce(handlePageChange, 500);

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

  const refresh = () => {
    if (!pcapFileId && (selectedTimeInfo as IGlobalTime).relative) {
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(selectedTimeInfo as IGlobalTime),
      });
    }
    queryMedataLogs({
      page: 1,
    });
  };

  const tableData = useMemo(() => {
    let result: any[] = [];

    if (
      protocol === EMetadataProtocol.IMAP ||
      protocol === EMetadataProtocol.POP3 ||
      protocol === EMetadataProtocol.SMTP
    ) {
      result = protocolLogsMap[EMetadataProtocol.MAIL] || [];
    } else if (protocol === EMetadataProtocol.ICMPV4 || protocol === EMetadataProtocol.ICMPV6) {
      result = protocolLogsMap[EMetadataProtocol.ICMP] || [];
    } else {
      result = protocolLogsMap[protocol] || [];
    }

    return result.map((item, index) => {
      return {
        ...item,
        index: index + 1,
      };
    });
  }, [protocol, protocolLogsMap]);

  return (
    <>
      <div id="metadataTable" className={styles.table}>
        <EnhancedTable<IMetadataLog>
          sortDirection={`${sortDirection}end` as any}
          tableKey={tableKey}
          rowKey={() => v1()}
          defaultShowColumns={defaultShowColumnsForFlow[protocol]}
          loading={queryLogsLoading}
          columns={fullTableColumns}
          dataSource={tableData as MetaLog[]}
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
                    onClick={refresh}
                  >
                    刷新
                  </Button>
                  {protocol !== EMetadataProtocol.FILE && (
                    <DownLoadPktBtn
                      queryFn={(param: any) => {
                        return queryPkt(
                          {
                            ...param,
                            ...getFilterParams(),
                            networkId,
                            packetFileId: pcapFileId,
                          },
                          EQueryLogToPkt.MetaData,
                          protocol,
                        );
                      }}
                      totalPkt={pageProps.total}
                      params={{
                        ...getFilterParams(),
                        columns: getColumnParams({
                          cols: columns,
                          tableKey,
                        }),
                        entry,
                      }}
                      loading={queryLogsLoading}
                    />
                  )}
                  <ExportFile
                    loading={queryLogsLoading}
                    totalNum={pageProps.total}
                    accessKey={'exportBtn'}
                    queryFn={(params: any) => {
                      return queryExportFile(
                        {
                          ...params,
                          ...getFilterParams(),
                          networkId,
                          packetFileId: pcapFileId,
                          columns: getColumnParams({
                            cols: columns,
                            tableKey,
                          }),
                        },
                        EQueryLogToPkt.MetaData,
                        protocol,
                      );
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

export default MetadataLayout;
