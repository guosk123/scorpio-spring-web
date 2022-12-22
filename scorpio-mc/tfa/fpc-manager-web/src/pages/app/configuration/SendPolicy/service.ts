import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

/** 用来获取外发策略列表 */
export async function querySendPolicy() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy`);
}

/** 用来获取启用的外发策略列表 */
export async function querySendPolicyStateOn() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/state-on`);
}

/** 删除外发策略 */
export async function deleteSendPolicy(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function changeSendPolicyState(id: string, state: boolean) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-policy/${id}/state`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      state: state ? '1' : '0',
    },
  });
}
