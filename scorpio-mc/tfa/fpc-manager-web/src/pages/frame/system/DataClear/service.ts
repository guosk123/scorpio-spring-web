import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';

export async function clearData(params: string[]) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/data-clear`, {
    method: 'POST',
    data: {
      dataClearParams: params.join(','),
    },
  });
}

export async function queryDataClearCategory(): Promise<
  IAjaxResponseFactory<Record<string, string>>
> {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/data-clear/category`);
}
