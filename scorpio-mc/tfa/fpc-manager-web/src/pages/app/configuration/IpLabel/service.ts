import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { EIpLabelCatagory, IIpLabel } from './typings';

export async function createIpLabel(params: Omit<IIpLabel, 'id'>) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label`, {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function updateIpLabel({ id, ...restParams }: IIpLabel) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label/${id}`, {
    method: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function deleteIpLabel(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label/${id}`, {
    method: 'DELETE',
  });
}

export async function queryIpLabel(params?: {
  category?: EIpLabelCatagory;
  name?: string;
  page: number;
  pageSize: number;
}): Promise<IAjaxResponseFactory<IPageFactory<IIpLabel>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label?${stringify(params)}`);
}

export async function queryIpLabelDetail(id: string): Promise<IAjaxResponseFactory<IIpLabel>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label/${id}`);
}

export async function queryIpLabelStat(): Promise<
  IAjaxResponseFactory<Record<EIpLabelCatagory, number>>
> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-label/statistics`);
}
