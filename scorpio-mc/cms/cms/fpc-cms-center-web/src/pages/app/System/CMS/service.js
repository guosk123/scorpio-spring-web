import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryCmsSettings() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/cms-settings`);
}

export async function updateCmsSettings(params) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/cms-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
