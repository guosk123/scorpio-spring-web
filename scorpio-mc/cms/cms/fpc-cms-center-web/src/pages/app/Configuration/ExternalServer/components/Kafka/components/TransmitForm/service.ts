import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { EReceiverType } from '../../../../typings';
import type { IkafkaTransmitForm } from './typing';

const { API_VERSION_PRODUCT_V1 } = config;

export async function queryTransmitkafkaById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`);
}

export async function createTransmitkafka(params: IkafkaTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver`, {
    type: 'POST',
    data: {
      ...params,
      receiverType: EReceiverType.KAFKA,
    },
  });
}

export async function updateTransmitkafka({ id, ...rest }: IkafkaTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'PUT',
    data: {
      ...rest,
      receiverType: EReceiverType.KAFKA,
    },
  });
}

export async function updateTransmitMail({ id, ...rest }: IkafkaTransmitForm) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-receiver/${id}`, {
    type: 'PUT',
    data: {
      ...rest,
      receiverType: EReceiverType.KAFKA,
    },
  });
}
