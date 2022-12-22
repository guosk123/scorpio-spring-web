/**
 * 会话应用识别前的流量存储
 */
export enum EExceptFlowAction {
  SAVE = '1',
  NO_SAVE = '0',
}

export enum EApplicationPolicyState {
  'Disable' = '0',
  'Enable' = '1',
}

export enum EApplicationPolicyAction {
  'STORE' = 'store',
  'TRUNCATE' = 'truncate',
  'DROP' = 'drop',
}
/**
 * 应用过滤策略
 */
export interface IApplicationPolicy {
  id: string;
  /**
   * 应用过滤规则名称
   */
  name: string;
  /**
   * 描述信息
   */
  description?: string;

  /** 源Ip */
  sourceIp: string;
  /** 源端口 */
  sourcePort: number;
  /** 目的Ip */
  destIp: string;
  /** 目的端口 */
  destPort: number;
  /** 传输层协议 */
  protocol: string;
  /** vlanid */
  vlanId: number;
  /** 应用id */
  applicationId: {
    categoryId: string | null;
    subCategoryId: string | null;
    applicationId: string | null;
  }[];
  /** action "action"有三种动作，分别是:"store"、"truncate"和"drop"，分别表示全量存储，截断存储和不存储 */
  action: EApplicationPolicyAction;
  /** "truncLen"指截断长度，仅在截断存储时生效 */
  truncLen?: number;

  /** 启用状态 */
  state: EApplicationPolicyState;
  /** 生效网络 */
  networkId: string;
  /** 生效网络组 */
  networkGroupId: string;
  /** 是否可用 */
  disabled?: boolean;
}

/**
 * 应用过滤策略表单
 */
export interface IApplicationPolicyForm {
  id: string;
  /**
   * 应用过滤规则名称
   */
  name: string;
  /**
   * 描述信息
   */
  description?: string;

  tuple: string;
  /** 生效网络 */
  networkId: string;
}
