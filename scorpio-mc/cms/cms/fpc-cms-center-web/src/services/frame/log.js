import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';

export async function queryLogs(params) {
  return ajax(`${application.API_VERSION_V1}/system/logs${params && `?${stringify(params)}`}`);
}
