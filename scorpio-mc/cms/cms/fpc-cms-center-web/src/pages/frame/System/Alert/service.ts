/**
 * ===========
 *  系统告警
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type { ISystemAlertRule } from './typings';

export async function querySystemAlertRules() {
  return ajax(`${config.API_VERSION_V1}/system/alarm-settings`);
}

export async function querySystemAlertRuleDetail({ id }: { id: string }) {
  return ajax(`${config.API_VERSION_V1}/system/alarm-settings/${id}`);
}

export async function updateSystemAlertRule(params: ISystemAlertRule) {
  const { id, ...restParams } = params;
  return ajax(`${config.API_VERSION_V1}/system/alarm-settings/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function updateSystemAlertRuleState(params: ISystemAlertRule) {
  const { id, state } = params;
  return ajax(`${config.API_VERSION_V1}/system/alarm-settings/${id}/state`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      state,
    },
  });
}
