import { API_VERSION_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';

export interface IProductInfo {
  name: string;
  version: string;
  series: string;
  corporation: string;
  description: string;
  file: File;
}

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
