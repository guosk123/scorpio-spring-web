import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import type { NATConfig } from './typings';

export async function queryNatConfig(): Promise<IAjaxResponseFactory<NATConfig>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/nat-config`);
}

export async function updateNatConfig(data: NATConfig): Promise<IAjaxResponseFactory<void>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/nat-config`, {
    method: 'PUT',
    data,
  });
}
