import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ESortDirection } from '../analysis/typings';

export interface IServiceHistogramParams {
  startTime: string;
  endTime: string;
  interval: number;
  serviceId?: string;
  networkId: string;
  sortProperty?: string;
  sortDirection?: ESortDirection;
}

export async function queryServiceHistogram(params: IServiceHistogramParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/as-histogram?${stringify(params)}`);
}

export async function queryTotalPayload(params: {
  networkId: string;
  startTime: string;
  endTime: string;
}) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/networks/as-total-payload?${stringify(params)}`,
  );
}

export async function queryTopUserFlow(
  params: IServiceHistogramParams,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/l3-devices/as-count?${stringify(params)}`);
}

export async function queryIpConversationTop(
  params: IServiceHistogramParams,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-count?${stringify(params)}`);
}
