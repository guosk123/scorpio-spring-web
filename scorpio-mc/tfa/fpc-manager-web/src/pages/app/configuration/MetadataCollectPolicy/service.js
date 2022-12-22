import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

/**
 * 获取所有的采集配置列表
 */
export async function queryCollectPolicys() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys`);
}

/**
 * 获取某个采集策略详情
 */
export async function queryCollectPolicyDetail({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys/${id}`);
}

/**
 * 新建采集策略
 */
export async function createCollectPolicy(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新采集策略
 */
export async function updateCollectPolicy({ id, ...restParams }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除采集策略
 * @param {String} id
 */
export async function deleteCollectPolicy({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 启用、禁用
 * @param {String} id
 * @param {String} state 新的状态
 */
export async function changeState({ id, state }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/collect-policys/${id}/state`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      state,
    },
  });
}
