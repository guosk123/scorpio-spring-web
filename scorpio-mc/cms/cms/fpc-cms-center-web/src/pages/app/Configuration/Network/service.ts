import application from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import type { INetworkGroup, INetworkSensor } from '@/pages/app/Configuration/Network/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { INetworkPolicy } from './typings';
const { API_VERSION_PRODUCT_V1 } = application;

// 探针网络表单参数
export interface ISensorNetworkParams {
  id?: string;
  // 网络相关
  networkInSensorId: string;
  networkInSensorName: string;
  name?: string;
  // 探针相关
  sensorName: string;
  sensorId: string;
  sensorType: string;
  owner: string;
  // 描述
  description?: string;
  sendPolicyIds?: string;
}
// 网络组表单参数
export interface INetworkGroupsParams {
  id?: string;
  name: string;
  networkInSensorIds?: string;
  networkInSensorNames?: string;
  description: string;
  sendPolicyIds?: string;
}

/** 探针网络相关接口 */
// 用来获取网络探针列表
export async function queryNetworkSensors(): Promise<IAjaxResponseFactory<INetworkSensor[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks`);
}

// 用来获取单个网络探针信息
export async function queryNetworkSensorById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks/${id}`);
}

// 获取探针设备树状结构
export async function queryNetworkSensorTree() {
  return ajax(`${API_VERSION_PRODUCT_V1}/central/devices/as-list`);
}

// 获取探针下的网络列表
export async function queryNetworkInSensorListById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks-in-sensor/${id}`);
}

// 添加所有
export async function createAllSensorNetworks() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks-all`, {
    type: 'POST',
  });
}
// 添加探针网络
export async function createSensorNetwork(params: ISensorNetworkParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

// 编辑探针网络
export async function updateSensorNetwork({ id, ...restParams }: ISensorNetworkParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

// 删除探针网络
export async function deleteSensorNetwork(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/** 网络组相关接口 */
// 用来获取网络组列表
export async function queryNetworkGroups(): Promise<IAjaxResponseFactory<INetworkGroup[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-network-groups`);
}

// 用来获取单个网络组信息
export async function queryNetworkGroupById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-network-groups/${id}`);
}

// 删除网络组
export async function deleteNetworkGroup(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-network-groups/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

// 批量删除网络组
export async function deleteBatchNetworkGroup(ids: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-network-groups/batch`, {
    type: 'POST',
    data: {
      ids,
    },
  });
}

// 创建网络组
export async function createNetworkGroup(params: INetworkGroupsParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks-groups`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}
// 编辑网络组
export async function updateNetworkGroup({ id, ...restParams }: INetworkGroupsParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sensor-networks-groups/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 查询网络绑定的策略
 */
export async function queryNetworkPolicy(params: INetworkPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/network-policies?${stringify(params)}`);
}

/**
 * 新建网络绑定的策略
 */
export async function createNetworkPolicy(params: INetworkPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks-policies`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新网络绑定的策略
 */
export async function updateNetworkPolicy({ id, ...restParams }: INetworkPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks-policies/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/** 用来获取启用的外发策略列表 */
export async function querySendPolicyStateOn() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/state-on`);
}