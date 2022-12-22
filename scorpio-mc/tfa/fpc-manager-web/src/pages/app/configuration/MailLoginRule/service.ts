import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { EMailRuleAction, EMailRuleStatus, IMailLoginRule } from './typings';

export async function queryAllMailLoginRule(params: {
  mailAddress?: string;
  action?: EMailRuleAction;
  pageNumber: number;
  pageSize: number;
}): Promise<IAjaxResponseFactory<IPageFactory<IMailLoginRule>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rules?${stringify(params)}`);
}

export async function queryMailLoginRuleDetail(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rule/${id}`);
}

export async function createMailLoginRule(rule: Partial<IMailLoginRule>) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rule`, {
    method: 'POST',
    data: {
      ...rule,
    },
  });
}

export async function deleteMailLoginRule(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rule/${id}`, {
    method: 'DELETE',
  });
}

export async function updateMailLoginRuleState(
  id: string,
  state: EMailRuleStatus,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rule/${id}/state`, {
    method: 'PUT',
    data: {
      state,
    },
  });
}

export async function updateMailLoginRule(
  id: string,
  updates: Partial<IMailLoginRule>,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/mail-rule/${id}`, {
    method: 'PUT',
    data: {
      ...updates,
    },
  });
}
