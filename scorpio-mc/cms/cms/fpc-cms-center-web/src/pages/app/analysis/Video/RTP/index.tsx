import EnhancedTable from '@/components/EnhancedTable';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type {
  EFieldOperandType,
  EFieldType,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import ExportFile from '@/components/ExportFile'; 
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecords/Record/typing'; 
import { ReloadOutlined } from '@ant-design/icons';
import { Button, Divider, Space } from 'antd';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ESortDirection } from '../../typings';
import { VideoContext } from '../components/NetworkTimeLayout';
import type { IRTPFlow } from '../typings';
import { EVideoTabType } from '../typings';
import { allRTPColumns } from './constant';
import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import { EMetadataTabType } from '@/pages/app/appliance/Metadata/typings'; 
import CustomPagination from '@/components/CustomPagination';
import { getTablePaginationDefaultSettings, PAGE_DEFAULT_SIZE } from '@/common/app';
import type { ITableQueryParams } from '../services';
import { queryRTPFlowCount } from '../services';
import { queryRTPFlows } from '../services';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import { v4 as uuidv4 } from 'uuid';
import { stringify } from 'qs';
import { snakeCase } from 'lodash';
import useCallbackState from '@/hooks/useCallbackState';
import config from '@/common/applicationConfig';
import { ETimeType } from '@/components/GlobalTimeSelector';
import { VideoTabsContext } from '../components/VideoEditTabs';
import type { INetworkSensor } from '@/pages/app/Configuration/Network/typings';

const { API_BASE_URL, API_VERSION_PRODUCT_V1 } = config
const COLUMN_ACTION_KEY = 'action';
const COLUMN_INDEX_KEY = 'index';
const tableKey = 'npmd-rtp-flow-record-table';
/** 默认展示的列 */
const RTP_FLOW_RECORD_DEFAULT_COLUMNS = [
  COLUMN_INDEX_KEY,
  'from',
  'src_ip',
  'src_port',
  'to',
  'dest_ip',
  'dest_port',
  'ip_protocol',
  COLUMN_ACTION_KEY,
];
const RTP_FLOW_RECORD_EXCLUDE_COLS: string[] = [];

const getColumnParams = getColumnParamsFunc(RTP_FLOW_RECORD_EXCLUDE_COLS);
function getColumns(columns: string[] | undefined) {
  return getColumnParams({
    cols: columns,
    tableKey: tableKey,
  });
}

export default function RTP() {
  /** --------------------------- 上下文 --------------------------- */
  /** VideoTabsContext */
  const [state] = useContext(VideoTabsContext);
  /** 网络时间选择器上下文 */
  const { network, globalSelectedTime, setNetworkSelect } = useContext(VideoContext)!;

  /** --------------------------- 表格相关 --------------------------- */
  /** 排序方向 */
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);

  /* 表格变化 */
  const handleTableChange = (page: any, filters: any, sorter: any) => {
    const newSortDirection: ESortDirection =
      sorter.order === 'descend' ? ESortDirection.DESC : ESortDirection.ASC;
    setSortDirection(newSortDirection);
  };

  /** --------------------------- 标识相关 ---------------------------*/
  /** 加载标志 */
  const [loading, setLoading] = useState<boolean>(true);

  /** --------------------------- 过滤相关 ---------------------------*/
  /** 过滤条件 */
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  /** 过滤列 */
  const filterField = useMemo(() => {
    return allRTPColumns
      .filter((item) => item.searchable)
      .map((item) => {
        const dataIndex = item.dataIndex as string;

        return {
          title: item.title as string,
          dataIndex: snakeCase(dataIndex),
          type: item.fieldType as EFieldType,
          operandType: item.operandType!,
          enumValue: item.enumValue,
        };
      });
  }, []);

  /** 过滤条件变化 */
  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  /** 重制过滤条件 */
  const handleResetFilter = () => {
    setFilterCondition([]);
  };

  /** --------------------------- 分页相关 --------------------------- */
  /** 总数 */
  const [totalElements, setTotalElements] = useCallbackState<{ total: number }>({ total: 0 });
  /** 分页信息 */
  const [pagination, setPagination] = useState<{ page: number; pageSize: number }>({
    page: 1,
    pageSize: getTablePaginationDefaultSettings().pageSize! || PAGE_DEFAULT_SIZE,
  });

  /** --------------------------- 时间相关 --------------------------- */
  const startTime = useMemo(() => {
    return globalSelectedTime?.startTime;
  }, [globalSelectedTime]);

  const endTime = useMemo(() => {
    return globalSelectedTime?.endTime;
  }, [globalSelectedTime?.endTime]);

  /** --------------------------- 列相关 --------------------------- */
  /** 操作列 */
  const actionCol: IFlowRecordColumnProps<IRTPFlow> = useMemo(() => {
    return {
      dataIndex: COLUMN_ACTION_KEY,
      title: '操作',
      width: 400,
      show: true,
      fixed: 'right',
      disabled: true,
      render: (text: any, record: IRTPFlow) => {
        const { startTimestamp, endTimestamp } = globalSelectedTime!;
        return (
          <>
            {/* <span className="link" onClick={() => {}}>
                播放
              </span>
              <Divider type="vertical" /> */}
            <span
              className="link"
              onClick={() => {
                const srcIp = record?.srcIp;
                const destIp = record?.destIp;

                const filterParam: (IFilter | IFilterGroup)[] = [];
                filterParam.push(
                  {
                    field: 'src_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: srcIp,
                  },
                  {
                    field: 'dest_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: destIp,
                  },
                );

                jumpNewPage(
                  getLinkUrl(
                    `/analysis/video/devices?jumpTabs=${EVideoTabType.SEGMENT}&tabTitle=${`${
                      record?.srcIp
                    }-${record?.destIp}_分段分析&filter=${encodeURIComponent(
                      JSON.stringify(filterParam),
                    )}&from=${startTimestamp}&to=${endTimestamp}&timeType=${ETimeType.CUSTOM}`}`,
                  ),
                );
              }}
            >
              分段分析
            </span>
            <Divider type="vertical" />
            <span
              className="link"
              onClick={() => {
                const srcIp = record?.srcIp;
                const destIp = record?.destIp;

                const srcIpIsV4 = srcIp && isIpv4(srcIp);
                const destIpIsV4 = destIp && isIpv4(destIp);

                const filterParam: (IFilter | IFilterGroup)[] = [];
                filterParam.push(
                  {
                    field: srcIpIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                    operator: EFilterOperatorTypes.EQ,
                    operand: srcIp,
                  },
                  {
                    field: 'port_initiator',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.srcPort,
                  },
                  {
                    field: destIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                    operator: EFilterOperatorTypes.EQ,
                    operand: destIp,
                  },
                  {
                    field: 'port_responder',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.destPort,
                  },
                  {
                    field: 'ip_protocol',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.ipProtocol,
                  },
                  {
                    field: 'l7_protocol_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: '292',
                  },
                  {
                    field: 'network_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: (network as INetworkSensor)?.networkInSensorId || network?.id,
                  },
                );

                jumpNewPage(
                  getLinkUrl(
                    `/flow-trace/flow-record?filter=${encodeURIComponent(
                      JSON.stringify(filterParam),
                    )}&from=${startTimestamp}&to=${endTimestamp}&timeType=${ETimeType.CUSTOM}`,
                  ),
                );
              }}
            >
              RTP会话详单
            </span>
            <Divider type="vertical" />
            <span
              className="link"
              onClick={() => {
                const srcIp = record?.inviteSrcIp;
                const destIp = record?.inviteDestIp;

                const srcIpIsV4 = srcIp && isIpv4(srcIp);
                const destIpIsV4 = destIp && isIpv4(destIp);

                const filterParam: (IFilter | IFilterGroup)[] = [];
                filterParam.push(
                  {
                    field: srcIpIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                    operator: EFilterOperatorTypes.EQ,
                    operand: srcIp,
                  },
                  {
                    field: 'port_initiator',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteSrcPort,
                  },
                  {
                    field: destIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                    operator: EFilterOperatorTypes.EQ,
                    operand: destIp,
                  },
                  {
                    field: 'port_responder',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteDestPort,
                  },
                  {
                    field: 'ip_protocol',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteIpProtocol,
                  },
                  {
                    field: 'l7_protocol_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: '307',
                  },
                  {
                    field: 'network_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: (network as INetworkSensor)?.networkInSensorId || network?.id,
                  },
                );
                jumpNewPage(
                  getLinkUrl(
                    `/flow-trace/flow-record?filter=${encodeURIComponent(
                      JSON.stringify(filterParam),
                    )}&from=${startTimestamp}&to=${endTimestamp}&timeType=${ETimeType.CUSTOM}`,
                  ),
                );
              }}
            >
              SIP会话详单
            </span>
            <Divider type="vertical" />
            <span
              className="link"
              onClick={() => {
                const srcIp = record?.inviteSrcIp;
                const destIp = record?.inviteDestIp;

                const filterParam: (IFilter | IFilterGroup)[] = [];
                filterParam.push(
                  {
                    field: 'src_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: srcIp,
                  },
                  {
                    field: 'src_port',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteSrcPort,
                  },
                  {
                    field: 'dest_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: destIp,
                  },
                  {
                    field: 'dest_port',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteDestPort,
                  },
                  {
                    field: 'ip_protocol',
                    operator: EFilterOperatorTypes.EQ,
                    operand: record?.inviteIpProtocol,
                  },
                  {
                    field: 'network_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: (network as INetworkSensor)?.networkInSensorId || network?.id,
                  },
                );
                jumpNewPage(
                  getLinkUrl(
                    `/flow-trace/mata-data-detail?jumpTabs=${EMetadataTabType.SIP}&filter=${encodeURIComponent(
                        JSON.stringify(filterParam),
                    )}&from=${new Date(globalSelectedTime!.originStartTime).getTime()}&to=${new Date(
                      globalSelectedTime!.originEndTime,
                    ).getTime()}&timeType=${ETimeType.CUSTOM}`,
                  ),
                )
              }}
            >
              SIP应用层协议详单
            </span>
          </>
        );
      },
    };
  }, [globalSelectedTime, network]);

  /** 展示列 */
  const showColumns: IFlowRecordColumnProps<IRTPFlow>[] = useMemo(() => {
    return [...allRTPColumns, actionCol]
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
          render: (value, record, index) => {
            let label = value as any;
            if (col.render) {
              label = col.render(value, record, index);
            }

            if (!col.searchable) {
              return label;
            }

            return (
              <FilterBubble
                dataIndex={snakeCase(col.dataIndex as string)}
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
  }, [actionCol]);

  /** 选择展示的列（导出的列） */
  const [columns, setColumns] = useState<string[]>();

  /** --------------------------- 数据相关 --------------------------- */
  /** dsl */
  const dsl = useMemo(() => {
    // 过滤条件转dsl
    let filterSpl = filterCondition2Spl(filterCondition, filterField);

    // 网络转dsl
    if ((network as INetworkSensor)?.networkInSensorId || network?.id) {
      if (filterSpl) {
        filterSpl += ' AND ';
      }
      filterSpl += `( network_id<Array> = ${(network as INetworkSensor)?.networkInSensorId || network?.id} )`;
    }
    return filterSpl;
  }, [filterCondition, filterField, network]);

  /** 查询参数 */
  const queryParams = useMemo(() => {
    const params = {
      sortDirection,
      startTime,
      endTime,
      dsl,
      page: pagination.page - 1,
      pageSize: pagination.pageSize,
    };
    return params;
  }, [dsl, endTime, pagination, sortDirection, startTime]);

  /** 表格数据 */
  const [dataSource, setDataSource] = useState<IRTPFlow[]>([]);

  /** 查询表格数据 */
  const queryDataSources = useCallback(async () => {
    setLoading(true);
    const { success: elemSuccess, result: elementTotalObj } = await queryRTPFlowCount(
      queryParams as ITableQueryParams,
    );
    if (elemSuccess) {
      setTotalElements(elementTotalObj, async () => {
        const { success, result } = await queryRTPFlows({
          ...queryParams,
          columns: getColumns(columns),
        } as ITableQueryParams);
        if (success) {
          setDataSource(result?.content || []);
        }
      });
    }
    setLoading(false);
  }, [columns, queryParams]);

  const exportParams = useMemo(() => {
    return {
      networkId: (network as INetworkSensor)?.networkInSensorId || network?.id,
      startTime,
      endTime,
      dsl,
      sortDirection,
      columns: getColumns(columns),
      count: totalElements?.total,
    };
  }, [columns, dsl, endTime, network, sortDirection, startTime, totalElements?.total]);

  /** 初始化过滤条件 */
  useEffect(() => {
    const { filter } = (state as any)?.shareInfo || {};
    if (filter) {
      setFilterCondition([...filter]);
    }
  }, []);

  useEffect(() => {
    /** 初始加载数据 */
    queryDataSources();
  }, [queryDataSources]);

  /** 打开网络 */
  useEffect(() => {
    /** 打开网络选择器 */
    if (setNetworkSelect) {
      setNetworkSelect(true);
    }
  }, []);

  return (
    <>
      {/* 表格 */}
      <div id="flowRecordTable">
        <EnhancedTable<IRTPFlow>
          sortDirection={`${sortDirection}end` as any}
          tableKey={tableKey}
          rowKey={uuidv4()}
          loading={loading}
          columns={showColumns}
          /** 需要默认展示的时候打开 */
          // defaultShowColumns={RTP_FLOW_RECORD_DEFAULT_COLUMNS}
          dataSource={dataSource}
          pagination={false}
          onChange={handleTableChange}
          onColumnChange={setColumns}
          extraTool={
            <FieldFilter
              key={`${tableKey}-filter`}
              fields={filterField}
              onChange={handleFilterChange}
              condition={filterCondition}
              historyStorageKey={`${tableKey}-history`}
              extra={
                <Space size="small">
                  <Button
                    type="primary"
                    icon={<ReloadOutlined />}
                    loading={loading}
                    onClick={queryDataSources}
                  >
                    刷新
                  </Button>
                  <Button icon={<ReloadOutlined />} onClick={handleResetFilter} loading={loading}>
                    重置
                  </Button>
                  <ExportFile
                    loading={loading}
                    totalNum={totalElements?.total || 0}
                    queryFn={async (params: any) => {
                      const { fileType, ...restParams } = params;
                      console.log({ fileType: fileType, ...restParams, ...exportParams });
                      window.open(
                        `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metadata/protocol-rtp-logs/as-export?${stringify(
                          { fileType: fileType, ...restParams, ...exportParams },
                        )}`,
                      );
                    }}
                  />
                </Space>
              }
            />
          }
          extraFooter={
            <CustomPagination
              currentPage={pagination.page}
              pageSize={pagination.pageSize}
              total={totalElements.total}
              onChange={function (currentPage: number, pageSize: number): void {
                setPagination({
                  page: currentPage,
                  pageSize,
                });
              }}
            />
          }
        />
      </div>
    </>
  );
}
