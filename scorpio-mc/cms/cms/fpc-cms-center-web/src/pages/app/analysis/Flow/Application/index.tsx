import { EMetricApiType } from '@/common/api/analysis';
import config from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { ConnectState } from '@/models/connect';
import type { IFieldProperty } from '@/pages/app/analysis/components/fieldsManager';
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
import { abortAjax, snakeCase } from '@/utils/utils';
import { CloseSquareOutlined, ExportOutlined, StopOutlined } from '@ant-design/icons';
import type { TableColumnProps } from 'antd';
import { Button, Card, Divider, Dropdown, Menu, message, Select, Space, Tooltip } from 'antd';
import type { TablePaginationConfig } from 'antd/lib/table/interface';
import { stringify } from 'qs';
import type { ReactNode } from 'react';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { IGlobalSelectedTime } from 'umi';
import { connect, history, useParams } from 'umi';
import { v4 as uuidv4 } from 'uuid';
import type { TrendChartData } from '../../components/AnalysisChart';
import AnalysisChart from '../../components/AnalysisChart';
import DimensionsDirlldownMenu from '../../components/DimensionsDirlldownMenu';
import {
  completeTimePoint,
  fixedColumns,
  getColumnParams,
  getPacketLink,
} from '../../components/FlowAnalysis';
import { getFlowRecordLink } from '../../components/FlowAnalysis/constant';
import styles from '../../components/FlowAnalysis/index.less';
import type { ITableMenuListProps } from '../../components/WithMenuTable';
import WithMenuTable from '../../components/WithMenuTable';
import { queryNetworkFlow, queryNetworkFlowHistogram } from '../../Flow/service';
import { ServiceAnalysisContext } from '../../Service/index';
import type { IFlowAppStatFileds, IFlowQueryParams, IUriParams } from '../../typings';
import { ANALYSIS_APPLICATION_TYPE_ENUM, ESourceType, tableTop } from '../../typings';

const { API_BASE_URL, API_VERSION_PRODUCT_V1 } = config;
enum EFileType {
  EXCEL = 'excel',
  CSV = 'csv',
}

export const applicationStatFields: string[] = [...flowCommonFields, ...flowSubFields.applications];
export const commonFieldList = applicationStatFields.filter(
  (field) =>
    fieldsMapping[field].formatterType !== EFormatterType.ENUM &&
    field !== 'bytepsAvg' &&
    field !== 'tcpEstablishedSuccessRate',
);
export const enumFieldList: string[] = applicationStatFields.filter(
  (field) => fieldsMapping[field].formatterType === EFormatterType.ENUM,
);

enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

export type LocalTableColumnProps = IFlowAppStatFileds & {
  index: number;
};

export const overloadFieldsMapping: Record<string, IFieldProperty> = {
  downstreamBytes: { name: '下行字节数', formatterType: EFormatterType.BYTE },
  upstreamBytes: { name: '上行字节数', formatterType: EFormatterType.BYTE },
  downstreamPackets: { name: '下行数据包数', formatterType: EFormatterType.COUNT },
  upstreamPackets: { name: '上行数据包数', formatterType: EFormatterType.COUNT },
  downstreamPayloadBytes: { name: '下行负载字节数', formatterType: EFormatterType.BYTE },
  upstreamPayloadBytes: { name: '上行负载字节数', formatterType: EFormatterType.BYTE },
  downstreamPayloadPackets: { name: '下行负载数据包数', formatterType: EFormatterType.COUNT },
  upstreamPayloadPackets: { name: '上行负载数据包数', formatterType: EFormatterType.COUNT },
};

const defaultShowColumns = [
  'index',
  'applicationId',
  'categoryId',
  'subcategoryId',
  'totalBytes',
  'downstreamBytes',
  'upstreamBytes',
  'establishedSessions',
  'operate',
];

// 与表格字段统一
enum TabKey {
  APP = 'applicationId',
  CATEGORY = 'categoryId',
  SUB_CATEGORY = 'subcategoryId',
}

interface IApplicationProps {
  globalSelectedTime: IGlobalSelectedTime;
  SAKnowledgeModel: ConnectState['SAKnowledgeModel'];
  currentNetworkId?: string;
  currentFilterCondition?: IFilter[];
  needHeight?: number;
}

const Application: React.FC<IApplicationProps> = (props) => {
  const {
    globalSelectedTime,
    SAKnowledgeModel: { allApplicationMap, allCategoryMap, allSubCategoryMap },
    currentNetworkId,
    currentFilterCondition,
    needHeight,
  } = props;

  const [flowDetailHistogramData, setFlowDetailHistogramData] = useState<any>([]);
  const [detailQueryLoading, setDetailQueryLoading] = useState(false);
  const isDimensionsTab = history.location.pathname.includes(dimensionsUrl);
  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId: urlNetworkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    console.log(tmpNetworkId, 'tmpNetworkId');
    if (!isDimensionsTab && tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);

  const networkId = useMemo(() => {
    if (currentNetworkId) {
      return currentNetworkId;
    }
    return urlNetworkId;
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

  const [currentMenu, setCurrentMenu] = useState<TabKey>(TabKey.APP);
  const [switchMenu, setSwitchMenu] = useState(false);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  const [queryIds, setQueryIds] = useState<string[]>([]);
  const queryIdsRef = useRef<string[]>([]);
  const [showCancelBtn, setShowCancelBtn] = useState(false);
  const [top, setTop] = useState(tableTop[0]);
  const [showColumns, setShowColumns] = useState<string[]>();

  const selectedTimeInfo = useMemo(() => {
    return globalSelectedTime;
  }, [globalSelectedTime]);

  const [fixHeight] = useState(() => {
    if (needHeight) {
      return needHeight;
    }
    return 0;
  });

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

  useEffect(() => {
    queryIdsRef.current = queryIds;
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
    return () => {};
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
    abortAjax(['/metric/applications/as-histogram', '/metric/applications']);
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

  // 应用、子分类、分类
  const applicationType = useMemo(() => {
    if (currentMenu === TabKey.APP) {
      return ANALYSIS_APPLICATION_TYPE_ENUM['应用'];
    }
    if (currentMenu === TabKey.CATEGORY) {
      return ANALYSIS_APPLICATION_TYPE_ENUM['分类'];
    }
    return ANALYSIS_APPLICATION_TYPE_ENUM['子分类'];
  }, [currentMenu]);

  const mapEnumFieldToName = useCallback(
    (fieldId: string, name: string) => {
      switch (name) {
        case 'applicationId': {
          return allApplicationMap[fieldId]?.nameText || fieldId;
        }
        case 'categoryId': {
          return allCategoryMap[fieldId]?.nameText || fieldId;
        }
        case 'subcategoryId': {
          return allSubCategoryMap[fieldId]?.nameText || fieldId;
        }
        default: {
          return fieldId;
        }
      }
    },
    [allApplicationMap, allCategoryMap, allSubCategoryMap],
  );

  const jumpToAppTab = useCallback(
    (_currentMenu: TabKey, record: IFlowAppStatFileds) => {
      const { categoryId, subcategoryId } = record;
      let recordFilter: IFilter[] = [];
      if (currentMenu === TabKey.CATEGORY) {
        recordFilter = [
          {
            field: 'category_id',
            operator: EFilterOperatorTypes.EQ,
            operand: String(categoryId),
          },
        ];
      }
      if (currentMenu === TabKey.SUB_CATEGORY) {
        recordFilter = [
          {
            field: 'subcategory_id',
            operator: EFilterOperatorTypes.EQ,
            operand: String(subcategoryId),
          },
        ];
      }

      if (addConditionToFilter && recordFilter.length > 0) {
        addConditionToFilter(recordFilter);
      }

      setCurrentMenu(TabKey.APP);
    },
    [addConditionToFilter, currentMenu],
  );

  const enumFieldColums: TableColumnProps<LocalTableColumnProps>[] = useMemo(() => {
    return enumFieldList.map((field) => {
      const { name } = fieldsMapping[field];
      const isKeyword = fixedColumns.applications.includes(field);
      let renderFunc: (text: string) => ReactNode = (text: string) =>
        mapEnumFieldToName(text, field);
      if (isKeyword) {
        renderFunc = (value: string) => {
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
        dataIndex: field,
        width: name.length * 18 + 40,
        ellipsis: true,
        render: (text: string) => renderFunc(text),
      };
    });
  }, [addConditionToFilter, mapEnumFieldToName]);

  const columns = useMemo(() => {
    const fullColumns: TableColumnProps<LocalTableColumnProps>[] = [];
    fullColumns.push(
      ...commonFieldList.map((field) => {
        const { name, formatterType } =
          (overloadFieldsMapping && overloadFieldsMapping[field]) || fieldsMapping[field];
        const renderFunc = fieldFormatterFuncMap[formatterType];
        return {
          title: name,
          dataIndex: field,
          width: name.length * 18 + 40,
          align: 'center' as any,
          ellipsis: true,
          sorter: SortedTypes.includes(formatterType),
          ...(renderFunc ? { render: (value: any) => renderFunc(value) } : {}),
        };
      }),
    );
    return fullColumns;
  }, []);

  const [state, dispatch] = useContext(
    (() => {
      if (isDimensionsTab) {
        return DimensionsSearchContext;
      }
      return serviceId ? ServiceAnalysisContext : AnalysisContext;
    })(),
  );

  // 切换条件，列表刷新，此时图中的过滤条件清空
  useEffect(() => {
    setSelectedRow(null);
  }, [filter]);

  const actionColumn = useMemo(() => {
    const action: TableColumnProps<LocalTableColumnProps> = {
      title: '操作',
      dataIndex: 'operate',
      align: 'center',
      width: 100,
      fixed: 'right',
      render: (text: any, record: LocalTableColumnProps) => {
        if (currentMenu === TabKey.APP) {
          return (
            <Space className={styles.operateColumnContanier}>
              <Button
                size="small"
                type="link"
                onClick={() => {
                  const filterParam = getFlowRecordLink({
                    type: EMetricApiType.application,
                    record,
                    filter,
                  });
                  jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.FLOWRECORD, {
                    filter: filterParam as any,
                    globalSelectedTime: {
                      startTime: globalSelectedTime.startTime || 0,
                      endTime: globalSelectedTime.endTime || 0,
                    },
                    networkId,
                    serviceId,
                  });
                }}
              >
                会话详单
              </Button>

              <Divider type="vertical" />
              <Button
                size="small"
                type="link"
                onClick={() => {
                  const filterParam = getPacketLink({
                    type: EMetricApiType.application,
                    record,
                    filter,
                    networkId: networkId!,
                    serviceId,
                  });
                  jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.PACKET, {
                    filter: filterParam as any,
                    globalSelectedTime: {
                      startTime: (globalSelectedTime.startTime || '')?.toString(),
                      endTime: (globalSelectedTime.endTime || '')?.toString(),
                    },
                  });
                }}
              >
                数据包
              </Button>
            </Space>
          );
        }
        return (
          <Space className={styles.operateColumnContanier}>
            <span
              className="link"
              onClick={(event) => {
                event.stopPropagation();
                event.preventDefault();
                jumpToAppTab(currentMenu, record);
              }}
            >
              应用
            </span>
          </Space>
        );
      },
    };
    return action;
  }, [currentMenu, filter, state, dispatch, networkId, serviceId, jumpToAppTab]);

  const appTableColumns: TableColumnProps<LocalTableColumnProps>[] = useMemo(() => {
    const nextColumns: TableColumnProps<LocalTableColumnProps>[] = [
      {
        title: '#',
        align: 'center',
        dataIndex: 'index',
        width: 60,
        fixed: 'left',
      },
      ...enumFieldColums,
      ...columns,
      actionColumn,
    ];
    return nextColumns
      .map((col) => {
        const nextCol = { ...col };
        if (fixedColumns.applications.includes(col.dataIndex as string)) {
          nextCol.fixed = 'left';
        }
        if (col.dataIndex === sortProperty) {
          nextCol.sortOrder = `${sortDirection}end` as any;
        } else {
          nextCol.sortOrder = false as any;
        }
        return nextCol;
      })
      .filter((item) => !isDimensionsTab || item.dataIndex !== 'operate');
    // return nextColumns;
  }, [enumFieldColums, columns, actionColumn, sortProperty, sortDirection, isDimensionsTab]);

  // 计算下钻标志
  const drilldownFlag = useMemo(() => {
    return computedDrilldownFlag(filter as IFilter[]);
  }, [filter]);

  const dsl = useMemo(() => {
    let internalDsl = filterCondition2Spl(filter, filterFields);
    if (isDimensionsTab) {
      internalDsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
      return internalDsl;
    }
    if (internalDsl) {
      internalDsl += ' AND ';
    }
    internalDsl += `(type = ${String(applicationType)}) `;
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
  }, [filter, networkId, serviceId, selectedTimeInfo, applicationType, drilldownFlag, networkType]);

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const flowAnalysisDetail = isDimensionsTab ? getTabDetail(state) : {};
  // TODO: 查询参数约束
  const queryParams = useCallback(() => {
    const { searchBoxInfo } = flowAnalysisDetail;
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
      metricApi: EMetricApiType.application,
      startTime: selectedTimeInfo.startTime as string,
      endTime: selectedTimeInfo.endTime as string,
      interval: selectedTimeInfo.interval as number,
      sortProperty: snakeCase(sortProperty),
      type: applicationType,
      sortDirection,
      count: top,
      dsl,
      columns: getColumnParams({
        cols: showColumns,
        tableKey: `flow-${EMetricApiType.application}-${currentMenu}`,
      }),
      drilldown: drilldownFlag ? BOOL_YES : BOOL_NO,
    } as IFlowQueryParams;
  }, [
    sourceType,
    serviceId,
    currentMenu,
    showColumns,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sortProperty,
    applicationType,
    sortDirection,
    top,
    dsl,
    drilldownFlag,
    networkType,
    currentMenu,
    networkId,
  ]);

  // const queryData = useCallback(async () => {
  //   // 如果当前存在 queryId，先取消查询
  //   if (queryIdsRef.current.length > 0) {
  //     await cancelTask(true);
  //   }

  //   // 第一个是查询表格的查询 ID
  //   // 第二个是查询统计的查询 ID
  //   const ids = [uuidv4(), uuidv4()];
  //   setQueryIds(ids);

  //   dispatch({
  //     type: 'npmdModel/queryNetworkFlow',
  //     payload: { ...queryParams, queryIds: ids },
  //   }).then(({ abort }: { abort: boolean }) => {
  //     setSwitchMenu(false);
  //     if (abort) {
  //       return;
  //     }
  //     // 查询成功后就清空
  //     setQueryIds([]);
  //   });
  // }, [dispatch, queryParams]);

  // useEffect(() => {
  //   queryData();
  // }, [queryData]);

  useEffect(() => {
    return () => {
      cancelTask(true);
    };
  }, []);

  const [flowHistogramData, setFlowHistogramData] = useState<any>([]);
  const [flowTableData, setFlowTableData] = useState<any>([]);
  const [queryLoading, setQueryLoading] = useState(false);

  useEffect(() => {
    if (!showColumns || !showColumns.length) {
      return;
    }
    const { metricApi } = queryParams();
    setQueryLoading(true);
    // 如果当前存在 queryId，先取消查询
    if (queryIdsRef.current.length > 0) {
      cancelTask(true);
    }

    // 第一个是查询表格的查询 ID
    // 第二个是查询统计的查询 ID
    const ids = [uuidv4(), uuidv4()];
    setQueryIds(ids);
    Promise.all([
      queryNetworkFlow({ ...queryParams(), queryId: ids[0], count: top }),
      queryNetworkFlowHistogram({ ...queryParams(), queryId: ids[1] }),
    ]).then((res) => {
      const [table, histogram] = res;
      // 查询成功后就清空
      setSwitchMenu(false);
      setQueryIds([]);
      setQueryLoading(false);
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
  }, [queryParams, top]);

  useEffect(() => {
    if (!selectedRow) {
      return;
    }
    const payload: Record<string, any> = { ...queryParams() };
    if (selectedRow) {
      const { applicationId, categoryId, subcategoryId } = selectedRow;
      if (currentMenu === TabKey.APP) {
        payload.id = applicationId;
      } else if (currentMenu === TabKey.CATEGORY) {
        payload.id = categoryId;
      } else if (currentMenu === TabKey.SUB_CATEGORY) {
        payload.id = subcategoryId;
      }
    }
    setDetailQueryLoading(true);
    queryNetworkFlowHistogram(payload as IFlowQueryParams).then((res) => {
      const { success, result } = res;
      setDetailQueryLoading(false);
      if (success) {
        setFlowDetailHistogramData(result);
      }
    });
  }, [queryParams, selectedRow, currentMenu]);

  const handleTableChange = (pagination: TablePaginationConfig, _filter: any, sorter: any) => {
    if (sorter.field !== sortProperty) {
      setSortProperty(sorter.field);
      setSortDirection(ESortDirection.DESC);
    } else {
      setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
    }
  };

  const currentFormatter = useMemo(() => {
    const valueType = fieldsMapping[sortProperty]?.formatterType;
    // 图中的字节数一律转换为bps
    const tmpValueType = valueType === EFormatterType.BYTE ? EFormatterType.BYTE_PS : valueType;
    return fieldFormatterFuncMap[tmpValueType];
  }, [sortProperty]);

  const handleRowClick = (e: any, record: LocalTableColumnProps) => {
    // 再次点击取消
    if (selectedRow?.index === record.index) {
      setSelectedRow(null);
      return;
    }
    setSelectedRow(record);
  };

  const cancelSelectedRow = () => {
    setSelectedRow(null);
  };

  // 与其他流量子菜单的不同之处
  const getSeriesName = useCallback(
    (record: IFlowAppStatFileds) => {
      let seriesName = '[--]';
      const { applicationId, categoryId, subcategoryId } = record as IFlowAppStatFileds;
      switch (currentMenu) {
        case TabKey.APP: {
          seriesName = allApplicationMap[applicationId]?.nameText || (applicationId as string);
          break;
        }
        case TabKey.CATEGORY: {
          seriesName = allCategoryMap[categoryId]?.nameText || (categoryId as string);
          break;
        }
        case TabKey.SUB_CATEGORY: {
          seriesName = allSubCategoryMap[subcategoryId]?.nameText || (subcategoryId as string);
          break;
        }
        default: {
          break;
        }
      }
      return seriesName;
    },
    [allApplicationMap, allCategoryMap, allSubCategoryMap, currentMenu],
  );

  // 导出
  const handleExport = (fileType: EFileType) => {
    const params = {
      ...queryParams(),
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
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/applications/as-export?${stringify(params)}`,
    );
  };

  const isByteToBandwidth = useMemo(() => {
    return fieldsMapping[sortProperty].formatterType === EFormatterType.BYTE;
  }, [sortProperty]);

  const chartData = useMemo(() => {
    const tmp: Record<string, TrendChartData> = {};
    if (flowHistogramData.applications) {
      flowHistogramData.applications.forEach((item: any) => {
        const seriesName: string = getSeriesName(item);
        if (seriesName) {
          if (!tmp[seriesName]) {
            tmp[seriesName] = [];
          }
          tmp[seriesName].push([item.timestamp, item[sortProperty] as number]);
        }
      });
    }
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
    return tmp;
  }, [
    flowHistogramData.applications,
    getSeriesName,
    isByteToBandwidth,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    selectedTimeInfo.startTime,
    sortProperty,
  ]);

  const detailChartData = useMemo(() => {
    const tmp: Record<string, TrendChartData> = {};
    if (flowDetailHistogramData.length > 0) {
      flowDetailHistogramData.forEach((item: any) => {
        const seriesName: string = getSeriesName(item as IFlowAppStatFileds);
        if (seriesName) {
          if (!tmp[seriesName]) {
            tmp[seriesName] = [];
          }
          tmp[seriesName].push([item.timestamp, (item[sortProperty] as number) || 0]);
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

  const dataSource = useMemo(() => {
    if (switchMenu) {
      return [];
    }

    if (flowTableData.applications) {
      return (flowTableData.applications as IFlowAppStatFileds[]).map((data, index) => {
        return {
          ...data,
          index: index + 1,
        };
      });
    }
    return [];
  }, [flowTableData.applications, switchMenu]);

  const seriesOrder = useMemo(() => {
    if (flowTableData.applications) {
      return flowTableData.applications
        .map((data: any) => {
          return getSeriesName(data);
        })
        .slice(0, 10);
    }
    return undefined;
  }, [flowTableData, getSeriesName]);

  const menuList: ITableMenuListProps<TabKey>[] = [
    {
      title: '应用',
      key: TabKey.APP,
      excludeColumn: ['categoryId', 'subcategoryId', 'networkId', 'serviceId', 'type'],
    },
    {
      title: '分类',
      key: TabKey.CATEGORY,
      excludeColumn: ['applicationId', 'subcategoryId', 'networkId', 'serviceId', 'type'],
    },
    {
      title: '子分类',
      key: TabKey.SUB_CATEGORY,
      excludeColumn: ['categoryId', 'applicationId', 'networkId', 'serviceId', 'type'],
    },
  ];

  const handleMenuChange = (menuKey: string) => {
    if (menuKey !== currentMenu) {
      setSelectedRow(null);
      setCurrentMenu(menuKey as TabKey);
      setSwitchMenu(true);
    }
  };

  const topTenChart = (
    <AnalysisChart
      data={chartData}
      loading={queryLoading}
      seriesOrder={seriesOrder}
      unitConverter={currentFormatter}
      filterCondition={filter}
      networkId={networkId}
      serviceId={serviceId}
      brushMenus={[{ text: '数据包', key: 'packet' }]}
      selectedTimeInfo={selectedTimeInfo}
      isDilldownChart={!isDimensionsTab}
    />
  );

  const selectRowToFilter = useMemo(() => {
    const res: any = [];
    if (selectedRow) {
      const { applicationId } = selectedRow;
      if (currentMenu === TabKey.APP) {
        res.push({
          field: 'applicationId',
          operator: EFilterOperatorTypes.EQ,
          operand: applicationId,
        });
        return res;
      }
    }
    return undefined;
  }, [currentMenu, selectedRow]);

  const detailChart = (
    <AnalysisChart
      loading={detailQueryLoading}
      data={detailChartData}
      filterCondition={filter}
      brushMenus={[{ text: '数据包', key: 'packet' }]}
      selectedTimeInfo={selectedTimeInfo}
      unitConverter={currentFormatter}
      selectRowToFilter={selectRowToFilter}
      isDilldownChart={!isDimensionsTab}
    />
  );

  const withMenuTable = (
    <WithMenuTable<LocalTableColumnProps, TabKey>
      needHeight={fixHeight}
      tableKey={`flow-${EMetricApiType.application}-${currentMenu}`}
      currentMenu={currentMenu}
      onMenuChange={handleMenuChange}
      menuItemList={menuList}
      columns={appTableColumns}
      loading={queryLoading}
      size="small"
      rowKey={(record) => record.index}
      onChange={handleTableChange}
      onColumnChange={setShowColumns}
      bordered={true}
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
          <Select
            defaultValue={top}
            style={{ width: 120 }}
            onChange={(key) => {
              setTop(key);
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
      dataSource={dataSource.filter((item: any) => {
        const itemName = mapEnumFieldToName(item[currentMenu], currentMenu);
        if (itemName === item[currentMenu]) {
          return false;
        }
        return true;
      })}
      onRow={(record) => {
        return {
          onClick: (event) => {
            handleRowClick(event, record);
          },
        };
      }}
    />
  );

  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          title={`${getSeriesName(selectedRow)}:${fieldsMapping[sortProperty]?.name}`}
          extra={
            <span onClick={cancelSelectedRow} className={styles.closeBtn}>
              <CloseSquareOutlined />
            </span>
          }
          style={{ marginBottom: '10px' }}
        >
          {detailChart}
        </Card>
      ) : (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          title={fieldsMapping[sortProperty]?.name}
          style={{ marginBottom: '10px' }}
        >
          {topTenChart}
        </Card>
      )}
      {withMenuTable}
    </>
  );
};

const mapStateToProps = ({ appModel: { globalSelectedTime }, SAKnowledgeModel }: ConnectState) => ({
  SAKnowledgeModel,
  globalSelectedTime,
});

export default connect(mapStateToProps)(Application);
