export interface INetif {
  id: string;
  name: string;
  specification: number;
  category: ' ' | '0' | '1' | '2' | '3';
  categoryText: string;
  state: '0' | '1';
  /**
   * 带宽
   */
  bandwidth: number;
  /**
   * 带宽统计时间
   */
  metricTime: string;
  updateTime: string;
  description?: string;
  type?: '1' | '2';
  typeText?: string;
  ipv4Address?: string;
  ipv6Address?: string;
  ipv4Gateway?: string;
  ipv6Gateway?: string;
}
export interface INetifAnalysis {
  id: string;
  /**
   * 时间戳
   */
  timestamp: number;
  /**
   * 网络ID
   */
  networkId: string;
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

export interface IAnalysisHistogramResult {}

export enum ENetifMetricType {
  /**
   * 接口统计
   */
  'NETIF' = 'netifs',
}

export interface INetif {
  id: string;
  name: string;
  specification: number;
  category: ' ' | '0' | '1' | '2' | '3';
  categoryText: string;
  state: '0' | '1';
  /**
   * 带宽
   */
  bandwidth: number;
  /**
   * 带宽统计时间
   */
  metricTime: string;
  updateTime: string;
  description?: string;
  type?: '1' | '2';
  typeText?: string;
  ipv4Address?: string;
  ipv6Address?: string;
  ipv4Gateway?: string;
  ipv6Gateway?: string;
}
export interface INetifAnalysis {
  id: string;
  /**
   * 时间戳
   */
  timestamp: number;
  /**
   * 网络ID
   */
  networkId: string;
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

export interface IAnalysisHistogramResult {}
