import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import type { ISystemLog } from '@/pages/frame/System/LogAlerm/Log/typings';

export async function querySystemLogs(
  params: Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ISystemLog>>> {
  return ajax(`${config.API_VERSION_V1}/system/logs${params && `?${stringify(params)}`}`);
}
