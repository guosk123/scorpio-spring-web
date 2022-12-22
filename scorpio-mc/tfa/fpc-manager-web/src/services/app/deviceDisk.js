/**
 * ===========
 *  RAID状态
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryDeviceDisks() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-disks`);
}

export async function updateDeviceDisks(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-disks`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
