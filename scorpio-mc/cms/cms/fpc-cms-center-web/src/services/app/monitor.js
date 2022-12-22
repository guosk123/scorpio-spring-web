import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

export async function queryMetrics() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/monitor-metrics`);
}

export async function queryCpuAndMemoryUsages(params) {
  return ajax(
    `${config.API_VERSION_PRODUCT_V1}/system/monitor-metrics/as-histogram?${stringify(params)}`,
  );
}
