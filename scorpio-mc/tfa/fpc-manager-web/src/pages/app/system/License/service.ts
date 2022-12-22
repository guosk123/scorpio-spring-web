import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

// eslint-disable-next-line import/prefer-default-export
export async function queryLicense() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/licenses`);
}
