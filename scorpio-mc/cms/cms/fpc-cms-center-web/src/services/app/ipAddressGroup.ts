/**
 * ===========
 *  地址组管理
 * ===========
 */
import application from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IPageParms } from '@/common/typings';
import type { IpAddressGroup } from '@/pages/app/Configuration/IpAddressGroup/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
const { API_VERSION_PRODUCT_V1 } = application;

export async function queryIpAddressGroups(
  params: IPageParms & Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<IpAddressGroup>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/host-groups${params && `?${stringify(params)}`}`,
  );
}

export async function queryAllIpAddressGroup() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups/as-list`);
}

export async function queryIpAddressGroupDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups/${id}`);
}

export async function createIpAddressGroup(params: Record<string, any>) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function updateIpAddressGroup(params: Record<string, any>) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function deleteIpAddressGroup({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 导入
 */
export async function importIpAddressGroups(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/host-groups/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
