import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryCmsSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/cms-settings`);
}

export async function updateCmsSettings(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/cms-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
