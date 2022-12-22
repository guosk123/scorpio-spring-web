import type { IBaselineSettingData, IMetricSettingData, IMetricSettingQueryParams, IPerformanceSettingData } from "@/pages/app/analysis/typings";
import ajax from "@/utils/frame/ajax";
import config from '@/common/applicationConfig';
import { stringify } from "qs";

const { API_VERSION_PRODUCT_V1 } = config;

/**
 * 性能响应时间配置
 */
 export async function queryPerformanceSetting(params: IMetricSettingQueryParams) {
  const resTime: Promise<unknown> = ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/metric-settings?${stringify(params)}`,
  );
  const baselineData: Promise<unknown> = ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/baseline-settings?${stringify(params)}`,
  );
  return Promise.all([resTime, baselineData])
    .then((values) => {
      const reult1 = (values[0] as any).result;
      const result2 = (values[1] as any).result;
      const res: IPerformanceSettingData = {
        responseTime: reult1 as IMetricSettingData[],
        baseline: result2.find((item: IBaselineSettingData) => {
          return item.category === 'responseLatency';
        }),
      };
      return { result: res, success: true };
    })
    .catch(() => {
      return {
        success: false,
        result: {},
      };
    });
}

/**
 * 更新性能响应时间配置
 */
 export async function updatePerformanceSetting(params: any) {
  const resTime: Promise<unknown> = ajax(`${API_VERSION_PRODUCT_V1}/appliance/metric-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      metricSettings: JSON.stringify(params.time),
    },
  });
  const baselineData: Promise<unknown> = ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/baseline-settings`,
    {
      type: 'POST',
      data: {
        _method: 'PUT',
        baselineSettings: JSON.stringify(params.baseline),
      },
    },
  );
  return Promise.all([resTime, baselineData])
    .then(() => {
      return { success: true };
    })
    .catch(() => {
      return {
        success: false,
      };
    });
}