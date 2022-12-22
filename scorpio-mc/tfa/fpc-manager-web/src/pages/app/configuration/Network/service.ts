import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { INetwork, INetworkPolicy } from './typings';
import { stringify } from 'qs';

/**
 * 所有的网络
 */
export async function queryAllNetworks() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks`);
}

/**
 * 获取网络详情
 */
export async function queryNetworkDetail(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks/${id}`);
}

/**
 * 新建网络
 */
export async function createNetwork(params: INetwork) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新网络
 */
export async function updateNetwork({ id, ...restParams }: INetwork) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除网络
 */
export async function deleteNetwork(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 获取网络接口关联关系
 */
export async function queryNetworkNetifs() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/network-netifs`);
}

/**
 * 查询网络绑定的策略
 */
export async function queryNetworkPolicy(params: INetworkPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/network-policies?${stringify(params)}`);
}

/**
 * 更新网络绑定的策略
 */
export async function updateNetworkPolicy({ networkId, ...restParams }: INetworkPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks/${networkId}/policies`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}
