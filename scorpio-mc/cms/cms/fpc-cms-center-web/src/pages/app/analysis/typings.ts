import type { EMetricApiType } from '@/common/api/analysis';
import type { ERealTimeStatisticsFlag } from '@/models/app';
import type { ESensorStatus } from '../Configuration/Network/typings';
import type { INetworkType } from '../Network/typing';
import type {
  AllFields,
  AppUniqueFields,
  CommonFields,
  HostGroupUniqueFields,
  IpAddressUniqueFields,
  IpConversationUniqueFields,
  LocationUniqueFields,
  MacAddressUniqueFields,
  PortUniqueFields,
  ProtocolUniqueFields,
} from './components/fieldsManager';
import type { DHCPFields } from './components/fieldsManager/fieldsGroup/dhcpAnalysisFieldsGroup';

/** 以太网帧长统计指标 */
export interface IFramePacketLengthMetric {
  /** 包长小于等于64的包数（0，64] */
  tinyPackets: number;
  /** 包长65-127的包数（65，127] */
  smallPackets: number;
  /** 包长128-255的包数（128，255] */
  mediumPackets: number;
  /** 包长256-511的包数（256，512] */
  bigPackets: number;
  /** 包长512-1023的包数（512，1023] */
  largePackets: number;
  /** 包长1024-1517的包数（1024，1517] */
  hugePackets: number;
  /** 包长1518以上的包数 [1518，无穷大） */
  jumboPackets: number;
}

/** IP协议包数统计指标 */
export interface IIpProtocolPacketMetric {
  /** TCP总数据包 */
  tcpTotalPackets: number;
  /** UDP总数据包 */
  udpTotalPackets: number;
  /** ICMP总数据包 */
  icmpTotalPackets: number;
  /** ICMP6总数据包 */
  icmp6TotalPackets: number;
  /** 其他总数据包 */
  otherTotalPackets: number;
}

export interface IAlertMsgCntParams {
  startTime: string;
  endTime: string;
  networkId?: string;
  serviceId?: string;
}

/** 以太网类型统计指标 */
export interface IFrameCategoryMetric {
  /** IPv4以太网帧数 */
  ipv4Frames: number;
  /** IPv6以太网帧数 */
  ipv6Frames: number;
  /** ARP以太网帧数 */
  arpFrames: number;
  ieee8021xFrames: number;
  ipxFrames: number;
  lacpFrames: number;
  mplsFrames: number;
  stpFrames: number;
  otherFrames: number;
}

/** 数据包类型统计指标 */
export interface IBytesCategoryMetric {
  /** 单播数据包字节数 */
  unicastBytes: number;
  /** 广播数据包字节数 */
  broadcastBytes: number;
  /** 多播数据包字节数 */
  multicastBytes: number;
}

/** 分片数据包统计指标 */
export interface IFragmentMetric {
  fragmentTotalBytes: number;
  fragmentTotalPackets: number;
}

/** DSCP 统计 */
export interface IDscpMetric {
  dscp: {
    volumn: { type: string; totalBytes: number }[];
    histogram: { timestamp: string; type: string; totalBytes: number }[];
  };
}

/** 会话统计指标 */
export interface ISessionsMetric {
  /** 活动会话数 */
  activeSessions: number;
  /** 并发会话数 */
  concurrentSessions: number;
  /** 新建会话总数 */
  establishedSessions: number;
  /** 销毁会话数 */
  destroyedSessions: number;
  /** TCP新建会话数 */
  establishedTcpSessions: number;
  /** UDP新建会话数 */
  establishedUdpSessions: number;
  /** ICMP新建会话数 */
  establishedIcmpSessions: number;
  /** 其他协议新建会话数 */
  establishedOtherSessions: number;
  /** 上行新建会话数 */
  establishedUpstreamSessions: number;
  /** 下行新建会话数 */
  establishedDownstreamSessions: number;
}

/** 性能统计指标 */
export interface IPerformanceMetric {
  /** 服务器响应时延均值 */
  serverResponseLatencyAvg: number;
  /** 服务器响应时延峰值 */
  serverResponseLatencyPeak: number;
  /** 上周期同期，服务器响应时延均值 */
  lastWeekSamePeriodServerResponseLatencyAvg: number;
  /** 服务器响应时延基线值 */
  baselineServerResponseLatencyAvg: number;

  /** 服务器迅速响应个数 */
  serverResponseFastCounts: number;
  /** 服务器正常响应个数 */
  serverResponseNormalCounts: number;
  /** 服务器超时响应个数 */
  serverResponseTimeoutCounts: number;
  /**  客户端网络总时延 */
  tcpClientNetworkLatency: number;
  /**  客户端网络时延均值 */
  tcpClientNetworkLatencyAvg: number;
  /**  服务端网络总时延 */
  tcpServerNetworkLatency: number;
  /**  服务端网络时延均值 */
  tcpServerNetworkLatencyAvg: number;

  /** 客户端网络时延统计次数 */
  tcpClientNetworkLatencyCounts: number;
  /** 服务端网络时延统计次数 */
  tcpServerNetworkLatencyCounts: number;
  /** 服务器响应总时延 */
  serverResponseLatency: number;
  /** 服务器响应时延统计次数 */
  serverResponseLatencyCounts: number;
  /** TCP客户端重传包数 */
  tcpClientRetransmissionPackets: number;
  /** TCP客户端总包数 */
  tcpClientPackets: number;
  /** TCP服务端重传包数 */
  tcpServerRetransmissionPackets: number;
  /** TCP服务端总包数 */
  tcpServerPackets: number;
  /** 客户端零窗口包数 */
  tcpClientZeroWindowPackets: number;
  /** 服务端零窗口包数 */
  tcpServerZeroWindowPackets: number;
}

/** TCP指标统计 */
export interface ITcpMetric {
  /** TCP建连失败数 */
  tcpEstablishedFailCounts: number;
  /** TCP建连成功数 */
  tcpEstablishedSuccessCounts: number;

  /** TCP同步包 */
  tcpSynPackets: number;
  /** TCP客户端syn包数 */
  tcpClientSynPackets: number;
  /** TCP服务端syn包数 */
  tcpServerSynPackets: number;

  /** 客户端零窗口包数 */
  // tcpClientZeroWindowPackets: number;
  /** 服务端零窗口包数 */
  // tcpServerZeroWindowPackets: number;

  /** TCP客户端重传包数 */
  tcpClientRetransmissionPackets: number;
  /** TCP服务端重传包数 */
  tcpServerRetransmissionPackets: number;

  /** 数据包重传率 */
  tcpRetransmissionRate: number;
  /** 客户端重传率 */
  tcpClientRetransmissionRate: number;
  /** 服务端重传率 */
  tcpServerRetransmissionRate: number;

  /** TCP同步确认包 */
  tcpSynAckPackets: number;
  /** TCP同步重置包 */
  tcpSynRstPackets: number;

  /** TCP连接平均响应时间(ms) */
  tcpEstablishedTimeAvg: number;
  /** TCP零窗口包数 */
  tcpZeroWindowPackets: number;
  /** tcp客户端零窗口包数 */
  tcpClientZeroWindowPackets: number;
  /** tcp服务端零窗口包数 */
  tcpServerZeroWindowPackets: number;
}

/** 三层主机 Top */
export interface IL3DevicesTop {
  l3DevicesTop: {
    /** 三层主机总流量 Top */
    totalBytes: {
      ip: string;
      value: number;
    }[];
    /** 三层主机总会话数 Top */
    totalSessions: {
      ip: string;
      value: number;
    }[];
  };
}

/** 三层主机通讯对 Top */
export interface IIpConversationTop {
  ipConversationTop: {
    /** 三层主机通讯对总流量 Top */
    totalBytes: {
      ipA: string;
      ipB: string;
      value: number;
    }[];
    /** 三层主机通讯对总会话数 Top */
    totalSessions: {
      ipA: string;
      ipB: string;
      value: number;
    }[];
  };
}

/** 带宽统计指标 */
export interface IByteMetric {
  /** 总流量 */
  totalBytes: number;
  /** 上行均值带宽 */
  upstreamBytes: number;
  /** 下行均值带宽 */
  downstreamBytes: number;

  /** 不捕获丢弃流量 */
  filterDiscardBytes: number;
  /** 超规格丢弃流量 */
  overloadDiscardBytes: number;
  /** 去重丢弃流量 */
  deduplicationBytes: number;

  /**
   * 峰值Bytes每秒
   * @description 换算带宽时注意 *8
   */
  bytepsPeak: number;
}

/** 数据包统计指标 */
export interface IPacketMetric {
  /** 总数据包数 */
  totalPackets: number;
  /** 峰值包速率 */
  packetpsPeak: number;
  /** 上行数据包 */
  upstreamPackets: number;
  /** 下行数据包 */
  downstreamPackets: number;
  /** 不捕获丢弃包数 */
  filterDiscardPackets: number;
  /** 超规格丢弃包数 */
  overloadDiscardPackets: number;
  /** 去重丢弃包数 */
  deduplicationPackets: number;
}

/** 额外的统计指标 */
export interface IExtraMetric {
  /** 告警数量 */
  alertCounts: number;
}

/**
 * 网络概览页面统计数据
 */
export interface INetworkDashboardData
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    IL3DevicesTop,
    IIpConversationTop,
    IPerformanceMetric,
    ITcpMetric,
    IFramePacketLengthMetric,
    IIpProtocolPacketMetric,
    IFrameCategoryMetric,
    IBytesCategoryMetric,
    IDscpMetric {
  /** 网络 ID */
  networkId: string;

  /** 趋势图时间统计 */
  histogram: ({
    timestamp: string;
  } & IFramePacketLengthMetric &
    IIpProtocolPacketMetric &
    IFrameCategoryMetric &
    IBytesCategoryMetric &
    IFragmentMetric &
    ISessionsMetric &
    IPerformanceMetric &
    ITcpMetric)[];
}

/**
 * 业务概览页面统计数据
 */
export interface IServiceDashboardData
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    IL3DevicesTop,
    IIpConversationTop,
    ITcpMetric,
    IPerformanceMetric,
    IDscpMetric {
  /** 业务 ID */
  serviceId: string;

  /** 趋势图时间统计 */
  histogram: ({
    timestamp: string;
  } & IFragmentMetric)[];
}

// TODO: 类型名称修改
export enum ESourceType {
  'NETWORK' = 'network',
  'SERVICE' = 'service',
  'OFFLINE' = 'packetFile',
}

/** 查询参数 */
export interface IMetricQueryParams {
  /** 查询类型 */
  metricType?: ESourceType;
  /* TODO: 替换metricType为sourceType */
  sourceType?: ESourceType;
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
  /** DSL查询表达式 */
  dsl?: string;
  /** 是否开启实时统计 */
  realTime?: ERealTimeStatisticsFlag;
  /** 排序字段，默认start_time */
  sortProperty?: string;
  /** 排序方向，默认desc */
  sortDirection?: ESortDirection;
}

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
  columns?: string;
}

/**
 * 排序方向
 */
export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}

// 所有网络分析
// =====================
/** 所有网络分析查询参数 */
export interface INetworkStatParams {
  /** 开始时间 */
  startTime: string;
  /** 截止时间 */
  endTime: string;
  /** 时间间隔 */
  interval: number;
  /** 排序字段，默认start_time */
  sortProperty?: string;
  /** 排序方向，默认desc */
  sortDirection?: ESortDirection;
}

/** 所有网络统计数据 */
export interface INetworkStatData
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    IPerformanceMetric,
    ITcpMetric,
    IFramePacketLengthMetric,
    IIpProtocolPacketMetric,
    IFrameCategoryMetric,
    IBytesCategoryMetric,
    IDscpMetric {
  networkId?: string;
  networkGroupId?: string;
  networkName?: string;
  /** 网络的总带宽（Mbps） */
  networkBandwidth?: number;
  /** 子网归属于哪个物理网络的 ID */
  parentId?: string;
  children?: INetworkStatData[] | undefined;
  status: ESensorStatus;
  /** 类型 */
  type: INetworkType;
}

/** 所有业务分析查询参数 */
export interface IServiceStatParams {
  /** 开始时间 */
  startTime: string;
  /** 截止时间 */
  endTime: string;
  /** 时间间隔 */
  interval: number;
  // 下面2个参数必须同时存在或不存在
  // 同时存在时表示，查询某个业务在某个网络下的统计情况
  // 同时不存在时表示，时间范围内所有业务的统计情况
  serviceId?: string;
  networkId?: string;

  // 由于业务最多可支持1万以内的数据量
  // 所以为了性能保证，要调整为分页
  // 新增了分页和查询参数
  pageSize?: number;
  page?: number;
  name?: string;
  sortProperty?: string;
  sortDirection?: ESortDirection;
  // 1表示只看关注业务
  isFlow: '0' | '1';
}
/** 所有业务统计数据 */
export interface IServiceStatData
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    ISessionsMetric,
    IPerformanceMetric,
    IFragmentMetric,
    ITcpMetric {
  serviceId: string;
  /**
   * @deprecated
   */
  serviceName?: string;
  networkId?: string;
  networkGroupId?: string;
  /**
   * @deprecated
   */
  networkName?: string;
}

/** 指标配置公共参数 */
export interface IMetricSettingQueryParams {
  sourceType: ESourceType;
  networkId?: string;
  networkGroupId?: string;
  serviceId: string;
}

/** 基线类型 */
export enum EBaselineCategory {
  /** 带宽基线定义 */
  'BANDWIDTH' = 'bandwidth',
  /** 流量基线定义  */
  'FLOW' = 'flow',
  /** 数据包基线定义  */
  'PACKET' = 'packet',
  /** 响应时间基线定义  */
  'RESPONSE_LATENCY' = 'responseLatency',
}
/** 基线设置 */
export interface IBaselineSettingData extends IMetricSettingQueryParams {
  /** 基线类型 */
  category: EBaselineCategory;
  /** 权重模式 */
  weightingModel: string;
  /** 基线窗口 */
  windowingModel: string;
  /** 回顾周期 */
  windowingCount: number;
}

// TODO: 确定类型
/**
 * 通用的统计分析指标可配置类型
 */
export enum EMetricSettingCategory {
  /** 性能配置：超时响应时间 */
  'SERVER_RESPONSE_TIMEOUT' = 'server_response_timeout',
  /** 性能配置：正常响应时间 */
  'SERVER_RESPONSE_NORMAL' = 'server_response_normal',
  /** 建连分析配置：长连接认定时间 */
  'LONG_CONNECTION' = 'long_connection',
}

/** 指标配置数据 */
export interface IMetricSettingData {
  metric: EMetricSettingCategory;
  value: string | number;
}

/**
 * URL中的params 对象
 */
export interface IUriParams {
  networkId?: string;
  networkGroupId?: string;
  serviceId: string;
  pcapFileId?: string;
}

/** 负载量统计数据 */
export interface IPayloadStat extends ISessionsMetric {
  /** 时间戳 */
  timestamp: string;

  /** 总带宽 */
  bandwidth: number;
  /** 上行带宽 */
  upstreamBandwidth: number;
  /** 下行带宽 */
  downstreamBandwidth: number;
  /** 上周期同期带宽 */
  lastWeekSamePeriodBandwidth: number;
  /** 带宽基线 */
  baselineBandwidth: number;
  /** 不捕获 */
  filterDiscardBytes: number;
  /** 不捕获带宽 */
  filterDiscardBandwidth: number;
  /** 超规格 */
  overloadDiscardBytes: number;
  /** 超规格带宽 */
  overloadDiscardBandwidth: number;
  /** 去重 */
  deduplicationBytes: number;
  /** 去重带宽 */
  deduplicationBandwidth: number;
  /** 带宽峰值 */
  bytepsPeak: number;

  /** 总流量 */
  totalBytes: number;
  /** 上行总流 */
  upstreamBytes: number;
  /** 上行总流 */
  downstreamBytes: number;
  /** 上周期同期流量 */
  lastWeekSamePeriodTotalBytes: number;
  /** 流量基线 */
  baselineTotalBytes: number;

  /** 总数据包数 */
  totalPackets: number;
  /** 上行包数 */
  upstreamPackets: number;
  /** 下行包数 */
  downstreamPackets: number;
  /** 上周期同期数据包 */
  lastWeekSamePeriodTotalPackets: number;
  /** 数据包基线 */
  baselineTotalPackets: number;

  /** 独立用户数 */
  uniqueIpCounts: number;
}

/** 性能统计数据 */
export interface IPerformanceStatData extends IPerformanceMetric, ISessionsMetric {
  /** 时间戳 */
  timestamp: string;
  /** TCP客户端重传包数 */
  tcpClientRetransmissionPackets: number;
  /** TCP服务端重传包数 */
  tcpServerRetransmissionPackets: number;
}

/** TCP指标统计数据 */
export interface ITcpStatData extends ITcpMetric {
  /** 时间戳 */
  timestamp: string;
}

/** 性能设置页面的数据 */
export interface IPerformanceSettingData {
  responseTime?: IMetricSettingData[];
  baseline?: IBaselineSettingData;
}

// TODO: 考虑类型名称是否合适
/** 响应时间提交参数 */
export type IResponseTimeSettingParams = {
  sourceType: ESourceType;
  metric: string;
  value: number;
} & IUriParams;

export type ValuesOf<T extends string[]> = T[number];

export type IFlowLocationStatFileds = Record<LocationUniqueFields | CommonFields, string | number>;
export type IFlowAppStatFileds = Record<AppUniqueFields | CommonFields, string | number>;
export type IFlowProtocolStatFileds = Record<ProtocolUniqueFields | CommonFields, string | number>;
export type IFlowPortStatFileds = Record<PortUniqueFields | CommonFields, string | number>;
export type IFlowHostGroupStatFileds = Record<
  HostGroupUniqueFields | CommonFields,
  string | number
>;
export type IFlowMacStatFileds = Record<MacAddressUniqueFields | CommonFields, string | number>;
export type IFlowIpStatFileds = Record<IpAddressUniqueFields | CommonFields, string | number>;
export type IFlowIpConversationStatFileds = Record<
  IpConversationUniqueFields | CommonFields,
  string | number
>;

export type IDHCPStatFields = Record<DHCPFields, string | number>;

export type IFlowAnalysisData = Record<AllFields, number | string>;

export enum ANALYSIS_APPLICATION_TYPE_ENUM {
  '分类' = 0,
  '子分类',
  '应用',
}

export interface IExecutionResult
  extends IExtraMetric,
    IByteMetric,
    IPacketMetric,
    IPerformanceMetric,
    ITcpMetric,
    IFramePacketLengthMetric,
    IIpProtocolPacketMetric,
    IFrameCategoryMetric,
    IBytesCategoryMetric,
    IDscpMetric {}

export enum EPcapState {
  /** 等待分析 */
  'WAITANALYSIS' = '0',
  /** 正在分析 */
  'ANALYZING' = '1',
  /** 分析异常 */
  'EXCEPTION' = '2',
  /** 分析完成 */
  'COMPLETE' = '3',
  /** 已删除 */
  'DELETED' = '4',
}

/** 实时统计定时刷新间隔 */
export const REAL_TIME_POLLING_MS = 3000;

export interface HttpAnalysisResult {
  httpRequest: {
    timestamp: string;
    requestCounts: number;
    responseCounts: number;
    errorResponseCounts: number;
  }[];
  httpMethod: {
    key: string;
    count: number;
  }[];
  httpCode: {
    key: string;
    count: number;
  }[];
  os: {
    type: string;
    count: number;
  }[];
}

export interface IPGraphResult {
  nodes: string[];
  edges: {
    srcIp: string;
    destIp: string;
    totalBytes: number;
    establishedSessions: number;
  }[];
}

/** IP会话关系图查询条件 */
export interface IIpConversationGraphParams extends Omit<IFlowQueryParams, 'metricApi'> {
  /** 最小新建会话数 */
  minEstablishedSessions?: number;
  /** 最小总字节数 */
  minTotalBytes?: number;
}
/** IP会话关系图 */
export interface IIpConversationGraph {
  ipAAddress: string;
  ipBAddress: string;
  totalBytes: number;
  establishedSessions: number;
  applications?: number[];
}

export const tableTop = [10, 100, 300, 500, 1000];

export enum EServiceType {
  /** 总体服务 */
  TOTALSERVICE = 'totalService',
  /** 内网服务 */
  INTRANETSERVICE = 'intranetService',
  /** 外网服务 */
  INTERNETSERVICE = 'internetService',
}
