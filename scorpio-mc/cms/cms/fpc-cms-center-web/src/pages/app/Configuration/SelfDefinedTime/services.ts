import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import { stringify } from 'qs';
import type { ECustomTimeType, ICustomTime } from './typings';
const { API_VERSION_PRODUCT_V1 } = config;
// interface IPaginationParams {
//   page?: number;
//   pageSize?: number;
// }

export async function queryAllSelfdefinedTime(params?: {
  type?: ECustomTimeType;
}): Promise<IAjaxResponseFactory<ICustomTime[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times?${stringify(params)}`);
}

export async function createSingleSelfdefinedTime(formData: ICustomTime) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times`, {
    type: 'POST',
    data: formData,
  });
}

export async function deleteSelfDefinedTime(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
export async function deleteAllSelfdefinedTimes(ids: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times/batch`, {
    type: 'POST',
    data: {
      idList: ids,
      _method: 'DELETE',
    },
  });
}

export async function querySingleDetailTime({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times/${id}`, {
    type: 'GET',
  });
}

export async function updateSelfDefinedTimeDetail(params: any) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}
