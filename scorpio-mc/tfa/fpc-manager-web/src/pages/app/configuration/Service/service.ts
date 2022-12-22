import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { IFollowServiceParams, IService } from './typings';
import { stringify } from 'qs';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';

/**
 * 所有的业务
 */
export async function queryAllServices(): Promise<IAjaxResponseFactory<IService[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/as-list`);
}

/** 获取业务分页列表 */
export async function queryServices(
  params: any,
): Promise<IAjaxResponseFactory<IPageFactory<IService>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services?${stringify(params)}`);
}

/**
 * 获取业务详情
 */
export async function queryServiceDetail(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/${id}`);
}

/**
 * 新建业务
 */
export async function createService(params: IService) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新业务
 */
export async function updateService({ id, ...restParams }: IService) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除业务
 */
export async function deleteService(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 导入业务
 */
export async function importService(formData: { file: any }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

/**
 * 获取关注的业务
 */
export async function queryServiceFollows() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/follow`);
}
/**
 * 关注（取消关注）业务
 */
export async function updateServiceFollow(params: IFollowServiceParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/follow`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
