import type { EMetricApiType } from '@/common/api/analysis';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFieldProperty } from '../../analysis/components/fieldsManager';
import { EModelAlias } from '../../analysis/components/fieldsManager';
import { EFieldEnumValueSource } from '../../analysis/components/fieldsManager';
import { EFormatterType } from '../../analysis/components/fieldsManager';
import type { ESortDirection, ESourceType } from '../../analysis/typings';

export const SORT_PROPERTY_LOCALITY_LIST = [
  {
    text: '会话数',
    value: 'establishedSessions',
  },
  {
    text: '总字节数',
    value: 'totalBytes',
  },
];

export const fieldsMapping: Record<string, IFieldProperty> = {
  ip_address: {
    name: 'IP/IP网段',
    formatterType: EFormatterType.TEXT,
    filterFieldType: EFieldType.IPV4 || EFieldType.IPV6,
    filterOperandType: EFieldOperandType.IP,
  },
  minEstablishedSessions: {
    name: '最小新建会话数',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  minTotalBytes: {
    name: '最小字节数',
    formatterType: EFormatterType.BYTE,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  sortProperty: {
    name: '线粗维度',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.LOCAL,
    enumValue: SORT_PROPERTY_LOCALITY_LIST,
  },
  count: {
    name: '会话对数量',
    formatterType: EFormatterType.COUNT,
    filterOperandType: EFieldOperandType.NUMBER,
  },
  networkId: {
    name: '网络',
    formatterType: EFormatterType.ENUM,
    filterOperandType: EFieldOperandType.ENUM,
    enumSource: EFieldEnumValueSource.MODEL,
    enumValue: EModelAlias.network,
  },
};

export interface IFlowQueryParams {
  metricType?: ESourceType;
  sourceType?: ESourceType;
  /** 查询类型 */
  metricApi: EMetricApiType;
  /** 网络 ID */
  networkId?: string;
  /** 业务 ID */
  serviceId?: string;
  /** 离线文件ID */
  packetFileId?: string;
  /** 开始时间 */
  startTime: string;
  /** 截止时间 */
  endTime: string;
  /** 时间间隔 */
  interval: number;
  /** 取 Top 前多少，默认Top10 */
  count?: number;
  type?: number;
  /** DSL查询表达式 */
  dsl: string;
  /**
   * 是否是下钻查询
   * @value 0 不下钻
   * @value 1 下钻
   */
  drilldown: '0' | '1';
  /** 排序字段，默认start_time */
  sortProperty?: string;
  /** 排序方向，默认desc */
  sortDirection?: ESortDirection;
  /** 查询 id，用于取消查询 */
  queryId?: string;
  queryIds?: string[];
  tableQueryId?: string;
  chartQueryId?: string;
}

/** IP会话关系图 */
export interface IIpConversationGraph {
  applications: number[];
  ipAAddress: string;
  ipBAddress: string;
  totalBytes: number;
  establishedSessions: number;
}

export interface NodeMap {
  id: string;
  establishedSessions?: number;
  totalBytes?: number;
}

export interface GraphData {
  originData: {
    nodes: string[];
    edges: {
      source: string;
      target: string;
      totalBytes: number;
      establishedSessions: number;
    }[];
  };
  nodeMap: Record<string, NodeMap>;
  minAndMax: [number, number];
}

export interface IIpConversationGraphHistory {
  name: string;
  id: string;
  data?: string;
  graphData?: GraphData;
}

/** IP会话关系图查询条件 */
export interface IIpConversationGraphParams extends Omit<IFlowQueryParams, 'metricApi'> {
  /** 最小新建会话数 */
  minEstablishedSessions?: number;
  /** 最小总字节数 */
  minTotalBytes?: number;
}

export interface IIpConversationHistoryParams {
  name?: string;
  id: string;
  data?: string;
}

export interface IIpConversationHistoryParams {
  name?: string;
  id: string;
  data?: string;
}

/** IP会话关系图查询条件 */
export interface IIpConversationGraphParams extends Omit<IFlowQueryParams, 'metricApi'> {
  /** 最小新建会话数 */
  minEstablishedSessions?: number;
  /** 最小总字节数 */
  minTotalBytes?: number;
}
