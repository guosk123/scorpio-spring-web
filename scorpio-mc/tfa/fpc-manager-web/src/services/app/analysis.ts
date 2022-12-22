import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IAnalysisParams } from '../../pages/app/Home/typings';

export async function queryMetricAnalysys(params: IAnalysisParams): Promise<any> {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}${restParams && `?${stringify(restParams)}`}`,
  );
}

export async function queryMetricAnalysysHistogram(params: IAnalysisParams): Promise<IAjaxResponseFactory<any>> {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}/as-histogram${restParams && `?${stringify(restParams)}`
    }`,
  );
}

interface ICountRankParams {
  startTime: string;
  endTime: string;
  interval: number;
  count: number;
  sortProperty: string;
  sortDirection: 'desc' | ' asc';
}

/**
 * 应用流量统计Top
 */
export async function countApplicationFlow(params: ICountRankParams): Promise<any> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/applications/as-count${params && `?${stringify(params)}`}`,
  );
}

/**
 * 协议流量统计Top
 */
export async function countL7protocolFlow(params: ICountRankParams): Promise<any> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/l7-protocols/as-count${params && `?${stringify(params)}`}`,
  );
}
