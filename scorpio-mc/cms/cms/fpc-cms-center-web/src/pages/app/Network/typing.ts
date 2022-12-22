export enum ENetworkTabs {
  DASHBOARD = 'dashboard',
  /** 负载量 */
  PAYLOAD = 'payload',
  /** 基线配置 */
  BASELINE = 'baseline',
  /** 性能 */
  PERFORMANCE = 'performance',
  /** 响应时间配置 */
  PERFORMANCESETTING = 'performanceSetting',
  /** TCP指标 */
  TCPSTATS = 'tcpStats',
  /** 告警消息 */
  ALERT = 'alert',
  /** 应用层协议分析 */
  METADATA = 'metadata',
  /** 流量分析 */
  FLOW = 'flow',
  /** 重传分析 */
  RETRANSMISSION = 'retransmission',
  /** 建连分析 */
  CONNECTION = 'connection',
  /** 会话详单 */
  FLOWRECORD = 'flowRecord',
  /** 数据包 */
  PACKET = 'packet',
  /** 在线分析 */
  PACKETANALYSIS = 'packetAnalysis',
}
export enum ENetowrkType {
  NETWORK_GROUP = 'networkGroup',
  NETWORK = 'network',
}

export interface INetworkTreeItem {
  title: string;
  value: string;
  key: string;
  type: ENetowrkType;
  recordId: string;
  networkInSensorIds?: string;
  children?: INetworkTreeItem[];
  status: '0' | '1';
  statusDetail?: string;
  logicNetwork?: boolean;
}

/** 实时统计开关 */
export enum ERealTimeStatisticsFlag {
  'OPEN' = '1',
  'CLOSED' = '0',
}

export enum INetworkType {
  'GROUP' = '网络组',
  'SENSORNETWORK' = '探针网络',
  'LOGICALSUBNET' = '逻辑子网',
}

export enum ENetworkDirectionType {
  TOTAL = 'total',
  INSIDE = 'insideService',
  OUTSIDE = 'outsideService',
}

export const NETWORK_DIRECTION_NAME = {
  [ENetworkDirectionType.TOTAL]: '全部',
  [ENetworkDirectionType.INSIDE]: '内网',
  [ENetworkDirectionType.OUTSIDE]: '外网',
};

export const NETWORK_DIRECITON_METRIC_SUFFIX = {
  [ENetworkDirectionType.TOTAL]: '',
  [ENetworkDirectionType.INSIDE]: 'InsideService',
  [ENetworkDirectionType.OUTSIDE]: 'OutsideService',
};
