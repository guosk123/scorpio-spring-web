export enum ENetifCategory {
  'DEFAULT' = ' ',
  'MANAGER' = '0',
  'RECEIVE' = '1',
  'REPLAY' = '2',
  'NETFLOW' = '3',
}
export enum ENetifType {
  'DPDK' = '1',
  'COMMON' = '2',
}
export interface INetif {
  id: string;
  name: string;
  specification: number;
  category: ENetifCategory;
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
  /** 接口是否被其他资源（网络，实时转发策略，全包查询任务）占用，导致接口用途不能修改 */
  useMessage?: string;
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
