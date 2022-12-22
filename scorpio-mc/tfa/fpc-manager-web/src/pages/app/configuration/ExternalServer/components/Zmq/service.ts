import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import qs from 'qs';

/** 用来获取邮件外发配置列表 */
export async function queryTransmitZmq() {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/external-receiver/type?${qs.stringify({
      receiverType: '3',
    })}`,
  );
}

/** 删除邮件外发 */
export async function deleteTransmitZmq(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
