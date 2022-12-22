import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryAllProtocolLabels() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/metadata/protocols/labels`);
}
