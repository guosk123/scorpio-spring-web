import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IPageParms } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { INetworkPerm, IUpdateNetworkPermParams } from './typings';

const { API_VERSION_PRODUCT_V1 } = config;

/**
 * 获取可分配权限的用户及其当前的网络（组）权限
 */
export async function queryNetworkPerms(
  params: IPageParms,
): Promise<IAjaxResponseFactory<IPageFactory<INetworkPerm>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/user-network-perms?${stringify(params)}`);
}

/**
 * 配置某个用户的网络（组）权限
 */
export async function updateNetworkPerms(
  params: IUpdateNetworkPermParams,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/user-network-perms`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
