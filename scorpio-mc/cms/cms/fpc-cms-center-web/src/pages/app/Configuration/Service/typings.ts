export interface IServiceRule {
  ipAddress: string;
  protocol: 'ALL' | 'TCP' | 'UDP';
  port: string;
}

export type IServiceMap = Record<string, IService>;

export const MAX_CUSTOM_SERVICE_LIMIT = 1000;

export interface IService {
  id: string;
  name: string;
  networkIds: string;
  networkGroupIds: string;
  networkNames: string;
  /**
   * 应用集合，csv
   */
  application: string;
  description?: string;
}

export interface IServiceFormData extends IService {
  rule: string;
}

export interface IServiceNetworkId {
  sourceType: 'network' | 'subnet';
  sourceId: string;
}

/** 用户关注的业务信息 */
export interface IFollowService {
  userId: string;
  serviceId: string;
  networkId?: string;
  networkGroupId?: string;
  followTime: string;
}

export enum EServiceFollowState {
  'FOLLOW' = '1',
  'CANCEL_FOLLOW' = '0',
}

/** 更新用户关注业务时是参数 */
export interface IFollowServiceParams {
  serviceId: string;
  networkGroupId?: string;
  networkId?: string;
  /**
   * 业务关注状态
   * @value 0 取消关注
   * @value 1 关注
   */
  state: EServiceFollowState;
}

export interface IServiceLink {
  link?: string;
  metric?: string;
}
