/**
 * ===========
 *  流量存储配置
 * ===========
 */

import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryStorageSpaceSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/storage-spaces`);
}

export async function updateStorageSpaceSettings(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/storage-spaces`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
