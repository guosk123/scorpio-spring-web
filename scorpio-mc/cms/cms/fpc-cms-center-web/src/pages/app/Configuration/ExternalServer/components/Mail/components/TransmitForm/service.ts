import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { EReceiverType } from '../../../../typings';
import type { IMailTransmitForm } from './typing';

const { API_VERSION_PRODUCT_V1 } = config;

/** 用来获取单条邮件外发配置 */
export async function queryTransmitMailById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`);
}

/** 提交邮件外发配置表单 */
export async function createTransmitMail(params: IMailTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver`, {
    type: 'POST',
    data: {
      ...params,
      receiverType: EReceiverType.MAIL,
    },
  });
}

export async function updateTransmitMail({ id, ...rest }: IMailTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'PUT',
    data: {
      ...rest,
      receiverType: EReceiverType.MAIL,
    },
  });
}
