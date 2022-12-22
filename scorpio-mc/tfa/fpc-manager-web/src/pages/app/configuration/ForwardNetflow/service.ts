import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IForwardPolicy, IForwardPolicyStatData, IForwardRule } from './typings';
import { EForwardPolicyState } from './typings';

const FORWARD_RULES_API_PREFIX = `${API_VERSION_PRODUCT_V1}/appliance/forward-rules`;
const FORWARD_POLICY_API_PREFIX = `${API_VERSION_PRODUCT_V1}/appliance/forward-policies`;

type ResType<T> = Promise<IAjaxResponseFactory<T>>;
type ResPageType<T> = Promise<IAjaxResponseFactory<IPageFactory<T>>>;

export async function queryForwardRules(params: any): ResPageType<IForwardRule> {
  return ajax(`${FORWARD_RULES_API_PREFIX}?${stringify(params)}`);
}

export async function queryForwardRulesList(): ResType<IForwardRule[]> {
  return ajax(`${FORWARD_RULES_API_PREFIX}/as-list`);
}

export async function queryForwardRuleDetail(ruleId: string): ResType<IForwardRule> {
  return ajax(`${FORWARD_RULES_API_PREFIX}/${ruleId}`);
}

export async function createForwardRule(rule: IForwardRule): ResType<any> {
  return ajax(`${FORWARD_RULES_API_PREFIX}`, {
    method: 'POST',
    data: rule,
  });
}

export async function updateForwardRule(rule: IForwardRule): ResType<any> {
  const { id } = rule;
  return ajax(`${FORWARD_RULES_API_PREFIX}/${id}`, {
    method: 'POST',
    data: {
      _method: 'PUT',
      ...rule,
    },
  });
}

export async function deleteForwardRule(ruleId: string): ResType<any> {
  return ajax(`${FORWARD_RULES_API_PREFIX}/${ruleId}`, {
    method: 'DELETE',
  });
}

export async function createForwardPolicy(policy: IForwardPolicy): ResType<any> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}`, {
    method: 'POST',
    data: policy,
  });
}

export async function queryForwardPolicies(
  params: Record<string, any>,
): ResPageType<IForwardPolicy> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}?${stringify(params)}`);
}

export async function queryForwardPolicyDetail(id: string): ResType<IForwardPolicy> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}/${id}`);
}

export async function enableForwardPolicy(id: string): ResType<any> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}/${id}/state`, {
    method: 'PUT',
    data: {
      state: EForwardPolicyState.启用,
    },
  });
}

export async function disableForwardPolicy(id: string): ResType<any> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}/${id}/state`, {
    method: 'PUT',
    data: {
      state: EForwardPolicyState.停用,
    },
  });
}

export async function updateForwardPolicy(policy: IForwardPolicy): ResType<any> {
  const { id } = policy;

  return ajax(`${FORWARD_POLICY_API_PREFIX}/${id}`, {
    method: 'POST',
    data: {
      _method: 'PUT',
      ...policy,
    },
  });
}

export async function deleteForwardPolicy(id: string): ResType<any> {
  return ajax(`${FORWARD_POLICY_API_PREFIX}/${id}`, {
    method: 'DELETE',
  });
}

export async function queryNetworkPolicy(): ResType<any> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/networks/forward-policies`);
}

export async function queryForwardPolicyHistogram(
  params: Record<string, any>,
): ResType<IForwardPolicyStatData[]> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/forward-policies/as-histogram?${stringify(params)}`,
  );
}
