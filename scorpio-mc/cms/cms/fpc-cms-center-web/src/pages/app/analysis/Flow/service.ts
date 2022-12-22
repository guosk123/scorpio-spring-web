import ajax from '@/utils/frame/ajax';
import type { IFlowQueryParams } from '../typings';
import config from '@/common/applicationConfig';
import { stringify } from 'qs';

const { API_VERSION_PRODUCT_V1 } = config;

// 查询网络流量图表数据
export async function queryNetworkFlowHistogram(params: IFlowQueryParams) {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}/as-histogram?${
      restParams && stringify(restParams)
    }`,
  );
}

// 查询网络流量表格数据
export async function queryNetworkFlow(params: IFlowQueryParams) {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}?${restParams && stringify(restParams)}`,
  );
}

