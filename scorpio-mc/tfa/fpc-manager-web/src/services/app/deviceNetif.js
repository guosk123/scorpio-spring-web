/**
 * ===========
 *  接口配置
 * ===========
 */
import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryDeviceNetifs() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs`);
}

export async function queryReceiveDeviceNetifUsages(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs/as-histogram?${stringify(params)}`);
}

export async function updateDeviceNetifs(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
