import type { IProductInfo } from '@/models/frame/global';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
const { API_VERSION_V1 } = config;

export const updateProductInfo = (params: IProductInfo) => {
  return ajax(`${API_VERSION_V1}/system/custom-product-infos`, {
    data: JSON.stringify(params),
    type: 'POST',
    processData: false,
    contentType: 'application/json',
  });
};
export const queryProductInfo = () => {
  return ajax(`${API_VERSION_V1}/system/custom-product-infos`);
};
