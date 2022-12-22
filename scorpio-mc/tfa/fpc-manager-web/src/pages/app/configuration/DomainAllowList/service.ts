import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { DomainAllowListItem } from './typings';

export async function queryDomainAllowList(
  params: any,
): Promise<IAjaxResponseFactory<IPageFactory<DomainAllowListItem>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white?${stringify(params)}`);
}

export async function queryDomainAllowListDetail(
  id: string,
): Promise<IAjaxResponseFactory<DomainAllowListItem>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white/${id}`);
}

export async function createDomainAllowListItem(
  item: Omit<DomainAllowListItem, 'id'>,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white`, {
    method: 'POST',
    data: item,
  });
}

export async function updateDomainAllowListItem(item: DomainAllowListItem): Promise<any> {
  const { id, ...rest } = item;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white/${id}`, {
    method: 'PUT',
    data: rest,
  });
}

export async function deleteDomainAllowListItem(id: string): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white/${id}`, {
    method: 'DELETE',
  });
}

export async function deleteDomainAllowList(
  params?: any,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white`, {
    method: 'DELETE',
    data: params,
  });
}

export async function importDomainAllowList(formData: any): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/domain-white/as-import`, {
    method: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
