import { proTablePagination } from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import {
  EFieldOperandType,
  EFieldType,
  EFilterOperatorTypes,
} from '@/components/FieldFilter/typings';
import { deduplicateCondition } from '@/components/FieldFilter/utils';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app//analysis/typings';
import DownLoadPktBtn, {
  EQueryLogToPkt,
  queryPkt,
} from '@/pages/app/appliance/components/DownLoadPktBtn';
import ExportFile, { queryExportFile } from '@/pages/app/appliance/components/ExportFile';
import useFlowRecordColumns from '@/pages/app/appliance/FlowRecord/hooks/useFlowRecordColumns';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecord/Record';
import { getColumnParams } from '@/pages/app/appliance/FlowRecord/Record';
import {
  cancelQueryTask,
  pingQueryTask,
  queryFlowLogsStatistics,
} from '@/pages/app/appliance/FlowRecord/service';
import type { IFlowRecordData, IQueryRecordParams } from '@/pages/app/appliance/FlowRecord/typings';
import { ETablekeyEntry } from '@/pages/app/appliance/FlowRecord/typings';
import {
  matadataDetailKV,
} from '@/pages/app/appliance/Metadata/Analysis/typings';
import { packetUrl } from '@/pages/app/appliance/Packet';
import { isIpv4 } from '@/pages/app/netflow/utils/filterTools';
import { abortAjax, getLinkUrl, jumpNewPage, parseArrayJson, scrollTo } from '@/utils/utils';
import {
  FolderOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  StopOutlined,
} from '@ant-design/icons';
import { useLatest } from 'ahooks';
import { Button, message, Popconfirm, Space, Spin, Tooltip } from 'antd';
import type { TablePaginationConfig } from 'antd/lib/table';
import { debounce } from 'lodash';
import moment from 'moment';
import { stringify } from 'qs';
import type { ReactNode } from 'react';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, useDispatch, useLocation, useSelector } from 'umi';
import { v1 } from 'uuid';
import { jumpPacketFromFlowRecord } from './utils';

/** 表格操作列的 dataindex */
const COLUMN_ACTION_KEY = 'action';

export const FLOW_RECORD_DEFAULT_COLUMNS = [
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
  flowId?: string;
  sid?: number;
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

const DEFAULT_PAGINATION = {
  currentPage: proTablePagination.defaultCurrent!,
  pageSize: proTablePagination.defaultPageSize!,
  total: 0,
  totalPages: 0,
  pageElements: 0,
};

interface IFlowRecordProps extends IFlowRecordEmbedProps, ILocationProps {
  cancelQueryTaskLoading: boolean | undefined;
}
const FlowRecord: React.FC<IFlowRecordProps> = ({
  tableKey = 'npmd-flow-record-table',
  filterHistoryKey = 'npmd-flow-record-filter-history',
  extraDsl,
  displayMetrics = [],
  extraAction,
}) => {
  const {
    query: { filter = '', flowId, sid },
  } = useLocation() as any as {
    query: ILocationProps;
  };

  // 当前排序的字段
  const [sortProperty, setSortProperty] = useState<string>('report_time');
  // 当前排序的方向
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  // 是否显示停止按钮
  const [cancelButtonVisible, setCancelButtonVisible] = useState<boolean>(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  const [pagination, setPagination] = useState(DEFAULT_PAGINATION);

  const [columns, setColumns] = useState<string[]>();

  const dispatch = useDispatch<Dispatch>();
  // const [recordData, setRecordData] = useState<IFlowRecordData[]>([]);

  const recordData = useSelector<ConnectState, IFlowRecordData[]>(
    (state) => state.flowRecordModel.recordData,
  );

  const dataLoading = useSelector<ConnectState, boolean>(
    (state) => !!state.loading.effects['flowRecordModel/queryFlowRecords'],
  );
  const [totalLoading, setTotalLoading] = useState(false);

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const [refresh, setRefresh] = useState(0);

  const firstLoadingRef = useRef(true);

  const selectedTimeRef = useLatest(globalSelectedTime);

  // filter过滤条件
  // 转换过滤条件
  const filterJson: IFilterCondition = parseArrayJson(decodeURIComponent(filter));

  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(() => {
    return deduplicateCondition(filterJson, new Set());
  });

  // total代表查询流日志总数的queryId, data是查询流日志接口的queryId;
  const queryIdRef = useRef<{ total?: string; data?: string; lastQueryDataId?: string }>({});
  const timerRef = useRef<{ total?: NodeJS.Timer; data?: NodeJS.Timer }>({});

  const baseCols = useFlowRecordColumns(setFilterCondition);

  /** 所有的流日志字段 */
  const allColumns: IFlowRecordColumnProps<IFlowRecordData>[] = useMemo(() => {
    return [
      ...baseCols,
      {
        title: '操作',
        dataIndex: COLUMN_ACTION_KEY,
        width: 170,
        show: true,
        fixed: 'right',
        disabled: true,
        render: (_, record) => {
          const metadataFilter: IFilter[] = [];
          const srcIp = record.ipv4_initiator || record.ipv6_initiator;
          const destIp = record.ipv4_responder || record.ipv6_responder;
          if (record.network_id.length > 0) {
            metadataFilter.push({
              field: 'network_id',
              operator: EFilterOperatorTypes.EQ,
              operand: record.network_id[0],
            });
          }

          // 如果目标元数据不区分v4、v6
          if (['SOCKS4', 'SIP'].includes(matadataDetailKV[record.l7_protocol_id])) {
            if (srcIp) {
              metadataFilter.push({
                field: 'src_ip',
                operator: EFilterOperatorTypes.EQ,
                operand: srcIp,
              });
            }
            if (destIp) {
              metadataFilter.push({
                field: 'dest_ip',
                operator: EFilterOperatorTypes.EQ,
                operand: destIp,
              });
            }
          } else {
            if (srcIp) {
              metadataFilter.push({
                field: isIpv4(srcIp) ? 'src_ipv4' : 'src_ipv6',
                operator: EFilterOperatorTypes.EQ,
                operand: srcIp,
              });
            }
            if (destIp) {
              metadataFilter.push({
                field: isIpv4(destIp) ? 'dest_ipv4' : 'dest_ipv6',
                operator: EFilterOperatorTypes.EQ,
                operand: destIp,
              });
            }
          }

          // 填充端口条件
          if (record.port_initiator) {
            metadataFilter.push({
              field: 'src_port',
              operator: EFilterOperatorTypes.EQ,
              operand: record.port_initiator,
            });
          }
          if (record.port_responder) {
            metadataFilter.push({
              field: 'dest_port',
              operator: EFilterOperatorTypes.EQ,
              operand: record.port_responder,
            });
          }

          const packetFilter = jumpPacketFromFlowRecord(record);

          let startTime: number;
          let endTime: number;
          if (record.start_time && record.duration) {
            startTime = moment(record.start_time).valueOf();
            endTime = startTime + record.duration + 60000;
          }
          return (
            <Space direction="horizontal">
              <span
                className="link"
                onClick={() => {
                  const url = getLinkUrl(
                    `${packetUrl}?${stringify({
                      filter: encodeURIComponent(JSON.stringify(packetFilter)),
                      from: new Date(
                        startTime || selectedTimeRef.current.originStartTime,
                      ).getTime(),
                      to: new Date(endTime || selectedTimeRef.current.originEndTime).getTime(),
                      timeType: ETimeType.CUSTOM,
                    })}`,
                  );
                  jumpNewPage(url);
                }}
              >
                数据包
              </span>
              <span
                className={matadataDetailKV[record.l7_protocol_id] ? 'link' : 'disabled'}
                onClick={() => {
                  if (matadataDetailKV[record.l7_protocol_id]) {
                    let protocolName = matadataDetailKV[record.l7_protocol_id];
                    if (protocolName.toLocaleLowerCase() === 'icmp') {
                      if (record.ipv4_initiator) {
                        protocolName = 'icmpv4';
                      } else {
                        protocolName = 'icmpv6';
                      }
                    }
                    if (protocolName.toLocaleLowerCase() === 'dhcp') {
                      if (record.ipv6_initiator) {
                        protocolName = 'dhcpv6';
                      }
                    }

                    const url = getLinkUrl(
                      `/analysis/trace/metadata/record?${stringify({
                        jumpTabs: protocolName,
                        filter: encodeURIComponent(JSON.stringify(metadataFilter)),
                        from: new Date(
                          startTime || selectedTimeRef.current.originStartTime,
                        ).getTime(),
                        to: new Date(endTime || selectedTimeRef.current.originEndTime).getTime(),
                        timeType: ETimeType.CUSTOM,
                      })}`,
                    );
                    jumpNewPage(url);
                  }
                }}
              >
                应用层协议
              </span>
            </Space>
          );
        },
      },
    ];
  }, [baseCols, selectedTimeRef]);

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
          directionConfig: item.directionConfig,
        };
      });
  }, [displayMetricInfos]);

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
          align: col.dataIndex !== COLUMN_ACTION_KEY ? undefined : ('center' as any),
          ellipsis: true,
          sortOrder: (col.dataIndex === sortProperty ? `${sortDirection}end` : false) as any,
          render: (value, record, index) => {
            if (col.fieldType === EFieldType.ARRAY) {
              const values: string[] = record[col.dataIndex as string];
              return values
                .filter((item) => {
                  return col.enumValue?.find((enumItem) => enumItem.value === item)?.text;
                })
                .map((item) => {
                  let content: string = item;
                  if (col.operandType === EFieldOperandType.ENUM) {
                    content =
                      col.enumValue?.find((enumItem) => enumItem.value === item)?.text || '';
                  }
                  return (
                    <FilterBubble
                      key={content}
                      containerStyle={{ display: 'inline' }}
                      dataIndex={col.dataIndex as string}
                      label={<span className="table-cell-button">{content}</span>}
                      operand={item}
                      fieldType={col.fieldType}
                      operandType={col.operandType as EFieldOperandType}
                      onClick={(newFilter) => {
                        setFilterCondition((prev) => [...prev, newFilter]);
                      }}
                    />
                  );
                });
            }

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
    setPagination((prev) => {
      return {
        ...prev,
        currentPage: 1,
      };
    });
  };

  useEffect(() => {
    if (!firstLoadingRef.current) {
      const tmp = history.location.query || {};
      if (tmp.flowId || tmp.sid) {
        delete tmp.flowId;
        delete tmp.sid;
        history.replace({
          pathname: history.location.pathname,
          query: tmp,
        });
      }
    }
    firstLoadingRef.current = false;
  }, [filterCondition]);

  // dsl参数
  const dsl = useMemo(() => {
    let nextDsl = ``;

    nextDsl += filterCondition2Spl(filterCondition, filterField);

    // 拼接额外的 DSL 条件
    if (extraDsl) {
      if (nextDsl) {
        nextDsl += ` AND `;
      }
      nextDsl += `(${extraDsl}) `;
    }

    // 如果是在会话详单中，排除建连失败的流
    // 过滤规则可以用 "总包数>0" 即可，仅在会话详单中过滤掉
    if (nextDsl) {
      nextDsl += ` AND `;
    }
    nextDsl += `(total_packets > 0) `;

    // 拼接全局的时间和
    nextDsl += `| gentimes report_time start="${globalSelectedTime.originStartTime}" end="${globalSelectedTime.originEndTime}"`;

    return nextDsl;
  }, [
    filterCondition,
    filterField,
    extraDsl,
    globalSelectedTime.originStartTime,
    globalSelectedTime.originEndTime,
  ]);

  // 上下翻页
  const handlePageChange = (current: number, nextPageSize: number) => {
    scrollTo('#flowRecordTable');
    setPagination((prev) => {
      return {
        ...prev,
        pageSize: nextPageSize,
        currentPage: current,
      };
    });
  };
  const debouncedHandlePageChange = debounce(handlePageChange, 300);

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

  // 这里的条件变量，结果总数会变，因此需要重新查询统计接口
  const queryParams = useMemo<IQueryRecordParams>(() => {
    return {
      startTime: globalSelectedTime.originStartTime,
      endTime: globalSelectedTime.originEndTime,
      dsl,
      entry: ETablekeyEntry[tableKey],
    };
  }, [dsl, globalSelectedTime.originEndTime, globalSelectedTime.originStartTime, tableKey]);

  // 这里的参数不变，则查询结果的总条数不变，避免重复查询统计接口
  const basicParams = useMemo(() => {
    return {
      sortProperty,
      sortDirection,
      pageSize: pagination.pageSize,
      page: pagination.currentPage! - 1,
    };
  }, [pagination.currentPage, pagination.pageSize, sortDirection, sortProperty]);

  // 取消查询
  const cancelQuery = async (silent = false, type?: 'data' | 'total') => {
    if (type && !queryIdRef.current[type]) return;
    if (type === undefined && !queryIdRef.current.data && !queryIdRef.current.total) return;
    if (!silent) {
      message.loading('正在停止...');
    }

    setCancelQueryTaskLoading(true);

    const ajaxPaths = ['/global/slow-queries/heartbeat'];
    if (type) {
      ajaxPaths.push(
        type === 'data' ? '/appliance/flow-logs' : '/appliance/flow-logs/as-statistics',
      );
    } else {
      ajaxPaths.push('/appliance/flow-logs', '/appliance/flow-logs/as-statistics');
    }

    abortAjax(ajaxPaths);

    const { success } = await cancelQueryTask({
      queryId: type
        ? queryIdRef.current[type]!
        : Object.values(queryIdRef.current)
            .filter((item) => item)
            .join(','),
    });

    message.destroy();
    setCancelQueryTaskLoading(false);
    if (type !== 'data') {
      setTotalLoading(false);
    }
    setCancelButtonVisible(false);
    if (type) {
      queryIdRef.current[type] = undefined;
    } else {
      queryIdRef.current.data = undefined;
      queryIdRef.current.total = undefined;
    }

    if (!success) {
      message.warning('停止失败');
      return;
    }

    clearInterval(timerRef.current.data as NodeJS.Timer);
    clearInterval(timerRef.current.total as NodeJS.Timer);
  };

  useEffect(() => {
    // 存在一个loading为true, 则cancelButton显示
    // 小心闭包
    if (!dataLoading && !totalLoading) {
      setCancelButtonVisible(false);
      return;
    }

    const timer = setTimeout(() => {
      if (queryIdRef.current.data || queryIdRef.current.total) {
        setCancelButtonVisible(dataLoading || totalLoading);
      }
    }, 3000);
    return () => {
      clearTimeout(timer);
    };
  }, [dataLoading, totalLoading]);

  // 流日志查询
  useEffect(() => {
    if (columns !== undefined) {
      if (queryIdRef.current.data) {
        cancelQuery(true, 'data');
      }
      const queryId = v1();
      queryIdRef.current.data = queryId;
      const newParams = {
        ...queryParams,
        ...basicParams,
        queryId,
        columns: getColumnParams({ cols: columns, tableKey: tableKey, extra: ['l7_protocol_id'] }),
      };
      if (flowId) {
        newParams.flowId = flowId.toString();
      }
      if (sid) {
        newParams.sid = sid;
      }

      dispatch({
        type: 'flowRecordModel/queryFlowRecords',
        payload: {
          ...newParams,
          page: newParams.page + 1,
        },
      }).then(
        ({
          status,
          total,
          totalPages,
          success,
        }: {
          status: number;
          total: number;
          totalPages: number;
          success: boolean;
        }) => {
          setCancelButtonVisible(false);
          // 不管的请求成功了还是失败了，都去除查询ID
          queryIdRef.current.lastQueryDataId = queryIdRef.current.data;
          queryIdRef.current.data = undefined;
          // status=0时表示请求被取消了
          if (!status) {
            return;
          }
          if (success && (flowId || sid)) {
            setPagination((prev) => {
              return {
                ...prev,
                total,
                totalPages,
              };
            });
          }
        },
      );

      const ping = setInterval(() => {
        if (queryIdRef.current.data) {
          pingQueryTask({ queryId: queryIdRef.current.data }).then(({ success }) => {
            if (!success) {
              message.destroy();
            }
          });
        }
      }, 3000);

      return () => {
        clearInterval(ping);
      };
    }
    return;
  }, [queryParams, basicParams, flowId, refresh, columns, dispatch, tableKey, sid]);

  // 查询参数变化，重新查询流日志结果总数
  useEffect(() => {
    if (!flowId && !sid) {
      if (queryIdRef.current.total) {
        cancelQuery(true, 'total');
      }
      const queryId = v1();
      queryIdRef.current.total = queryId;
      setTotalLoading(true);
      queryFlowLogsStatistics({ ...queryParams, queryId }).then(({ success, result }) => {
        setTotalLoading(false);
        if (success) {
          queryIdRef.current.total = undefined;
          const { total } = result;
          setPagination((prev) => {
            return {
              ...prev,
              total,
              totalPages: Math.ceil(total / prev.pageSize),
            };
          });
        }
      });

      const ping = setInterval(() => {
        if (queryIdRef.current.total) {
          pingQueryTask({ queryId: queryIdRef.current.total }).then(({ success }) => {
            if (!success) {
              message.destroy();
            }
          });
        }
      }, 3000);
      timerRef.current.total = ping;

      return () => {
        clearInterval(ping);
      };
    }
    return;
  }, [flowId, queryParams, refresh, sid]);

  const pktQueryParams = useMemo(() => {
    return {
      ...queryParams,
      sortProperty,
      sortDirection,
    };
  }, [queryParams, sortDirection, sortProperty]);

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

    // 跳转
    const url = getLinkUrl(
      `${packetUrl}?${stringify({
        filter: encodeURIComponent(JSON.stringify(packetSupportFilter)),
        from: new Date(globalSelectedTime.originStartTime).getTime(),
        to: new Date(globalSelectedTime.originEndTime).getTime(),
        timeType: ETimeType.CUSTOM,
      })}`,
    );
    jumpNewPage(url);
  }, [filterCondition, globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  const jumpToPacketRender = useMemo(() => {
    return (
      <Popconfirm
        title="跳转数据包时不会携带高级过滤条件"
        onConfirm={jumpToPacket}
        icon={<InfoCircleOutlined style={{ color: 'blue' }} />}
      >
        <Button icon={<FolderOutlined />} type="primary" loading={dataLoading}>
          数据包
        </Button>
      </Popconfirm>
    );
  }, [dataLoading, jumpToPacket]);

  const networkInFilter = useMemo(() => {
    const res: string[] = [];
    filterCondition.forEach((ele: any) => {
      if (ele?.group) {
        ele.group.forEach((sub: IFilter) => {
          if (sub.field === 'network_id') {
            res.push(String(sub.operand));
          }
        });
      } else {
        if (ele.field === 'network_id') {
          res.push(ele.operand);
        }
      }
    });
    return res;
  }, [filterCondition]);

  return (
    <>
      {/* 表格 */}
      <div id="flowRecordTable">
        <EnhancedTable
          sortProperty={sortProperty}
          sortDirection={`${sortDirection}end` as any}
          tableKey={tableKey}
          // tableKey="flow-record-table"
          rowKey={() => v1()}
          loading={dataLoading}
          columns={showColumns}
          dataSource={dataLoading ? [] : recordData}
          pagination={false}
          onColumnChange={setColumns}
          defaultShowColumns={FLOW_RECORD_DEFAULT_COLUMNS}
          onChange={handleTableChange}
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
                        onClick={() => {
                          cancelQuery();
                        }}
                      >
                        停止
                      </Button>
                    </Tooltip>
                  )}
                  <DownLoadPktBtn
                    queryId={queryIdRef.current.lastQueryDataId}
                    queryFn={(param: any) => {
                      return queryPkt(
                        {
                          ...param,
                          ...pktQueryParams,
                          networkId: networkInFilter.join(','),
                          flowId,
                          sid,
                        },
                        EQueryLogToPkt.FlowLog,
                      );
                    }}
                    totalPkt={pagination.total}
                    loading={dataLoading}
                  />
                  <ExportFile
                    loading={dataLoading}
                    totalNum={pagination.total}
                    queryFn={(params: any) => {
                      return queryExportFile(
                        {
                          ...params,
                          ...pktQueryParams,
                          flowId,
                          sid,
                          queryId: queryIdRef.current.lastQueryDataId,
                          networkId: networkInFilter.join(','),
                          columns: getColumnParams({
                            cols: columns,
                            tableKey,
                          }),
                        },
                        EQueryLogToPkt.FlowLog,
                      );
                    }}
                  />
                  {jumpToPacketRender}
                  <Button
                    type="primary"
                    icon={<ReloadOutlined />}
                    loading={dataLoading}
                    onClick={() => {
                      setRefresh((prev) => prev + 1);
                    }}
                  >
                    刷新
                  </Button>
                  <Button icon={<ReloadOutlined />} onClick={handleReset} loading={dataLoading}>
                    重置
                  </Button>

                  {extraAction}
                </Space>
              }
            />
          }
          extraFooter={
            <Spin size="small" spinning={totalLoading}>
              <CustomPagination {...pagination} onChange={debouncedHandlePageChange} />
            </Spin>
          }
        />
      </div>
    </>
  );
};

export default FlowRecord;
