import { API_VERSION_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

export async function powerReboot() {
  return ajax(`${API_VERSION_V1}/system/reboot`, {
    type: 'POST',
    data: {
      _method: 'PUT',
    },
  });
}

export async function powerShutdown() {
  return ajax(`${API_VERSION_V1}/system/shutdown`, {
    type: 'POST',
    data: {
      _method: 'PUT',
    },
  });
}
