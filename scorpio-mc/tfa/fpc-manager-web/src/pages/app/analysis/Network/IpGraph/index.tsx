import { EMetricApiType } from '@/common/api/analysis';
import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { BOOL_NO, BOOL_YES } from '@/common/dict';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import FieldFilter from '@/components/FieldFilter';
import type {
  EFieldType,
  IEnumValue,
  IFilter,
  IFilterCondition,
} from '@/components/FieldFilter/typings';
import {
  EFieldOperandType,
  EFilterGroupOperatorTypes,
  EFilterOperatorTypes,
} from '@/components/FieldFilter/typings';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { ETimeType } from '@/components/GlobalTimeSelector';
import Message from '@/components/Message';
import type { ConnectState } from '@/models/connect';
import ExportFile from '@/pages/app/appliance/components/ExportFile';
import { cancelQueryTask } from '@/pages/app/appliance/FlowRecord/service';
import { packetUrl } from '@/pages/app/appliance/Packet';
import type { INetwork } from '@/pages/app/configuration/Network/typings';
import { abortAjax, getLinkUrl, isIpv4, jumpNewPage, timeFormatter } from '@/utils/utils';
import { ReloadOutlined, RollbackOutlined, StopOutlined } from '@ant-design/icons';
import { Button, Col, Form, Input, message, Modal, Row, Select, Space, Spin, Tooltip } from 'antd';
import _, { camelCase, snakeCase } from 'lodash';
import { stringify } from 'qs';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, TTheme } from 'umi';
import { connect, history, useParams } from 'umi';
import { v4 as uuidv4 } from 'uuid';
import type { EModelAlias, IFieldProperty } from '../../components/fieldsManager';
import {
  EFieldEnumValueSource,
  EFormatterType,
  getEnumValueFromModelNext,
} from '../../components/fieldsManager';
import type { IEdge, INode } from '../../components/ForceGraph';
import { defaultDarkTheme, defaultLightTheme } from '../../components/ForceGraph';
import Comparegraph from '../../components/ForceGraph/components/CompareGraph';
import type { IForceGraphProps } from '../../components/ForceGraph/typings';
import { EForceGraphIndex } from '../../components/ForceGraph/typings';
import type { IOfflinePcapData } from '../../OfflinePcapAnalysis/typing';
import {
  alterGraphHistory,
  createGraphHistory,
  deleteGraphHistory,
  deleteHistoryRelations,
  queryIpConversationGraph,
  queryIpConversationGraphHistory,
} from '../../service';
import type {
  GraphData,
  IIpConversationGraph,
  IIpConversationGraphHistory,
  IIpConversationGraphParams,
  IIpConversationHistoryParams,
  NodeMap,
} from '../../typings';
import { ESortDirection, ESourceType } from '../../typings';
import { fieldsMapping } from './typings';

const { Option } = Select;

const initFormValues = (ip?: any) => {
  return {
    ipAddress: ip,
    sortProperty: 'totalBytes',
    minEstablishedSessions: 0,
    minTotalBytes: 0,
    count: 100,
  };
};

export enum EIpGraphMode {
  'RTP' = 'rtp',
}
export interface IIPGraphProps {
  globalSelectedTime: Required<IGlobalTime>;
  theme: TTheme;
  currentPcpInfo: IOfflinePcapData;
  dispatch: Dispatch;
  allNetworks: INetwork[];
  /** 历史画布 */
  historyGraph?: boolean;
  /** 自定义field */
  customField?: Record<string, IFieldProperty>;
  /** 初始化状态 */
  initialCondition?: IFilterCondition;
  /** 默认网络id,传入后无法更改网络 */
  customNeworkId?: string;
  /** 模式 */
  mode?: EIpGraphMode;
  /** 节点点击回调 */
  onNodeClick?: (action: any, node: INode, id?: string | undefined) => void;
  /** 边回调问题 */
  onEdgeClick?: (action: any, edge: IEdge, id?: string | undefined) => void;
}

interface IFormData {
  /** IP/IP 网段 */
  ipAddress?: string;
  /** 排序字段 */
  sortProperty: 'establishedSessions' | 'totalBytes';
  /** 最小新建会话数 */
  minEstablishedSessions?: number;
  /** 最小总字节数 */
  minTotalBytes?: number;
  /** IP 会话对 Top N */
  count: number;
}

/** 节点操作菜单 */
export enum EActionMenuKey {
  /** IP下钻 */
  IP_DILLDOWN = 'ip-dilldown',
  /** 添加IP过滤 */
  IP_FILTER = 'ip-filter',
  /** 跳转到会话详单 */
  FLOW_RECORD = 'flow-record',
  /** 跳转到流量分析 */
  FLOW_LOCATION = 'flow/location',
  /** 跳转到数据包 */
  PACKET = 'packet',
  /** 删除边 */
  DELETE_EDGE = 'delete_edge',
  /** 删除点 */
  DELETE_NODE = 'delete_node',
  /** 新建历史画布 */
  CREATE_HISTORY = 'create_history',
  /** 保存为历史画布 */
  SAVE_HISTORY = 'save_history',
  /** RTP分段分析 */
  RTP_SEGMENT = 'rtp_segment',
  /** RTP流分析 */
  RTP_FLOW = 'rtp_flow',
  /** SIP详单 */
  SIP_REOCORD = 'sip_record',
}

const getInitFilterCondition = ({
  networkParamId,
  initialCondition,
}: {
  networkParamId: string;
  initialCondition: IFilterCondition;
}) => {
  const res: IFilter[] = [];
  if (history.location.query?.filter) {
    res.push(
      ...(JSON.parse(decodeURIComponent(history.location.query.filter as string)) as IFilter[]),
    );
  }
  if (networkParamId) {
    res.push({
      id: uuidv4(),
      field: 'network_id',
      /**
       * 字段的名字
       */
      operator: EFilterOperatorTypes.EQ,
      operand: networkParamId,
    });
  }

  return [...res, ...initialCondition];
};

const syncFilterToFormData = (filterCondition: any[]) => {
  let ipAddress = undefined,
    minTotalBytes = 0,
    minEstablishedSessions = 0,
    sortProperty = 'totalBytes' as 'establishedSessions' | 'totalBytes',
    count = 100;

  filterCondition.forEach((filter) => {
    switch ((filter as IFilter).field) {
      case 'ip_address': {
        ipAddress = (filter as IFilter).operand as string;
        break;
      }
      case 'min_established_sessions': {
        minEstablishedSessions = (filter as IFilter).operand as number;
        break;
      }
      case 'min_total_bytes': {
        minTotalBytes = (filter as IFilter).operand as number;
        break;
      }
      case 'sort_property': {
        sortProperty = (filter as IFilter).operand as 'establishedSessions' | 'totalBytes';
        break;
      }
      case 'count': {
        count = (filter as IFilter).operand as number;
        break;
      }
    }
  });
  return {
    ipAddress,
    minTotalBytes,
    minEstablishedSessions,
    sortProperty,
    count,
  };
};

const IPGraph: React.FC<IIPGraphProps> = ({
  globalSelectedTime,
  currentPcpInfo,
  dispatch,
  theme,
  historyGraph = true,
  customField = {},
  initialCondition = [],
  customNeworkId,
  mode,
  onNodeClick,
  onEdgeClick,
}) => {
  const { networkId: networkParamId, pcapFileId } = useParams() as {
    networkId: string;
    pcapFileId: string;
  };

  const [defaultNetworkId, setDefaultNetworkId] = useState<string>('');

  const networkId = useMemo(() => {
    return customNeworkId || defaultNetworkId;
  }, [customNeworkId, defaultNetworkId]);

  const queryAnalysisId = useMemo(() => {
    return networkId?.length ? networkId : pcapFileId;
  }, [networkId, pcapFileId]);

  const urlPath = useMemo(() => {
    return networkId?.length ? 'network' : 'offline';
  }, [networkId]);

  const pcapDetail = useMemo(() => {
    return currentPcpInfo;
  }, [currentPcpInfo]);

  // 计算时间
  const selectedTimeInfo = useMemo(() => {
    return timeFormatter(pcapDetail?.filterStartTime, pcapDetail?.filterEndTime);
  }, [pcapDetail]);

  const [chartHeight, setChartHeight] = useState<number>(500);
  // 查询数据的 loading
  const [queryLoading, setQueryLoading] = useState(false);
  // 停止查询 loading
  const [cancelQueryTaskLoading, setCancelQueryTaskLoading] = useState(false);
  const [queryConversationTaskId, setQueryConversationTaskId] = useState<string | null>();
  const queryConversationTaskIdRef = useRef<string | null>();
  const [showCancelBtn, setShowCancelBtn] = useState(false);

  const [graphData, setGraphData] = useState<GraphData>({
    originData: { edges: [], nodes: [] },
    nodeMap: {},
    minAndMax: [0, 0],
  });

  const [exportConversationData, setExportConversationData] = useState<IIpConversationGraph[]>([]);

  const [formData, setFormData] = useState<IFormData>(
    () => {
      if (history.location.query?.ipAddress) {
        return initFormValues(history.location.query?.ipAddress) as any
      }
      return syncFilterToFormData(getInitFilterCondition({ networkParamId, initialCondition }))
    }
  );

  /** 历史画布相关 */
  /** 历史画布列表 */
  const [ipGraphHistry, setIpGraphHistory] = useState<IIpConversationGraphHistory[]>([]);

  /** 选取的历史画布Id */
  const [selectHistoryIds, setSelectHistoryIds] = useState<string[]>([]);
  /** 选取的历史画布详细信息 */
  const selectedHistory = useMemo<IIpConversationGraphHistory[]>(() => {
    const historyList: IIpConversationGraphHistory[] = [];
    selectHistoryIds.forEach((id: string) => {
      const historyItem = ipGraphHistry.find((h) => h.id === id);
      if (historyItem) {
        historyList.push(historyItem);
      }
    });
    return historyList;
  }, [ipGraphHistry, selectHistoryIds]);

  /** 刷取操作类型 */
  const [brushType, setBrushType] = useState<'create' | 'update' | 'close'>('close');
  /** 刷取结果 */
  const [brushResult, setBrushResult] = useState<IIpConversationGraph[]>([]);
  /** 当前 创建｜修改的ip关系图 */
  const [currentGraphInfo, setCurrentGraphInfo] = useState<
    IIpConversationGraphHistory | Record<string, any>
  >({});

  // 过滤条件
  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(() => {
    return getInitFilterCondition({ networkParamId, initialCondition });
  });

  // 增加过滤条件
  const addConditionToFilter = (condition: IFilterCondition) => {
    setFilterCondition([...filterCondition, ...(condition as IFilter[])]);
  };

  const filterField = useMemo(() => {
    const fieldList: {
      enumValue?: IEnumValue[] | undefined;
      title: string;
      dataIndex: string;
      operandType: EFieldOperandType;
      operators?: EFilterOperatorTypes[];
      ranges: [number, number];
      type?: EFieldType;
    }[] = [];
    const fieldsMap = Object.keys(customField).length === 0 ? fieldsMapping : customField;
    Object.keys(fieldsMap).forEach((field: string) => {
      if (!pcapFileId || field !== 'networkId') {
        const { formatterType, name, filterOperandType, filterFieldType, enumSource, enumValue } =
          fieldsMap[field];
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

        // 离线文件下不加入网络过滤条件
        fieldList.push({
          title: name,
          dataIndex: snakeCase(field),
          operandType: filterOperandType as EFieldOperandType,
          type: filterFieldType as EFieldType,
          operators: [EFilterOperatorTypes.EQ],
          ranges: [10, 1000],
          ...(isEnum
            ? {
                enumValue: enumValueList,
              }
            : {}),
        });
      }
    });

    if (historyGraph) {
      fieldList.push({
        title: '历史画布',
        disabled: selectHistoryIds.length > 2,
        dataIndex: 'ip_history',
        operandType: EFieldOperandType.ENUM,
        operators: [EFilterOperatorTypes.EQ],
        ranges: [10, 1000],
        enumValue: ipGraphHistry.map((item) => {
          return {
            text: item.name,
            value: item.id,
          };
        }),
      } as any);
    }
    return fieldList;
  }, [customField, historyGraph, ipGraphHistry, pcapFileId, selectHistoryIds.length]);

  /** 获取历史画布 */
  useEffect(() => {
    setSelectHistoryIds(
      filterCondition.filter((c: any) => c.field === 'ip_history').map((c: any) => c.operand || ''),
    );
    const newNetworkId =
      (filterCondition.find((c: any) => c.field === 'network_id') as IFilter)?.operand || '';

    if (newNetworkId) {
      setDefaultNetworkId(newNetworkId as string);
    } else {
      setDefaultNetworkId('');
    }
  }, [filterCondition]);

  useEffect(() => {
    const tmpQuery = history.location?.query || {};
    delete tmpQuery.ipAddress;
    history.replace({ pathname: history.location.pathname, query: tmpQuery });
  }, []);

  useEffect(() => {
    queryConversationTaskIdRef.current = queryConversationTaskId;
  }, [queryConversationTaskId]);

  useEffect(() => {
    dispatch({
      type: 'networkModel/queryAllNetworks',
    });
  }, []);

  const getQeryParams = useCallback(
    (drilldownIpAddress?: string) => {
      // 组装dsl 查询条件
      let extraDsl = '';
      if (networkId) {
        extraDsl += `(network_id=${networkId})`;
      } else if (pcapFileId) {
        extraDsl += `(network_id=${pcapFileId})`;
      }

      const { ipAddress, minTotalBytes, minEstablishedSessions, sortProperty, count } = formData;

      // ip过滤条件
      // 如果有下钻的情况，优先以下载的情况稳住
      const finallyIp = drilldownIpAddress || ipAddress;
      if (finallyIp) {
        extraDsl += `${extraDsl.length ? ' and' : ''} ip_address = ${finallyIp}`;
      }

      if (extraDsl) {
        extraDsl = `(${extraDsl})`;
      }

      const params: IIpConversationGraphParams = {
        ...(() => {
          if (networkId) {
            return {
              sourceType: ESourceType.NETWORK,
              networkId,
            };
          }
          if (pcapFileId) {
            return {
              sourceType: ESourceType.OFFLINE,
              packetFileId: pcapFileId,
            };
          }
          return {};
        })(),
        startTime: pcapFileId ? selectedTimeInfo.startTime : globalSelectedTime.startTime,
        endTime: pcapFileId ? selectedTimeInfo.endTime : globalSelectedTime.endTime,
        interval: pcapFileId ? selectedTimeInfo.interval : globalSelectedTime.interval,
        serviceId: '',
        sortProperty: snakeCase(sortProperty),
        sortDirection: ESortDirection.DESC,
        minTotalBytes,
        minEstablishedSessions,
        count,
        dsl: `${extraDsl} | gentimes timestamp start="${
          pcapFileId ? selectedTimeInfo.startTime : globalSelectedTime.startTime
        }" end="${pcapFileId ? selectedTimeInfo.endTime : globalSelectedTime.endTime}"`,
        // 只要有 IP 查询，就下钻到会话详单
        drilldown: finallyIp ? BOOL_YES : BOOL_NO,
        ...(() => {
          if (mode === EIpGraphMode.RTP) {
            return {
              aggApplication: '1',
              drilldown: '1',
            };
          }
          return {};
        })(),
      };
      return params;
    },
    [
      networkId,
      pcapFileId,
      formData,
      selectedTimeInfo.startTime,
      selectedTimeInfo.endTime,
      selectedTimeInfo.interval,
      globalSelectedTime.startTime,
      globalSelectedTime.endTime,
      globalSelectedTime.interval,
      mode,
    ],
  );

  /** 转换成 Graph 所需要的数据 */
  const transformData = useCallback(
    /** IP 会话对查询的数据 */
    (
      ipConversationData: IIpConversationGraph[],
      /** 是否需要和原有数据进行合并处理 */
      merge: boolean = false,
      /** 历史数据 */
      isHistory: boolean = false,
      /** 历史id 用于下钻 可选 */
      historyId?: string,
    ) => {
      const transform = (
        nodes: string[],
        edges: {
          source: string;
          target: string;
          totalBytes: number;
          establishedSessions: number;
          applications?: number[];
        }[],
        nextNodeMap: Record<string, NodeMap>,
        min: any,
        max: any,
      ) => {
        let transformEdge = edges;
        for (let i = 0; i < ipConversationData.length; i += 1) {
          const row = ipConversationData[i];
          const ipAAddress = row.ipAAddress as string;
          const ipBAddress = row.ipBAddress as string;

          // 组装 nodes
          if (!nodes.includes(ipAAddress)) {
            nodes.push(ipAAddress);
            nextNodeMap[ipAAddress] = {
              id: ipAAddress,
              [formData.sortProperty]: 0,
            };
          }
          if (!nodes.includes(ipBAddress)) {
            nodes.push(ipBAddress);
            nextNodeMap[ipBAddress] = {
              id: ipBAddress,
              [formData.sortProperty]: 0,
            };
          }

          // 组装边信息

          // 下钻出来的数据一定会包含之前的历史数据
          // 用新的数据代替旧的数据
          transformEdge = transformEdge.filter((item) => {
            return (
              `${item.source}_${item.target}` !== `${ipAAddress}_${ipBAddress}` &&
              `${item.target}_${item.source}` !== `${ipAAddress}_${ipBAddress}`
            );
          });

          edges.push({
            source: ipAAddress,
            target: ipBAddress,
            establishedSessions: row.establishedSessions as number,
            totalBytes: row.totalBytes as number,
            applications: row?.applications as number[],
          });
          if (nextNodeMap[ipAAddress] && nextNodeMap[ipBAddress]) {
            nextNodeMap[ipAAddress][formData.sortProperty]! += row[
              camelCase(formData.sortProperty)
            ] as number;
            nextNodeMap[ipBAddress][formData.sortProperty]! += row[
              camelCase(formData.sortProperty)
            ] as number;
          }
        }

        let transformMin = min;
        let transformMax = max;

        // 每个节点的子节点去重
        Object.keys(nextNodeMap).forEach((id, idx) => {
          if (idx === 0) {
            transformMin = nextNodeMap[id][formData.sortProperty]!;
            transformMax = nextNodeMap[id][formData.sortProperty]!;
          }
          if (nextNodeMap[id][formData.sortProperty]! > max) {
            transformMax = nextNodeMap[id][formData.sortProperty]!;
          }
          if (nextNodeMap[id][formData.sortProperty]! < min) {
            transformMin = nextNodeMap[id][formData.sortProperty]!;
          }
        });

        return {
          nodeMap: nextNodeMap,
          minAndMax: [transformMin, transformMax],
          originData: { nodes, edges },
        };
      };
      if (isHistory) {
        if (!merge) {
          return transform([], [], {}, 0, 0) as GraphData;
        }
        // historyId
        if (historyId) {
          setIpGraphHistory((prev) => {
            const newGraph = _.cloneDeep(prev);
            const index = newGraph.findIndex((item) => item.id === historyId);
            if (index >= 0) {
              const historyItem = newGraph[index];
              const nodes = historyItem?.graphData?.originData.nodes || [];
              const edges = historyItem?.graphData?.originData.edges || [];

              const nextNodeMap = historyItem?.graphData?.nodeMap ?? {};
              const transformMin = historyItem?.graphData?.minAndMax[0] ?? 0;
              const transformMax = historyItem?.graphData?.minAndMax[1] ?? 0;
              const historyData = transform(
                nodes,
                edges,
                nextNodeMap,
                transformMin,
                transformMax,
              ) as GraphData;
              newGraph.splice(index, 1, {
                ...newGraph[index],
                graphData: historyData,
              });
              return newGraph;
            }
            return newGraph;
          });
        }
        return;
      } else {
        setGraphData((prev) => {
          const nodes = merge ? _.cloneDeep(prev.originData.nodes) : [];
          const edges = merge ? _.cloneDeep(prev.originData.edges) : [];

          const nextNodeMap = merge ? _.cloneDeep(prev.nodeMap) : {};
          const transformMin = merge ? prev.minAndMax[0] ?? 0 : 0;
          const transformMax = merge ? prev.minAndMax[1] ?? 0 : 0;
          return transform(nodes, edges, nextNodeMap, transformMin, transformMax) as GraphData;
        });
        return;
      }
    },
    [formData.sortProperty],
  );

  /**
   * 停止查询任务
   * @params silence 当为 true 时不触发任何提示
   */
  const cancelTask = async (silent: boolean = false) => {
    const queryId = queryConversationTaskIdRef.current;
    if (!queryId) {
      return;
    }

    const cancelMsgKey = 'cancel-task-key';
    if (!silent) {
      message.loading({ content: '正在停止...', key: cancelMsgKey, duration: 0 });
      setCancelQueryTaskLoading(true);
    }

    abortAjax([
      `/metric/${EMetricApiType.ipConversation}/as-histogram`,
      `/metric/${EMetricApiType.ipConversation}`,
    ]);
    setQueryConversationTaskId(null);
    const { success } = await cancelQueryTask({ queryId });

    if (silent) {
      return;
    }
    message.destroy(cancelMsgKey);
    if (!success) {
      message.warning('停止失败');
    } else {
      message.success('停止成功');
    }

    setCancelQueryTaskLoading(false);
    setQueryLoading(false);
  };
  // ====== 停止查询按钮 =====

  const handleHeightChange = useCallback((height: number) => {
    setChartHeight(height);
  }, []);

  const convertIpConversation = (
    ipConversationData: IIpConversationGraph[],
    isDilldown: boolean,
    isHistory = false,
    historyId?: string,
  ) => {
    const ipGroupMap: Record<string, IIpConversationGraph> = {};
    for (let index = 0; index < ipConversationData.length; index++) {
      const row = ipConversationData[index];

      const { ipAAddress, ipBAddress, ...restFields } = row;
      const AB = `${ipAAddress}__${ipBAddress}`;
      const BA = `${ipBAddress}__${ipAAddress}`;

      const abValue = ipGroupMap[AB];
      const baValue = ipGroupMap[BA];

      const currentValue = abValue || baValue;

      if (currentValue) {
        // 除了2个 IP外，其他的字段全部相加
        // 遍历字段，相加
        const fieldList = Object.keys(restFields);
        for (let j = 0; j < fieldList.length; j++) {
          const key = fieldList[j];
          // 一定不是 ipAAddress, ipBAddress
          currentValue[key] = (currentValue[key] + (restFields[key] || 0)) as number;
        }

        ipGroupMap[abValue ? AB : BA] = {
          ...currentValue,
        };
      } else {
        ipGroupMap[AB] = { ...row };
      }
    }
    const mergedData: IIpConversationGraph[] = Object.values(ipGroupMap);
    return transformData(mergedData, isDilldown, isHistory, historyId);
  };

  /** 查询历史画布信息 */
  const updateHistoryGraph = async (updateId?: string) => {
    const { success, result } = await queryIpConversationGraphHistory();
    if (!success) {
      return;
    }
    setIpGraphHistory([
      ...result.map((graph: IIpConversationHistoryParams) => {
        return {
          ...graph,
          graphData: convertIpConversation(JSON.parse(graph.data!), false, true),
        };
      }),
    ]);
    if (updateId) {
      setTimeout(() => {
        /** 更新画布信息 */
        if (selectHistoryIds.includes(updateId)) {
          setSelectHistoryIds([...selectHistoryIds]);
        }
      });
    }
  };

  useEffect(() => {
    updateHistoryGraph();
  }, []);

  /**
   * 查询 graph 数据
   * @params drilldownIpAddress 下钻时携带的 IP 地址
   */
  const queryGraph = useCallback(
    async (drilldownIpAddress?: string, isHistory = false, historyId?: string) => {
      // 如果当前存在 queryId，先取消查询
      // if (queryConversationTaskIdRef.current) {
      //   await cancelTask(true);
      // }

      const params = getQeryParams(drilldownIpAddress);

      // 查询 ID
      const queryId = uuidv4();
      setQueryConversationTaskId(queryId);

      const isDilldown = !!drilldownIpAddress;
      const messageKey = 'dilldown_message';
      // 如果不是下钻就显示 loading
      if (isDilldown) {
        message.loading({ content: '下钻中...', key: messageKey, duration: 0 });
      } else {
        setQueryLoading(true);
      }

      const { status, result: ipConversationData = [] } = await queryIpConversationGraph({
        ...params!,
        queryId,
      });
      setExportConversationData(ipConversationData);
      // status=0时表示请求被取消了
      if (!status) {
        return;
      }
      // 清空查询 ID
      setQueryConversationTaskId(null);
      // 下钻某个 IP 时要进行数据合并，其他时候都不合并
      // 数据合并累加
      // A->B 和 B->A 的数据都会存在，所以要合并成1条

      convertIpConversation(ipConversationData, isDilldown, isHistory, historyId);

      if (isDilldown) {
        message.success({ content: '下钻数据加载完成', key: messageKey, duration: 1 });
      } else {
        setQueryLoading(false);
      }
    },
    [transformData, getQeryParams],
  );

  const handleFinish = useCallback(() => {
    setFormData(syncFilterToFormData(filterCondition));
  }, [filterCondition]);


  const getJumpUrl = useCallback(
    ({
      type,
      nodeInfo,
      edgeInfo,
      target,
    }: {
      // 节点类型
      type: 'node' | 'edge';
      // 节点数据
      nodeInfo?: INode;
      // 边数据
      edgeInfo?: IEdge;
      // 跳转目标
      target: EActionMenuKey;
    }) => {
      let flowRecordFilter;
      let packetFilter;
      let flowLocationFilter;

      if (type === 'node') {
        const ipAddress = nodeInfo!.id;
        const isV4 = isIpv4(ipAddress);
        packetFilter = [
          {
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: ipAddress,
          },
        ];
        flowLocationFilter = [
          {
            field: 'ip_address',
            operator: EFilterOperatorTypes.EQ,
            operand: ipAddress,
          },
        ];
        flowRecordFilter = {
          operator: EFilterGroupOperatorTypes.OR,
          group: [
            {
              field: isV4 ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: ipAddress,
            },
            {
              field: isV4 ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: ipAddress,
            },
          ],
        };
      } else {
        const sourceIp = (edgeInfo!.source as any).id;
        const targetIp = (edgeInfo!.target as any).id;
        const srcIsV4 = isIpv4(sourceIp);
        const targetIsV4 = isIpv4(targetIp);

        packetFilter = [
          {
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: sourceIp,
          },
          {
            field: 'ipAddress',
            operator: EFilterOperatorTypes.EQ,
            operand: targetIp,
          },
        ];
        flowLocationFilter = [
          {
            field: 'ip_address',
            operator: EFilterOperatorTypes.EQ,
            operand: sourceIp,
          },
          {
            field: 'ip_address',
            operator: EFilterOperatorTypes.EQ,
            operand: targetIp,
          },
        ];
        flowRecordFilter = {
          operator: EFilterGroupOperatorTypes.OR,
          group: [
            {
              operator: EFilterGroupOperatorTypes.AND,
              group: [
                {
                  field: srcIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                  operator: EFilterOperatorTypes.EQ,
                  operand: sourceIp,
                },
                {
                  field: targetIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                  operator: EFilterOperatorTypes.EQ,
                  operand: targetIp,
                },
              ],
            },
            {
              operator: EFilterGroupOperatorTypes.AND,
              group: [
                {
                  field: targetIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
                  operator: EFilterOperatorTypes.EQ,
                  operand: targetIp,
                },
                {
                  field: srcIsV4 ? 'ipv4_responder' : 'ipv6_responder',
                  operator: EFilterOperatorTypes.EQ,
                  operand: sourceIp,
                },
              ],
            },
          ],
        };
      }

      // 跳转到会话详单
      if (target === EActionMenuKey.FLOW_RECORD) {
        if (pcapFileId) {
          return getLinkUrl(
            `/analysis/${urlPath}/${queryAnalysisId}/flow-record?from=${new Date(
              globalSelectedTime.originStartTime,
            ).valueOf()}&to=${new Date(globalSelectedTime.originEndTime).valueOf()}&timeType=${
              ETimeType.CUSTOM
            }&filter=${encodeURIComponent(JSON.stringify([flowRecordFilter]))}`,
          );
        } else {
          return getLinkUrl(
            `/analysis/trace/flow-record?from=${new Date(
              globalSelectedTime.originStartTime,
            ).valueOf()}&to=${new Date(globalSelectedTime.originEndTime).valueOf()}&timeType=${
              ETimeType.CUSTOM
            }&filter=${encodeURIComponent(
              JSON.stringify([
                flowRecordFilter,
                ...(networkId
                  ? [
                      {
                        field: 'network_id',
                        operator: EFilterOperatorTypes.EQ,
                        operand: queryAnalysisId,
                      },
                    ]
                  : []),
              ]),
            )}`,
          );
        }
      }
      // 跳转到数据包
      if (target === EActionMenuKey.PACKET) {
        return getLinkUrl(
          `${packetUrl}?&filter=${encodeURIComponent(
            JSON.stringify([
              ...packetFilter,
              ...(networkId
                ? [
                    {
                      field: 'network_id',
                      operator: EFilterOperatorTypes.EQ,
                      operand: queryAnalysisId,
                    },
                  ]
                : []),
            ]),
          )}&from=${new Date(globalSelectedTime.originStartTime).valueOf()}&to=${new Date(
            globalSelectedTime.originEndTime,
          ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
        );
      }
      // 跳转到流量统计
      if (target === EActionMenuKey.FLOW_LOCATION) {
        if (pcapFileId) {
          return getLinkUrl(
            `/analysis/${urlPath}/${queryAnalysisId}/flow/ip-conversation?filters=${encodeURIComponent(
              JSON.stringify(flowLocationFilter),
            )}&from=${new Date(globalSelectedTime.originStartTime).valueOf()}&to=${new Date(
              globalSelectedTime.originEndTime,
            ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
          );
        } else if (networkId) {
          return getLinkUrl(
            `/analysis/performance/network/${networkId}/flow/?filters=${encodeURIComponent(
              JSON.stringify(flowLocationFilter),
            )}&from=${new Date(globalSelectedTime.originStartTime).valueOf()}&to=${new Date(
              globalSelectedTime.originEndTime,
            ).valueOf()}&timeType=${ETimeType.CUSTOM}`,
          );
        }
      }
      return '';
    },
    [
      pcapFileId,
      networkId,
      globalSelectedTime.originEndTime,
      globalSelectedTime.originStartTime,
      queryAnalysisId,
      urlPath,
    ],
  );
  /** 处理访问关系节点点击 */
  const handleMenuClick: IForceGraphProps['onNodeClick'] = (action, node, id) => {
    const { key } = action;
    switch (key) {
      case EActionMenuKey.PACKET:
      case EActionMenuKey.FLOW_LOCATION:
      case EActionMenuKey.FLOW_RECORD:
        jumpNewPage(getJumpUrl({ type: 'node', target: key, nodeInfo: node }));
        break;
      case EActionMenuKey.IP_FILTER:
        addConditionToFilter([
          {
            id: uuidv4(),
            field: 'ip_address',
            operator: EFilterOperatorTypes.EQ,
            operand: node.id,
          } as IFilter,
        ]);
        break;
      case EActionMenuKey.IP_DILLDOWN:
        if (id === 'default') {
          queryGraph(node.id);
        } else {
          queryGraph(node.id, true, id);
        }

        break;
      case EActionMenuKey.CREATE_HISTORY:
      case EActionMenuKey.SAVE_HISTORY:
        if (id === 'default') {
          const edgeData = graphData?.originData?.edges;
          const result = edgeData
            .filter((data) => data?.source === node.id || data?.target === node.id)
            .map((data) => ({
              ...data,
              ipBAddress: data.source,
              ipAAddress: data.target,
            }));
          setBrushResult(result);
        } else {
          const graphHistoryData = ipGraphHistry.find((h) => h.id === id);
          if (!graphHistoryData) {
            return;
          }
          const edgeData = graphHistoryData?.graphData?.originData.edges || [];
          const result = edgeData.filter(
            (data) => data.source === node.id || data.target === node.id,
          );
          setBrushResult(
            result.map((data) => ({
              ...data,
              ipBAddress: data.source,
              ipAAddress: data.target,
            })),
          );
        }
        setCurrentGraphInfo({});

        if (action.key === EActionMenuKey.CREATE_HISTORY) {
          setBrushType('create');
        } else {
          setBrushType('update');
        }
        break;
      default:
        break;
    }
    if (onNodeClick) {
      onNodeClick(action, node, id);
    }
  };

  /** 点击边回调 */
  const handleEdgeClick: IForceGraphProps['onEdgeClick'] = (action, edge, id) => {
    const { key } = action;
    switch (key) {
      case EActionMenuKey.PACKET:
      case EActionMenuKey.FLOW_LOCATION:
      case EActionMenuKey.FLOW_RECORD:
        jumpNewPage(getJumpUrl({ type: 'edge', target: key, edgeInfo: edge }));
        break;
      case EActionMenuKey.CREATE_HISTORY:
      case EActionMenuKey.SAVE_HISTORY:
        setBrushResult([
          {
            ...edge.__proto__,
            ipBAddress: edge.__proto__?.source,
            ipAAddress: edge.__proto__?.target,
          },
        ]);
        setCurrentGraphInfo({});

        if (action.key === EActionMenuKey.CREATE_HISTORY) {
          setBrushType('create');
        } else {
          setBrushType('update');
        }
        break;
      default:
        break;
    }
    if (onEdgeClick) {
      onEdgeClick(action, edge, id);
    }
  };
  /** 刷取回调 */
  const handleBrushEnd: IForceGraphProps['onBrushEnd'] = (action, result) => {
    const { key } = action;
    switch (key) {
      case EActionMenuKey.CREATE_HISTORY:
        setBrushResult(result);
        setCurrentGraphInfo({});
        setBrushType('create');
        break;
      case EActionMenuKey.SAVE_HISTORY:
        setBrushResult(result);
        setCurrentGraphInfo({});
        setBrushType('update');
        break;
      default:
        break;
    }
  };

  /** 删除画布 */
  const handleDeleteGraph = (id: string) => {
    Modal.confirm({
      title: '确定要删除吗?',
      onOk: async () => {
        const { success } = await deleteGraphHistory(id);
        if (success) {
          message.success('删除成功!');
          updateHistoryGraph();
          const index = selectedHistory.findIndex((h) => h.id === id);
          if (index !== undefined) {
            setSelectHistoryIds([
              ...selectHistoryIds.slice(0, index),
              ...(index === selectHistoryIds.length - 1 ? [] : selectHistoryIds.slice(index + 1)),
            ]);
            setFilterCondition(
              filterCondition.filter((c) => {
                if ((c as IFilter)?.field === 'ip_history' && (c as IFilter)?.operand !== id) {
                  return true;
                }
                return false;
              }),
            );
          }
        } else {
          message.error('删除失败!');
        }
      },
    });
  };

  /** 关闭画布 */
  const handleCloseGraph = (id: string) => {
    const index = selectedHistory.findIndex((h) => h.id === id);
    if (index !== undefined) {
      setSelectHistoryIds([
        ...selectHistoryIds.slice(0, index),
        ...(index === selectHistoryIds.length - 1 ? [] : selectHistoryIds.slice(index + 1)),
      ]);
      setFilterCondition(
        filterCondition.filter((c) => {
          if ((c as IFilter)?.field === 'ip_history' && (c as IFilter)?.operand !== id) {
            return true;
          }
          return false;
        }),
      );
    }
  };

  /** 删除关系 */
  const handleDeleteRelation: IForceGraphProps['onDeleteRalation'] = (id, action, edge, node) => {
    const { name, data: graphDataJson } = ipGraphHistry.find((h) => h.id === id) || {};
    if (!graphDataJson) {
      return;
    }
    const graphFromJson = JSON.parse(graphDataJson) as IIpConversationGraph[];
    let alteredData: IIpConversationGraph[] = [];
    /** 如果是删除边，直接删除 */
    if (action.key === EActionMenuKey.DELETE_EDGE && edge) {
      alteredData = graphFromJson.filter(
        (data) => !(data.ipAAddress === edge.source && data.ipBAddress === edge.target),
      );
    }
    /** 如果是删除节点，删除关联的所有边 */
    if (action.key === EActionMenuKey.DELETE_NODE && node) {
      alteredData = graphFromJson.filter(
        (data) => data.ipAAddress !== node.id && data.ipBAddress !== node.id,
      );
    }

    Modal.confirm({
      title: '确定要删除吗?',
      onOk: async () => {
        const { success } = await deleteHistoryRelations({
          id,
          name: name!,
          data: JSON.stringify(alteredData),
        });
        if (success) {
          message.success('删除成功!');
          updateHistoryGraph(id);
        } else {
          message.error('删除失败!');
        }
      },
    });
  };

  useEffect(() => {
    queryGraph();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [networkId, pcapFileId, formData, globalSelectedTime]);

  // ======维持查询心跳 S=====
  const pingQueryTask = useCallback(() => {
    // 没有 ID 时不 ping
    if (!queryConversationTaskId) {
      return;
    }
    dispatch({
      type: 'flowRecordModel/pingQueryTask',
      payload: {
        queryId: queryConversationTaskId,
      },
    });
  }, [dispatch, queryConversationTaskId]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    let timer: any;
    if (queryConversationTaskId) {
      timer = window.setInterval(() => {
        setShowCancelBtn(true);
        pingQueryTask();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    window.clearTimeout(timer);
    setShowCancelBtn(false);
    return () => {};
  }, [pingQueryTask, queryConversationTaskId]);

  const handleFilterChange = (newFilter: IFilterCondition) => {
    newFilter.filter((item, i) => {
      const index = newFilter.findIndex((f) => (f as IFilter).field === (item as IFilter).field);
      if (index >= 0 && index !== i) {
        if (
          (item as IFilter).field !== 'ip_history' &&
          (item as IFilter).field !== 'l_7_protocol_id'
        ) {
          newFilter.splice(index, 1);
        }
      }
    });
    setFilterCondition(newFilter);
  };

  const renderHistoryMenu = () => {
    if (brushType === 'create') {
      return (
        <Form.Item label="画布名称">
          <Input
            placeholder="请输入画布名称"
            onChange={(e) => {
              setCurrentGraphInfo({
                name: e.target.value,
              });
            }}
          />
        </Form.Item>
      );
    }
    if (brushType === 'update') {
      return (
        <Form.Item label="画布名称">
          <Select
            placeholder="请选择需要加入的画布"
            onChange={(value) => {
              const historyItem = ipGraphHistry.find((h) => h.id === value) || {};
              setCurrentGraphInfo(historyItem);
            }}
          >
            {ipGraphHistry.map((item) => {
              return (
                <Option key={item.id} value={item.id}>
                  {item.name}
                </Option>
              );
            })}
          </Select>
        </Form.Item>
      );
    }
    return '';
  };

  const searchLine = (
    <>
      <Row gutter={12} wrap={false} justify="space-between">
        <Col span={12}>
          <FieldFilter
            fields={filterField}
            onChange={handleFilterChange}
            condition={filterCondition}
            historyStorageKey="tfa-netflow-flow-record-filter-history"
            simple={true}
          />
        </Col>
        <Col span={8} style={{ textAlign: 'right' }}>
          <Form.Item>
            <Space>
              <ExportFile
                loading={queryLoading}
                totalNum={Object.values(graphData.nodeMap)?.length}
                accessKey={'exportBtn'}
                queryFn={async (params: any) => {
                  const { fieldType, ...restParams } = params;
                  console.log(
                    `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-graph/as-export?${stringify(
                      { fileType: params.fileType, ...restParams, ...getQeryParams() },
                    )}`,
                  );
                  window.open(
                    `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-graph/as-export?${stringify(
                      { fileType: params.fileType, ...restParams, ...getQeryParams() },
                    )}`,
                  );
                }}
              />
              {showCancelBtn && (
                <Tooltip title="结束任务可能会导致查询不完整">
                  <Button
                    icon={<StopOutlined />}
                    type="primary"
                    danger
                    loading={cancelQueryTaskLoading}
                    onClick={() => cancelTask()}
                  >
                    停止
                  </Button>
                </Tooltip>
              )}
              <Button
                icon={<ReloadOutlined />}
                type="primary"
                onClick={handleFinish}
                loading={queryLoading}
              >
                查询
              </Button>
              <Button
                loading={queryLoading}
                icon={<RollbackOutlined />}
                onClick={() => {
                  setFormData(initFormValues() as any);
                  setFilterCondition([]);
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Col>
      </Row>
    </>
  );

  const renderContent = () => {
    if (queryLoading) {
      return <Message message={<Spin spinning />} />;
    }
    if (Object.keys(graphData.nodeMap).length < 1 && selectedHistory.length < 1) {
      return <Message />;
    }

    return (
      <Comparegraph
        historyGraph={historyGraph}
        data={[
          {
            theme: theme === 'light' ? defaultLightTheme : defaultDarkTheme,
            weightField: formData.sortProperty,
            height: chartHeight,
            nodes: Object.values(graphData.nodeMap),
            edges: graphData.originData.edges,
            nodeActions: [
              { key: EActionMenuKey.IP_DILLDOWN, label: 'IP下钻' },
              { key: EActionMenuKey.IP_FILTER, label: '添加IP过滤' },
              { key: EActionMenuKey.FLOW_RECORD, label: '会话详单' },
              // ...(networkId ? [{ key: EActionMenuKey.FLOW_LOCATION, label: '流量分析' }] : []),
              ...(networkId ? [{ key: EActionMenuKey.PACKET, label: '数据包' }] : []),
              ...(historyGraph
                ? [
                    { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                    { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                  ]
                : []),
              ...(mode === EIpGraphMode.RTP
                ? [{ key: EActionMenuKey.SIP_REOCORD, label: 'SIP详单' }]
                : []),
            ],
            onNodeClick: handleMenuClick,
            edgeActions: [
              { key: EActionMenuKey.FLOW_RECORD, label: '会话详单' },
              // ...(networkId ? [{ key: EActionMenuKey.FLOW_LOCATION, label: '流量分析' }] : []),
              ...(mode === EIpGraphMode.RTP
                ? [
                    { key: EActionMenuKey.RTP_SEGMENT, label: '分段分析' },
                    { key: EActionMenuKey.RTP_FLOW, label: 'RTP流分析' },
                  ]
                : []),
              ...[{ key: EActionMenuKey.PACKET, label: '数据包' }].filter(() => networkId),
              ...(historyGraph
                ? [
                    { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                    { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                  ]
                : []),
            ],
            onEdgeClick: handleEdgeClick,
            brushActions: [
              ...(historyGraph
                ? [
                    { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                    { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                  ]
                : []),
            ],
            onBrushEnd: handleBrushEnd,
            title: '访问关系图',
            id: 'default',
          },
          ...(historyGraph
            ? selectedHistory.map((item) => {
                return {
                  theme: theme === 'light' ? defaultLightTheme : defaultDarkTheme,
                  weightField: formData.sortProperty,
                  height: chartHeight,
                  nodes: Object.values(item.graphData!.nodeMap),
                  edges: item.graphData!.originData.edges,
                  nodeActions: [
                    { key: EActionMenuKey.IP_DILLDOWN, label: 'IP下钻' },
                    // { key: EActionMenuKey.IP_FILTER, label: '添加IP过滤' },
                    { key: EActionMenuKey.FLOW_RECORD, label: '会话详单' },
                    { key: EActionMenuKey.FLOW_LOCATION, label: '流量分析' },
                    ...(networkId ? [{ key: EActionMenuKey.PACKET, label: '数据包' }] : []),
                    { key: EActionMenuKey.DELETE_NODE, label: '删除' },
                    ...(historyGraph
                      ? [
                          { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                          { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                        ]
                      : []),
                  ],
                  onNodeClick: handleMenuClick,
                  edgeActions: [
                    { key: EActionMenuKey.FLOW_RECORD, label: '会话详单' },
                    { key: EActionMenuKey.FLOW_LOCATION, label: '流量分析' },
                    ...(networkId ? [{ key: EActionMenuKey.PACKET, label: '数据包' }] : []),
                    { key: EActionMenuKey.DELETE_EDGE, label: '删除' },
                    ...(historyGraph
                      ? [
                          { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                          { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                        ]
                      : []),
                  ],
                  onEdgeClick: handleEdgeClick,
                  brushActions: historyGraph
                    ? [
                        { key: EActionMenuKey.CREATE_HISTORY, label: '新建历史画布' },
                        { key: EActionMenuKey.SAVE_HISTORY, label: '保存到历史画布' },
                      ]
                    : [],
                  onBrushEnd: handleBrushEnd,
                  title: item.name,
                  id: item.id,
                  onDeleteRalation: handleDeleteRelation,
                };
              })
            : []),
        ]}
        edgeIndex={
          mode === EIpGraphMode.RTP
            ? [EForceGraphIndex.APPLICATIONS]
            : [EForceGraphIndex.TOTAL_BYTES, EForceGraphIndex.ESTABLISHEDSESSIONS]
        }
        deleteGraph={handleDeleteGraph}
        closeGraph={handleCloseGraph}
      />
    );
  };

  return (
    <>
      <AutoHeightContainer headerRender={searchLine} onHeightChange={handleHeightChange}>
        {renderContent()}
      </AutoHeightContainer>
      {historyGraph ? (
        <Modal
          destroyOnClose
          closable={false}
          title={(() => {
            if (brushType === 'create') {
              return '新建历史画布';
            } else if (brushType === 'update') {
              return '添加到历史画布';
            } else {
              return '';
            }
          })()}
          visible={brushType !== 'close'}
          footer={
            <>
              <Button
                type="primary"
                disabled={currentGraphInfo.name === undefined}
                onClick={async () => {
                  if (brushType === 'create') {
                    const params = {
                      ...(currentGraphInfo as IIpConversationGraphHistory),
                      id: uuidv4(),
                      data: JSON.stringify(brushResult),
                    };
                    delete params.graphData;
                    const { success } = await createGraphHistory(params);
                    if (success) {
                      message.success('创建成功!');
                      updateHistoryGraph();
                      setCurrentGraphInfo({});
                    } else {
                      message.error('创建失败!');
                    }
                  } else if (brushType === 'update') {
                    const params = {
                      ...(currentGraphInfo as IIpConversationGraphHistory),
                      data: JSON.stringify(brushResult),
                    };

                    delete params.graphData;
                    const { success } = await alterGraphHistory(params);
                    if (success) {
                      message.success('添加成功!');
                      updateHistoryGraph(params.id);
                      setCurrentGraphInfo({});
                    } else {
                      message.error('添加失败!');
                    }
                  }
                  setBrushType('close');
                }}
              >
                确定
              </Button>
              <Button
                onClick={() => {
                  setBrushType('close');
                }}
              >
                关闭
              </Button>
            </>
          }
        >
          {renderHistoryMenu()}
        </Modal>
      ) : null}
    </>
  );
};

export default connect(
  ({
    appModel: { globalSelectedTime },
    npmdModel: { currentPcpInfo },
    networkModel: { allNetworks },
    settings,
  }: ConnectState) => ({
    globalSelectedTime,
    currentPcpInfo,
    theme: settings.theme,
    allNetworks,
  }),
)(IPGraph);
