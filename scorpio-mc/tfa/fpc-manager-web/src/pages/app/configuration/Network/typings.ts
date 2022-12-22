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
  filterRuleIds: string;

  /** 外发策略 */
  sendPolicyIds?: string;
  /**
   * 描述信息
   */
  description?: string;
}

export interface INetworkFormData extends Omit<INetwork, 'netif' | 'extraSettings'> {
  netif: string;
  extraSettings: string;
  sendPolicyIds: string;
}

/**
 * 网络的策略配置
 */
export interface INetworkPolicy {
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
