/**
 * ===========
 *  系统时间
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryDeviceNtps() {
  return ajax(`${config.API_VERSION_V1}/system/device-ntps`);
}

export async function updateDeviceNtps(params) {
  return ajax(`${config.API_VERSION_V1}/system/device-ntps`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryDeviceNtpState({ id }) {
  return ajax(`${config.API_VERSION_V1}/system/device-ntps/${id}/state`);
}
