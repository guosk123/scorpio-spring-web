import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ECustomTimeType, ICustomTime } from './typings';

interface IPaginationParams {
  page?: number;
  pageSize?: number;
}

export async function querySelfDefinedTime(params?: {
  type?: ECustomTimeType;
}): Promise<IAjaxResponseFactory<ICustomTime[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times?${stringify(params)}`);
}

export async function createSelfDefinedTime(formData: ICustomTime) {
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
export async function deleteSelfDefinedTimes(ids: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/custom-times/batch`, {
    type: 'POST',
    data: {
      idList: ids,
      _method: 'DELETE',
    },
  });
}

export async function querySelfDefinedTimeDetail({ id }: { id: string }) {
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
