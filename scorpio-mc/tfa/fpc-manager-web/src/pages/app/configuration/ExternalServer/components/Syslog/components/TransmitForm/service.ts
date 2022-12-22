import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { EReceiverType } from '../../../../typings';
import type { ISyslogTransmitForm } from './typing';

/** 用来获取单条邮件外发配置 */
export async function queryTransmitSyslogById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`);
}

/** 提交邮件外发配置表单 */
export async function createTransmitSyslog(params: ISyslogTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver`, {
    type: 'POST',
    data: {
      ...params,
      receiverType: EReceiverType.SYSLOG,
    },
  });
}

export async function updateTransmitSyslog({ id, ...rest }: ISyslogTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'PUT',
    data: {
      ...rest,
      receiverType: EReceiverType.SYSLOG,
    },
  });
}
