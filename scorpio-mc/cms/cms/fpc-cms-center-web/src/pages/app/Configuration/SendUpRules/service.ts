import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const { API_VERSION_PRODUCT_V1 } = config

/** 用来获取外发规则列表 */
export async function queryTransmitRules() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule`);
}

/** 删除外发规则 */
export async function deleteSendUpRules(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/send-rule/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
