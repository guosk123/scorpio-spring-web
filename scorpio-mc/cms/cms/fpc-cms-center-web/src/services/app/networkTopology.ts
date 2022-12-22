import application from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
const { API_VERSION_PRODUCT_V1 } = application;
export interface INetworkTopology {
  topology: string;
  metric?: string;
}

export async function updateNetworkTopology(params: INetworkTopology) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/network-topologys`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryNetworkTopology() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/network-topologys`);
}
