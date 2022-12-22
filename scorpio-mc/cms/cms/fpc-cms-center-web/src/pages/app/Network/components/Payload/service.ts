import type { IMetricSettingQueryParams } from "@/pages/app/analysis/typings";
import ajax from "@/utils/frame/ajax";
import config from '@/common/applicationConfig';
import { stringify } from "qs";


const { API_VERSION_PRODUCT_V1 } = config;

/**
 *  基线定义查询接口
 */
 export async function queryBaselineSetting(params: IMetricSettingQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/baseline-settings?${stringify(params)}`);
}

/**
 * 更新基线定义
 */
 export async function updataBaselineSetting(params: { baselineSettings: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/baseline-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}