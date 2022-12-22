import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryArchiveSettings() {
  return ajax(`${config.API_VERSION_V1}/system/archive-settings`);
}

export async function updateArchiveSettings(params) {
  return ajax(`${config.API_VERSION_V1}/system/archive-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryBackupSettings() {
  return ajax(`${config.API_VERSION_V1}/system/backup-settings`);
}

export async function updateBackupSettings(params) {
  return ajax(`${config.API_VERSION_V1}/system/backup-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
