import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IMetricSettingQueryParams } from '../typings';

const { API_VERSION_PRODUCT_V1 } = config;

// --------------
/**
 *  通用的统计指标参数设置
 */
export async function queryMetricSetting(params: IMetricSettingQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/metric-settings?${stringify(params)}`);
}

/**
 * 更新通用的统计指标
 */
 export async function updataMetricSetting(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/metric-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}