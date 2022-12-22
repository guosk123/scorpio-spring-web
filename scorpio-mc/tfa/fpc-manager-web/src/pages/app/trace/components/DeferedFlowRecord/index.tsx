import { getCurrentPageSize } from '@/common/app';
import CustomPagination from '@/components/CustomPagination';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParams } from '@/pages/app/appliance/FlowRecord/Record';
import { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter, IFilterCondition, IFilterGroup } from '@/components/FieldFilter/typings';
import {
  EFieldOperandType,
  EFieldType,
  EFilterGroupOperatorTypes,
  EFilterOperatorTypes,
} from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { EQueryLogToPkt } from '@/pages/app/appliance/components/DownLoadPktBtn';
import ExportFile, { queryExportFile } from '@/pages/app/appliance/components/ExportFile';
import useFlowRecordColumns from '@/pages/app/appliance/FlowRecord/hooks/useFlowRecordColumns';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecord/Record';
import { cancelQueryTask, pingQueryTask } from '@/pages/app/appliance/FlowRecord/service';
import type { IFlowRecordData, IQueryRecordParams } from '@/pages/app/appliance/FlowRecord/typings';
import { jumpPacketFromFlowRecord } from '@/pages/app/security/RecordQuery/FlowRecord/utils';
import { abortAjax, getLinkUrl, ipV4Regex, isCidr, jumpNewPage } from '@/utils/utils';
import { StopOutlined } from '@ant-design/icons';
import { useInterval } from 'ahooks';
import { Button, message, Select, Space, Spin, Tooltip } from 'antd';
import type { TablePaginationConfig } from 'antd/es/table/interface';
import moment from 'moment';
import { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch, useSelector } from 'umi';
import { v1 } from 'uuid';
import { IOCContext, searchField, tabContent } from '../../IOC';
import styles from '../common.less';
import FilterTag from '../FilterTag';
import { packetUrl } from '@/pages/app/appliance/Packet';
import { FLOW_RECORD_DEFAULT_COLUMNS } from '@/pages/app/security/RecordQuery/FlowRecord';

const IOC_FLOW_RECORD_TABLE_KEY = 'ioc-flow-record';

const ipFieldConvert = {
  ip_initiator: {
    isV4: 'ipv4_initiator',
    isV6: 'ipv6_initiator',
  },
  ip_responder: {
    isV4: 'ipv4_responder',
    isV6: 'ipv6_responder',
  },
};

enum EQueryType {
  'ALL' = 'ALL',
  'INITIATOR' = 'INITIATOR',
  'RECPONDER' = 'RESPONDER',
}

const DeferedFlowRecord = () => {
  const { currentTab, search, networkId } = useContext(IOCContext);

  // 当前排序的字段
  const [sortProperty, setSortProperty] = useState<string>('report_time');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [pageParams, setPageParams] = useState<{ page: number; pageSize: number }>({
    page: 1,
    pageSize: getCurrentPageSize(),
  });
  const [columns, setColumns] = useState<string[]>();
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);
  // 是否显示停止按钮
  const [cancelButtonVisible, setCancelButtonVisible] = useState<boolean>(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  // total代表查询流日志总数的queryId, data是查询流日志接口的queryId;
  const queryIdRef = useRef<{ total?: string; data?: string }>({});
  const [cacheQueryId, setCacheQueryId] = useState<string | undefined>(queryIdRef.current.data);
  // 查询类型：全量，主动，被动
  const [queryType, setQueryType] = useState<EQueryType>(EQueryType.ALL);

  const dispatch = useDispatch<Dispatch>();

  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );

  const loadingEffects = useSelector<ConnectState, Record<string, boolean | undefined>>(
    (state) => state.loading.effects,
  );

  const tableData = useSelector<ConnectState, IFlowRecordData[]>(
    (state) => state.flowRecordModel.recordData,
  );

  const total = useSelector<ConnectState, number>(
    (state) => state.flowRecordModel.pagination.total,
  );

  const dataLoading = loadingEffects['flowRecordModel/queryFlowRecords'];
  const totalLoading = loadingEffects['flowRecordModel/queryFlowLogsStatistics'];

  // 流日志列声明
  const baseCols = useFlowRecordColumns(setFilterCondition);

  /** 所有的流日志字段 */
  const allColumns: IFlowRecordColumnProps<IFlowRecordData>[] = useMemo(() => {
    return [
      ...baseCols,
      {
        title: '操作',
        dataIndex: 'action',
        width: 100,
        show: true,
        fixed: 'right',
        disabled: true,
        render: (_, record) => {
          const packetFilter: IFilter[] = jumpPacketFromFlowRecord(record);

          let startTime: number;
          let endTime: number;
          if (record.start_time && record.duration) {
            startTime = moment(record.start_time).valueOf();
            endTime = startTime + record.duration + 60000;
          }
          return (
            <span
              className="link"
              onClick={() => {
                const url = getLinkUrl(
                  `${packetUrl}?filter=${encodeURIComponent(
                    JSON.stringify(packetFilter),
                  )}&from=${new Date(
                    startTime || globalSelectedTime.originStartTime,
                  ).valueOf()}&to=${new Date(
                    endTime || globalSelectedTime.originEndTime,
                  ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                );
                jumpNewPage(url);
              }}
            >
              数据包
            </span>
          );
        },
      },
    ];
  }, [baseCols, globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  const filterField = useMemo(() => {
    return allColumns
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
        };
      });
  }, [allColumns]);

  const showColumns: IFlowRecordColumnProps<IFlowRecordData>[] = useMemo(() => {
    return allColumns
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
            if (col.fieldType === EFieldType.ARRAY) {
              const values: string[] = record[col.dataIndex as string];
              return values.map((item) => {
                let content: string = item;
                if (col.operandType === EFieldOperandType.ENUM) {
                  content =
                    col.enumValue?.find((enumItem) => enumItem.value === item)?.text || item;
                }
                return (
                  <FilterBubble
                    key={item}
                    dataIndex={col.dataIndex as string}
                    label={<span className="table-cell-button">{content}</span>}
                    fieldType={col.fieldType}
                    operand={item}
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
  }, [allColumns, sortDirection, sortProperty]);

  // 这里的参数不变，则查询结果的总条数不变，避免重复查询统计接口
  const baseParams = useMemo(() => {
    return {
      sortProperty,
      sortDirection,
      pageSize: pageParams.pageSize,
      page: pageParams.page,
    };
  }, [pageParams, sortDirection, sortProperty]);

  const dsl = useMemo(() => {
    const time = {
      start: globalSelectedTime.originStartTime,
      end: globalSelectedTime.originEndTime,
    };
    let spl = filterCondition2Spl(filterCondition, filterField);

    // 网络id可以多选
    if (networkId.length > 0) {
      if (spl) {
        spl += ' AND ';
      }
      const netFilter: IFilterGroup = { operator: EFilterGroupOperatorTypes.OR, group: [] };
      networkId.forEach((item) => {
        netFilter.group.push({
          field: 'network_id',
          operator: EFilterOperatorTypes.EQ,
          operand: item,
        });
      });
      spl += filterCondition2Spl([netFilter], filterField);
    }

    if (search?.isValid && search.value) {
      if (spl) {
        spl += ' AND ';
      }

      const fields = searchField[currentTab]['flow-record'];

      if (fields && fields.length > 1) {
        const filter: IFilterGroup = { operator: EFilterGroupOperatorTypes.OR, group: [] };

        const isV4 = ipV4Regex.test(search.value);
        const isV4Cidr = isCidr(search.value, 'IPv4');

        // 这里基本上是硬编码
        fields
          .filter((field) => {
            if (queryType === EQueryType.INITIATOR) {
              return field.indexOf('initiator') > -1;
            }
            if (queryType === EQueryType.RECPONDER) {
              return field.indexOf('responder') > -1;
            }
            return field;
          })
          .forEach((field) => {
            const tmpField = ipFieldConvert[field][isV4 || isV4Cidr ? 'isV4' : 'isV6'] || field;
            filter.group.push({
              field: tmpField,
              operator: EFilterOperatorTypes.EQ,
              operand: search.value,
            });
          });

        spl += filterCondition2Spl([filter], filterField);
      }
    }

    if (spl) {
      spl += ` AND `;
    }
    spl += `(total_packets > 0) | gentimes report_time start="${time.start}" end="${time.end}"`;

    return spl;
  }, [
    currentTab,
    filterCondition,
    filterField,
    globalSelectedTime.originEndTime,
    globalSelectedTime.originStartTime,
    networkId,
    queryType,
    search,
  ]);

  // 这里的条件变量，结果总数会变，因此需要重新查询统计接口
  const queryParams = useMemo<IQueryRecordParams>(() => {
    return {
      startTime: globalSelectedTime.originStartTime,
      endTime: globalSelectedTime.originEndTime,
      dsl,
    };
  }, [dsl, globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  // 取消查询
  const cancelQuery = async (silent = false) => {
    if (!queryIdRef.current.data && !queryIdRef.current.total) return;
    if (!silent) {
      message.loading('正在停止...');
    }
    setCancelQueryTaskLoading(true);
    abortAjax([
      '/appliance/flow-logs',
      '/appliance/flow-logs/as-statistics',
      '/global/slow-queries/heartbeat',
    ]);

    const { success } = await cancelQueryTask({
      queryId: Object.values(queryIdRef.current)
        .filter((item) => item)
        .join(','),
    });
    setCancelQueryTaskLoading(false);
    queryIdRef.current = {};
    if (silent) return;

    setCancelButtonVisible(false);
    message.destroy();

    if (!success) {
      message.warning('停止失败');
      return;
    }
  };

  const query = useCallback(
    (params: IQueryRecordParams) => {
      if (queryIdRef.current.data || queryIdRef.current.total) {
        cancelQuery(true);
      }
      const queryId = v1();
      const statQueryId = v1();
      queryIdRef.current.data = queryId;
      setCacheQueryId(queryId);

      queryIdRef.current.total = statQueryId;
      dispatch({
        type: 'flowRecordModel/queryFlowRecords',
        payload: {
          ...params,
          queryId,
        },
      }).then(() => {
        setCancelButtonVisible(false);
        queryIdRef.current.data = undefined;
      });
      dispatch({
        type: 'flowRecordModel/queryFlowLogsStatistics',
        payload: {
          ...params,
          queryId: statQueryId,
        },
      }).then(() => {
        queryIdRef.current.total = undefined;
      });
    },
    [dispatch],
  );

  useEffect(() => {
    // 存在一个loading为true, 则cancelButton显示
    // 小心闭包
    if (!dataLoading && !totalLoading) {
      setCancelButtonVisible(false);
      return;
    }

    const timer = setTimeout(() => {
      if (queryIdRef.current.data || queryIdRef.current.total) {
        setCancelButtonVisible(true);
      }
    }, 3000);
    return () => {
      clearTimeout(timer);
    };
  }, [dataLoading, totalLoading]);

  useInterval(
    () => {
      pingQueryTask({
        queryId: Object.values(queryIdRef.current)
          .filter((i) => i)
          .join(','),
      }).then(({ success }) => {
        if (!success) {
          message.destroy();
        }
      });
    },
    dataLoading || totalLoading ? 3000 : undefined,
    { immediate: true },
  );

  useEffect(() => {
    query({
      ...queryParams,
      ...baseParams,
      columns: getColumnParams({
        cols: columns,
        tableKey: IOC_FLOW_RECORD_TABLE_KEY,
      }),
    });
    return () => {
      cancelQuery(true);
    };
  }, [baseParams, columns, query, queryParams]);

  const handlePageChange = (currentPage: number, pageSize: number) => {
    setPageParams({
      page: currentPage,
      pageSize: pageSize,
    });
  };

  const removeFilterItem = (idx: number) => {
    setFilterCondition((prev) => {
      return prev.filter((item, i) => idx !== i);
    });
  };

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

  return (
    <EnhancedTable
      autoHeight={tabContent[currentTab].length < 2}
      {...(tabContent[currentTab].length < 2 ? {} : { scroll: { x: 'max-content', y: 500 } })}
      tableKey={IOC_FLOW_RECORD_TABLE_KEY}
      columns={showColumns}
      dataSource={tableData}
      loading={dataLoading}
      onChange={handleTableChange}
      pagination={false}
      rowKey={() => v1()}
      onColumnChange={setColumns}
      defaultShowColumns={FLOW_RECORD_DEFAULT_COLUMNS}
      extraTool={
        <div className={styles.header}>
          <div>
            <div className={styles.title}>
              <div>会话详单</div>
            </div>
            <FilterTag filter={filterCondition} onRemove={removeFilterItem} fields={filterField} />
          </div>
          <Space direction="horizontal">
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
            <Select
              value={queryType}
              onChange={setQueryType}
              options={[
                { label: '全量会话', value: EQueryType.ALL },
                { label: '主动会话', value: EQueryType.INITIATOR },
                { label: '被动会话', value: EQueryType.RECPONDER },
              ]}
            />
            <ExportFile
              loading={dataLoading || false}
              totalNum={total}
              queryFn={(params: any) => {
                return queryExportFile(
                  {
                    ...params,
                    ...queryParams,
                    queryId: cacheQueryId,
                    tableKey: IOC_FLOW_RECORD_TABLE_KEY,
                    columns: getColumnParams({
                      cols: columns,
                      tableKey: IOC_FLOW_RECORD_TABLE_KEY,
                    }),
                  },
                  EQueryLogToPkt.FlowLog,
                );
              }}
            />
          </Space>
        </div>
      }
      extraFooter={
        <Spin spinning={totalLoading} size="small">
          <CustomPagination
            loading={totalLoading}
            onChange={handlePageChange}
            currentPage={pageParams.page}
            pageSize={pageParams.pageSize}
            total={total}
          />
        </Spin>
      }
    />
  );
};

export default DeferedFlowRecord;
