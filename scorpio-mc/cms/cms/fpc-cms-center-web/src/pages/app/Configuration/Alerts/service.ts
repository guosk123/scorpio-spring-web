/**
 * ===========
 *  业务告警
 * ===========
 */
import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import application from '@/common/applicationConfig';
import type {
  IAlertRule,
  AlertLevelTypes,
  IAlertSyslog,
  EAlertCategory,
  IAlertMessage,
} from './typings';
import { EAlertRuleStatus } from './typings';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';

const { API_VERSION_PRODUCT_V1 } = application;
export interface IQueryParams {
  page: number;
  pageSize: number;
  name?: string;
  category?: EAlertCategory;
  level?: AlertLevelTypes;
  networkId?: string;
  serviceId?: string;
}

export async function queryAlertMessages(
  params: { startTime?: string; endTime?: string } & IQueryParams,
): Promise<IAjaxResponseFactory<IPageFactory<IAlertMessage>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/alert-messages${params && `?${stringify(params)}`}`,
  );
}

export async function queryAlertMessageDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-messages/${id}`);
}

export async function queryAlertRules(
  params: IQueryParams,
): Promise<IAjaxResponseFactory<IPageFactory<IAlertRule>>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/alert-rules${params && `?${stringify(params)}`}`,
  );
}

export async function queryAllAlertRules(params: { category: string }) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/alert-rules/as-list${params && `?${stringify(params)}`}`,
  );
}

export async function createAlertRule(params: IAlertRule) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function queryAlertRuleDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules/${id}`);
}

export async function updateAlertRule(params: IAlertRule) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function enableAlertRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules/${id}/status`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: EAlertRuleStatus.ENABLE,
    },
  });
}

export async function disableAlertRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules/${id}/status`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: EAlertRuleStatus.DISENABLE,
    },
  });
}

export async function deleteAlertRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-rules/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function queryAlertSyslogSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-syslogs`);
}

export async function updateAlertSyslogSettings(params: IAlertSyslog) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-syslogs`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function disposeAlertRule(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-messages/${params.id}/solve`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      reason: params.reason,
    },
  });
}

export async function queryAlertAnalysisDetail(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/alert-messages/as-analysis?${stringify(params)}`,
  );
}
