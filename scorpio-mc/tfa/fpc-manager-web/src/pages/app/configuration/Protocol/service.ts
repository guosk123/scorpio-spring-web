import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryAllProtocolLabels() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/protocols/labels`);
}
