import type { EMetricApiType } from '@/common/api/analysis';
import type {
  ANALYSIS_APPLICATION_TYPE_ENUM,
  DHCP_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_ENUM,
} from '@/common/app';

export type THomeMetricType = Extract<EMetricApiType, EMetricApiType.netif | EMetricApiType.network>;

/**
 * 统计分析查询条件
 */
export interface IAnalysisParams {
  metricApi: THomeMetricType;
  networkId: string;

  /**
   * 国家
   */
  countryId?: string;
  /**
   * 省份
   */
  provinceId?: string;
  /**
   * 城市
   */
  cityId?: string;

  serviceId?: string;
  /**
   * 应用类型
   */
  type?: ANALYSIS_APPLICATION_TYPE_ENUM;
  applicationId?: string;

  l7ProtocolId?: string;
  port?: string;
  hostgroupId?: string;
  macAddress?: string;
  ipAddress?: string;

  /**
   * DHCP客户端、服务端详情时
   *
   * 需要拼接 clientIpAddress_clientMacAddress
   *
   * 或 serverIpAddress_serverMacAddress
   */
  id?: string;
  // type?: EAggCategory;
  messageType?: DHCP_V6_MESSAGE_TYPE_ENUM | DHCP_MESSAGE_TYPE_ENUM;

  startTime: string;
  endTime: string;
  interval: number;

  sortProperty: string;
  sortDirection: ESortDirection;
  dsl?: string;
  netifName?: string;
}

export interface IAnalysisCommonField {
  id: string;
  /**
   * 时间戳
   */
  timestamp: number;
  /**
   * 网络ID
   */
  networkId: string;
}

/**
 * 网络统计结果
 */
export interface INetworkAnalysis extends IAnalysisCommonField {
  // === 带宽统计 ===
  /**
   * 流量大小
   */
  totalBytes: number;
  /**
   * 进网（下行）流量
   */
  downstreamBytes: number;
  /**
   * 出网（上行）流量
   */
  upstreamBytes: number;
  /**
   * 不捕获丢弃流量
   */
  filterDiscardBytes: number;
  /**
   * 超规格丢弃流量
   */
  overloadDiscardBytes: number;
  /**
   * 去重丢弃流量
   */
  deduplicationBytes: number;

  // === 包数统计 ===
  /**
   * 总数据包
   */
  totalPackets: number;
  /**
   * 进网（下行）数据包
   */
  downstreamPackets: number;
  /**
   * 出网（上行）数据包
   */
  upstreamPackets: number;
  /**
   * 不捕获丢弃包数
   */
  filterDiscardPackets: number;
  /**
   * 超规格丢弃包数
   */
  overloadDiscardPackets: number;
  /**
   * 去重丢弃包数
   */
  deduplicationPackets: number;

  // === 包长统计 ===

  /**
   * 包长小于等于64的包数（0，64]
   */
  tinyPackets: number;
  /**
   * 包长65-127的包数（65，127]
   */
  smallPackets: number;
  /**
   * 包长128-255的包数（128，255]
   */
  mediumPackets: number;
  /**
   * 包长256-511的包数（256，512]
   */
  bigPackets: number;
  /**
   * 包长512-1023的包数（512，1023]
   */
  largePackets: number;
  /**
   * 包长1024-1517的包数（1024，1517]
   */
  hugePackets: number;
  /**
   * 包长1518以上的包数 [1518，无穷大）
   */
  jumboPackets: number;
  /**
   * 平均包长
   */
  packetLengthAvg: number;

  // === 会话统计 ===
  /**
   * 活动会话数
   */
  activeSessions: number;
  /**
   * 并发会话数
   */
  concurrentSessions: number;
  /**
   * 新建会话总数
   */
  establishedSessions: number;
  /**
   * 销毁会话数
   */
  destroyedSessions: number;
  /**
   * TCP新建会话数
   */
  establishedTcpSessions: number;
  /**
   * UDP新建会话数
   */
  establishedUdpSessions: number;
  /**
   * ICMP新建会话数
   */
  establishedIcmpSessions: number;
  /**
   * 其他协议新建会话数
   */
  establishedOtherSessions: number;
  /**
   * 上行新建会话数
   */
  establishedUpstreamSessions: number;
  /**
   * 下行新建会话数
   */
  establishedDownstreamSessions: number;

  // === 以太网类型统计 ===
  /**
   * IPv4以太网帧数
   */
  ipv4Frames: number;
  /**
   * IPv6以太网帧数
   */
  ipv6Frames: number;
  /**
   * ARP以太网帧数
   */
  arpFrames: number;

  ieee8021xFrames: number;
  ipxFrames: number;
  lacpFrames: number;
  mplsFrames: number;
  stpFrames: number;
  otherFrames: number;

  // IP协议包数统计
  /**
   * TCP总数据包
   */
  tcpTotalPackets: number;
  /**
   * UDP总数据包
   */
  udpTotalPackets: number;
  /**
   * ICMP总数据包
   */
  icmpTotalPackets: number;
  /**
   * ICMP6总数据包
   */
  icmp6TotalPackets: number;
  /**
   * 其他总数据包
   */
  otherTotalPackets: number;
  /**
   * 分片数据包
   */
  fragmentTotalPackets: number;

  // === TCP相关统计 ===
  /**
   * TCP同步包
   */
  tcpSynPackets: number;
  /**
   * TCP同步确认包
   */
  tcpSynAckPackets: number;
  /**
   * TCP同步重置包
   */
  tcpSynRstPackets: number;
  /**
   * TCP无响应次数
   */
  tcpNoAckCounts: number;
  /**
   * TCP成功连接次数
   */
  tcpEstablishedCounts: number;
  /**
   * TCP连接平均响应时间
   */
  tcpEstablishedTimeAvg: number;
  /**
   * TCP零窗口包数
   */
  tcpZeroWindowsPackets: number;

  // === 数据包类型统计 ===
  /**
   * 单播数据包字节数
   */
  unicastBytes: number;
  /**
   * 广播数据包字节数
   */
  broadcastBytes: number;
  /**
   * 多播数据包字节数
   */
  multicastBytes: number;

  // === 性能统计 ===
  /**
   * 客户端网络时延均值
   */
  clientNetworkLatencyAvg: number;
  /**
   * 服务器网络时延均值
   */
  serverNetworkLatencyAvg: number;
  /**
   * 服务器响应时延均值
   */
  serverProcessingTimeAvg: number;
}

/**
 * 接口统计
 */
export interface INetifAnalysis extends IAnalysisCommonField {
  /**
   * 网口
   */
  netifName: string;

  // === 总带宽统计 ===
  /**
   * 总流量
   */
  totalBytes: number;
  /**
   * 进网（下行）流量
   */
  downstreamBytes: number;
  /**
   * 出网（上行）流量
   */
  upstreamBytes: number;
  /**
   * 发送流量
   */
  transmitBytes: number;

  // === 总包数统计 ===
  /**
   * 总数据包
   */
  totalPackets: number;
  /**
   * 进网数据包
   */
  downstreamPackets: number;
  /**
   * 出网数据包
   */
  upstreamPackets: number;
  /**
   * 发送包数
   */
  transmitPackets: number;
}

export interface ICountApplication {
  applicationId: string;
  totalBytes: number;
}

export interface ICountL7Procolcol {
  l7ProtocolId: string;
  totalBytes: number;
}

/**
 * 网络分析的直方图数据
 */
export interface INetworkHistogram extends INetworkAnalysis {
  /**
   * 最大客户端网络时延均值
   */
  maxClientNetworkLatencyAvg: number;
  /**
   * 最小客户端网络时延均值
   */
  minClientNetworkLatencyAvg: number;
  /**
   * 最大服务器网络时延均值
   */
  maxServerNetworkLatencyAvg: number;
  /**
   * 最小服务器网络时延均值
   */
  minServerNetworkLatencyAvg: number;
  /**
   * 最大服务器响应时延均值
   */
  maxServerProcessingTimeAvg: number;
  /**
   * 最小服务器响应时延均值
   */
  minServerProcessingTimeAvg: number;
  /**
   * 最大TCP连接平均响应时间
   */
  maxTcpEstablishedTimeAvg: number;
  /**
   * 最小TCP连接平均响应时间
   */
  minTcpEstablishedTimeAvg: number;

  /**
   * 最小活动会话数
   */
  minActiveSessions: number;
  /**
   * 最大活动会话数
   */
  maxActiveSessions: number;
  /**
   * 最小并发会话数
   */
  minConcurrentSessions: number;
  maxConcurrentSessions: number;
  /**
   * 新建会话总数
   */
  minEstablishedSessions: number;
  maxEstablishedSessions: number;
  /**
   * 销毁会话数
   */
  minDestroyedSessions: number;
  maxDestroyedSessions: number;
  /**
   * TCP新建会话数
   */
  minEstablishedTcpSessions: number;
  maxEstablishedTcpSessions: number;
  /**
   * UDP新建会话数
   */
  minEstablishedUdpSessions: number;
  maxEstablishedUdpSessions: number;
  /**
   * ICMP新建会话数
   */
  minEstablishedIcmpSessions: number;
  maxEstablishedIcmpSessions: number;
  /**
   * 其他协议新建会话数
   */
  minEstablishedOtherSessions: number;
  maxEstablishedOtherSessions: number;
  /**
   * 上行新建会话数
   */
  minEstablishedUpstreamSessions: number;
  maxEstablishedUpstreamSessions: number;
  /**
   * 下行新建会话数
   */
  minEstablishedDownstreamSessions: number;
  maxEstablishedDownstreamSessions: number;
}

/**
 * 应用流量统计Top
 */
export type IApplicaitonFlowTopN = Record<string, number>;

/**
 * 排序方向
 */
export enum ESortDirection {
  'DESC' = 'desc',
  'ASC' = 'asc',
}
