import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { IAlarmAndLogSyslog } from './typings';

export async function querySystemSyslogSettings() {
  return ajax(`${config.API_VERSION_V1}/system/syslogs`);
}

export async function updateSystemSyslogSettings(params: IAlarmAndLogSyslog) {
  return ajax(`${config.API_VERSION_V1}/system/syslogs`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
