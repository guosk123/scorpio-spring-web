import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import { stringify } from 'qs';

export async function queryNetworkTree() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/metric/equipment/network-tree`);
}

export async function queryDimensionsTable(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/metric/equipment/table?${stringify(params)}`);
}

export async function queryDimensionsChart(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/metric/networks/as-histogram?${stringify(params)}`);
}

/**
 * 获取地理位置
 */
export async function queryGeolocations() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/appliance/geolocation/rules`);
}
