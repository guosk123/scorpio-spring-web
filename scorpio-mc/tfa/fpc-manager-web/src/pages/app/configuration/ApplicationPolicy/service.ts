/**
 * ===========
 *  应用过滤配置
 * ===========
 */

import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type {
  EApplicationPolicyState,
  IApplicationPolicy,
  IApplicationPolicyForm,
} from './typings';

/**
 * 获取分页的应用过滤策略
 */
export async function queryApplicationPolicies(params: {
  pageSize: number;
  page: number;
}): Promise<IAjaxResponseFactory<IPageFactory<IApplicationPolicy>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/filter-rules${params && `?${stringify(params)}`}`,
  );
}

export interface IMoveAppPoliciesParams {
  idList: string;
  operator?: string;
  page: number;
  pageSize: number;
}
/** 移动规则 */
export async function moveApplicationPolicies(params: IMoveAppPoliciesParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/change/priority`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/** 修改规则状态 */
export async function alterApplicationPolicyStates(params: {
  idList: string;
  state: EApplicationPolicyState;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/change/state`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/**
 * 所有的应用过滤策略
 */
export async function queryAllApplicationPolicies() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/as-list`);
}

/**
 * 获取应用过滤策略
 */
export async function queryApplicationPolicyDetail({ policyId }: { policyId: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-policies/${policyId}`);
}

/**
 * 新建应用过滤策略
 */
export async function createApplicationPolicy(params: IApplicationPolicyForm, before?: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules`, {
    type: 'POST',
    data: {
      ...params,
      ...(() => {
        if (before) {
          return {
            before,
          };
        }
        return {};
      })(),
    },
  });
}

/**
 * 更新应用过滤策略
 */
export async function updateApplicationPolicy({ id, ...restParams }: IApplicationPolicy) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 导出
 */
export async function exportApplicationPolicy() {
  const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}/appliance/filter-rules/as-export`;
  window.location.href = url;
}

/**
 * 删除应用过滤策略
 */
export async function deleteApplicationPolicy(idList: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/deleted/batch`, {
    type: 'DELETE',
    data: {
      _method: 'DELETE',
      idList,
    },
  });
}


/**
 * 导入自定义分类、子分类、应用
 */
export async function importFilterRules(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/filter-rules/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
