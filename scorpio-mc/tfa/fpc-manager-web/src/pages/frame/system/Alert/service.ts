/**
 * ===========
 *  系统告警
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';
import type { ISystemAlertRule } from './typings';

export async function querySystemAlertRules() {
  return ajax(`${API_VERSION_V1}/system/alarm-settings`);
}

export async function querySystemAlertRuleDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/alarm-settings/${id}`);
}

export async function updateSystemAlertRule(params: ISystemAlertRule) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_V1}/system/alarm-settings/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function updateSystemAlertRuleState(params: ISystemAlertRule) {
  const { id, state } = params;
  return ajax(`${API_VERSION_V1}/system/alarm-settings/${id}/state`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      state,
    },
  });
}
