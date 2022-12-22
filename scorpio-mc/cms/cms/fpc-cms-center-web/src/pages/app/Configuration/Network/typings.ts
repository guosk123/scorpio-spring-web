//  ----- 探针网路 ------
// 探针类型
export enum ENetworkSensorType {
  '普通探针' = 0,
  '软件探针' = 1,
}

export enum EDeviceType {
  'CMS' = '0',
  'NPM' = '1',
}

// 表单页面类型
export enum EPageMode {
  'update' = 0,
  'create' = 1,
}

// 子网类型
export enum ELogicalSubnetType {
  '网段子网络' = 0,
  'VLAN子网络' = 1,
  'MAC子网络' = 2,
  'MPLS子网络' = 3,
  'GRE子网络' = 4,
  'VXLAN子网络' = 5,
}

// 探针状态
export enum ESensorStatus {
  'ONLINE' = '0',
  'OFFLINE' = '1',
}

// 探针网络类型
export interface INetworkSensor {
  id: string;
  name: string; // 编辑，展示用到的name
  sensorId: string;
  sensorName: string;
  sensorType: string;
  description: string;
  deviceSerialNumber: string;
  networkInSensorId: string;
  networkInSensorName: string;
  owner: string;
  status: ESensorStatus;
  statusDetail?: string;
  bandwidth: number;
  sendPolicyIds?: string;
}

// 探针中的网络类型
export interface INetworkInSensor {
  id: string;
  fpcId: string;
  fpcNetworkId: string;
  fpcNetworkName: string;
}

// 探针树状接口类型
export interface INetworkSensorTree {
  deviceId: string;
  deviceName: string;
  sensorType: string;
  owner: string;
  deviceType: 'CMS' | 'NPM';
  description: string;
  child?: INetworkSensorTree[];
  status: ESensorStatus;
  statusDetail?: string;
}

// 网络组类型
export interface INetworkGroup {
  id: string;
  name: string;
  description: string;
  networkInSensorNames: string;
  networkInSensorIds: string;
  bandwidth: number;
  status: ESensorStatus;
  statusDetail?: string;
}

export type INetworkGroupMap = Record<string, INetworkGroup>;

// 逻辑子网类型
export interface ILogicalSubnet {
  id: string;
  name: string;
  network: string;
  type: ELogicalSubnetType;
  totalBandwidth: number;
}

// ----- network ------
export type TNetifDirection = 'upstream' | 'downstream' | 'hybrid';
export interface INetworkNetif {
  networkId?: string;
  netifName: string;
  /**
   * 配置带宽（Mbps）
   */
  specification: number;
  /**
   * 业务接口的流量流向
   */
  direction: TNetifDirection;
}
export type INetworkSensorMap = Record<string, INetworkSensor>;
export type INetworkMap = Record<string, INetwork>;

export const MAX_CUSTOM_NETWORK_LIMIT = 32;

export type IAction = '0' | '1';

export interface INetwork {
  id: string;
  name: string;
  /**
   * 流量方向
   */
  netifType: IAction;
  /**
   * 网络 ID
   */
  networkInSensorId: string;
  /**
   * 网络名称
   */
  networkInSensorName: string;
  /**
   * 业务接口
   */
  netif: INetworkNetif[];
  /**
   * 网络总带宽(Mbps)
   * @description 根据接口中的带宽累加计算得出
   */
  bandwidth?: number;
  /** 网络额外的配置参数 */
  extraSettings: {
    /**
     * 是否生成流日志
     */
    flowlogDefaultAction: IAction;
    /**
     * 额外包含哪些状态流，csv格式
     * @eg syn_sent
     */
    flowlogExceptStatus?: string;
    /**
     * 是否生成元数据
     */
    metadataDefaultAction: IAction;

    /**
     * 统计需要额外包含的流
     * @eg. interframe,preamble
     */
    flowlogExceptStatistics?: string;

    /** 是否生成 */
    sessionVlanAction: IAction;
  };
  /**
   * 内网IP地址
   */
  insideIpAddress?: string;
  /**
   * 捕获过滤规则
   */
  ingestPolicyId: string;
  /**
   * 应用过滤规则
   */
  filterPolicyId: string;

  /**
   * 描述信息
   */
  description?: string;
}

export interface INetworkFormData extends Omit<INetwork, 'netif' | 'extraSettings'> {
  netif: string;
  extraSettings: string;
}

/**
 * 网络的策略配置
 */
export interface INetworkPolicy {
  id: string;
  networkId: string;
  networkName: string;
  /**
   * 策略类型
   *
   * - ingest: 捕获规则
   * - filter: 应用规则
   */
  policyType: 'ingest' | 'filter';
  policyId: string;
  policyName?: string;
}
