import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';

export async function queryMetrics() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/monitor-metrics`);
}

export async function queryCpuAndMemoryUsages(params) {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/monitor-metrics/as-histogram?${stringify(params)}`);
}
