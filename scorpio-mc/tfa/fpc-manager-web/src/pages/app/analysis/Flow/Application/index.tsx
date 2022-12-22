import { EMetricApiType } from '@/common/api/analysis';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import EllipsisCom from '@/components/EllipsisCom';
import { getColumnParamsFunc } from '@/components/EnhancedTable/utils';
import { filterCondition2Spl } from '@/components/FieldFilter';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { ETimeType } from '@/components/GlobalTimeSelector';
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
import { EFileType } from '@/pages/app/appliance/components/ExportFile';
import { cancelQueryTask } from '@/pages/app/appliance/FlowRecord/service';
import { abortAjax, jumpNewPage, snakeCase } from '@/utils/utils';
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
import type { ConnectProps, Dispatch, IGlobalSelectedTime } from 'umi';
import { connect, useParams } from 'umi';
import { v4 as uuidv4 } from 'uuid';
import type { TrendChartData } from '../../components/AnalysisChart';
import AnalysisChart from '../../components/AnalysisChart';
import {
  completeTimePoint,
  fixedColumns,
  getFlowRecordLink,
  getPacketLink,
  tableTop,
} from '../../components/FlowAnalysis';
import styles from '../../components/FlowAnalysis/index.less';
import {
  computedDrilldownFlag,
  FilterContext,
  filterFields,
} from '../../components/PageLayoutWithFilter';
import type { ITableMenuListProps } from '../../components/WithMenuTable';
import WithMenuTable from '../../components/WithMenuTable';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
import type { IFlowAppStatFileds, IFlowQueryParams, IUriParams } from '../../typings';
import { ANALYSIS_APPLICATION_TYPE_ENUM, ESourceType } from '../../typings';

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

// 与表格字段统一
enum TabKey {
  APP = 'applicationId',
  CATEGORY = 'categoryId',
  SUB_CATEGORY = 'subcategoryId',
}

export interface IApplicationProps extends ConnectProps {
  dispatch: Dispatch;
  globalSelectedTime: IGlobalSelectedTime;
  flowTableData: Record<EMetricApiType, IFlowAppStatFileds[]>;
  flowHistogramData: Record<EMetricApiType, (IFlowAppStatFileds & { timestamp: number })[]>;
  flowDetailHistogramData: (IFlowAppStatFileds & { timestamp: number })[];
  currentPcpInfo: IOfflinePcapData | null;
  SAKnowledgeModel: ConnectState['SAKnowledgeModel'];
  queryLoading: boolean | undefined;
  detailQueryLoading: boolean | undefined;
  beforeOldestPacketArea: any;
  currentNetworkId?: string;
  currentFilterCondition?: IFilter[];
  needHeight?: number;
}

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

// const getColParams = (cols: string[]) => {
//   const tmpFilterArr = ['index', 'operate', 'action'];
//   const defCol = defaultShowColumns.filter((item) => {
//     return !tmpFilterArr.includes(item);
//   });
//   const storageCol = cols?.filter((item) => {
//     return !tmpFilterArr.includes(item);
//   });
//   return storageCol.length ? storageCol.join(',') : defCol.join(',');
// };

const getColParams = getColumnParamsFunc(['index', 'operate', 'action']);

const Application: React.FC<IApplicationProps> = (props) => {
  const {
    dispatch,
    globalSelectedTime,
    flowTableData,
    flowHistogramData,
    flowDetailHistogramData,
    SAKnowledgeModel: { allApplicationMap, allCategoryMap, allSubCategoryMap },
    queryLoading,
    detailQueryLoading,
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
  const [currentMenu, setCurrentMenu] = useState<TabKey>(TabKey.APP);
  const [switchMenu, setSwitchMenu] = useState(false);
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  const [tableQueryId, setTableQueryId] = useState<string | null>(null);
  const [chartQueryId, setChartQueryId] = useState<string | null>(null);
  // useEffect(() => {
  //   if (currentFilterCondition && addConditionToFilter) {
  //     console.log(currentFilterCondition, 'currentFilterCondition');
  //     // addConditionToFilter(currentFilterCondition);
  //   }
  // }, [currentFilterCondition]);
  // const [queryIds, setQueryIds] = useState<string[]>([]);
  const queryIds = useMemo<string[]>(() => {
    const res = [tableQueryId, chartQueryId].filter((id) => id) as string[];
    return res;
  }, [chartQueryId, tableQueryId]);
  const clearQueryIds = () => {
    setTableQueryId(null);
    setChartQueryId(null);
  };
  const [showColumns, setShowColumns] = useState<string[]>([]);

  const queryIdsRef = useRef<string[]>([]);

  const [showCancelBtn, setShowCancelBtn] = useState(false);

  const [fixHeight] = useState(() => {
    if (needHeight) {
      return needHeight;
    }
    return 0;
  });

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

  useEffect(() => {
    queryIdsRef.current = queryIds;
  }, [queryIds]);

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
    return () => {};
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
    abortAjax(['/metric/applications/as-histogram', '/metric/applications']);
    // setQueryIds([]);
    clearQueryIds();
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

  const selectedTimeInfo = useMemo(() => {
    // if (pcapFileId && currentPcpInfo) {
    //   return timeFormatter(currentPcpInfo?.filterStartTime, currentPcpInfo?.filterEndTime);
    // }
    return globalSelectedTime;
  }, [globalSelectedTime]);

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
        render: (text: string) => <EllipsisCom>{renderFunc(text)}</EllipsisCom>,
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
          sorter: SortedTypes.includes(formatterType),
          ...(renderFunc
            ? { render: (value: any) => <EllipsisCom>{renderFunc(value)}</EllipsisCom> }
            : {}),
        };
      }),
    );
    return fullColumns;
  }, []);

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
              <span
                className="link"
                onClick={() => {
                  const url = getFlowRecordLink({
                    type: EMetricApiType.application,
                    record,
                    filter,
                    networkId: networkId!,
                    serviceId,
                    pcapFileId,
                  });
                  jumpNewPage(
                    `${url}&from=${new Date(
                      selectedTimeInfo.startTime as string,
                    ).getTime()}&to=${new Date(
                      selectedTimeInfo.endTime as string,
                    ).getTime()}&timeType=${ETimeType.CUSTOM}`,
                  );
                }}
              >
                会话详单
              </span>

              <Divider type="vertical" />
              {pcapFileId === undefined && (
                <span
                  className="link"
                  onClick={() => {
                    const url = getPacketLink({
                      type: EMetricApiType.application,
                      record,
                      filter,
                      networkId: networkId!,
                      serviceId,
                    });
                    jumpNewPage(
                      `${url}&from=${new Date(
                        selectedTimeInfo.startTime as string,
                      ).getTime()}&to=${new Date(
                        selectedTimeInfo.endTime as string,
                      ).getTime()}&timeType=${ETimeType.CUSTOM}`,
                    );
                  }}
                >
                  数据包
                </span>
              )}
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
  }, [
    currentMenu,
    pcapFileId,
    filter,
    networkId,
    serviceId,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    jumpToAppTab,
  ]);

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
    return nextColumns.map((col) => {
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
    });
    // return nextColumns;
  }, [enumFieldColums, columns, actionColumn, sortProperty, sortDirection]);

  // 计算下钻标志
  const drilldownFlag = useMemo(() => {
    return computedDrilldownFlag(filter as IFilter[]);
  }, [filter]);

  const dsl = useMemo(() => {
    let internalDsl = filterCondition2Spl(filter, filterFields, (f: IFilter) => {
      return `${f.field} ${f.operator} ${f.operand}`;
    });
    if (internalDsl) {
      internalDsl += ' AND ';
    }
    internalDsl += `(type = ${String(applicationType)}) `;
    if (pcapFileId) {
      internalDsl += `AND (${`network_id=${pcapFileId}`})`;
      internalDsl += `| gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
    } else {
      internalDsl += networkId === 'ALL' ? '' : `AND (${`network_id=${networkId}`})`;
      // 下钻的情况下，如果 serviceId 为空时，不携带此条件
      // 下钻是从 ClickHouse 中取数据
      internalDsl += drilldownFlag && !serviceId ? '' : ` AND (service_id="${serviceId}")`;
      internalDsl += ` | gentimes timestamp start="${selectedTimeInfo.startTime}" end="${selectedTimeInfo.endTime}"`;
    }

    return internalDsl;
  }, [filter, networkId, serviceId, pcapFileId, selectedTimeInfo, applicationType, drilldownFlag]);

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
  const queryParams = useCallback(() => {
    return {
      sourceType,
      networkId: networkId === 'ALL' ? undefined : networkId,
      serviceId,
      packetFileId: pcapFileId,
      metricApi: EMetricApiType.application,
      startTime: selectedTimeInfo.startTime as string,
      endTime: selectedTimeInfo.endTime as string,
      interval: selectedTimeInfo.interval as number,
      sortProperty: snakeCase(sortProperty),
      type: applicationType,
      sortDirection,
      count: top,
      dsl,
      columns: getColParams({
        cols: showColumns,
        tableKey: `flow-${EMetricApiType.application}-${currentMenu}`,
      }),
      drilldown: drilldownFlag ? BOOL_YES : BOOL_NO,
    } as IFlowQueryParams;
  }, [
    sourceType,
    networkId,
    serviceId,
    pcapFileId,
    selectedTimeInfo.startTime,
    selectedTimeInfo.endTime,
    selectedTimeInfo.interval,
    sortProperty,
    applicationType,
    sortDirection,
    top,
    dsl,
    showColumns,
    currentMenu,
    drilldownFlag,
  ]);

  const queryData = useCallback(async () => {
    // 如果当前存在 queryId，先取消查询
    if (queryIdsRef.current.length > 0) {
      await cancelTask(true);
    }

    // 第一个是查询表格的查询 ID
    // 第二个是查询统计的查询 ID
    const ids = [uuidv4(), uuidv4()];
    // setQueryIds(ids);
    setTableQueryId(ids[0]);
    setChartQueryId(ids[1]);
    const tmpChartPayload: IFlowQueryParams = { ...queryParams(), chartQueryId: ids[1] };
    const tmpTablePayload: IFlowQueryParams = { ...queryParams(), tableQueryId: ids[0] };
    dispatch({
      type: 'npmdModel/queryNetworkFlow',
      payload: tmpChartPayload,
    })
      .then(({ abort }: { abort: boolean }) => {
        setSwitchMenu(false);
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
  }, [dispatch, queryParams]);

  useEffect(() => {
    if (!showColumns || !showColumns.length) {
      return;
    }
    queryData();
  }, [queryData, showColumns]);

  useEffect(() => {
    return () => {
      cancelTask(true);
    };
  }, []);

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

    const queryDetailId = uuidv4();
    // setQueryIds((prevQueryIds) => {
    //   return [...prevQueryIds, queryDetailId];
    // });
    setChartQueryId(queryDetailId);
    payload.queryId = queryDetailId;

    dispatch({
      type: 'npmdModel/queryNetworkFlowDetailHistogramData',
      payload,
    }).then(() => {
      setChartQueryId(null);
      // setQueryIds((prevQueryIds) => prevQueryIds.filter((id) => id !== queryDetailId));
    });
  }, [queryParams, selectedRow, currentMenu, dispatch]);

  const handleTableChange = (pagination: TablePaginationConfig, _filter: any, sorter: any) => {
    if (sorter.field !== sortProperty) {
      setSortProperty(sorter.field);
      setSortDirection(ESortDirection.DESC);
    } else {
      setSortDirection(sorter.order === 'ascend' ? ESortDirection.ASC : ESortDirection.DESC);
    }
  };

  // 图表中的数据格式化方法
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

  // 切换条件，列表刷新，此时图中的过滤条件清空
  useEffect(() => {
    setSelectedRow(null);
  }, [filter]);

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

  const isByteToBandwidth = useMemo(() => {
    return fieldsMapping[sortProperty].formatterType === EFormatterType.BYTE;
  }, [sortProperty]);

  const chartData = useMemo(() => {
    const tmp: Record<string, TrendChartData> = {};
    if (flowHistogramData.applications) {
      flowHistogramData.applications.forEach((item) => {
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
      flowDetailHistogramData.forEach((item) => {
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
        .map((data) => {
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
      markArea={beforeOldestPacketArea}
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
      markArea={beforeOldestPacketArea}
    />
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
    window.open(
      `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/applications/as-export?${stringify(params)}`,
    );
  };

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
      onColumnChange={(cols) => setShowColumns(cols)}
      bordered={true}
      defaultShowColumns={defaultShowColumns}
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

  const chartCardTitle = useMemo(() => {
    return fieldsMapping[sortProperty]?.formatterType === EFormatterType.BYTE
      ? `${fieldsMapping[sortProperty]?.name.split('字节数')[0]}带宽`
      : fieldsMapping[sortProperty]?.name;
  }, [sortProperty]);

  return (
    <>
      {selectedRow ? (
        <Card
          size="small"
          bodyStyle={{ padding: 0, paddingTop: '8px' }}
          title={`${getSeriesName(selectedRow)}:${chartCardTitle}`}
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
          title={chartCardTitle}
          style={{ marginBottom: '10px' }}
        >
          {topTenChart}
        </Card>
      )}
      {withMenuTable}
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
  SAKnowledgeModel,
  loading: { effects },
}: ConnectState) => ({
  flowTableData,
  flowHistogramData,
  currentPcpInfo,
  flowDetailHistogramData,
  SAKnowledgeModel,
  globalSelectedTime,
  queryLoading: effects['npmdModel/queryNetworkFlow'],
  detailQueryLoading: effects['npmdModel/queryNetworkFlowDetailHistogramData'],
  beforeOldestPacketArea,
});

// @ts-ignore
export default connect(mapStateToProps)(Application);
