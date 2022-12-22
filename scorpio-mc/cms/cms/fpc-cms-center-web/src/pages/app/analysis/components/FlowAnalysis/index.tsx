import { EMetricApiType } from '@/common/api/analysis';
import config from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import EnhancedTable from '@/components/EnhancedTable';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
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
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import { cancelQueryTask, pingQueryTask } from '@/pages/app/appliance/FlowRecords/service';
import useFieldList from '@/pages/app/appliance/Packet/hooks/useFieldList/useFieldList';
// import { packetSearchableFields } from '@/pages/app/appliance/Packet/PacketPage';
import { DimensionsSearchContext } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs';
import { dimensionsUrl } from '@/pages/app/GlobalSearch/DimensionsSearch/SeartchTabs/constant';
import { EDRILLDOWN } from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { AnalysisContext, NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { getTabDetail } from '@/pages/app/Network/components/EditTabs';
import {
  computedDrilldownFlag,
  FilterContext,
  filterFields,
} from '@/pages/app/Network/components/Flow';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType, ENetworkTabs } from '@/pages/app/Network/typing';
import { abortAjax, camelCase, snakeCase } from '@/utils/utils';
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
import { connect, history, useParams } from 'umi';
import { v4 as uuidv4 } from 'uuid';
import { queryNetworkFlow, queryNetworkFlowHistogram } from '../../Flow/service';
import { ServiceAnalysisContext } from '../../Service/index';
import type { IFlowAnalysisData, IFlowQueryParams } from '../../typings';
import { ESourceType, tableTop } from '../../typings';
import type { TrendChartData } from '../AnalysisChart';
import AnalysisChart from '../AnalysisChart';
import DimensionsDirlldownMenu from '../DimensionsDirlldownMenu';
import IpDirlldownMenu from '../IpDirlldownMenu';
import { getFlowRecordLink } from './constant';
import styles from './index.less';

enum EFileType {
  EXCEL = 'excel',
  CSV = 'csv',
}

const { API_BASE_URL, API_VERSION_PRODUCT_V1 } = config;
// import moment from 'moment';

enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

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

  res.shift();

  return res;
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

  packetSearchableFields: string[];
}

/** 生成数据包跳转按钮 */
export const getPacketLink = ({
  type,
  record,
  filter,
  packetSearchableFields,
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
        if (field !== 'ip_protocol' && record[field] !== 'ALL') {
          copyFilter.push({
            field,
            operator: EFilterOperatorTypes.EQ,
            operand: String(record[field]).toLocaleUpperCase(),
          });
        }
      }
    });
  }
  return copyFilter;
  // return JSON.stringify(copyFilter);
  // return getLinkUrl(`${urlPrefix}/packet?filter=${encodeURIComponent(JSON.stringify(copyFilter))}`);
};

export const getColumnParams = getColumnParamsFunc(['index', 'operate', 'action']);

type LocalTableColumnProps = IFlowAnalysisData & {
  index: number;
};

interface IFlowAnalysisProps {
  flowAnalysisType: TFlowAnalysisType;
  // 枚举值转为显示的名称
  mapEnumFieldToName: (value: string, field: string) => string;
  // 表格中不显示的列
  excludeFields: string[];
  // 图表series的名称，业务含义是每个统计指标下的topX的名称
  getSeriesName: (record: IFlowAnalysisData) => string;
  overloadFieldsMapping?: Record<string, IFieldProperty>;

  globalSelectedTime: Required<IGlobalTime>;
  currentFilterCondition?: IFilter[];
  currentNetworkId?: string;
  needHeight?: number;
}

const FlowAnalysis: React.FC<IFlowAnalysisProps> = (props) => {
  const {
    globalSelectedTime,
    flowAnalysisType,
    mapEnumFieldToName,
    excludeFields,
    getSeriesName,
    overloadFieldsMapping,
    currentNetworkId,
    currentFilterCondition,
    needHeight,
  } = props;

  // 此处用的hooks取的数据
  const fieldList = useFieldList({});

  const packetSearchableFields = useMemo(() => {
    return fieldList.filter((field) => field.searchable).map((field) => field.dataIndex);
  }, [fieldList]);

  const [flowDetailHistogramData, setFlowDetailHistogramData] = useState<any>([]);
  const [detailQueryLoading, setDetailQueryLoading] = useState(false);
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);

  const [showColumns, setShowColumns] = useState<string[]>();
  const { serviceId: urlServiceId, networkId: urlNetworkId } = useParams() as {
    networkId: string;
    serviceId: string;
  };

  //  const { serviceId, networkId: urlNetworkId } = useMemo(() => {
  //   const tmpNetworkId = urlIds.networkId || '';
  //   console.log(tmpNetworkId,'tmpNetworkId');
  //   if (!isDimensionsTab && tmpNetworkId.includes('^')) {
  //     return {
  //       serviceId: urlIds.serviceId,
  //       networkId: tmpNetworkId.split('^')[1],
  //     };
  //   }
  //   return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  // }, [urlIds.networkId, urlIds.serviceId]);
  const { serviceId, networkId: urlnetworkId } = useMemo(() => {
    if (!isDimensionsTab && urlNetworkId && urlNetworkId.includes('^')) {
      return {
        serviceId: urlServiceId || '',
        networkId: urlNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlServiceId, networkId: urlNetworkId };
  }, [urlNetworkId, urlServiceId]);

  const networkId = useMemo(() => {
    if (currentNetworkId) {
      return currentNetworkId;
    }
    return urlnetworkId;
  }, [currentNetworkId, urlnetworkId]);

  const [fixHeight] = useState(() => {
    if (needHeight) {
      return needHeight;
    }
    return 0;
  });

  const [state, dispatch] = useContext<any>(
    (() => {
      if (isDimensionsTab) {
        return DimensionsSearchContext;
      }
      return serviceId ? ServiceAnalysisContext : AnalysisContext;
    })(),
  );
  const flowAnalysisDetail = isDimensionsTab ? getTabDetail(state) : {};
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
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );

  // 时间信息
  const selectedTimeInfo = useMemo(() => {
    return globalSelectedTime;
  }, [globalSelectedTime]);

  // 切换时清理之前的条件
  useEffect(() => {
    setSelectedRow(null);
  }, [networkId, serviceId]);

  // 枚举字段与非枚举字段
  const statFields: string[] = flowCommonFields.concat(flowSubFields[flowAnalysisType]);
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
                    isDimensionsTab ? (
                      <DimensionsDirlldownMenu
                        drilldownWithFilter={[
                          {
                            field: filterField,
                            operator: EFilterOperatorTypes.EQ,
                            operand: String(value),
                          },
                        ].concat(filter as any)}
                      />
                    ) : (
                      <IpDirlldownMenu
                        indexKey={`${field}-flow`}
                        ipAddressKeys={value}
                        tableKey={'flowAnalysis'}
                      />
                    )
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
              <Button
                size="small"
                type="link"
                onClick={() => {
                  const filterParam = getFlowRecordLink({ type: flowAnalysisType, record, filter });
                  jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.FLOWRECORD, {
                    filter: filterParam as any,
                    networkId,
                    serviceId,
                    globalSelectedTime,
                  });
                }}
              >
                会话详单
              </Button>

              {/* IP 地址组不支持跳转到数据包 */}
              {flowAnalysisType !== EMetricApiType.hostGroup && (
                <>
                  <Divider type="vertical" />
                  <Button
                    size="small"
                    type="link"
                    onClick={() => {
                      const filterParam = getPacketLink({
                        type: flowAnalysisType,
                        record,
                        filter,
                        networkId,
                        serviceId,
                        packetSearchableFields,
                      });
                      jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.PACKET, {
                        filter: filterParam as any,
                        globalSelectedTime,
                      });
                    }}
                  >
                    数据包
                  </Button>
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
    filter,
    state,
    dispatch,
    networkId,
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
                    DrilldownMenu={
                      isDimensionsTab ? (
                        <DimensionsDirlldownMenu
                          drilldownWithFilter={[
                            {
                              field: snakeCase(field),
                              operator: EFilterOperatorTypes.EQ,
                              operand: String(value),
                            },
                          ].concat(filter as any)}
                        />
                      ) : (
                        <div style={{ display: 'none' }} />
                      )
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
    return nextColumns
      .map((col) => {
        const nextCol = { ...col };
        if (nextCol.dataIndex === sortProperty) {
          nextCol.sortOrder = `${sortDirection}end` as any;
        } else {
          nextCol.sortOrder = false as any;
        }
        return nextCol;
      })
      .filter((item) => !isDimensionsTab || item.dataIndex !== 'operate');
  }, [reSortedColumns, sortProperty, sortDirection, isDimensionsTab]);

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

    if (isDimensionsTab) {
      internalDsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
      return internalDsl;
    }

    // 这里不下钻的时候，默认是查 ES
    // 下钻的时候，java 后端解析 DSL 自己拼 SQL
    if (networkType === ENetowrkType.NETWORK) {
      if (internalDsl) {
        internalDsl += ' AND ';
      }
      internalDsl += ` (${`network_id=${networkId}`})`;
    }
    // 下钻的情况下，如果 serviceId 为空时，不携带此条件
    // 下钻是从 ClickHouse 中取数据
    internalDsl +=
      drilldownFlag && !serviceId
        ? ''
        : `${internalDsl ? ' AND ' : ' '}(service_id="${serviceId || ''}")`;
    internalDsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;

    return internalDsl;
  }, [
    filter,
    networkId,
    serviceId,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    flowAnalysisType,
    drilldownFlag,
    networkType,
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

  const queryParams = useMemo<IFlowQueryParams>(() => {
    const dimensionsQueryData = (() => {
      const { searchBoxInfo, shareRow, drilldown = EDRILLDOWN.NOTDRILLDOWN } = flowAnalysisDetail;
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
      return {
        srcIp: searchBoxInfo?.content,
        startTime: globalSelectedTime.originStartTime,
        endTime: globalSelectedTime.originEndTime,
        ...tmpIds,
      };
    })();
    const { searchBoxInfo, shareRow, drilldown = EDRILLDOWN.NOTDRILLDOWN } = flowAnalysisDetail;
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
    return {
      sourceType: isDimensionsTab ? undefined : sourceType,
      ...(isDimensionsTab
        ? tmpIds
        : (() => {
            if (networkType === ENetowrkType.NETWORK) {
              return {
                networkId: networkId,
              };
            }
            if (networkType === ENetowrkType.NETWORK_GROUP) {
              return {
                networkGroupId: networkId,
              };
            }
            return {};
          })()),
      serviceId,
      metricApi: flowAnalysisType,
      startTime: selectedTimeInfo.startTime as string,
      endTime: selectedTimeInfo.endTime as string,
      interval: selectedTimeInfo.interval as number,
      sortProperty: snakeCase(sortProperty),
      sortDirection,
      dsl,
      columns: getColumnParams({ cols: showColumns || [], tableKey: `flow-${flowAnalysisType}` }),
      drilldown: drilldownFlag ? BOOL_YES : BOOL_NO,
    };
  }, [
    showColumns,
    sourceType,
    networkId,
    serviceId,
    flowAnalysisType,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sortProperty,
    sortDirection,
    dsl,
    drilldownFlag,
    networkType,
  ]);

  const [flowHistogramData, setFlowHistogramData] = useState([]);
  const [flowTableData, setFlowTableData] = useState<any>([]);
  const [queryLoading, setQueryLoading] = useState(false);
  const [queryIds, setQueryIds] = useState<string[]>([]);
  const queryIdsRef = useRef<string[]>([]);
  const [showCancelBtn, setShowCancelBtn] = useState(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  const [top, setTop] = useState(tableTop[0]);
  const [refreshId, setRefreshId] = useState<string>(uuidv4());
  useEffect(() => {
    queryIdsRef.current = queryIds;
  }, [queryIds]);

  // ======维持查询心跳 S=====
  // ======维持查询心跳 S=====
  const pingQueryTaskFn = useCallback(() => {
    // 没有 ID 时不 ping
    if (queryIds.length === 0) {
      return;
    }
    pingQueryTask({
      queryId: queryIds.join(','),
    }).then((success: boolean) => {
      if (!success) {
        message.destroy();
      }
    });
  }, [queryIds]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    let timer: any;
    if (queryIds.length > 0) {
      timer = window.setInterval(() => {
        setShowCancelBtn(true);
        pingQueryTaskFn();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    window.clearTimeout(timer);
    setShowCancelBtn(false);
    return undefined;
  }, [pingQueryTaskFn, queryIds]);

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
    setQueryIds([]);
    const { success } = await cancelQueryTask({ queryId: queryIdList.join(',') });

    if (silent) {
      return;
    }

    message.destroy();
    if (!success) {
      message.warning('停止失败');
      return;
    }

    message.success('停止成功');
    setCancelQueryTaskLoading(false);
  };

  useEffect(() => {
    return () => {
      cancelTask(true);
    };
  }, []);

  // 切换条件，列表刷新，此时图中的过滤条件清空
  useEffect(() => {
    setSelectedRow(null);
  }, [filter]);

  const queryData = useCallback(() => {
    // 如果当前存在 queryId，先取消查询
    if (queryIdsRef.current.length > 0) {
      cancelTask(true);
    }
    // 第一个是查询表格的查询 ID
    // 第二个是查询统计的查询 ID
    const ids = [uuidv4(), uuidv4()];
    setQueryIds(ids);
    const { metricApi } = queryParams;
    setQueryLoading(true);
    Promise.all([
      queryNetworkFlow({ ...queryParams, queryId: ids[0], count: top }),
      queryNetworkFlowHistogram({ ...queryParams, queryId: ids[1] }),
    ]).then((res) => {
      const [table, histogram] = res;
      setQueryLoading(false);
      // 查询成功后就清空
      setQueryIds([]);
      const newData: { tableData: any[]; histogramData: any[] } = {
        tableData: table.success ? table.result : [],
        histogramData: histogram.success ? histogram.result : [],
      };

      newData.histogramData?.sort(
        (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
      );
      setFlowTableData((preData: any) => {
        return {
          ...preData,
          [metricApi]: newData.tableData,
        };
      });
      setFlowHistogramData((preData: any) => {
        return {
          ...preData,
          [metricApi]: newData.histogramData,
        };
      });
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryParams, top]);

  useEffect(() => {
    if (!showColumns || !showColumns.length) {
      return;
    }
    queryData();
  }, [queryData, showColumns]);

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
    setDetailQueryLoading(true);
    queryNetworkFlowHistogram(payload as IFlowQueryParams).then((res) => {
      const { success, result } = res;
      setDetailQueryLoading(false);
      if (success) {
        setFlowDetailHistogramData(result);
      }
    });
  }, [selectedRow, queryParams, flowAnalysisType]);

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
    const tmp: Record<string, TrendChartData> = {};
    if (flowHistogramData[flowAnalysisType]) {
      flowHistogramData[flowAnalysisType].forEach((item: any) => {
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
          return [item[0], item[1] / (selectedTimeInfo.interval || 1)];
        });
      }
    });
    setRefreshId(uuidv4());
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
      flowDetailHistogramData.forEach((item: any) => {
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
          return [item[0], item[1] / (selectedTimeInfo.interval || 1)];
        });
      }
    });
    setRefreshId(uuidv4());
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
    if (flowTableData[flowAnalysisType]) {
      return flowTableData[flowAnalysisType]
        .map((data: any) => {
          return getSeriesName(data);
        })
        .slice(0, 10);
    }
    return undefined;
  }, [flowAnalysisType, flowTableData, getSeriesName]);

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

  // 图表节点实例
  const topTenChart = (
    <AnalysisChart
      key={refreshId}
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
      isDilldownChart={!isDimensionsTab}
    />
  );
  // 点击某一行时显示的图表实例
  const detailChart = (
    <AnalysisChart
      key={refreshId}
      loading={detailQueryLoading}
      data={detailChartData}
      unitConverter={currentFormatter}
      filterCondition={filter}
      networkId={networkId}
      serviceId={serviceId}
      brushMenus={[{ text: '数据包', key: 'packet' }]}
      selectedTimeInfo={selectedTimeInfo}
      selectRowToFilter={selectRowToFilter}
      isDilldownChart={!isDimensionsTab}
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
        onColumnChange={setShowColumns}
        columns={tableColumns}
        bordered={true}
        dataSource={dataSource}
        autoHeight
        fixHeight={fixHeight}
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
                setQueryLoading(true);
              }}
            >
              {tableTop.map((item) => (
                <Select.Option key={item} value={item}>{`Top${item}`}</Select.Option>
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
  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          title={`${getSeriesName(selectedRow)}:${
            (overloadFieldsMapping && overloadFieldsMapping[sortProperty]?.name) ||
            fieldsMapping[sortProperty]?.name
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
          title={
            (overloadFieldsMapping && overloadFieldsMapping[sortProperty]?.name) ||
            fieldsMapping[sortProperty]?.name
          }
        >
          {topTenChart}
        </Card>
      )}
      {table}
    </>
  );
};

const mapStateToProps = ({ appModel: { globalSelectedTime } }: ConnectState) => ({
  globalSelectedTime,
});

export default connect(mapStateToProps)(FlowAnalysis);
