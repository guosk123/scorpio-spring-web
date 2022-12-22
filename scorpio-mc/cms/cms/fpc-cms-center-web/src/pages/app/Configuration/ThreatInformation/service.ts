import appConfig from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

const { API_VERSION_PRODUCT_V1 } = appConfig;

export async function ThreatbookbasicTagSelection() {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/ti-threatbook/basic-tags`);
}
