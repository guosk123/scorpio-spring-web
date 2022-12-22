/**
 * ===========
 *  捕获策略
 * ===========
 */

import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';
import type { IIngestPolicy } from './typings';
import { stringify } from 'qs';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';

const { API_VERSION_PRODUCT_V1 } = application;
/**
 * 获取分页的捕获策略
 */
export async function queryIngestPolicies(params: {
  pageSize: number;
  page: number;
}): Promise<IAjaxResponseFactory<IPageFactory<IIngestPolicy>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/ingest-policies${params && `?${stringify(params)}`}`,
  );
}

/**
 * 所有的捕获策略
 */
export async function queryAllIngestPolicies() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ingest-policies/as-list`);
}

/**
 * 获取捕获策略
 */
export async function queryIngestPolicyDetail(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ingest-policies/${id}`);
}

/**
 * 新建捕获策略
 */
export async function createIngestPolicy(params: IIngestPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ingest-policies`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新捕获策略
 */
export async function updateIngestPolicy({ id, ...restParams }: IIngestPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ingest-policies/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除捕获策略
 */
export async function deleteIngestPolicy(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ingest-policies/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
