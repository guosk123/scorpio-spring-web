/**
 * ===========
 *  SNMP
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';
import type { ISnmpSettings } from './typings';

export async function querySystemSnmpSettings() {
  return ajax(`${API_VERSION_V1}/system/snmp-settings`);
}

export async function updateSystemSnmpSettings(data: ISnmpSettings) {
  return ajax(`${API_VERSION_V1}/system/snmp-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...data,
    },
  });
}
