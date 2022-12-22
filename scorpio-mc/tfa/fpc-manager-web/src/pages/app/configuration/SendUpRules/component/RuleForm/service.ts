import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
// import qs from 'qs';

export interface ISendUpRulesForm {
  name: string;
  sendRuleContent: string;
}

/** 用来获取外发规则列表 */
export async function queryDatasources() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule/clickhouse-tables`);
}

/** 提交规则外发配置表单 */
export async function createSendUpRules(params: ISendUpRulesForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/** 获取rule信息 */
export async function querySendUpRuleById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule/${id}`);
}

/** 编辑外发规则配置表单 */
export async function updateSendUpRules({ id, ...params }: ISendUpRulesForm & { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
