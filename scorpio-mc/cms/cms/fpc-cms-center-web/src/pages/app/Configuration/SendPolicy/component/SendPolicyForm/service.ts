import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const { API_VERSION_PRODUCT_V1 } = config

export interface ISendPolicyForm {
  name: string;
  externalReceiverId: string;
  sendRuleId: string;
  state: string;
}

/** 用来获取外发规则列表 */
export async function queryExternalReceiver() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver`);
}

/** 提交规则外发配置表单 */
export async function createSendPolicy(params: ISendPolicyForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}
/** 编辑外发规则配置表单 */
export async function updateSendPolicy({ id, ...params }: ISendPolicyForm & { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/** 获取rule信息 */
export async function querySendPolicyById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/${id}`);
}

/** 获取smtp配置信息 */
export async function getSmtpConfiguration() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/smtp-configuration`);
}
