import application from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const { API_VERSION_PRODUCT_V1 } = application;
export interface IServiceLinkApiParams {
  serviceId: string;
  link?: string;
  metric?: string;
}

export async function updateServiceLink(params: IServiceLinkApiParams) {
  const { serviceId, ...restParams } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/${serviceId}/link`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

export async function queryServiceLink(params: IServiceLinkApiParams) {
  const { serviceId } = params;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/services/${serviceId}/link`);
}
