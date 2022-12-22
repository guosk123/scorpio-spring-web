import appConfig from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type {
  IMitreAttack,
  IRuleClasstype,
  ISuricataAlertMessage,
  SuricataAlertEvent,
  SuricataStatisticsResult,
} from './typings';

const { API_VERSION_PRODUCT_V1 } = appConfig;

export async function querySuricataMitreAttack(props?: {
  startTime?: string;
  endTime?: string;
}): Promise<IAjaxResponseFactory<IMitreAttack[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/mitre-attacks?${stringify(props || {})}`);
}

export async function querySuricataRuleClasstype(props: {
  startTime?: string;
  endTime?: string;
}): Promise<IAjaxResponseFactory<IRuleClasstype[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes?${stringify(props)}`);
}

export async function createSuricataRuleClasstype(data: { name: string; level: number }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes`, {
    method: 'POST',
    data,
  });
}

export async function updateSuricataRuleClasstype({ id, ...restParams }: IRuleClasstype) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes/${id}`, {
    method: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function deleteSuricataRuleClasstype(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes/${id}`, {
    method: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

// 安全分析统计
export async function queryAlertMessageStatistics(params: {
  dsl: string;
  count?: number;
}): Promise<IAjaxResponseFactory<SuricataStatisticsResult[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/alert-messages/statistics?${stringify(params)}`);
}

export async function queryAlertMessageHistogram(params: {
  dsl: string;
  interval: number;
}): Promise<IAjaxResponseFactory<any[]>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/suricata/alert-messages/as-histogram?${stringify(params)}`,
  );
}

export async function queryAlertMessageList(
  params: {
    page: number;
    pageSize: number;
    dsl: string;
  } & Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ISuricataAlertMessage>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/alert-messages?${stringify(params)}`);
}

interface AlertRelateParams {
  srcIp: string;
  destIp: string;
  sid: number;
  startTime: string;
  endTime: string;
}
export async function queryAlertDetailRelation(
  params: AlertRelateParams,
): Promise<IAjaxResponseFactory<SuricataAlertEvent[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/alert-messages/relation?${stringify(params)}`);
}

// export async function queryAlertMessageTags(): Promise<IAjaxResponseFactory<string[]>> {
//   return ajax(`${API_VERSION_PRODUCT_V1}/suricata/alert-messages/basic-tags`);
// }
