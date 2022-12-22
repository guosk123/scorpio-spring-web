/**
 * ===========
 *  单点登录
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type { IServerIpSettings } from './typings';

export async function querySystemServerIpSettings() {
  return ajax(`${config.API_VERSION_V1}/system/server-ip-settings`);
}

export async function updateSystemServerIpSettings(data: IServerIpSettings) {
  return ajax(`${config.API_VERSION_V1}/system/server-ip-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...data,
    },
  });
}
