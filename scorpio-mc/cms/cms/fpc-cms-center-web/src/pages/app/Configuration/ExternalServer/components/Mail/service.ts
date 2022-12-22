import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import qs from 'qs';

const { API_VERSION_PRODUCT_V1 } = config

/** 用来获取邮件外发配置列表 */
export async function queryTransmitMail() {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/external-receiver/type?${qs.stringify({
      receiverType: '0',
    })}`,
  );
}

/** 删除邮件外发 */
export async function deleteTransmitMail(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
