import type {
  IEnumValue,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import type { IFlowRecord, IUrlParams } from '../typing';
import type { AppModelState } from '@/models/app/index';
import type { ConnectState } from '@/models/connect';
import type { EModelAlias } from '@/pages/app/analysis/components/fieldsManager';
import type { IFlowRecordColumnProps } from '../../appliance/FlowRecord/Record';
import type { Dispatch } from 'umi';
import type { ETimeType } from '@/components/GlobalTimeSelector';
import FieldFilter, { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import ajax from '@/utils/frame/ajax';
import EnhancedTable from '@/components/EnhancedTable';
import CustomPagination from '@/components/CustomPagination';
import numeral from 'numeral';
import moment from 'moment';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { snakeCase } from '@/utils/utils';
import { Tooltip, Spin } from 'antd';
import { stringify } from 'qs';
import { useEffect, useCallback, useState, useMemo } from 'react';
import { connect, useParams } from 'umi';
import { ESortDirection } from '../typing';
import { bytesToSize } from '@/utils/utils';
import {
  EFieldOperandType,
  EFieldType,
  EFilterGroupOperatorTypes,
} from '@/components/FieldFilter/typings';
import {
  EFieldEnumValueSource,
  EFormatterType,
  getEnumValueFromModelNext,
} from '@/pages/app/analysis/components/fieldsManager';
import { replaceFilterGroup, isIpv4 } from '../utils/filterTools';
import { fieldsMapping } from '../typing';
import { v1 as uuidv1 } from 'uuid';
import { querySessionTotalElement } from '../service';
import { EDrilldown } from '../typing';

// 分页数据接口
interface IPageProps {
  currentPage: number;
  pageSize: number;
  total: number;
}

// 会话详单组件参数接口
interface IFlowRecordParams extends AppModelState {
  location: {
    query: {
      filter: string;
      timeType: ETimeType;
      from: string;
      to: string;
    };
  };
  dispatch: Dispatch;
}

// 流量分析分析下各子类别支持的过滤条件
const allSelectValue = [
  'duration',
  'ingest_packets',
  'transmit_packets',
  'total_packets',
  'ingest_bytes',
  'transmit_bytes',
  'total_bytes',
  'src_ip',
  'dest_ip',
  'port',
  'src_port',
  'dest_port',
  'protocol',
  'ip_address',
];

// 获取FilterField
function getFilterFields() {
  const fieldList: {
    enumValue?: IEnumValue[] | undefined;
    title: string;
    dataIndex: string;
    operandType: EFieldOperandType;
    type?: EFieldType;
  }[] = [];
  allSelectValue.forEach((field: string) => {
    const { formatterType, name, filterOperandType, filterFieldType, enumSource, enumValue } =
      fieldsMapping[field];
    const isEnum = formatterType === EFormatterType.ENUM;
    const enumValueList: IEnumValue[] = [];
    if (isEnum) {
      if (enumSource === EFieldEnumValueSource.LOCAL) {
        enumValueList.push(...(enumValue as IEnumValue[]));
      } else {
        const modelData = getEnumValueFromModelNext(enumValue as EModelAlias);
        if (modelData) {
          enumValueList.push(...modelData.list);
        }
      }
    }

    fieldList.push({
      title: name,
      dataIndex: snakeCase(field),
      operandType: filterOperandType as EFieldOperandType,
      type: filterFieldType as EFieldType,
      ...(isEnum
        ? {
            enumValue: enumValueList,
          }
        : {}),
    });
  });
  return fieldList;
}

// filterField
const filterField = getFilterFields();

const ConvDetails = ({ location, globalSelectedTime }: IFlowRecordParams) => {
  // 动态路由参数
  const urlParams = useParams<IUrlParams>();
  // 分页信息
  const [pageProps, setPageProps] = useState<IPageProps>({
    currentPage: 1,
    pageSize: 10,
    total: 0,
  });
  // queryid
  const [queryId] = useState<string>(uuidv1());

  /** 加载标记 */
  const [queryLoading, setQueryLoading] = useState<boolean>(false);

  /** 排序相关 */
  const [sortProperty, setSortProperty] = useState<string>('total_bytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.ASC);

  /** 过滤相关 */
  // 过滤条件
  const [filterCondition, setFilterCondition] = useState<(IFilterGroup | IFilter)[]>([]);
  // 下钻标记
  const drilldown = useMemo(() => {
    return filterCondition.length > 0 ? EDrilldown.drilldown : EDrilldown.undrilldown;
  }, [filterCondition]);

  // 替换过滤器ip名称
  const replaceFilterIpName = (item: IFilter) => {
    if (item.field === 'src_ip' || item.field === 'dest_ip') {
      return {
        ...item,
        field: `${item.field}${isIpv4(`${item.operand}`) ? 'v4' : 'v6'}`,
      };
    } else if (item.field === 'ip_address') {
      const { operator, operand } = item;
      return {
        group: [
          {
            field: `src_ip${isIpv4(`${operand}`) ? 'v4' : 'v6'}`,
            operand,
            operator,
          },
          {
            field: `dest_ip${isIpv4(`${operand}`) ? 'v4' : 'v6'}`,
            operand,
            operator,
          },
        ],
        operator: operator === '=' ? EFilterGroupOperatorTypes.OR : EFilterGroupOperatorTypes.AND,
      };
    } else if (item.field === 'port') {
      const { operator, operand } = item;
      return {
        group: [
          {
            field: `src_port`,
            operand,
            operator,
          },
          {
            field: `dest_port`,
            operand,
            operator,
          },
        ],
        operator: operator === '!=' ? EFilterGroupOperatorTypes.AND : EFilterGroupOperatorTypes.OR,
      };
    }
    return item;
  };

  // dsl查询语句
  const dsl = useMemo(() => {
    return `${filterCondition2Spl(
      filterCondition.map((item: IFilter | IFilterGroup) => {
        if ((item as IFilterGroup).group !== undefined) {
          // 高级过滤
          return replaceFilterGroup(item as IFilterGroup, replaceFilterIpName);
        } else {
          // 简单过滤
          return replaceFilterIpName(item as IFilter);
        }
      }),
      [
        ...filterField,
        {
          dataIndex: 'src_ipv4',
          operandType: EFieldOperandType.IP,
          title: '源IPv4地址',
          type: EFieldType.IPV4,
        },
        {
          dataIndex: 'dest_ipv4',
          operandType: EFieldOperandType.IP,
          title: '目的IPv4地址',
          type: EFieldType.IPV4,
        },
        {
          dataIndex: 'src_ipv6',
          operandType: EFieldOperandType.IP,
          title: '源IPv6地址',
          type: EFieldType.IPV6,
        },
        {
          dataIndex: 'dest_ipv6',
          operandType: EFieldOperandType.IP,
          title: '目的IPv6地址',
          type: EFieldType.IPV6,
        },
        {
          dataIndex: 'dest_port',
          operandType: EFieldOperandType.PORT,
          title: '目的端口',
        },
        {
          dataIndex: 'src_port',
          operandType: EFieldOperandType.PORT,
          title: '源端口',
        },
      ],
    )}| gentimes report_time start="${globalSelectedTime.originStartTime}" end="${
      globalSelectedTime.originEndTime
    }"`;
  }, [filterCondition, globalSelectedTime.originEndTime, globalSelectedTime.originStartTime]);

  // 更新分页信息，查询数据总量
  useEffect(() => {
    querySessionTotalElement({
      queryId,
      startTime: globalSelectedTime.originStartTime,
      endTime: globalSelectedTime.originEndTime,
      deviceName: urlParams.deviceName,
      netifNo: urlParams.netifNo,
      dsl: drilldown === EDrilldown.drilldown ? dsl : undefined,
      drilldown,
    }).then((res) => {
      if (!res.success) {
        return;
      }
      setPageProps((item) => ({
        ...item,
        total: res.result.total,
      }));
    });
  }, [
    urlParams.deviceName,
    urlParams.netifNo,
    urlParams,
    globalSelectedTime,
    queryId,
    dsl,
    drilldown,
  ]);

  /** 过滤器回调函数 */
  // 过滤条件变化回调
  const handleFilterChange = (newFilter: IFilterCondition) => {
    setFilterCondition(newFilter);
  };

  // 增加过滤条件
  const addConditionToFilter = (condition: IFilterCondition) => {
    setFilterCondition([...filterCondition, ...(condition as IFilter[])]);
  };

  /** 表格数据相关 */
  // 会话数据
  const [recordData, setRecordData] = useState<IFlowRecord[]>();
  // 表格列定义
  const tableColumns: IFlowRecordColumnProps<IFlowRecord>[] = [
    {
      title: '记录时间',
      dataIndex: 'report_time',
      sorter: true,
      align: 'center',
      ellipsis: true,
      width: 170,
      render: (ariseTime) => {
        return moment(ariseTime).tz('8').format('YYYY-MM-DD HH:mm:ss');
      },
    },
    {
      title: '开始时间',
      dataIndex: 'start_time',
      width: 170,
      align: 'center',
      ellipsis: true,
      sorter: true,
      render: (ariseTime) => moment(ariseTime).tz('8').format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '结束时间',
      dataIndex: 'end_time',
      width: 170,
      align: 'center',
      ellipsis: true,
      sorter: true,
      render: (ariseTime) => moment(ariseTime).tz('8').format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '源IP',
      dataIndex: 'src_ip',
      align: 'center',
      width: 120,
      render: (_, record) => {
        const ipInitiator = record.src_ip;
        if (!ipInitiator) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'src_ip'}
            label={
              <Tooltip placement="topLeft" title={ipInitiator}>
                {ipInitiator}
              </Tooltip>
            }
            operand={ipInitiator}
            operandType={EFieldOperandType.IPV4}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '目的IP',
      dataIndex: 'dest_ip',
      align: 'center',
      width: 120,
      render: (_, record) => {
        const ipInitiator = record.dest_ip;
        if (!ipInitiator) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'dest_ip'}
            label={
              <Tooltip placement="topLeft" title={ipInitiator}>
                {ipInitiator}
              </Tooltip>
            }
            operand={ipInitiator}
            operandType={EFieldOperandType.IPV4}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '源端口',
      dataIndex: 'src_port',
      align: 'center',
      width: 90,
      render: (_, record) => {
        const portInitiator = record.src_port;
        if (!portInitiator && portInitiator !== 0) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'src_port'}
            label={
              <Tooltip placement="topLeft" title={portInitiator}>
                {portInitiator}
              </Tooltip>
            }
            operand={portInitiator}
            operandType={EFieldOperandType.PORT}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '目的端口',
      dataIndex: 'dest_port',
      align: 'center',
      width: 90,
      render: (_, record) => {
        const portResponder = record.dest_port;
        if (!portResponder && portResponder !== 0) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'dest_port'}
            label={
              <Tooltip placement="topLeft" title={portResponder}>
                {portResponder}
              </Tooltip>
            }
            operand={portResponder}
            operandType={EFieldOperandType.PORT}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '协议',
      dataIndex: 'protocol',
      align: 'center',
      width: 100,
      render: (_, record) => {
        const protocolInitiator = record.protocol;
        if (!protocolInitiator) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'protocol'}
            label={
              <Tooltip placement="topLeft" title={protocolInitiator}>
                {protocolInitiator}
              </Tooltip>
            }
            operand={protocolInitiator}
            operandType={EFieldOperandType.STRING}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '总字节数',
      dataIndex: 'total_bytes',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const totalBytes = record.total_bytes;
        if (totalBytes === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'total_bytes'}
            label={
              <Tooltip placement="topLeft" title={totalBytes ? bytesToSize(totalBytes) : 0}>
                {totalBytes ? bytesToSize(totalBytes) : 0}
              </Tooltip>
            }
            operand={totalBytes}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '总包数',
      dataIndex: 'total_packets',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const packetResponser = record.total_packets;
        if (packetResponser === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'total_packets'}
            label={
              <Tooltip placement="topLeft" title={numeral(packetResponser).format('0,0')}>
                {numeral(packetResponser).format('0,0')}
              </Tooltip>
            }
            operand={packetResponser}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '发送字节数',
      dataIndex: 'transmit_bytes',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const BytesInitiator = record.transmit_bytes;
        if (BytesInitiator === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'transmit_bytes'}
            label={
              <Tooltip placement="topLeft" title={BytesInitiator ? bytesToSize(BytesInitiator) : 0}>
                {BytesInitiator ? bytesToSize(BytesInitiator) : 0}
              </Tooltip>
            }
            operand={BytesInitiator}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '发送包数',
      dataIndex: 'transmit_packets',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const packetResponser = record.transmit_packets;
        if (packetResponser === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'transmit_packets'}
            label={
              <Tooltip placement="topLeft" title={numeral(packetResponser).format('0,0')}>
                {numeral(packetResponser).format('0,0')}
              </Tooltip>
            }
            operand={packetResponser}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '接收字节数',
      dataIndex: 'ingest_bytes',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const BytesInitiator = record.ingest_bytes;
        if (BytesInitiator === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'ingest_bytes'}
            label={
              <Tooltip placement="topLeft" title={BytesInitiator ? bytesToSize(BytesInitiator) : 0}>
                {BytesInitiator ? bytesToSize(BytesInitiator) : 0}
              </Tooltip>
            }
            operand={BytesInitiator}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '接收包数',
      dataIndex: 'ingest_packets',
      align: 'center',
      sorter: true,
      width: 100,
      render: (_, record) => {
        const packetResponser = record.ingest_packets;
        if (packetResponser === undefined) {
          return null;
        }

        return (
          <FilterBubble
            dataIndex={'ingest_packets'}
            label={
              <Tooltip placement="topLeft" title={numeral(packetResponser).format('0,0')}>
                {numeral(packetResponser).format('0,0')}
              </Tooltip>
            }
            operand={packetResponser}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '标志位',
      dataIndex: 'tcp_flag',
      align: 'center',
      width: 100,
    },
    {
      title: 'DSCP',
      dataIndex: 'dscp_flag',
      align: 'center',
      width: 100,
    },
    {
      title: '持续时间(ms)',
      dataIndex: 'duration',
      align: 'center',
      sorter: true,
      width: 100,
      ellipsis: true,
      show: true,
      searchable: true,
      operandType: EFieldOperandType.NUMBER,
      render: (_, record) => {
        const timeInitiator = record.duration;
        if (timeInitiator === undefined) {
          return undefined;
        }

        return (
          <FilterBubble
            dataIndex={'duration'}
            label={
              <Tooltip placement="topLeft" title={timeInitiator}>
                {timeInitiator}
              </Tooltip>
            }
            operand={timeInitiator}
            operandType={EFieldOperandType.NUMBER}
            onClick={(newFilter) => {
              if (addConditionToFilter) {
                addConditionToFilter([newFilter]);
              }
            }}
          />
        );
      },
    },
    {
      title: '流设备',
      dataIndex: 'device_name',
      align: 'center',
      width: 100,
    },
    {
      title: '入接口',
      dataIndex: 'in_netif',
      align: 'center',
      width: 100,
    },
    {
      title: '出接口',
      dataIndex: 'out_netif',
      align: 'center',
      width: 100,
    },
  ];
  // 请求数据
  const updateTableData = useCallback(() => {
    setQueryLoading(true);
    const newParams = {
      queryId,
      sortProperty,
      sortDirection,
      startTime: globalSelectedTime.startTime,
      endTime: globalSelectedTime.endTime,
      deviceName: urlParams.deviceName,
      netifNo: urlParams.netifNo,
      pageSize: pageProps.pageSize,
      pageNumber: pageProps.currentPage - 1,
      dsl: drilldown === EDrilldown.drilldown ? dsl : undefined,
      drilldown,
    } as any;
    ajax(`${API_VERSION_PRODUCT_V1}/metric/netflows/session-records?${stringify(newParams)}`).then(
      ({ success, result }) => {
        if (success) {
          setRecordData(result.content);
        }
        setQueryLoading(false);
      },
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    pageProps.currentPage,
    pageProps.pageSize,
    sortDirection,
    sortProperty,
    globalSelectedTime,
    urlParams,
    queryId,
    dsl,
  ]);

  // 一开始加载页面，添加过滤器
  useEffect(() => {
    const { filter } = location.query;
    // 初始化过滤条件
    if (filter) {
      setFilterCondition([JSON.parse(filter), ...filterCondition]);
    }
  }, []);

  // 时间变动，重新获取数据
  useEffect(() => {
    updateTableData();
  }, [
    globalSelectedTime,
    urlParams,
    pageProps.currentPage,
    pageProps.pageSize,
    updateTableData,
    dsl,
    sortDirection,
    sortProperty,
    location,
  ]);

  return (
    <div>
      <div id="netflow-flow-record-table">
        <EnhancedTable<IFlowRecord>
          tableKey="netflow-flow-record-table"
          rowKey={uuidv1()}
          loading={queryLoading}
          columns={tableColumns}
          dataSource={recordData}
          extraTool={
            <FieldFilter
              fields={filterField}
              onChange={handleFilterChange}
              condition={filterCondition}
              historyStorageKey="tfa-netflow-flow-record-filter-history"
              simple={false}
            />
          }
          onChange={(_, filter, sorter: any) => {
            if (sorter.order === `${ESortDirection.ASC}end`) {
              setSortDirection(ESortDirection.ASC);
            } else if (sorter.order === `${ESortDirection.DESC}end`) {
              setSortDirection(ESortDirection.DESC);
            }
            setSortProperty(snakeCase(sorter.field));
          }}
          pagination={false}
          extraFooter={
            <Spin spinning={false} size="small">
              <CustomPagination
                {...pageProps}
                onChange={(currentPage: number, pageSize: number) => {
                  setPageProps((props) => ({
                    ...props,
                    currentPage,
                    pageSize,
                  }));
                }}
              />
            </Spin>
          }
        />
      </div>
    </div>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
}))(ConvDetails);
