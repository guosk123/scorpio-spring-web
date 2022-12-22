import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';

export async function queryArchiveSettings() {
  return ajax(`${API_VERSION_V1}/system/archive-settings`);
}

export async function updateArchiveSettings(params) {
  return ajax(`${API_VERSION_V1}/system/archive-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryBackupSettings() {
  return ajax(`${API_VERSION_V1}/system/backup-settings`);
}

export async function updateBackupSettings(params) {
  return ajax(`${API_VERSION_V1}/system/backup-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
