/**
 * ===========
 *  接口配置
 * ===========
 */
import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

const { API_VERSION_PRODUCT_V1 } = config;

export async function queryDeviceNetifs() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs`);
}

/**
 * 管理口接口和业务口接口合并
 * 利用 type 来做区分
 * type: '' - 业务口
 * type: 'management-port' - 管理口
 */
export async function queryDeviceNetifUsages(params) {
  const { name, type = '', ...rest } = params;
  let moduleName = 'appliance/app-netifs';
  if (type === 'management-port') {
    moduleName = 'system/device-netifs';
  }
  return ajax(`${API_VERSION_PRODUCT_V1}/${moduleName}/${name}/as-histogram?${stringify(rest)}`);
}

export async function queryReceiveDeviceNetifUsages(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs/as-histogram?${stringify(params)}`);
}

export async function updateDeviceNetifs(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/device-netifs`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryMetricAnalysysHistogram(params) {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}/as-histogram${
      restParams && `?${stringify(restParams)}`
    }`,
  );
}
