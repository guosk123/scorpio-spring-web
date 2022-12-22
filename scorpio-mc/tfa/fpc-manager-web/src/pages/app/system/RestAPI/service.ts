import { API_VERSION_PRODUCT_V1, API_VERSION_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ERestStatType, IRestAPIRecord, IRestMetric } from './typings';

export async function queryRestApiRecord(
  params: Record<string, string | number | undefined>,
): Promise<IAjaxResponseFactory<IPageFactory<IRestAPIRecord>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/restapis?${stringify(params)}`);
}

interface QueryStatParams {
  type: ERestStatType;
  startTime: string;
  endTime: string;
  interval: number;
  count: number;
}

export async function queryRestStat(
  params: QueryStatParams,
): Promise<IAjaxResponseFactory<IRestMetric[]>> {
  const { type, ...rest } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/system/restapis/as-${type}?${stringify(rest)}`);
}

export async function queryRestStatList(
  params: Omit<QueryStatParams, 'interval' | 'count'>,
): Promise<IAjaxResponseFactory<IRestMetric[]>> {
  const { type, ...rest } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/system/restapis/${type}/as-list?${stringify(rest)}`);
}

export async function queryRestPermUserList(): Promise<
  IAjaxResponseFactory<{ name: string; userId: string }[]>
> {
  return ajax(`${API_VERSION_V1}/system/restapi-users/as-list`);
}
