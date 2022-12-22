import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type {
  EFieldOperandType,
  EFieldType, IEnumValue, IField, IFilterCondition
} from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType, getGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection, ESourceType } from '@/pages/app//analysis/typings';
import type { IOfflinePcapData } from '@/pages/app/analysis/OfflinePcapAnalysis/typing';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { jumpPacketFromFlowRecord } from '@/pages/app/security/RecordQuery/FlowRecord/utils';
import { abortAjax, getLinkUrl, jumpNewPage, parseArrayJson, scrollTo } from '@/utils/utils';
import {
  FolderOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  StopOutlined
} from '@ant-design/icons';
import { Button, message, Popconfirm, Space, Spin, Tooltip } from 'antd';
import type { ColumnProps, TablePaginationConfig } from 'antd/lib/table';
import { connect } from 'dva';
import _ from 'lodash';
import moment from 'moment';
import type { ReactNode } from 'react';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, FlowRecordModelState } from 'umi';
import { history, initQueryParams, useLocation, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import DownLoadPktBtn, { EQueryLogToPkt, queryPkt } from '../../components/DownLoadPktBtn';
import ExportFile, { queryExportFile } from '../../components/ExportFile';
import { packetUrl } from '../../Packet';
import { ANALYSIS_RESULT_ID_PREFIX } from '../../ScenarioTask/Result';
import useFlowRecordColumns from '../hooks/useFlowRecordColumns';
import { cancelQueryTask } from '../service';
import type { IFlowRecordData, IQueryRecordParams } from '../typings';

/** 表格操作列的 dataindex */
const COLUMN_ACTION_KEY = 'action';

export const FLOW_RECORD_EXCLUDE_COLS = [
  'tcp_client_zero_window_packets_rate',
  'tcp_server_zero_window_packets_rate',
];

export const getColumnParams = getColumnParamsFunc(FLOW_RECORD_EXCLUDE_COLS);

/**
 * 表格定义
 */
export interface IFlowRecordColumnProps<RecordType> extends ColumnProps<RecordType> {
  /**
   * 搜索时的提示信息
   */
  searchTip?: string;

  /**
   * 是否在表格中显示
   */
  show?: boolean;

  disabled?: boolean;
  /**
   * 是否可以被搜索
   */
  searchable?: boolean;
  enumValue?: IEnumValue[];
  /**
   * 字段的类型
   */
  fieldType?: EFieldType;
  /**
   * 操作数类型
   */
  operandType?: EFieldOperandType;

  directionConfig?: IField['directionConfig'];
}

/**
 * 格式化ISO时间到纳秒
 * @param time String
 *
 * @eg 2020-04-15T17:26:55.123456789Z => 1420114230
 */
export const convertTime2Ns = (time: string | number) => {
  if (!time) {
    return '';
  }

  const timeString = String(time);

  let newTime = '';
  // 判断是否是ISO8601对象
  if (timeString.indexOf('T') > -1) {
    // 精确到秒的时间戳
    const timestampSeconds = moment(timeString).format('X');
    // 取出来剩下的小数
    const restString = timeString.split('.')[1];

    if (restString) {
      // 去掉时区相关信息
      let nsString = restString.split('+')[0].split('Z')[0];

      // 补齐到9位长度
      if (String(nsString).length < 9) {
        nsString += '0';
      }

      newTime = `${timestampSeconds + nsString}`;
    } else {
      // 如果没有就补齐
      newTime = String(+timestampSeconds * Math.pow(1000, 3));
    }
  } else {
    // 这里是毫秒的时间戳，直接补齐到纳秒
    newTime = String(+timeString * Math.pow(1000, 2));
  }

  return newTime;
};

export interface ILocationProps {
  /**
   * 接收从外部接收过滤条件
   */
  filter?: string;
  /**
   * 各种分析任务的ID
   */
  analysisResultId?: string;
  /**
   * 各种分析任务的开始时间
   */
  analysisStartTime?: string;
  /**
   * 各种分析任务的结束时间
   */
  analysisEndTime?: string;
}

export interface IFlowRecordEmbedProps {
  /** 表格的 KEY 值 */
  tableKey: string;
  /** 过滤条件在 localstorage 中的 KEY 值 */
  filterHistoryKey: string;
  /** 额外添加的 DSL 过滤条件 */
  extraDsl?: string;
  /**
   * 需要展示的表格字段
   * @value [] 数组为空时，展示所有的统计指标
   */
  displayMetrics?: string[];

  /**
   * 过滤条件是否只读的
   * 不可编辑、不可添加删除
   */
  filterIsReadonly?: boolean;

  /** 额外的操作按钮 */
  extraAction?: ReactNode | string;
}

interface IFlowRecordProps extends IFlowRecordEmbedProps, ILocationProps {
  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;

  flowRecordModel: FlowRecordModelState;
  queryFlowRecordLoading: boolean | undefined;
  queryFlowLogsStatisticsLoading: boolean | undefined;
  currentPcpInfo: IOfflinePcapData;

  filterJsonFromTab?: string;
}
const FlowRecord: React.FC<IFlowRecordProps> = ({
  tableKey = 'npmd-flow-record-table',
  filterHistoryKey = 'npmd-flow-record-filter-history',
  extraDsl,
  displayMetrics = [],
  extraAction,
  currentPcpInfo,
  dispatch,
  flowRecordModel: { recordData, pagination },
  globalSelectedTime,
  queryFlowRecordLoading,
  queryFlowLogsStatisticsLoading,
  filterJsonFromTab,
}) => {
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
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const [pageIsReady, setPageIsReady] = useState<boolean>(false);
  // 当前排序的字段
  const [sortProperty, setSortProperty] = useState<string>('report_time');
  // 当前排序的方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  // 是否显示停止按钮
  const [cancelButtonVisible, setCancelButtonVisible] = useState<boolean>(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  // 查询的uuid
  const [queryTaskIds, setQueryTaskIds] = useState<string[]>([]);
  const queryTaskIdsRef = useRef<string[]>([]);
  // filter过滤条件
  // 转换过滤条件
  const filterJson: IFilterCondition = parseArrayJson(decodeURIComponent(filter));
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(
    deduplicateCondition(filterJson, new Set()),
  );

  const [columns, setColumns] = useState<string[]>();

  useEffect(() => {
    const tmpQuery = history.location.query || {};
    delete tmpQuery.filter;
    history.replace({
      pathname: history.location.pathname,
      query: tmpQuery,
    });
  }, []);

  const resetPagination = useCallback(() => {
    dispatch({
      type: 'flowRecordModel/updateState',
      payload: {
        pagination: {
          ...pagination,
          currentPage: 1,
        },
      },
    });
  }, [dispatch, pagination]);

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

    // 离线文件分析
    if (pcapFileId && currentPcpInfo) {
      const globalFirstTime = globalSelectedTime?.originStartTime,
        gloablLastTime = globalSelectedTime?.originEndTime;
      const firstTime = currentPcpInfo?.filterStartTime,
        lastTime = currentPcpInfo?.filterEndTime;
      // console.log(globalFirstTime,'globalFirstTime',gloablLastTime,'gloablLastTime');
      // console.log(firstTime,'firstTime',lastTime,"lastTime");
      return {
        startTime: firstTime,
        endTime: lastTime,
        originStartTime: globalFirstTime,
        originEndTime: gloablLastTime,
      };
    }

    return globalSelectedTime;
  }, [
    analysisResultId,
    analysisStartTime,
    analysisEndTime,
    pcapFileId,
    currentPcpInfo,
    globalSelectedTime,
  ]);

  const baseCols = useFlowRecordColumns(setFilterCondition, false);

  /** 所有的流日志字段 */
  const actionCol: IFlowRecordColumnProps<IFlowRecordData> = useMemo(() => {
    return {
      title: '操作',
      dataIndex: COLUMN_ACTION_KEY,
      width: 240,
      show: true,
      fixed: 'right' as any,
      disabled: true,
      render: (text: any, record: IFlowRecordData) => {
        let startTime: number;
        let endTime: number;
        if (record.start_time && record.duration) {
          startTime = moment(record.start_time).valueOf();
          endTime = startTime + record.duration + 60000;
        }

        const packetFilter = jumpPacketFromFlowRecord(record);

        // 如果来自于场景分析，跳转到特殊的数据包路径
        // networkId取记录的第一个即可
        if (analysisResultId) {
          const [firstNetworkId] = record.network_id;
          if (!firstNetworkId) {
            return null;
          }
          return (
            <span
              className="link"
              onClick={() => {
                jumpNewPage(
                  getLinkUrl(
                    `/analysis/security/scenario-task/result/packet?networkId=${firstNetworkId}&analysisStartTime=${new Date(
                      startTime || selectedTimeInfo.originStartTime,
                    ).valueOf()}&analysisEndTime=${new Date(
                      endTime || selectedTimeInfo.originEndTime,
                    ).valueOf()}&filter=${encodeURIComponent(JSON.stringify(packetFilter))}`,
                  ),
                );
              }}
            >
              数据包
            </span>
          );
        }

        if (pcapFileId) {
          return (
            <span
              className="link"
              onClick={() => {
                jumpNewPage(
                  getLinkUrl(
                    `/analysis/offline/${pcapFileId}/packet?filter=${encodeURIComponent(
                      JSON.stringify(packetFilter),
                    )}&from=${new Date(
                      startTime || selectedTimeInfo.originStartTime,
                    ).valueOf()}&to=${new Date(
                      endTime || selectedTimeInfo.originEndTime,
                    ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                  ),
                );
              }}
            >
              数据包
            </span>
          );
        }
        return (
          <span
            className="link"
            onClick={() => {
              jumpNewPage(
                getLinkUrl(
                  `${packetUrl}?filter=${encodeURIComponent(
                    JSON.stringify(packetFilter),
                  )}&from=${new Date(
                    startTime || selectedTimeInfo.originStartTime,
                  ).valueOf()}&to=${new Date(
                    endTime || selectedTimeInfo.originEndTime,
                  ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                ),
              );
            }}
          >
            数据包
          </span>
        );
      },
    };
  }, [
    analysisResultId,
    pcapFileId,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
  ]);

  const allColumns = useMemo(() => {
    return [...baseCols, actionCol];
  }, [actionCol, baseCols]);

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

  const filterField: IField[] = useMemo(() => {
    return displayMetricInfos
      .filter((item) => item.searchable)
      .map((item) => {
        // 特殊处理2个重传率
        const dataIndex = item.dataIndex as string;
        // TODO: 过滤表达式支持函数
        // if (item.dataIndex === 'tcp_client_retransmission_rate') {
        //   dataIndex = 'divide(tcp_client_retransmission_packets,upstream_packets)';
        // }
        // if (item.dataIndex === 'tcp_server_retransmission_rate') {
        //   dataIndex = 'divide(tcp_server_retransmission_packets,downstream_packets)';
        // }

        return {
          title: item.title as string,
          dataIndex,
          type: item.fieldType as EFieldType,
          operandType: item.operandType!,
          enumValue: item.enumValue || [],
          directionConfig: item.directionConfig,
        };
      });
  }, [displayMetricInfos]);

  const sourceType: ESourceType = useMemo(() => {
    if (pcapFileId) {
      return ESourceType.OFFLINE;
    }
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    return ESourceType.NETWORK;
  }, [serviceId, pcapFileId]);

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
  }, [displayMetricInfos, sortDirection, sortProperty]);

  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  /**
   * 重置过滤条件
   */
  const handleReset = () => {
    setFilterCondition([]);
  };

  // 条件发生变化重置分页信息
  useEffect(() => {
    resetPagination();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterCondition]);

  // 隐藏停止按钮
  const hideCancelButton = () => {
    setCancelButtonVisible(false);
  };

  // dsl参数
  const dsl = useMemo(() => {
    let nextDsl = ``;
    queryStatsFlagRef.current = true;

    nextDsl += filterCondition2Spl(filterCondition, filterField);

    if (networkId) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(network_id<Array>=${networkId}) `;
    }
    if (serviceId) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(service_id<Array>=${serviceId}) `;
    }

    if (pcapFileId) {
      if (nextDsl) {
        nextDsl += ' AND ';
      }
      nextDsl += `(network_id<Array>=${pcapFileId}) `;
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
    if (pathname.includes('/flow-record')) {
      if (nextDsl) {
        nextDsl += ` AND `;
      }
      nextDsl += `(total_packets > 0) `;
    }

    // 拼接全局的时间和
    nextDsl += `| gentimes report_time start="${selectedTimeInfo.originStartTime}" end="${selectedTimeInfo.originEndTime}"`;

    return nextDsl;
  }, [
    filterCondition,
    filterField,
    networkId,
    serviceId,
    pcapFileId,
    extraDsl,
    pathname,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
  ]);

  const sortParams = useMemo(() => {
    return {
      sortProperty,
      sortDirection,
    };
  }, [sortDirection, sortProperty]);

  // 流日志接口参数
  const queryFlowRecordParams = useMemo(() => {
    const queryParams: Partial<IQueryRecordParams> = {
      sourceType,
      packetFileId: pcapFileId,
      startTime: selectedTimeInfo.originStartTime,
      endTime: selectedTimeInfo.originEndTime,
      ...sortParams,
      dsl,
    };

    // 兼容各种查询任务的详情
    if (analysisResultId) {
      queryParams.id = analysisResultId ? `${ANALYSIS_RESULT_ID_PREFIX}${analysisResultId}` : '';
    }

    return queryParams;
  }, [
    sourceType,
    pcapFileId,
    sortParams,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
    dsl,
    analysisResultId,
  ]);

  // ====== 停止查询按钮 =====
  // ====== 数据查询 =====
  useEffect(() => {
    queryTaskIdsRef.current = queryTaskIds;
  }, [queryTaskIds]);

  const [tableQueryId, setTableQueryId] = useState<string | undefined>();

  /** 查询流日志列表 */
  const queryFlowRecords = useCallback(
    async (params: Partial<IQueryRecordParams>) => {
      // 如果当前存在 queryId，先取消查询
      if (queryTaskIdsRef.current.length > 0) {
        await cancelQueryFlowRecords(true);
        setCancelButtonVisible(false);
      }

      const queryParams: Partial<IQueryRecordParams> = {
        ...queryFlowRecordParams,
        pageSize: pagination.pageSize,
        page: pagination.currentPage,
        ...params,
      };

      // 添加新的查询ID
      const queryTableId = uuidv1();
      const queryStatId = uuidv1();
      setTableQueryId(queryTableId);
      setQueryTaskIds((prevQuesyTaskIds) => {
        return [...prevQuesyTaskIds, queryTableId];
      });
      // 查询数据
      dispatch({
        type: 'flowRecordModel/queryFlowRecords',
        payload: {
          ...queryParams,
          queryId: queryTableId,
          tableKey,
          columns: getColumnParams({
            cols: columns,
            tableKey,
          }),
        },
      }).then(({ status }: { status: number }) => {
        // status=0时表示请求被取消了
        if (!status) {
          return;
        }
        hideCancelButton();
        // 不管的请求成功了还是失败了，都去除查询ID
        setQueryTaskIds((queryIds) => queryIds.filter((id) => id !== queryTableId));
      });

      // 同步查询记录的总数量，当条件发生变化时
      if (queryStatsFlagRef.current && !params.flowId) {
        setQueryTaskIds((prevQuesyTaskIds) => {
          return [...prevQuesyTaskIds, queryStatId];
        });
        // 页面左侧统计聚会搜索
        dispatch({
          type: 'flowRecordModel/queryFlowLogsStatistics',
          payload: {
            sourceType: queryParams.sourceType,
            id: queryParams.id as string,
            packetFileId: queryParams.packetFileId,
            startTime: queryParams.startTime as string,
            endTime: queryParams.endTime as string,
            dsl: queryParams.dsl as string,
            queryId: queryStatId,
          },
        }).then(({ status }: { status: number }) => {
          // status=0时表示请求被取消了
          if (!status) {
            return;
          }
          queryStatsFlagRef.current = false;
          // 不管的请求成功了还是失败了，都去除查询ID
          setQueryTaskIds((queryIds) => queryIds.filter((id) => id !== queryStatId));
        });
      }
    },
    [
      dispatch,
      columns,
      pagination.currentPage,
      pagination.pageSize,
      tableKey,
      queryFlowRecordParams,
    ],
  );

  // 上下翻页
  const handlePageChange = (current: number, nextPageSize: number) => {
    scrollTo('#flowRecordTable');
    dispatch({
      type: 'flowRecordModel/updateState',
      payload: {
        pagination: {
          ...pagination,
          pageSize: nextPageSize,
          currentPage: current,
        },
      },
    });
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

  /**
   * 停止查询任务
   * @params silent 当为 true 时不触发任何提示
   */
  const cancelQueryFlowRecords = async (silent = false) => {
    const queryIdList = queryTaskIdsRef.current;
    if (queryIdList.length === 0) {
      return;
    }
    if (!silent) {
      message.loading('正在停止...');
    }
    setCancelQueryTaskLoading(true);

    // 直接取消查询
    abortAjax(['/appliance/flow-logs', '/appliance/flow-logs/as-statistics']);
    setQueryTaskIds([]);
    const { success } = await cancelQueryTask({ queryId: queryTaskIdsRef.current.join(',') });

    if (silent) {
      return;
    }

    message.destroy();
    if (!success) {
      message.warning('停止失败');
      return;
    }

    if (queryFlowRecordLoading) {
      dispatch({
        type: 'flowRecordModel/updateState',
        payload: {
          recordData: [],
        },
      });
    }

    message.success('停止成功');
    setCancelButtonVisible(false);
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
    return () => {};
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

  const firstLoadingFlowId = useRef<string | undefined>();
  const firstLoading = useRef(true);

  const clearFlowId = () => {
    const tmp = history.location.query || {};
    delete tmp.flowId;
    history.replace({
      pathname: history.location.pathname,
      query: tmp,
    });
  };

  // 改变条件后清除flowid
  useEffect(() => {
    if (!firstLoading.current || !firstLoadingFlowId.current) {
      firstLoadingFlowId.current = history.location.query?.flowId as string;
    }
    firstLoading.current = false;
  }, [
    sourceType,
    networkId,
    serviceId,
    pcapFileId,
    selectedTimeInfo.originStartTime,
    selectedTimeInfo.originEndTime,
    dsl,
    analysisResultId,
  ]);

  // 查询条件变化时，重新触发请求
  useEffect(() => {
    if (pageIsReady) {
      hideCancelButton();
      // 重新请求
      const flowId =
        firstLoadingFlowId.current || (history.location.query?.flowId as undefined | string);
      queryFlowRecords({ flowId });
      clearFlowId();
    }
  }, [pageIsReady, queryFlowRecords]);

  useEffect(() => {
    return () => {
      cancelQueryFlowRecords(true);
    };
  }, []);

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

  useEffect(() => {
    if (!pageIsReady) {
      setPageIsReady(true);
    }
  }, [pageIsReady]);

  const refresh = () => {
    if (!pcapFileId && (selectedTimeInfo as IGlobalTime).relative) {
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime(selectedTimeInfo as IGlobalTime),
      });
    } else {
      // 防止请求被取消了，刷新的时候列表和总条数统计都重新查询
      queryStatsFlagRef.current = true;

      const prevCurrentPage = pagination.currentPage;
      // 当前页为1时，直接触发请求
      if (prevCurrentPage === 1) {
        queryFlowRecords({});
      } else {
        // 否则将请求数更新为1，参数的变更触发请求
        dispatch({
          type: 'flowRecordModel/updateState',
          payload: {
            pagination: {
              ...pagination,
              currentPage: 1,
            },
          },
        });
      }
    }
  };

  const jumpToPacket = useCallback(() => {
    // 数据包页面支持的过滤类型
    const packetSupportFilterType = [
      'ipv4_initiator',
      'ipv4_responder',
      'ipv6_initiator',
      'ipv6_responder',
      'ipv4',
      'ipv6',
      'port_initiator',
      'port_responder',
      'port',
      // 'ip_protocol',单独处理
      'l7_protocol_id',
      'vlan_id',
      'country_id_initiator',
      'country_id_responder',
      'country_id',
      'province_id_initiator',
      'province_id_responder',
      'province_id',
      'city_id_initiator',
      'city_id_responder',
      'city_id',
      'ethernet_initiator',
      'ethernet_responder',
      'ethernet',
      'application_id',
    ];
    // 对过滤条件进行过滤
    const packetSupportFilter = filterCondition
      .map((sub: any) => {
        if (!sub.hasOwnProperty('operand') && sub.group.length < 2) {
          return sub.group[0];
        }
        return sub;
      })
      .filter(
        (item: any) =>
          item.hasOwnProperty('operand') &&
          ((item.field === 'ip_protocol' &&
            ['tcp', 'udp', 'icmp', 'sctp'].includes(item.operand)) ||
            (packetSupportFilterType.includes(item.field) && item.operator === '=')),
      )
      // 整理过滤条件结构
      .map((ele: any) => ({
        field:
          ([
            'ipv4_initiator',
            'ipv4_responder',
            'ipv6_initiator',
            'ipv6_responder',
            'ipv4',
            'ipv6',
          ].includes(ele.field) &&
            'ipAddress') ||
          (['port_initiator', 'port_responder', 'port'].includes(ele.field) && 'port') ||
          (['country_id_initiator', 'country_id_responder', 'country_id'].includes(ele.field) &&
            'countryId') ||
          (['province_id_initiator', 'province_id_responder', 'province_id'].includes(ele.field) &&
            'provinceId') ||
          (['city_id_initiator', 'city_id_responder', 'city_id'].includes(ele.field) && 'cityId') ||
          (['ethernet_initiator', 'ethernet_responder', 'ethernet'].includes(ele.field) &&
            'macAddress') ||
          (ele.field === 'ip_protocol' && 'ipProtocol') ||
          (ele.field === 'l7_protocol_id' && 'l7ProtocolId') ||
          (ele.field === 'vlan_id' && 'vlanId') ||
          (ele.field === 'application_id' && 'applicationId'),
        operator: ele.operator,
        operand: ele.field === 'ip_protocol' ? ele.operand.toLocaleUpperCase() : ele.operand,
      }));

    if (networkId) {
      packetSupportFilter.push({
        field: 'network_id',
        operator: EFilterOperatorTypes.EQ,
        operand: networkId,
      });
    }

    if (serviceId) {
      packetSupportFilter.push({
        field: 'service_id',
        operator: EFilterOperatorTypes.EQ,
        operand: serviceId,
      });
    }

    // 跳转
    const url = pcapFileId
      ? getLinkUrl(
          `/analysis/offline/${pcapFileId}/packet?&filter=${encodeURIComponent(
            JSON.stringify(packetSupportFilter),
          )}&from=${moment(selectedTimeInfo.originStartTime).valueOf()}&to=${moment(
            selectedTimeInfo.originEndTime,
          ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
        )
      : getLinkUrl(
          `${packetUrl}?&filter=${encodeURIComponent(
            JSON.stringify(packetSupportFilter),
          )}&from=${moment(selectedTimeInfo.originStartTime).valueOf()}&to=${moment(
            selectedTimeInfo.originEndTime,
          ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
        );
    jumpNewPage(url);
  }, [
    filterCondition,
    networkId,
    pcapFileId,
    selectedTimeInfo.originEndTime,
    selectedTimeInfo.originStartTime,
    serviceId,
  ]);

  const isAdvancedFilter = useMemo(() => {
    let res = false;
    filterCondition.forEach((sub: any) => {
      if (sub.hasOwnProperty('group')) {
        res = true;
      }
    });
    return res;
  }, [filterCondition]);

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

  const jumpToPacketRender = useMemo(() => {
    return (
      <Popconfirm
        title="跳转数据包时不会携带高级过滤条件"
        onConfirm={jumpToPacket}
        disabled={!isAdvancedFilter}
        icon={<InfoCircleOutlined style={{ color: 'blue' }} />}
      >
        <Button
          icon={<FolderOutlined />}
          type="primary"
          loading={queryFlowRecordLoading}
          style={{ display: analysisResultId ? 'none' : '' }}
          onClick={isAdvancedFilter ? () => {} : jumpToPacket}
        >
          数据包
        </Button>
      </Popconfirm>
    );
  }, [analysisResultId, isAdvancedFilter, jumpToPacket, queryFlowRecordLoading]);

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
          onColumnChange={(cols) => setColumns(cols)}
          extraTool={
            <FieldFilter
              key="flow-record-filter"
              fields={filterField}
              onChange={handleFilterChange}
              condition={filterCondition}
              historyStorageKey={filterHistoryKey}
              extra={
                <Space size="small">
                  {cancelButtonVisible && (
                    <Tooltip title="结束任务可能会导致查询不完整">
                      <Button
                        icon={<StopOutlined />}
                        type="primary"
                        danger
                        loading={cancelQueryTaskLoading}
                        disabled={!queryFlowRecordLoading}
                        onClick={() => cancelQueryFlowRecords()}
                      >
                        停止
                      </Button>
                    </Tooltip>
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
                  {tableKey !== 'tcp-connection-error-table' && (
                    <DownLoadPktBtn
                      queryId={tableQueryId}
                      queryFn={(param: any) => {
                        return queryPkt(
                          {
                            ...param,
                            ...queryFlowRecordParams,
                            tableKey,
                            flowId: firstLoadingFlowId.current,
                          },
                          EQueryLogToPkt.FlowLog,
                        );
                      }}
                      totalPkt={pagination.total}
                      loading={queryFlowRecordLoading}
                    />
                  )}
                  <ExportFile
                    loading={queryFlowRecordLoading || false}
                    totalNum={pagination.total}
                    accessKey={'exportBtn'}
                    queryFn={(params: any) => {
                      return queryExportFile(
                        {
                          ...params,
                          ...queryFlowRecordParams,
                          tableKey,
                          queryId: tableQueryId,
                          columns: getColumnParams({
                            cols: columns,
                            tableKey,
                          }),
                          flowId: firstLoadingFlowId.current,
                        },
                        EQueryLogToPkt.FlowLog,
                      );
                    }}
                  />
                  {jumpToPacketRender}
                  <Button
                    icon={<ReloadOutlined />}
                    onClick={handleReset}
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
                {...pagination}
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

  appModel: { globalSelectedTime },
  npmdModel: { currentPcpInfo },
  loading,
}: ConnectState) => ({
  globalSelectedTime,
  flowRecordModel,
  currentPcpInfo,
  queryFlowRecordLoading: loading.effects['flowRecordModel/queryFlowRecords'],
  queryFlowLogsStatisticsLoading: loading.effects['flowRecordModel/queryFlowLogsStatistics'],
});

export default connect(mapStateToProps)(FlowRecord);
