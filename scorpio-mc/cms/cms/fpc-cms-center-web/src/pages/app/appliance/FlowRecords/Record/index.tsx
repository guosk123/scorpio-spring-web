import { DEFAULT_PAGE_SIZE_KEY, PAGE_DEFAULT_SIZE } from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import ExportFile from '@/components/ExportFile';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type {
  EFieldOperandType,
  EFieldType,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import useClearURL from '@/hooks/useClearURL';
import type { ConnectState } from '@/models/connect';
import type { IUriParams } from '@/pages/app//analysis/typings';
import { ESortDirection, ESourceType } from '@/pages/app//analysis/typings';
import { ServiceAnalysisContext, ServiceContext } from '@/pages/app/analysis/Service/index';
import { ANALYSIS_RESULT_ID_PREFIX } from '@/pages/app/appliance/ScenarioTask/Result';
import { AnalysisContext, NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { clearShareInfo, getTabDetail } from '@/pages/app/Network/components/EditTabs';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import storage from '@/utils/frame/storage';
import { parseArrayJson, scrollTo } from '@/utils/utils';
import { ReloadOutlined } from '@ant-design/icons';
import { useLatest } from 'ahooks';
import { Button, message, Space, Spin } from 'antd';
import type { TablePaginationConfig } from 'antd/lib/table';
import { connect } from 'dva';
import _ from 'lodash';
import moment from 'moment';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { history, useLocation, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import { initQueryParams } from '../model';
import { exportFlowRecords } from '../service';
import type { IFlowRecordData, IFlowStatisticsParams, IQueryRecordParams } from '../typings';
import CancelBtn from './components/CancelBtn';
import JumpToPacketBtn from './components/JumpToPacketBtn';
import useFlowRecordColumns, { COLUMN_ACTION_KEY } from './hooks/useFlowRecordColumns';

import type { IFlowRecordColumnProps, IFlowRecordProps, ILocationProps } from './typing';

export const FLOW_RECORD_EXCLUDE_COLS = [
  'tcp_client_zero_window_packets_rate',
  'tcp_server_zero_window_packets_rate',
];

export const getColumnParams = getColumnParamsFunc(FLOW_RECORD_EXCLUDE_COLS);

const FlowRecords: React.FC<IFlowRecordProps> = (props) => {
  const {
    tableKey = 'npmd-flow-record-table',
    filterHistoryKey = 'npmd-flow-record-filter-history',
    extraDsl,
    displayMetrics = [],
    extraAction,

    dispatch,
    flowRecordModel: { recordData, pagination },
    globalSelectedTime,
    allNetworkGroupMap,
    queryFlowRecordLoading,
    queryFlowLogsStatisticsLoading,
    cancelQueryTaskLoading,
    filterObj,
    tabName,
  } = props;

  const {
    pathname,
    query: {
      filter = '',
      // 支持从各种分析任务跳转过来
      analysisResultId,
      analysisStartTime,
      analysisEndTime,
    },
  } = useLocation() as any as {
    pathname: string;
    query: ILocationProps;
  };

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

  const [pageIsReady, setPageIsReady] = useState<boolean>(false);
  // 当前排序的字段
  const [sortProperty, setSortProperty] = useState<string>('report_time');
  // 当前排序的方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  // 是否显示停止按钮
  const [cancelButtonVisible, setCancelButtonVisible] = useState<boolean>(false);
  // 查询的uuid
  const [queryTaskIds, setQueryTaskIds] = useState<string[]>([]);
  const [state, analysisDispatch] = useContext<any>(
    serviceId ? ServiceAnalysisContext : AnalysisContext,
  );

  const [columns, setColumns] = useState<string[]>();

  const [tableQueryId, setTableQueryId] = useState<string | undefined>();

  useEffect(() => {
    if (!analysisDispatch) {
      return;
    }
    clearShareInfo(analysisDispatch);
  }, [analysisDispatch]);
  // filter过滤条件
  // 转换过滤条件
  // const filterJson: IFilterCondition = parseArrayJson(decodeURIComponent(filter));
  /**
   * {
                field: 'ipv6_responder',
                operator: EFilterOperatorTypes.EQ,
                operand: (record as IMetadataDhcp).srcIpv6,
              }
   */
  const urlShareInfo = JSON.parse(String(history.location.query?.filter || '{}'));
  const filterJson: IFilterCondition =
    urlShareInfo?.info?.filterData || parseArrayJson(filter.length ? filter : state?.shareInfo);
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(
    deduplicateCondition(filterObj || filterJson || [], new Set()).map((c) => {
      return {
        ...c,
        disabled: !!((c as IFilter).field === 'network_id'),
      };
    }),
  );
  const filterConditionRef = useLatest(filterCondition);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const queryStatsFlagRef = useRef(true);

  const selectedTimeInfo = useMemo(() => {
    // 场景分析任务
    if (analysisResultId && analysisStartTime && analysisEndTime) {
      let taskStartTime = decodeURIComponent(analysisStartTime);
      let taskEndTime = decodeURIComponent(analysisEndTime);
      taskStartTime = moment(taskStartTime).format();
      taskEndTime = moment(taskEndTime).format();

      return {
        startTime: taskStartTime,
        endTime: taskEndTime,
        originStartTime: taskStartTime,
        originEndTime: taskEndTime,
      };
    }

    return globalSelectedTime;
  }, [analysisResultId, analysisStartTime, analysisEndTime, globalSelectedTime]);

  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(() => {
    return parseInt(storage.get(DEFAULT_PAGE_SIZE_KEY) || '20', 10) || PAGE_DEFAULT_SIZE;
  });

  const pageProps = useMemo(() => {
    return {
      currentPage,
      pageSize,
      total: pagination.total,
    };
  }, [currentPage, pageSize, pagination.total]);

  /** 所有的流日志字段 */
  const allColumns = useFlowRecordColumns({
    selectedTimeInfo,
    tableKey,
    pageProps,
    handlerFilterCondition: setFilterCondition,
    analysisResultId,
  });

  const displayMetricInfos = useMemo(() => {
    if (displayMetrics.length === 0) {
      return allColumns;
    }
    return allColumns.filter((col) => {
      return (
        displayMetrics.indexOf(col.dataIndex as string) > -1 || col.dataIndex === COLUMN_ACTION_KEY
      );
    });
  }, [allColumns, displayMetrics]);

  const filterField = useMemo(() => {
    return displayMetricInfos
      .filter((item) => item.searchable)
      .map((item) => {
        // 特殊处理2个重传率
        const dataIndex = item.dataIndex as string;
        return {
          title: item.title as string,
          dataIndex,
          type: item.fieldType as EFieldType,
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
        };
      });
  }, [displayMetricInfos]);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    return ESourceType.NETWORK;
  }, [serviceId]);

  const showColumns: IFlowRecordColumnProps<IFlowRecordData>[] = useMemo(() => {
    return displayMetricInfos
      .filter((col) => col.show !== false)
      .map((col) => {
        let titleLength = ((col.title || '') as string).length + 1;
        if (titleLength < 6) {
          titleLength = 6;
        }
        return {
          ...col,
          key: col.dataIndex as any,
          width: col.width || 18 * titleLength + 20,
          align: 'center' as any,
          ellipsis: true,
          sortOrder: (col.dataIndex === sortProperty ? `${sortDirection}end` : false) as any,
          render: (value, record, index) => {
            let label = value;
            if (col.render) {
              label = col.render(value, record, index);
            }

            if (!col.searchable) {
              return label;
            }

            if (['network_id', 'service_id'].includes(String(col.dataIndex) || '')) {
              return label.map((showItem: any) => {
                return (
                  <FilterBubble
                    containerStyle={{ display: 'inline', padding: '0 4px' }}
                    dataIndex={col.dataIndex as string}
                    label={showItem.text}
                    fieldType={col.fieldType}
                    operand={String(showItem.value)}
                    operandType={col.operandType as EFieldOperandType}
                    onClick={(newFilter) => {
                      const copyFilter = { ...newFilter };
                      if (newFilter.field === 'network_id') {
                        copyFilter.disabled = true;
                      }
                      setFilterCondition((prev) => [...prev, copyFilter]);
                    }}
                    disabled={
                      filterConditionRef.current.findIndex((item) => {
                        if ((item as IFilterGroup).group) return false;
                        return (item as IFilter).field === 'network_id';
                      }) !== -1
                    }
                  />
                );
              });
            }

            return (
              <FilterBubble
                dataIndex={col.dataIndex as string}
                label={label}
                fieldType={col.fieldType}
                operand={String(value)}
                operandType={col.operandType as EFieldOperandType}
                onClick={(newFilter) => {
                  setFilterCondition((prev) => [...prev, newFilter]);
                }}
              />
            );
          },
        };
      });
  }, [displayMetricInfos, filterConditionRef, sortDirection, sortProperty]);

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };
  /**
   * 重置过滤条件
   */
  const handleResetFilter = () => {
    setFilterCondition([]);
  };

  // 页面的分页参数
  const pageParams = useMemo(() => {
    return {
      page: currentPage,
      pageSize: pageSize || PAGE_DEFAULT_SIZE,
    };
  }, [currentPage, pageSize]);

  // dsl参数
  const dsl = useMemo(() => {
    let nextDsl = ``;
    queryStatsFlagRef.current = true;

    nextDsl += filterCondition2Spl(
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
    // nextDsl += filterCondition2Spl(
    //   expendNetworkGroupConditions(filterCondition, allNetworkGroupMap),
    //   filterField,
    // );

    if (networkId && networkType === ENetowrkType.NETWORK) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(network_id<Array>=${networkId}) `;
      if (serviceId) {
        if (nextDsl) {
          nextDsl += ' AND ';
        }
        nextDsl += `(service_id<Array>=${serviceId}) `;
      }
    }

    // 拼接额外的 DSL 条件
    if (extraDsl) {
      if (nextDsl) {
        nextDsl += ` AND `;
      }
      nextDsl += `(${extraDsl}) `;
    }
    // 如果是在会话详单中，排除建连失败的流
    // 过滤规则可以用 "总包数>0" 即可，仅在会话详单中过滤掉
    if (tabName === ENetworkTabs.FLOWRECORD) {
      if (nextDsl) {
        nextDsl += ` AND `;
      }
      nextDsl += `(total_packets > 0) `;
    }
    // 拼接全局的时间和
    nextDsl += `| gentimes report_time start="${selectedTimeInfo.originStartTime}" end="${selectedTimeInfo.originEndTime}"`;

    return nextDsl;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    filterCondition,
    filterField,
    networkId,
    networkType,
    extraDsl,
    pathname,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
    serviceId,
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

  const sortParams = useMemo(() => {
    return {
      sortProperty,
      sortDirection,
    };
  }, [sortDirection, sortProperty]);

  // 流日志接口参数
  const queryFlowRecordParams = useMemo<Partial<IQueryRecordParams>>(() => {
    const queryParams: Partial<IQueryRecordParams> = {
      sourceType,
      startTime: selectedTimeInfo.originStartTime,
      endTime: selectedTimeInfo.originEndTime,
      ...sortParams,
      ...pageParams,
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
    // 如果是在网络分析或者业务分析下
    if (networkType === ENetowrkType.NETWORK_GROUP) {
      queryParams.networkGroupId = networkId;
      queryParams.serviceId = serviceId;
    }
    // 兼容各种查询任务的详情
    if (analysisResultId) {
      queryParams.id = analysisResultId ? `${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}` : '';
    }

    return queryParams;
  }, [
    networkGroupId,
    sourceType,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
    sortParams,
    pageParams,
    dsl,
    networkType,
    analysisResultId,
    networkId,
    serviceId,
  ]);

  const firstLoading = useRef(true);
  const paneDetail = useMemo(() => {
    if (firstLoading.current) {
      return !state
        ? {
            flowId: history.location.query?.flowId?.toString(),
            sid: history.location.query?.sid ? +history.location.query?.sid : undefined,
          }
        : getTabDetail(state);
    }
    return {};
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    sourceType,
    networkId,
    serviceId,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
    dsl,
    networkType,
    analysisResultId,
  ]);

  const queryFlowLogsStatistics = useCallback(
    (params: Partial<IFlowStatisticsParams>) => {
      dispatch({
        type: 'flowRecordModel/queryFlowLogsStatistics',
        payload: { ...params, queryId: uuidv1() },
      }).then(() => {
        queryStatsFlagRef.current = false;
      });
    },
    [dispatch],
  );

  /** 查询流日志列表 */
  const queryFlowRecords = (params: Partial<IQueryRecordParams>) => {
    const queryParams: Partial<IQueryRecordParams> = {
      ...params,
      dsl: params.dsl as string,
      flowId: paneDetail?.flowId || history.location.query?.flowId?.toString(),
      sid: paneDetail?.sid || history.location.query?.sid,
    };

    // 添加新的查询ID
    const queryId = uuidv1();

    setTableQueryId(queryId);
    setQueryTaskIds((prevQuesyTaskIds) => {
      return [...prevQuesyTaskIds, queryId];
    });
    // 查询数据
    dispatch({
      type: 'flowRecordModel/queryFlowRecords',
      payload: {
        ...queryParams,
        queryId,
        tableKey,
        columns: getColumnParams({
          cols: columns,
          tableKey,
        }),
      },
    }).then(() => {
      setCancelButtonVisible(false);
      // 不管的请求成功了还是失败了，都去除查询ID
      setQueryTaskIds((queryIds) => queryIds.filter((id) => id !== queryId));
    });

    // 如果是查询第一页，同步查询记录的总数量
    if (
      queryStatsFlagRef.current &&
      queryParams.page === 1 &&
      !paneDetail.sid &&
      !paneDetail.flowId
    ) {
      const queryData: any = {
        sourceType: queryParams.sourceType,
        id: queryParams.id as string,
        packetFileId: queryParams.packetFileId,
        startTime: queryParams.startTime as string,
        endTime: queryParams.endTime as string,
        dsl: queryParams.dsl as string,
        serviceId,
        flowId: paneDetail.flowId,
        ...(() => {
          if (networkGroupId) {
            return {
              networkGroupId: networkGroupId || '',
            };
          }
          return {};
        })(),
      };

      if (networkType === ENetowrkType.NETWORK_GROUP) {
        queryData.networkGroupId = networkId;
      }

      // console.log('queryData', queryData);
      // 页面左侧统计聚会搜索
      queryFlowLogsStatistics(queryData);
    }
  };

  // 上下翻页
  const handlePageChange = (current: number, nextPageSize: number) => {
    scrollTo('#flowRecordTable');
    setCurrentPage(current);
    setPageSize(nextPageSize);
  };
  const debouncedHandlePageChange = _.debounce(handlePageChange, 300);

  const handleTableChange = (_pagination: TablePaginationConfig, filters: any, sorter: any) => {
    let newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    const newSortProperty = sorter.field;
    // 如果当前排序字段不是现在的字段，默认是倒序
    if (newSortProperty !== sortProperty) {
      newSortDirection = ESortDirection.DESC;
    }

    setSortDirection(newSortDirection);
    setSortProperty(newSortProperty as string);
  };

  // ======维持查询心跳 S=====
  const pingQueryTask = useCallback(() => {
    // 没有 ID 时不 ping
    if (queryTaskIds.length === 0) {
      return;
    }
    dispatch({
      type: 'flowRecordModel/pingQueryTask',
      payload: {
        queryId: queryTaskIds.join(','),
      },
    }).then((success: boolean) => {
      if (!success) {
        message.destroy();
      }
    });
  }, [dispatch, queryTaskIds]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    if (queryTaskIds.length > 0) {
      const timer = window.setInterval(() => {
        pingQueryTask();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    return undefined;
  }, [queryTaskIds, pingQueryTask]);

  useEffect(() => {
    if (dispatch) {
      const init = async () => {
        await dispatch({
          type: 'flowRecordModel/initState',
          payload: {
            recordQueryParams: initQueryParams,
          },
        });

        // 获取所有的服务
        await dispatch({
          type: 'serviceModel/queryAllServices',
        });
        // 获取所有的IP地址组
        await dispatch({
          type: 'ipAddressGroupModel/queryAllIpAddressGroup',
        });
      };
      init();
    }
  }, [dispatch]);

  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'networkModel/queryAllNetworkGroups',
      });
    }
  }, [dispatch]);

  // 查询条件变化时，重新触发请求
  useEffect(() => {
    if (pageIsReady) {
      setCancelButtonVisible(false);
      // 重新请求
      queryFlowRecords({ ...queryFlowRecordParams });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    pageIsReady,
    queryFlowRecordParams,
    columns,
    tableKey,
    serviceId,
    networkType,
    queryFlowLogsStatistics,
    networkId,
  ]);

  useEffect(() => {
    if (!queryFlowRecordLoading) {
      setCancelButtonVisible(false);
    } else {
      /**
       * 检查请求的时间。超出阈值就显示停止按钮
       */
      const timer = window.setTimeout(() => {
        setCancelButtonVisible(queryFlowRecordLoading || false);
      }, 3000);
      return () => window.clearTimeout(timer);
    }
    return undefined;
  }, [queryFlowRecordLoading]);

  const defaultShowColumns = useMemo(() => {
    // 会话详单
    if (tableKey === 'npmd-flow-record-table') {
      return [
        'report_time',
        'ip_initiator',
        'port_initiator',
        'ip_responder',
        'port_responder',
        'total_bytes',
        'total_packets',
        'total_payload_bytes',
        'tcp_client_retransmission_packets',
        'tcp_server_retransmission_packets',
        'ip_protocol',
        // 'l7_protocol_id',
        'application_id',
        'country_id_initiator',
        'country_id_responder',
        'tcp_client_network_latency',
        'tcp_server_network_latency',
        'server_response_latency',
        'tcp_client_zero_window_packets',
        'tcp_server_zero_window_packets',
        COLUMN_ACTION_KEY,
      ];
    } else if (tableKey === 'analysis-tcp-retransmission-table') {
      // 重传分析
      return [
        'report_time',
        'ip_initiator',
        'port_initiator',
        'ip_responder',
        'port_responder',
        'upstream_packets',
        'downstream_packets',
        'tcp_client_retransmission_packets',
        'tcp_client_retransmission_rate',
        'tcp_server_retransmission_packets',
        'tcp_server_retransmission_rate',
        COLUMN_ACTION_KEY,
      ];
    } else if (tableKey === 'tcp-connection-error-table') {
      // 建连失败详单
      return [
        'report_time',
        'ip_initiator',
        'port_initiator',
        'ip_responder',
        'port_responder',
        'tcp_session_state',
        COLUMN_ACTION_KEY,
      ];
    } else if (tableKey === 'long-connection-table') {
      // 长连接分析
      return [
        'report_time',
        'duration',
        'ip_initiator',
        'port_initiator',
        'ip_responder',
        'port_responder',
        'upstream_bytes',
        'downstream_bytes',
        'tcp_session_state',
        COLUMN_ACTION_KEY,
      ];
    }
    return [];
  }, [tableKey]);

  useEffect(() => {
    if (!pageIsReady) {
      setPageIsReady(true);
    }
  }, [pageIsReady]);
  useClearURL();

  const refresh = () => {
    queryFlowRecords({ ...queryFlowRecordParams });
  };

  return (
    <>
      {/* 表格 */}
      <div id="flowRecordTable">
        <EnhancedTable<IFlowRecordData>
          sortProperty={sortProperty}
          sortDirection={`${sortDirection}end` as any}
          tableKey={tableKey}
          // tableKey="flow-record-table"
          rowKey="id"
          loading={queryFlowRecordLoading}
          columns={showColumns}
          dataSource={queryFlowRecordLoading ? [] : recordData}
          pagination={false}
          defaultShowColumns={defaultShowColumns}
          onChange={handleTableChange}
          onColumnChange={setColumns}
          extraTool={
            <FieldFilter
              key="flow-record-filter"
              fields={filterField}
              onChange={handleFilterChange}
              condition={filterCondition}
              historyStorageKey={filterHistoryKey}
              extra={
                <Space size="small">
                  {cancelButtonVisible && ( // cancelQueryTaskLoading
                    <CancelBtn
                      loading={cancelQueryTaskLoading}
                      resetRecordData={() => {
                        dispatch({
                          type: 'flowRecordModel/updateState',
                          payload: {
                            recordData: [],
                          },
                        });
                      }}
                    />
                  )}
                  <Button
                    type="primary"
                    icon={<ReloadOutlined />}
                    loading={queryFlowRecordLoading}
                    onClick={() => {
                      refresh();
                    }}
                  >
                    刷新
                  </Button>
                  <JumpToPacketBtn
                    info={{
                      filterCondition,
                      serviceId,
                      networkId,
                      startTime: globalSelectedTime.startTime,
                      endTime: globalSelectedTime.endTime,
                    }}
                  />
                  <ExportFile
                    loading={queryFlowRecordLoading || false}
                    totalNum={pagination.total}
                    queryFn={(params) => {
                      return exportFlowRecords({
                        ...params,
                        ...queryFlowRecordParams,
                        tableKey,
                        ...paneDetail,
                        queryId: tableQueryId,
                        columns: getColumnParams({
                          cols: columns,
                          tableKey,
                        }),
                      });
                    }}
                  />

                  <Button
                    icon={<ReloadOutlined />}
                    onClick={handleResetFilter}
                    loading={queryFlowRecordLoading}
                  >
                    重置
                  </Button>

                  {extraAction}
                </Space>
              }
            />
          }
          extraFooter={
            <Spin size="small" spinning={queryFlowLogsStatisticsLoading || queryFlowRecordLoading}>
              <CustomPagination
                loading={queryFlowLogsStatisticsLoading}
                {...pageProps}
                onChange={debouncedHandlePageChange}
              />
            </Spin>
          }
        />
      </div>
    </>
  );
};

const mapStateToProps = ({
  flowRecordModel,
  networkModel: { allNetworkGroupMap },

  appModel: { globalSelectedTime },
  loading,
}: ConnectState) => ({
  globalSelectedTime,
  flowRecordModel,
  allNetworkGroupMap,
  queryFlowRecordLoading: loading.effects['flowRecordModel/queryFlowRecords'],
  queryFlowLogsStatisticsLoading: loading.effects['flowRecordModel/queryFlowLogsStatistics'],
  cancelQueryTaskLoading: loading.effects['flowRecordModel/cancelQueryTask'],
});

export default connect(mapStateToProps)(FlowRecords);
