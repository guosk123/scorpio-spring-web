import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IMailLoginAlert } from '../typings';

export async function queryMailAlerts(params: {
  startTime: string;
  endTime: string;
  pageNumber: number;
  pageSize: number;
}): Promise<IAjaxResponseFactory<IPageFactory<IMailLoginAlert>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-alerts?${stringify(params)}`);
}

export async function queryMailAlertsTotal(params: {
  startTime: string;
  endTime: string;
}): Promise<IAjaxResponseFactory<{ total: number }>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-alerts/as-statistics?${stringify(params)}`);
}
