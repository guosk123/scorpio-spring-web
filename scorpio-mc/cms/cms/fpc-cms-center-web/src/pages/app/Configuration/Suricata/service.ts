import appConfig from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ISuricataRule } from './typings';

const { API_VERSION_PRODUCT_V1 } = appConfig;

export async function changeSuricataRuleState({ sids, state }: { sids: number[]; state: boolean }) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/suricata/rules/${encodeURIComponent(sids.join(','))}/state`,
    {
      method: 'PUT',
      data: {
        state: state ? '1' : '0',
      },
    },
  );
}

export async function enableAllSuricataRules() {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/all/state`, {
    method: 'PUT',
    data: {
      state: '1',
    },
  });
}

export async function disableAllSuricataRules() {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/all/state`, {
    method: 'PUT',
    data: {
      state: '0',
    },
  });
}

export async function deleteSuricataRules(sids: number[]) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/${encodeURIComponent(sids.join(','))}`, {
    method: 'DELETE',
  });
}

export async function deleteAllSuricataRules() {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/all`, {
    method: 'DELETE',
  });
}

export async function importSuricataRule(data: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/as-import`, {
    method: 'POST',
    processData: false,
    contentType: false,
    data,
  });
}

export async function createSuricataRule(data: ISuricataRule): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules`, {
    method: 'POST',
    data,
  });
}

/**
 * 获取规则来源枚举
 */
export async function querySuricataSource(): Promise<IAjaxResponseFactory<Record<string, string>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/sources`);
}

export async function querySuricataRules(
  data: Record<string, any>,
): Promise<IAjaxResponseFactory<IPageFactory<ISuricataRule>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules?${stringify(data)}`, {
    method: 'GET',
  });
}

export async function querySuricataRuleDetail(
  id: number,
): Promise<IAjaxResponseFactory<ISuricataRule>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/${id}`);
}

export async function updateSuricataRule({
  sid,
  ...restParams
}: ISuricataRule): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/${sid}`, {
    method: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function batchDeleteSuricataRule(query: Record<string, any>) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules?${stringify(query)}`, {
    method: 'DELETE',
  });
}

export async function batchChangeSuricataRule(
  query: Record<string, any>,
  change: {
    state?: string;
    classtypeId?: string;
    mitreTacticId?: string;
    mitreTechniqueId?: string;
    source?: string;
  },
) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rules/update`, {
    method: 'POST',
    data: {
      ...(query.sids ? { sids: query.sids.join(',') } : { query: JSON.stringify(query) }),
      ...change,
    },
  });
}

export async function importSuricataClasstype(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/rule-classtypes/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

/**
 * 导入威胁情报
 */
export async function importThreatIntelligenceRule(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/threat-intelligences/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
