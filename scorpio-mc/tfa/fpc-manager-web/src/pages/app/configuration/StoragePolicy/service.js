/**
 * ===========
 *  流量存储配置
 * ===========
 */

import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function query() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/storage-policies`);
}

export async function update(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/storage-policies`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
