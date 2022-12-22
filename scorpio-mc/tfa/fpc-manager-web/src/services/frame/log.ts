import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import type { ISystemLog } from '@/pages/frame/system/Log/typing';

export async function queryLogs(
  params: Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ISystemLog>>> {
  return ajax(`${API_VERSION_V1}/system/logs${params && `?${stringify(params)}`}`);
}
