import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function querySecuritySettings() {
  return ajax(`${config.API_VERSION_V1}/system/security-settings`);
}

export async function updateSecuritySettings(params) {
  return ajax(`${config.API_VERSION_V1}/system/security-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
