import { EMetricApiType } from '@/common/api/analysis';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter, IFilterCondition, IFilterGroup } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type {
  IFieldProperty,
  TFlowAnalysisType,
} from '@/pages/app/analysis/components/fieldsManager';
import {
  EFormatterType,
  fieldFormatterFuncMap,
  fieldsMapping,
  flowCommonFields,
  flowSubFields,
  SortedTypes,
} from '@/pages/app/analysis/components/fieldsManager';
import { EFileType } from '@/pages/app/appliance/components/ExportFile';
import { cancelQueryTask } from '@/pages/app/appliance/FlowRecord/service';
import { getOriginTime, packetSearchableFields, packetUrl } from '@/pages/app/appliance/Packet';
import { abortAjax, camelCase, getLinkUrl, isIpv4, jumpNewPage, snakeCase } from '@/utils/utils';
import {
  CloseSquareOutlined,
  ExportOutlined,
  ReloadOutlined,
  StopOutlined,
} from '@ant-design/icons';
import type { TableColumnProps } from 'antd';
import { Button, Card, Divider, Dropdown, Menu, message, Select, Space, Tooltip } from 'antd';
import type { TablePaginationConfig } from 'antd/lib/table/interface';
import { stringify } from 'qs';
import type { ReactNode } from 'react';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import { v4 as uuidv4 } from 'uuid';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
import type { IFlowAnalysisData, IFlowQueryParams, IUriParams } from '../../typings';
import { ESourceType } from '../../typings';
import type { TrendChartData } from '../AnalysisChart';
import AnalysisChart from '../AnalysisChart';
import IpDirlldownMenu from '../IpDirlldownMenu';
import {
  computedDrilldownFlag,
  FilterContext,
  filterFields,
  flowAnalysisFilterToFlowRecordFilter,
} from '../PageLayoutWithFilter';
import styles from './index.less';
// import moment from 'moment';

enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

export const tableTop = [10, 100, 300, 500, 1000];

// 固定的列, 每个分析下的key值，需要固定在表格的左侧
export const fixedColumns: Record<TFlowAnalysisType, string[]> = {
  [EMetricApiType.location]: ['countryId', 'provinceId', 'cityId', 'totalBytes'],
  [EMetricApiType.application]: ['applicationId', 'categoryId', 'subcategoryId'],
  [EMetricApiType.protocol]: ['l7ProtocolId'],
  [EMetricApiType.port]: ['port', 'ipProtocol'],
  [EMetricApiType.hostGroup]: ['hostgroupId'],
  [EMetricApiType.macAddress]: ['macAddress', 'ethernetType'],
  [EMetricApiType.ipAddress]: [
    'ipAddress',
    // 目前IP统计下展示的Mac地址都是ALL，展示出来无意义
    // 'macAddress',
    'ipLocality',
  ],
  [EMetricApiType.ipConversation]: ['ipAAddress', 'ipBAddress'],
};

const defColArr = [
  'totalBytes',
  'downstreamBytes',
  'upstreamBytes',
  'establishedSessions',
  'operate',
  'action',
];

// 默认显示列
const defaultShowColumnsForFlow: Record<TFlowAnalysisType, string[]> = {
  [EMetricApiType.location]: [...fixedColumns[EMetricApiType.location], ...defColArr],
  [EMetricApiType.application]: [...fixedColumns[EMetricApiType.application], ...defColArr],
  [EMetricApiType.protocol]: [...fixedColumns[EMetricApiType.protocol], ...defColArr],
  [EMetricApiType.port]: [...fixedColumns[EMetricApiType.port], ...defColArr],
  [EMetricApiType.hostGroup]: [...fixedColumns[EMetricApiType.hostGroup], ...defColArr],
  [EMetricApiType.macAddress]: [...fixedColumns[EMetricApiType.macAddress], ...defColArr],
  [EMetricApiType.ipAddress]: [...fixedColumns[EMetricApiType.ipAddress], ...defColArr],
  [EMetricApiType.ipConversation]: [...fixedColumns[EMetricApiType.ipConversation], ...defColArr],
};

// 每个表格中，需要提前的列
export const keyColumns: Record<TFlowAnalysisType, string[]> = {
  [EMetricApiType.location]: [],
  [EMetricApiType.application]: [],
  [EMetricApiType.protocol]: [],
  [EMetricApiType.port]: [],
  [EMetricApiType.hostGroup]: [],
  [EMetricApiType.macAddress]: [],
  [EMetricApiType.ipAddress]: [],
  [EMetricApiType.ipConversation]: [
    'totalBytes',
    'totalPackets',
    'downstreamBytes',
    'upstreamBytes',
    'downstreamPackets',
    'upstreamPackets',
  ],
};

export const operatorEnumList = [
  {
    text: '等于',
    value: EFilterOperatorTypes.EQ,
  },
  {
    text: '不等于',
    value: EFilterOperatorTypes.NEQ,
  },
];

// 时间补点
// TODO：时间补点放在这里是否合适，是否可以抽成公共函数
export const completeTimePoint = (
  seriesData: TrendChartData,
  startTime: string,
  endTime: string,
  interval: number = 60,
) => {
  const startTimestamp = new Date(startTime).getTime();
  const endTimestamp = new Date(endTime).getTime();

  // const now = moment().valueOf();

  const stepCount = (endTimestamp - startTimestamp) / 1000 / interval;

  const res: TrendChartData = [];
  for (let i = 0; i < seriesData.length; i += 1) {
    const seriesDataTimestamp = new Date(seriesData[i][0]).getTime();
    const timeIndex = Math.floor((seriesDataTimestamp - startTimestamp) / 1000 / interval);
    if (timeIndex >= 0 && timeIndex <= stepCount) {
      res[timeIndex] = seriesData[i];
    }
  }
  for (let i = 1; i < stepCount; i += 1) {
    if (!res[i]) {
      res[i] = [startTimestamp + interval * i * 1000, 0];
    }
  }

  // 判断是不是最近xxx， 如果是，则
  // if (now - endTimestamp < interval * 1000) {
  //   return res;
  // }

  // if(res[stepCount]) {
  //   res[stepCount] = [startTimestamp + interval * stepCount * 1000, 0];
  // }

  res.shift();

  return res;
};

/** 删除重复的 */
const removeRepeatConditon = (filter: IFilter[], target: IFilter) => {
  const repeatIndex = filter.map((condition, index) => {
    if (
      condition.field === target.field &&
      condition.operator === target.operator &&
      String(condition.operand) === String(target.operand)
    ) {
      return index;
    }
    return -1;
  });
  repeatIndex.forEach((index) => {
    if (index !== -1) {
      filter.splice(index, 1);
    }
  });
};

interface IActionLinkParams {
  /** 类型 */
  type: TFlowAnalysisType;
  /** 表格中每一行的记录数据 */
  record: IFlowAnalysisData;
  /** 已经存在的Filter条件 */
  filter: IFilter[];
  /** 网络ID */
  networkId: string;
  /** 业务ID */
  serviceId?: string;
  pcapFileId?: string;
}
/**
 * 生成详单跳转按钮
 */
export const getFlowRecordLink = ({
  type,
  record,
  filter,
  serviceId,
  networkId,
  pcapFileId,
}: IActionLinkParams) => {
  // 拼接全部的 filter
  const fullFilter: IFilterCondition = [];
  const copyFilter = [...filter];
  // 地区
  if (type === EMetricApiType.location) {
    const { countryId = '', provinceId = '' } = record;

    if (provinceId) {
      const targetFilter: IFilter = {
        field: 'province_id',
        operator: EFilterOperatorTypes.EQ,
        operand: provinceId,
      };
      removeRepeatConditon(copyFilter, targetFilter);
      fullFilter.push({
        field: 'province_id',
        operator: EFilterOperatorTypes.EQ,
        operand: String(provinceId),
      });
    } else {
      const targetFilter: IFilter = {
        field: 'country_id',
        operator: EFilterOperatorTypes.EQ,
        operand: countryId,
      };
      removeRepeatConditon(copyFilter, targetFilter);
      fullFilter.push({
        field: 'country_id',
        operator: EFilterOperatorTypes.EQ,
        operand: String(countryId),
      });
    }
  } else if (type === EMetricApiType.application) {
    const { applicationId } = record;
    const targetFilter: IFilter = {
      field: 'application_id',
      operator: EFilterOperatorTypes.EQ,
      operand: String(applicationId),
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push(targetFilter);
  } else if (type === EMetricApiType.protocol) {
    const { l7ProtocolId } = record;
    const targetFilter: IFilter = {
      field: 'l7_protocol_id',
      operator: EFilterOperatorTypes.EQ,
      operand: String(l7ProtocolId),
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push(targetFilter);
  } else if (type === EMetricApiType.port) {
    const { port, ipProtocol } = record;
    const targetFilter: IFilter = {
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: port,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: port,
    });
    if (ipProtocol !== 'ALL') {
      fullFilter.push({
        field: 'ip_protocol',
        operator: EFilterOperatorTypes.EQ,
        operand: ipProtocol,
      });
    }
  } else if (type === EMetricApiType.hostGroup) {
    const { hostgroupId } = record;
    const targetFilter: IFilter = {
      field: 'hostgroup_id',
      operator: EFilterOperatorTypes.EQ,
      operand: hostgroupId,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      field: 'hostgroup_id',
      operator: EFilterOperatorTypes.EQ,
      operand: hostgroupId,
    });
  } else if (type === EMetricApiType.macAddress) {
    const { macAddress } = record;
    const targetFilter: IFilter = {
      field: 'mac_address',
      operator: EFilterOperatorTypes.EQ,
      operand: macAddress,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    fullFilter.push({
      field: 'ethernet',
      operator: EFilterOperatorTypes.EQ,
      operand: macAddress,
    });
  } else if (type === EMetricApiType.ipAddress) {
    const { ipAddress } = record;
    const targetFilter: IFilter = {
      field: 'ip_address',
      operator: EFilterOperatorTypes.EQ,
      operand: ipAddress,
    };
    removeRepeatConditon(copyFilter, targetFilter);
    const ipAddressIsV4 = isIpv4(ipAddress as string);
    fullFilter.push({
      field: ipAddressIsV4 ? 'ipv4' : 'ipv6',
      operator: EFilterOperatorTypes.EQ,
      operand: ipAddress,
    });
  } else if (type === EMetricApiType.ipConversation) {
    const { ipAAddress, ipBAddress } = record;
    const aIsV4 = isIpv4(ipAAddress as string);
    const bIsV4 = isIpv4(ipBAddress as string);
    const condition1: IFilterGroup = {
      operator: EFilterGroupOperatorTypes.AND,
      group: [
        {
          field: aIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAAddress,
        },
        {
          field: bIsV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipBAddress,
        },
      ],
    };
    const condition2: IFilterGroup = {
      operator: EFilterGroupOperatorTypes.AND,
      group: [
        {
          field: bIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ipBAddress,
        },
        {
          field: aIsV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ipAAddress,
        },
      ],
    };
    fullFilter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [condition1, condition2],
    });
  }

  if (copyFilter.length > 0) {
    const result = flowAnalysisFilterToFlowRecordFilter(copyFilter);
    fullFilter.push(...result);
  }

  if (networkId) {
    fullFilter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId,
    });
  }

  if (serviceId) {
    fullFilter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId,
    });
  }

  let urlPrefix = `/analysis/trace`;
  if (pcapFileId) {
    urlPrefix = `/analysis/offline/${pcapFileId}`;
  }

  return getLinkUrl(
    `${urlPrefix}/flow-record?filter=${encodeURIComponent(JSON.stringify(fullFilter))}`,
  );
};

/** 生成数据包跳转按钮 */
export const getPacketLink = ({
  type,
  record,
  filter,
  networkId,
  serviceId,
}: IActionLinkParams) => {
  let copyFilter = [...filter];
  //  先排除非数据包的过滤条件
  copyFilter = copyFilter
    .filter((item) => packetSearchableFields.includes(camelCase(item.field)))
    .map((f) => {
      if (f.field === 'ip_protocol') {
        return {
          field: camelCase(f.field),
          operator: f.operator,
          operand: String(f.operand).toLocaleUpperCase() as any,
        };
      }
      return {
        field: camelCase(f.field),
        operator: f.operator,
        operand: f.operand,
      };
    });
  // IP 会话对单独处理，添加新的过滤条件
  if (type === EMetricApiType.ipConversation) {
    copyFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipAAddress,
    });
    copyFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipBAddress,
    });
  } else {
    fixedColumns[type].forEach((field) => {
      // 如果字段值有值，并且在数据包的过滤字段内，追加条件
      if (record[field] && packetSearchableFields.includes(field)) {
        // 过滤重复条件
        copyFilter = copyFilter.filter(
          (item) => !(item.field === field && item.operand === String(record[field])),
        );
        // 如果传输层协议为 ALL，跳转的时候不携带此参数
        if (field !== 'ipProtocol' || (field === 'ipProtocol' && record[field] !== 'ALL')) {
          copyFilter.push({
            field: camelCase(field),
            operator: EFilterOperatorTypes.EQ,
            operand: String(record[field]).toLocaleUpperCase(),
          });
        }
      }
    });
  }

  copyFilter.push({
    field: 'network_id',
    operator: EFilterOperatorTypes.EQ,
    operand: networkId,
  });

  if (serviceId) {
    copyFilter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId,
    });
  }

  return getLinkUrl(`${packetUrl}?filter=${encodeURIComponent(JSON.stringify(copyFilter))}`);
};

const getColumnParams = getColumnParamsFunc(['index', 'operate', 'action']);

type LocalTableColumnProps = IFlowAnalysisData & {
  index: number;
};

interface IFlowAnalysisProps {
  flowAnalysisType: TFlowAnalysisType;
  queryLoading: boolean | undefined;
  detailQueryLoading: boolean | undefined;
  // 枚举值转为显示的名称
  mapEnumFieldToName: (value: string, field: string) => string;
  // 表格中不显示的列
  excludeFields: string[];
  // 图表series的名称，业务含义是每个统计指标下的topX的名称
  getSeriesName: (record: IFlowAnalysisData) => string;
  overloadFieldsMapping?: Record<string, IFieldProperty>;

  dispatch: Dispatch;
  globalSelectedTime: Required<IGlobalTime>;
  flowTableData: Record<EMetricApiType, IFlowAnalysisData[]>;
  flowHistogramData: Record<EMetricApiType, IFlowAnalysisData[]>;
  flowDetailHistogramData: IFlowAnalysisData[];
  currentPcpInfo: IOfflinePcapData | null;
  beforeOldestPacketArea: any;
  currentFilterCondition?: IFilter[];
  currentNetworkId?: string;
  needHeight?: number;
}

const FlowAnalysis: React.FC<IFlowAnalysisProps> = (props) => {
  const {
    dispatch,
    globalSelectedTime,
    flowTableData,
    flowHistogramData,
    flowDetailHistogramData,
    flowAnalysisType,
    mapEnumFieldToName,
    excludeFields,
    getSeriesName,
    queryLoading,
    detailQueryLoading,
    overloadFieldsMapping,
    beforeOldestPacketArea,
    currentNetworkId,
    currentFilterCondition,
    needHeight,
  } = props;

  const { networkId: urlNetworkId, serviceId = '', pcapFileId }: IUriParams = useParams();
  const [networkId, setNetworkId] = useState(() => {
    if (currentNetworkId) {
      return currentNetworkId;
    }
    return urlNetworkId;
  });
  useEffect(() => {
    if (currentNetworkId) {
      return;
    }
    setNetworkId(urlNetworkId);
  }, [currentNetworkId, urlNetworkId]);

  const [sortProperty, setSortProperty] = useState<string>('totalBytes');
  const [sortDirection, setSortDirection] = useState<ESortDirection>(ESortDirection.DESC);
  const [selectedRow, setSelectedRow] = useState<LocalTableColumnProps | null>(null);
  const { filterCondition: baseFilter, addConditionToFilter: addFilterFn } =
    useContext(FilterContext);
  const [currentFilter, setCurrentFilter] = useState(() => {
    if (currentFilterCondition) {
      return currentFilterCondition;
    }
    return [];
  });
  const [filter, addConditionToFilter] = currentFilterCondition
    ? [currentFilter, setCurrentFilter]
    : [baseFilter, addFilterFn];
  const [showColumns, setShowColumns] = useState<string[]>();

  const [fixHeight] = useState(() => {
    if (needHeight) {
      return needHeight;
    }
    return 0;
  });

  const [tableQueryId, setTableQueryId] = useState<string | null>(null);
  const [chartQueryId, setChartQueryId] = useState<string | null>(null);
  // const [queryIds, setQueryIds] = useState<string[]>([]);
  const queryIds = useMemo<string[]>(() => {
    const res = [tableQueryId, chartQueryId].filter((id) => id) as string[];
    return res;
  }, [chartQueryId, tableQueryId]);
  const clearQueryIds = () => {
    setTableQueryId(null);
    setChartQueryId(null);
  };
  const queryIdsRef = useRef<string[]>([]);

  const [showCancelBtn, setShowCancelBtn] = useState(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);

  useEffect(() => {
    queryIdsRef.current = queryIds;
  }, [queryIds]);

  // ======维持查询心跳 S=====
  const pingQueryTask = useCallback(() => {
    // 没有 ID 时不 ping
    if (queryIds.length === 0) {
      return;
    }
    dispatch({
      type: 'flowRecordModel/pingQueryTask',
      payload: {
        queryId: queryIds.join(','),
      },
    }).then((success: boolean) => {
      if (!success) {
        message.destroy();
      }
    });
  }, [dispatch, queryIds]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    let timer: any;
    if (queryIds.length > 0) {
      timer = window.setInterval(() => {
        setShowCancelBtn(true);
        pingQueryTask();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    window.clearTimeout(timer);
    setShowCancelBtn(false);
    return undefined;
  }, [pingQueryTask, queryIds]);

  // ====== 停止查询按钮 =====

  /**
   * 停止查询任务
   * @params silence 当为 true 时不触发任何提示
   */
  const cancelTask = async (silent = false) => {
    const queryIdList = queryIdsRef.current;
    if (queryIdList.length === 0) {
      return;
    }
    if (!silent) {
      message.loading('正在停止...');
    }
    setCancelQueryTaskLoading(true);

    // 直接取消查询
    abortAjax([`/metric/${flowAnalysisType}/as-histogram`, `/metric/${flowAnalysisType}`]);
    // setQueryIds([]);
    clearQueryIds();
    const { success } = await cancelQueryTask({ queryId: queryIdList.join(',') });

    setCancelQueryTaskLoading(false);
    if (silent) {
      return;
    }

    message.destroy();
    if (!success) {
      message.warning('停止失败');
      return;
    }

    message.success('停止成功');
  };

  // 时间信息
  const selectedTimeInfo = useMemo(() => {
    return globalSelectedTime;
  }, [globalSelectedTime]);

  // 切换时清理之前的条件
  useEffect(() => {
    setSelectedRow(null);
  }, [networkId, serviceId, pcapFileId]);

  // 切换条件，列表刷新，此时图中的过滤条件清空
  useEffect(() => {
    setSelectedRow(null);
  }, [filter]);

  // 枚举字段与非枚举字段
  const statFields: string[] = [...flowCommonFields, ...flowSubFields[flowAnalysisType]];
  const notEnumFieldList = statFields.filter(
    (field) =>
      // 过滤掉平均带宽和tcp连接成功率
      fieldsMapping[field]?.formatterType !== EFormatterType.ENUM &&
      field !== 'bytepsAvg' &&
      field !== 'tcpEstablishedSuccessRate',
  );
  const enumFieldList: string[] = statFields.filter(
    (field) => fieldsMapping[field]?.formatterType === EFormatterType.ENUM,
  );

  // 非枚举字段的列属性
  const notEnumFieldsTableColumns = useMemo(() => {
    const nextColumns: TableColumnProps<LocalTableColumnProps>[] = [];
    nextColumns.push(
      ...notEnumFieldList.map((field) => {
        const { name, formatterType } =
          (overloadFieldsMapping && overloadFieldsMapping[field]) || fieldsMapping[field];
        const isKeyword = fixedColumns[flowAnalysisType].includes(field);
        const formatFunc = fieldFormatterFuncMap[formatterType];
        let renderFunc: (value: string) => ReactNode = formatFunc;
        if (isKeyword) {
          renderFunc = (value: string) => {
            // 如果字段是mac地址，并且字段的值为all， 则在table上显示空串
            if (field === 'macAddress' && value === 'ALL') {
              return '';
            }
            if (value === 'ALL') {
              return value;
            }

            let filterField = snakeCase(field);
            // 如果是 IP会话对，需要转换一下
            if (field === 'ipAAddress' || field === 'ipBAddress') {
              filterField = 'ip_address';
            }

            return (
              <div
                onClick={(event) => {
                  event.stopPropagation();
                  event.preventDefault();
                }}
              >
                <FilterBubble
                  dataIndex={filterField}
                  label={formatFunc(value)}
                  operand={value}
                  operandType={fieldsMapping[field].filterOperandType!}
                  DrilldownMenu={
                    field.indexOf('ip') > -1 ? (
                      <IpDirlldownMenu
                        fromField={field}
                        hasIpDirection={flowAnalysisType !== EMetricApiType.ipAddress}
                        ipAddressValue={value}
                      />
                    ) : undefined
                  }
                  onClick={(newFilter) => {
                    if (addConditionToFilter) {
                      addConditionToFilter([newFilter]);
                    }
                  }}
                />
              </div>
            );
          };
        }
        return {
          title: name,
          dataIndex: field,
          width: name.length * 18 + 40,
          fixed: fixedColumns[flowAnalysisType].includes(field) ? ('left' as any) : false,
          ellipsis: true,
          align: 'center' as any,
          sorter: SortedTypes.includes(formatterType),
          render: (text: any) => renderFunc(text),
        };
      }),
      {
        title: '操作',
        dataIndex: 'operate',
        align: 'center',
        width: 200,
        fixed: 'right',
        render: (text, record) => {
          return (
            <Space className={styles.operateColumnContanier}>
              <span
                className="link"
                onClick={() => {
                  const url = getFlowRecordLink({
                    type: flowAnalysisType,
                    record,
                    filter,
                    networkId: networkId!,
                    serviceId,
                    pcapFileId,
                  });
                  jumpNewPage(
                    `${url}&from=${new Date(
                      getOriginTime(selectedTimeInfo, 'start'),
                    ).valueOf()}&to=${new Date(
                      getOriginTime(selectedTimeInfo, 'end'),
                    ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                  );
                }}
              >
                会话详单
              </span>

              {/* IP 地址组不支持跳转到数据包 */}
              {pcapFileId === undefined && flowAnalysisType !== EMetricApiType.hostGroup && (
                <>
                  <Divider type="vertical" />
                  <span
                    className="link"
                    onClick={() => {
                      const url = getPacketLink({
                        type: flowAnalysisType,
                        record,
                        filter,
                        networkId: networkId!,
                        serviceId,
                      });

                      jumpNewPage(
                        `${url}&from=${new Date(
                          getOriginTime(selectedTimeInfo, 'start'),
                        ).valueOf()}&to=${new Date(
                          getOriginTime(selectedTimeInfo, 'end'),
                        ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
                      );
                    }}
                  >
                    数据包
                  </span>
                </>
              )}
            </Space>
          );
        },
      },
    );
    return nextColumns;
  }, [
    notEnumFieldList,
    overloadFieldsMapping,
    flowAnalysisType,
    addConditionToFilter,
    pcapFileId,
    filter,
    networkId,
    serviceId,
    selectedTimeInfo,
  ]);

  // 枚举字段的列
  const enumFieldsTableColumns: TableColumnProps<LocalTableColumnProps>[] = useMemo(() => {
    // 不显示服务和网络
    const tmpExclude = excludeFields.concat(['serviceId', 'networkId']);
    return enumFieldList
      .filter((field) => {
        return !tmpExclude.includes(field);
      })
      .map((field) => {
        if (fieldsMapping[field]) {
          const { name } = fieldsMapping[field];
          const isKeyword = fixedColumns[flowAnalysisType].includes(field);
          let renderFunc: (value: string) => ReactNode = (value: string) =>
            mapEnumFieldToName(value, field);
          if (isKeyword) {
            renderFunc = (value: string) => {
              if (value === 'ALL') {
                return mapEnumFieldToName(value, field);
              }
              return (
                <div
                  onClick={(event) => {
                    event.stopPropagation();
                    event.preventDefault();
                  }}
                >
                  <FilterBubble
                    dataIndex={snakeCase(field)}
                    label={mapEnumFieldToName(value, field)}
                    operand={value}
                    operandType={fieldsMapping[field].filterOperandType!}
                    onClick={(newFilter) => {
                      if (addConditionToFilter) {
                        addConditionToFilter([newFilter]);
                      }
                    }}
                  />
                </div>
              );
            };
          }
          return {
            title: name,
            align: 'center',
            fixed: fixedColumns[flowAnalysisType].includes(field) ? ('left' as any) : false,
            dataIndex: field,
            width: name.length * 18 + 40,
            ellipsis: true,
            render: (text: string) => renderFunc(text),
          };
        }
        return {};
      });
  }, [addConditionToFilter, enumFieldList, excludeFields, flowAnalysisType, mapEnumFieldToName]);

  // 将所有的列重新排序： fixedLeft, keyColumns, EnumColumns, ByteColumns, LatencyColumns, CountColumns
  const reSortedColumns = useMemo(() => {
    const nextColumns: TableColumnProps<LocalTableColumnProps>[] = [
      {
        title: '#',
        align: 'center',
        dataIndex: 'index',
        width: 60,
        fixed: 'left',
      },
      ...enumFieldsTableColumns,
      ...notEnumFieldsTableColumns,
    ];

    const nextColumnsCopy: TableColumnProps<LocalTableColumnProps>[] = [];
    nextColumnsCopy.push(
      ...nextColumns
        .filter((col) => col.fixed === 'left')
        .sort((aCol, bCol) => {
          return (
            // fixedColumns固定在所有列的最前方
            fixedColumns[flowAnalysisType].findIndex((item) => aCol.dataIndex === item) -
            fixedColumns[flowAnalysisType].findIndex((item) => bCol.dataIndex === item)
          );
        }),
    );
    // 在keyColumn中的列，顺序提前
    nextColumnsCopy.push(
      ...nextColumns
        .filter((col) => keyColumns[flowAnalysisType].includes(col.dataIndex as string))
        .sort((aCol, bCol) => {
          return (
            keyColumns[flowAnalysisType].findIndex((item) => aCol.dataIndex === item) -
            keyColumns[flowAnalysisType].findIndex((item) => bCol.dataIndex === item)
          );
        }),
    );
    // 使用formatterType定义列的顺序
    const columnOrderWithFormaterType = [
      EFormatterType.ENUM,
      EFormatterType.BYTE,
      EFormatterType.LATENCY,
      EFormatterType.COUNT,
      EFormatterType.TEXT,
    ];
    columnOrderWithFormaterType.forEach((valueType) => {
      nextColumnsCopy.push(
        ...nextColumns.filter(
          (col) =>
            !col.fixed &&
            !keyColumns[flowAnalysisType].includes(col.dataIndex as string) &&
            fieldsMapping[col.dataIndex as string].formatterType === valueType,
        ),
      );
    });
    nextColumnsCopy.push(...nextColumns.filter((col) => col.fixed === 'right'));

    return nextColumnsCopy;
  }, [enumFieldsTableColumns, notEnumFieldsTableColumns, flowAnalysisType]);

  // 表头排序的高亮显示
  const tableColumns: TableColumnProps<LocalTableColumnProps>[] = useMemo(() => {
    const nextColumns = reSortedColumns.concat();
    return nextColumns.map((col) => {
      const nextCol = { ...col };
      if (nextCol.dataIndex === sortProperty) {
        nextCol.sortOrder = `${sortDirection}end` as any;
      } else {
        nextCol.sortOrder = false as any;
      }
      return nextCol;
    });
  }, [sortProperty, sortDirection, reSortedColumns]);

  // 计算下钻标志
  const drilldownFlag = useMemo(() => {
    return computedDrilldownFlag(filter as IFilter[]);
  }, [filter]);

  // dsl语句生成
  const dsl = useMemo(() => {
    let internalDsl = filterCondition2Spl(filter, filterFields, (f: IFilter) => {
      // 如果是 IP会话对，并且在不下钻的情况下，需要特殊转换下IP字段
      // 下钻时，从流日志表取数据，不需要转换字段
      // 不下钻时，从 clickhouse 中取数据，clickhouse 中的字段是 ip_a_address 和 ip_b_address
      if (
        flowAnalysisType === EMetricApiType.ipConversation &&
        !drilldownFlag &&
        f.field === 'ip_address'
      ) {
        return `(ip_a_address ${f.operator} ${f.operand} or ip_b_address ${f.operator} ${f.operand})`;
      }
      return `${f.field} ${f.operator} ${f.operand}`;
    });

    // 这里不下钻的时候，默认是查 ES
    // 下钻的时候，java 后端解析 DSL 自己拼 SQL
    if (pcapFileId) {
      if (internalDsl) {
        internalDsl += ' AND ';
      }
      // 更新离线任务后，需要在dsl中添加fileid，对应数据库中字段为network_id
      internalDsl += ` (${`network_id=${pcapFileId}`})`;
      internalDsl += `| gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
    } else {
      if (internalDsl) {
        internalDsl += ' AND ';
      }
      internalDsl += ` (${`network_id=${networkId}`})`;
      // 下钻的情况下，如果 serviceId 为空时，不携带此条件
      // 下钻是从 ClickHouse 中取数据
      internalDsl += drilldownFlag && !serviceId ? '' : ` AND (service_id="${serviceId}")`;
      internalDsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
    }

    return internalDsl;
  }, [
    filter,
    networkId,
    serviceId,
    pcapFileId,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    flowAnalysisType,
    drilldownFlag,
  ]);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const [top, setTop] = useState(tableTop[0]);

  // TODO: 查询参数约束
  const queryParams = useMemo(() => {
    return {
      sourceType,
      networkId,
      serviceId,
      packetFileId: pcapFileId,
      metricApi: flowAnalysisType,
      startTime: selectedTimeInfo.startTime as string,
      endTime: selectedTimeInfo.endTime as string,
      interval: selectedTimeInfo.interval as number,
      sortProperty: snakeCase(sortProperty),
      sortDirection,
      count: top,
      dsl,
      drilldown: drilldownFlag ? BOOL_YES : BOOL_NO,
      columns: getColumnParams({ cols: showColumns || [], tableKey: `flow-${flowAnalysisType}` }),
    } as IFlowQueryParams;
  }, [
    sourceType,
    networkId,
    serviceId,
    pcapFileId,
    flowAnalysisType,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sortProperty,
    sortDirection,
    top,
    dsl,
    drilldownFlag,
    showColumns,
  ]);

  const queryData = useCallback(async () => {
    // 如果当前存在 queryId，先取消查询
    if (queryIdsRef.current.length > 0) {
      await cancelTask(true);
    }

    // 第一个是查询表格的查询 ID
    // 第二个是查询统计的查询 ID
    const ids = [uuidv4(), uuidv4()];
    setTableQueryId(ids[0]);
    setChartQueryId(ids[1]);
    // setQueryIds(ids);
    const tmpChartPayload: IFlowQueryParams = { ...queryParams, chartQueryId: ids[1] };
    const tmpTablePayload: IFlowQueryParams = { ...queryParams, tableQueryId: ids[0] };
    dispatch({
      type: 'npmdModel/queryNetworkFlow',
      payload: tmpChartPayload,
    })
      .then(({ abort }: { abort: boolean }) => {
        if (abort) {
          return;
        }
        // 查询成功后就清空
        // setQueryIds([]);
        setChartQueryId(null);
      })
      .catch(() => {
        setChartQueryId(null);
      });
    dispatch({
      type: 'npmdModel/queryNetworkFlowTable',
      payload: tmpTablePayload,
    })
      .then(({ abort }: { abort: boolean }) => {
        if (abort) {
          return;
        }
        // 查询成功后就清空
        // setQueryIds([]);
        setTableQueryId(null);
      })
      .catch(() => {
        setTableQueryId(null);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dispatch, queryParams]);

  // 请求表格和图表的数据
  useEffect(() => {
    if (!showColumns) {
      return;
    }
    queryData();
  }, [queryData, showColumns]);

  useEffect(() => {
    return () => {
      cancelTask(true);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 表格点击某一行时，请求详细数据
  useEffect(() => {
    if (!selectedRow) {
      return;
    }
    const payload: Record<string, any> = { ...queryParams };
    if (flowAnalysisType === EMetricApiType.location) {
      const { countryId, provinceId, cityId } = selectedRow;
      payload.countryId = countryId;
      payload.provinceId = provinceId;
      payload.cityId = cityId;
    }
    if (flowAnalysisType === EMetricApiType.protocol) {
      const { l7ProtocolId } = selectedRow;
      payload.l7ProtocolId = l7ProtocolId;
    }
    if (flowAnalysisType === EMetricApiType.port) {
      const { port } = selectedRow;
      payload.port = port;
    }
    if (flowAnalysisType === EMetricApiType.hostGroup) {
      const { hostgroupId } = selectedRow;
      payload.hostgroupId = hostgroupId;
    }
    if (flowAnalysisType === EMetricApiType.macAddress) {
      payload.macAddress = selectedRow.macAddress;
    }
    if (flowAnalysisType === EMetricApiType.ipAddress) {
      payload.ipAddress = selectedRow.ipAddress;
      payload.ipLocality = selectedRow.ipLocality;
    }
    if (flowAnalysisType === EMetricApiType.ipConversation) {
      payload.ipAAddress = selectedRow.ipAAddress;
      payload.ipBAddress = selectedRow.ipBAddress;
    }

    const queryId = uuidv4();
    // setQueryIds((prevQueryIds) => {
    //   return [...prevQueryIds, queryId];
    // });
    setChartQueryId(queryId);
    payload.queryId = queryId;

    dispatch({
      type: 'npmdModel/queryNetworkFlowDetailHistogramData',
      payload,
    }).then(() => {
      setChartQueryId(null);
      // setQueryIds((prevQueryIds) => prevQueryIds.filter((id) => id !== queryId));
    });
  }, [selectedRow, queryParams, flowAnalysisType, dispatch]);

  // 图表中的数据格式化方法
  const currentFormatter = useMemo(() => {
    const valueType = fieldsMapping[sortProperty]?.formatterType;
    // 图中的字节数一律转换为bps
    const tmpValueType = valueType === EFormatterType.BYTE ? EFormatterType.BYTE_PS : valueType;
    return fieldFormatterFuncMap[tmpValueType];
  }, [sortProperty]);

  // 点击表格中key值的时候，filter需要添加条件
  const handleRowClick = (e: any, record: LocalTableColumnProps) => {
    // 再次点击取消
    if (selectedRow?.index === record.index) {
      setSelectedRow(null);
      return;
    }
    setSelectedRow(record);
  };

  // 取消选中行
  const cancelSelectedRow = () => {
    setSelectedRow(null);
  };

  // 表格变更回调
  const handleTableChange = (pagination: TablePaginationConfig, _filter: any, sorter: any) => {
    if (sorter.field !== sortProperty) {
      setSortProperty(sorter.field);
      setSortDirection(ESortDirection.DESC);
    } else {
      setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
    }
  };

  const isByteToBandwidth = useMemo(() => {
    return fieldsMapping[sortProperty].formatterType === EFormatterType.BYTE;
  }, [sortProperty]);

  // 图表数据，依赖npmdModel中的数据
  const chartData = useMemo(() => {
    // {applicationId: data[]}
    const tmp: Record<string, TrendChartData> = {};
    if (flowHistogramData[flowAnalysisType]) {
      flowHistogramData[flowAnalysisType].forEach((item) => {
        const seriesName: string = getSeriesName(item as IFlowAnalysisData);
        if (seriesName) {
          if (!tmp[seriesName]) {
            tmp[seriesName] = [];
          }
          tmp[seriesName].push([item.timestamp as string, (item[sortProperty] as number) || 0]);
        }
      });
    }
    // 图表时间补点
    Object.keys(tmp).forEach((seriesName) => {
      tmp[seriesName] = completeTimePoint(
        tmp[seriesName],
        selectedTimeInfo.startTime!,
        selectedTimeInfo.endTime!,
        selectedTimeInfo.interval,
      );
      if (isByteToBandwidth) {
        tmp[seriesName] = tmp[seriesName].map((item) => {
          return [item[0], item[1] / selectedTimeInfo.interval];
        });
      }
    });
    return tmp;
  }, [
    flowAnalysisType,
    flowHistogramData,
    getSeriesName,
    isByteToBandwidth,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    selectedTimeInfo.startTime,
    sortProperty,
  ]);

  // 点击某一行时，显示的图表
  const detailChartData = useMemo(() => {
    const tmp: Record<string, TrendChartData> = {};
    if (flowDetailHistogramData.length > 0) {
      flowDetailHistogramData.forEach((item) => {
        const seriesName: string = getSeriesName(item as IFlowAnalysisData);
        if (seriesName) {
          if (!tmp[seriesName]) {
            tmp[seriesName] = [];
          }
          tmp[seriesName].push([item.timestamp as string, (item[sortProperty] as number) || 0]);
        }
      });
    }
    // 图表时间补点
    Object.keys(tmp).forEach((seriesName) => {
      tmp[seriesName] = completeTimePoint(
        tmp[seriesName],
        selectedTimeInfo.startTime!,
        selectedTimeInfo.endTime!,
        selectedTimeInfo.interval,
      );
      if (isByteToBandwidth) {
        tmp[seriesName] = tmp[seriesName].map((item) => {
          return [item[0], item[1] / selectedTimeInfo.interval];
        });
      }
    });
    return tmp;
  }, [
    flowDetailHistogramData,
    getSeriesName,
    isByteToBandwidth,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    selectedTimeInfo.startTime,
    sortProperty,
  ]);

  // 表格数据
  const dataSource = useMemo<LocalTableColumnProps[]>(() => {
    if (flowTableData[flowAnalysisType]) {
      return (flowTableData[flowAnalysisType] as IFlowAnalysisData[]).map((data, index) => {
        return {
          ...data,
          index: index + 1,
        };
      });
    }
    return [];
  }, [flowAnalysisType, flowTableData]);

  const seriesOrder = useMemo(() => {
    if (flowHistogramData[flowAnalysisType]) {
      return flowHistogramData[flowAnalysisType]
        .map((data) => {
          return getSeriesName(data);
        })
        .slice(0, 10);
    }
    return undefined;
  }, [flowAnalysisType, flowHistogramData, getSeriesName]);

  const selectRowToFilter = useMemo(() => {
    const res: any = [];
    if (selectedRow) {
      if (flowAnalysisType === EMetricApiType.location) {
        const { countryId, provinceId, cityId } = selectedRow;
        if (cityId) {
          res.push({
            field: 'cityId',
            operator: EFilterOperatorTypes.EQ,
            operand: cityId,
          });
        } else if (provinceId) {
          res.push({
            field: 'provinceId',
            operator: EFilterOperatorTypes.EQ,
            operand: provinceId,
          });
        } else if (countryId) {
          res.push({
            field: 'countryId',
            operator: EFilterOperatorTypes.EQ,
            operand: countryId,
          });
        }
      } else if (flowAnalysisType === EMetricApiType.protocol) {
        const { l7ProtocolId } = selectedRow;
        if (l7ProtocolId) {
          res.push({
            field: 'l7ProtocolId',
            operator: EFilterOperatorTypes.EQ,
            operand: l7ProtocolId,
          });
        }
      } else if (flowAnalysisType === EMetricApiType.port) {
        const { port } = selectedRow;
        if (port) {
          res.push({
            field: 'port',
            operator: EFilterOperatorTypes.EQ,
            operand: port,
          });
        }
      } else if (flowAnalysisType === EMetricApiType.macAddress) {
        const { macAddress } = selectedRow;
        if (macAddress) {
          res.push({
            field: 'macAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: macAddress,
          });
        }
      } else if (flowAnalysisType === EMetricApiType.ipAddress) {
        const { ipAddress } = selectedRow;
        if (ipAddress) {
          res.push({
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: ipAddress,
          });
        }
      } else if (flowAnalysisType === EMetricApiType.ipConversation) {
        const { ipAAddress, ipBAddress } = selectedRow;
        if (ipAAddress) {
          res.push({
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: ipAAddress,
          });
        }
        if (ipBAddress) {
          res.push({
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: ipBAddress,
          });
        }
      }
      // if (flowAnalysisType === EMetricApiType.hostGroup) {
      //   const { hostgroupId } = selectedRow;
      //   if (hostgroupId) {
      //     res.field = 'hostgroupId';
      //     res.operand = hostgroupId;
      //   }
      // }

      return res;
    }
    return undefined;
  }, [flowAnalysisType, selectedRow]);

  // 导出
  const handleExport = (fileType: EFileType) => {
    const params = {
      ...queryParams,
      fileType,
      queryId: uuidv4(),
      ...(() => {
        if (selectedRow && selectedRow?.l7ProtocolId) {
          return { l7ProtocolId: selectedRow?.l7ProtocolId };
        }
        return {};
      })(),
    };
    console.log(params);
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/${flowAnalysisType}/as-export?${stringify(
        params,
      )}`,
    );
  };

  // 图表节点实例
  const topTenChart = (
    <AnalysisChart
      loading={queryLoading}
      data={chartData}
      seriesOrder={seriesOrder}
      unitConverter={currentFormatter}
      filterCondition={filter}
      networkId={networkId}
      serviceId={serviceId}
      brushMenus={[{ text: '数据包', key: 'packet' }]}
      selectedTimeInfo={selectedTimeInfo}
      selectRowToFilter={selectRowToFilter}
      markArea={beforeOldestPacketArea}
    />
  );
  // 点击某一行时显示的图表实例
  const detailChart = (
    <AnalysisChart
      loading={detailQueryLoading}
      data={detailChartData}
      unitConverter={currentFormatter}
      filterCondition={filter}
      networkId={networkId}
      serviceId={serviceId}
      brushMenus={[{ text: '数据包', key: 'packet' }]}
      selectedTimeInfo={selectedTimeInfo}
      selectRowToFilter={selectRowToFilter}
      markArea={beforeOldestPacketArea}
    />
  );

  //  表格节点实例
  const table = (
    <div style={{ marginTop: 8 }}>
      <EnhancedTable<LocalTableColumnProps>
        tableKey={`flow-${flowAnalysisType}`}
        defaultShowColumns={defaultShowColumnsForFlow[flowAnalysisType]}
        size="small"
        loading={queryLoading}
        rowKey={(record: LocalTableColumnProps) => record.index}
        onChange={handleTableChange}
        columns={tableColumns}
        bordered={true}
        dataSource={dataSource}
        autoHeight
        fixHeight={fixHeight}
        onColumnChange={(cols) => setShowColumns(cols)}
        style={{ marginTop: '4px' }}
        extraTool={
          <Space size="small" style={{ display: 'flex', justifyContent: 'end' }}>
            <Tooltip title="结束任务可能会导致查询不完整">
              <Button
                icon={<StopOutlined />}
                type="primary"
                danger
                loading={cancelQueryTaskLoading}
                style={{ display: `${showCancelBtn ? '' : 'none'}` }}
                onClick={() => cancelTask()}
              >
                停止
              </Button>
            </Tooltip>
            <Button type="primary" icon={<ReloadOutlined />} onClick={queryData}>
              刷新
            </Button>
            <Select
              defaultValue={top}
              style={{ width: 120 }}
              onChange={(key) => {
                setTop(key);
              }}
            >
              {tableTop.map((item) => (
                <Select.Option value={item} key={item}>{`Top${item}`}</Select.Option>
              ))}
            </Select>
            <Dropdown
              // disabled={totalNum === 0}
              overlay={
                <Menu
                  onClick={(e) => {
                    handleExport(e.key as EFileType);
                  }}
                >
                  <Menu.Item key={EFileType.CSV}>导出 CSV 文件</Menu.Item>
                  <Menu.Item key={EFileType.EXCEL}>导出 Excel 文件</Menu.Item>
                </Menu>
              }
              trigger={['click']}
            >
              <Button icon={<ExportOutlined />} type="primary">
                导出
              </Button>
            </Dropdown>
          </Space>
        }
        onRow={(record) => {
          return {
            onClick: (event) => {
              handleRowClick(event, record);
            },
          };
        }}
      />
    </div>
  );

  const chartCardTitle = useCallback(
    (overloadFieldsMap?: Record<string, IFieldProperty>) => {
      let res = '--';
      if (overloadFieldsMap) {
        res =
          overloadFieldsMap[sortProperty]?.formatterType === EFormatterType.BYTE
            ? `${overloadFieldsMap[sortProperty]?.name.split('字节数')[0]}带宽`
            : overloadFieldsMap[sortProperty]?.name;
      }
      if (!res) {
        res =
          fieldsMapping[sortProperty]?.formatterType === EFormatterType.BYTE
            ? `${fieldsMapping[sortProperty]?.name.split('字节数')[0]}带宽`
            : fieldsMapping[sortProperty]?.name;
      }
      return res;
    },
    [sortProperty],
  );

  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          title={`${getSeriesName(selectedRow)}:${
            overloadFieldsMapping ? chartCardTitle(overloadFieldsMapping) : chartCardTitle()
          }`}
          extra={
            <span onClick={cancelSelectedRow} className={styles.pointer}>
              <CloseSquareOutlined />
            </span>
          }
        >
          {detailChart}
        </Card>
      ) : (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          className={styles.card}
          title={overloadFieldsMapping ? chartCardTitle(overloadFieldsMapping) : chartCardTitle()}
        >
          {topTenChart}
        </Card>
      )}
      {table}
    </>
  );
};

const mapStateToProps = ({
  npmdModel: {
    flowTableData,
    flowHistogramData,
    flowDetailHistogramData,
    currentPcpInfo,
    beforeOldestPacketArea,
  },
  appModel: { globalSelectedTime },
  loading: { effects },
}: ConnectState) => ({
  flowTableData,
  flowHistogramData,
  currentPcpInfo,
  flowDetailHistogramData,
  globalSelectedTime,
  queryLoading: effects['npmdModel/queryNetworkFlow'],
  detailQueryLoading: effects['npmdModel/queryNetworkFlowDetailHistogramData'],
  beforeOldestPacketArea,
});

export default connect(mapStateToProps)(FlowAnalysis);
