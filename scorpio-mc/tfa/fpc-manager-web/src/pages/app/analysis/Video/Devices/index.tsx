import EnhancedTable from '@/components/EnhancedTable';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecord/Record';
import { ReloadOutlined } from '@ant-design/icons';
import type { TablePaginationConfig } from 'antd';
import { Button, Divider, Space } from 'antd';
import { useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ESortDirection } from '@/pages/app/analysis/typings';
import { VideoTabsContext } from '../components/VideoEditTabs';
import { openNewVideoTab } from '../components/VideoEditTabs/constant';
import type { IIpDevices } from '../typings';
import { EVideoTabType } from '../typings';
import { allDeviceColumns } from './constant';
import { VideoContext } from '../components/NetworkTimeLayout';
import type {
  EFieldOperandType,
  EFieldType,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import { getTablePaginationDefaultSettings, PAGE_DEFAULT_SIZE } from '@/common/app';
import { snakeCase } from 'lodash';
import CustomPagination from '@/components/CustomPagination';
import type { ITableQueryParams } from '../services';
import { queryIpDevices } from '../services';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import { getLinkUrl, isIpv4, jumpNewPage } from '@/utils/utils';
import { ETimeType } from '@/components/GlobalTimeSelector';

/** 表格操作列的 dataindex */
const COLUMN_ACTION_KEY = 'action';
const tableKey = 'npmd-rtp-flow-device-table';
const RTP_FLOW_RECORD_EXCLUDE_COLS = ['rtpLossRate'];
const getColumnParams = getColumnParamsFunc(RTP_FLOW_RECORD_EXCLUDE_COLS);
function getColumns(columns: string[] | undefined) {
  return getColumnParams({
    cols: columns,
    tableKey: tableKey,
  });
}

export default function Devices() {
  /** --------------------------- 上下文 --------------------------- */
  /** VideoTabsContext */
  const [state, videoDispatch] = useContext(VideoTabsContext);
  /** 网络时间选择器上下文 */
  const { network, globalSelectedTime } = useContext(VideoContext)!;

  /** --------------------------- 表格相关 --------------------------- */
  /** 排序字段 */
  const [sortProperty, setSortProperty] = useState<string>('start_time');
  /** 排序方向 */
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  /* 表格变化 */
  const handleTableChange = (pagination: TablePaginationConfig, filters: any, sorter: any) => {
    if (sorter.field !== sortProperty) {
      setSortProperty(snakeCase(sorter.field));
      setSortDirection(ESortDirection.DESC);
    } else {
      setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
    }
  };

  /** --------------------------- 标识相关 ---------------------------*/
  /** 加载标志 */
  const [loading, setLoading] = useState<boolean>(true);

  /** --------------------------- 过滤相关 ---------------------------*/
  /** 过滤条件 */
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>([]);

  /** 过滤列 */
  const filterField = useMemo(() => {
    return allDeviceColumns
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
  /** 分页信息 */
  const [pagination, setPagination] = useState<{ page: number; pageSize: number }>({
    page: 1,
    pageSize: getTablePaginationDefaultSettings().pageSize! || PAGE_DEFAULT_SIZE,
  });

  const [totalElements, setTotalElements] = useState<number>(0);

  /** --------------------------- 时间相关 --------------------------- */
  const startTime = useMemo(() => {
    return globalSelectedTime?.startTime;
  }, [globalSelectedTime]);

  const endTime = useMemo(() => {
    return globalSelectedTime?.endTime;
  }, [globalSelectedTime?.endTime]);

  /** --------------------------- 列相关 --------------------------- */
  /** 操作列 */
  /** 所有的流日志字段 */
  const actionCol: IFlowRecordColumnProps<IIpDevices> = useMemo(() => {
    return {
      title: '操作',
      dataIndex: COLUMN_ACTION_KEY,
      width: 240,
      show: true,
      align: 'center',
      fixed: 'right',
      disabled: true,
      render: (text: any, record: IIpDevices) => {
        return (
          <>
            <span
              className="link"
              onClick={() => {
                openNewVideoTab(
                  state,
                  videoDispatch,
                  EVideoTabType.IP_GRAPH,
                  { record },
                  `${record?.deviceIp}_访问关系`,
                );
              }}
            >
              访问关系
            </span>
            <Divider type="vertical" />
            <span
              className="link"
              onClick={() => {
                const deviceIp = record?.deviceIp;
                const deviceCode = record?.deviceCode;
                openNewVideoTab(
                  state,
                  videoDispatch,
                  EVideoTabType.RTP_FLOW_LIST,
                  {
                    filter: [
                      {
                        group: [
                          {
                            field: 'src_ip',
                            operator: EFilterOperatorTypes.EQ,
                            operand: deviceIp,
                          },
                          {
                            field: 'dest_ip',
                            operator: EFilterOperatorTypes.EQ,
                            operand: deviceIp,
                          },
                        ],
                        operator: EFilterGroupOperatorTypes.OR,
                      },
                      {
                        group: [
                          {
                            field: 'from',
                            operator: EFilterOperatorTypes.EQ,
                            operand: deviceCode,
                          },
                          {
                            field: 'to',
                            operator: EFilterOperatorTypes.EQ,
                            operand: deviceCode,
                          },
                        ],
                        operator: EFilterGroupOperatorTypes.OR,
                      },
                    ],
                  },
                  `${record?.deviceIp}_RTP流分析`,
                );
              }}
            >
              RTP流分析
            </span>
            <Divider type="vertical" />
            <span
              className="link"
              onClick={() => {
                const deviceIp = record?.deviceIp;

                const deviceIpIsV4 = deviceIp && isIpv4(deviceIp);

                const filterParam: (IFilter | IFilterGroup)[] = [];
                filterParam.push(
                  {
                    group: [
                      {
                        field: deviceIpIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                        operator: EFilterOperatorTypes.EQ,
                        operand: deviceIp,
                      },
                      {
                        field: deviceIpIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                        operator: EFilterOperatorTypes.EQ,
                        operand: deviceIp,
                      },
                    ],
                    operator: EFilterGroupOperatorTypes.OR,
                  } as IFilterGroup,
                  {
                    field: 'l7_protocol_id',
                    operator: EFilterOperatorTypes.EQ,
                    operand: '292',
                  },
                );
                jumpNewPage(
                  getLinkUrl(
                    `/analysis/trace/flow-record?&filter=${encodeURIComponent(
                      JSON.stringify(filterParam),
                    )}&from=${new Date(globalSelectedTime!.originStartTime).valueOf()}&to=${new Date(
                      globalSelectedTime!.originEndTime,
                    ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                  ),
                );
              }}
            >
              会话详单
            </span>
          </>
        );
      },
    };
  }, [globalSelectedTime, state, videoDispatch]);

  /** 展示列 */
  const showColumns: IFlowRecordColumnProps<IIpDevices>[] = useMemo(() => {
    return [...allDeviceColumns, actionCol]
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
    const filterSpl = filterCondition2Spl(filterCondition, filterField);
    return `${filterSpl} | gentimes report_time start="${globalSelectedTime?.startTime}" end="${globalSelectedTime?.endTime}"`;
  }, [filterCondition, filterField, globalSelectedTime?.endTime, globalSelectedTime?.startTime]);

  /** 查询参数 */
  const queryParams = useMemo(() => {
    const params = {
      sortProperty,
      sortDirection,
      startTime,
      endTime,
      dsl,
      page: pagination.page - 1,
      pageSize: pagination.pageSize,
    };
    return params;
  }, [dsl, endTime, pagination.page, pagination.pageSize, sortDirection, sortProperty, startTime]);

  /** 表格数据 */
  const [dataSource, setDataSource] = useState<IIpDevices[]>([]);

  /** 查询表格数据 */
  const queryDataSources = useCallback(async () => {
    if (!network?.id) {
      return;
    }
    setLoading(true);
    const { success, result } = await queryIpDevices({
      ...queryParams,
      networkId: network?.id,
      columns: getColumns(columns),
    } as ITableQueryParams);
    if (success) {
      setDataSource(result?.content || []);
      setTotalElements(result?.totalElements || 0);
    }
    setLoading(false);
  }, [columns, network?.id, queryParams]);

  useEffect(() => {
    queryDataSources();
  }, [queryDataSources]);

  return (
    <>
      {/* 表格 */}
      <div id="flowRecordTable">
        <EnhancedTable<IIpDevices>
          sortProperty={sortProperty}
          sortDirection={`${sortDirection}end` as any}
          tableKey={tableKey}
          rowKey="id"
          loading={loading}
          columns={showColumns}
          dataSource={dataSource}
          pagination={false}
          onChange={handleTableChange}
          onColumnChange={setColumns}
          extraTool={
            <>
              <FieldFilter
                key={`${tableKey}-filter`}
                fields={filterField}
                onChange={handleFilterChange}
                condition={filterCondition}
                simple
                historyStorageKey={`${tableKey}-history`}
                extra={
                  <Space size="small">
                    <Button
                      type="primary"
                      icon={<ReloadOutlined />}
                      loading={loading}
                      onClick={() => {
                        queryDataSources();
                      }}
                    >
                      刷新
                    </Button>
                    <Button icon={<ReloadOutlined />} onClick={handleResetFilter} loading={loading}>
                      重置
                    </Button>
                  </Space>
                }
              />
            </>
          }
          extraFooter={
            <CustomPagination
              currentPage={pagination.page}
              pageSize={pagination.pageSize}
              total={totalElements}
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
