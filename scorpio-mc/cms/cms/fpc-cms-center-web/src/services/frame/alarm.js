import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryAlarms(params) {
  return ajax(`${config.API_VERSION_V1}/system/alarms${params && `?${stringify(params)}`}`);
}

export async function countGroupbyLevel() {
  return ajax(`${config.API_VERSION_V1}/system/alarms/group-by-level`);
}

export async function queryAlarmDetail({ id }) {
  return ajax(`${config.API_VERSION_V1}/system/alarms/${id}`);
}

export async function solveAlerm({ id, ...restParams }) {
  return ajax(`${config.API_VERSION_V1}/system/alarms/${id}/solve`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}
