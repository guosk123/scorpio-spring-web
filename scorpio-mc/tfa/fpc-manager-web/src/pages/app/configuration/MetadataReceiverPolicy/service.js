import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

/**
 * 获取发送配置
 */
export async function queryReceiverSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/receiver-settings`);
}

/**
 * 更新发送配置
 */
export async function updateReceiverSettings(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/receiver-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/**
 * 删除发送配置
 */
export async function deleteReceiverSettings({ id }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/receiver-settings/${id}`, {
    type: 'POST',
    data: {
      _method: 'delete',
    },
  });
}
