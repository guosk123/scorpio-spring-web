/** 网络权限 */
export interface INetworkPerm {
  userId: string;
  userName: string;
  networkIds: string;
  networkNames: string;
  networkGroupIds: string;
  networkGroupNames: string;
}

/**
 * 更新网络权限时的参数
 */
export interface IUpdateNetworkPermParams {
  /** 用户ID */
  userId: string;
  /**
   * 网络、子网ID
   * @description 填空则代表该用户不具有任何网络权限
   */
  networkIds?: string;
  /**
   * 网络组ID
   * @description 填空则代表该用户不具有任何网络组权限
   */
  networkGroupIds?: string;
}
