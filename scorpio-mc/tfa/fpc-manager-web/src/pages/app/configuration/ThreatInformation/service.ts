import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

export async function ThreatbookbasicTagSelection() {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/ti-threatbook/basic-tags`);
}
