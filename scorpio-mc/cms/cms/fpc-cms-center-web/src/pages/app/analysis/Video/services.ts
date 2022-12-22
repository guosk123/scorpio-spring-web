import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ESortDirection } from '../typings';

const { API_VERSION_PRODUCT_V1 }  = config

export interface ITableQueryParams {
  sortProperty: string;
  sortDirection: ESortDirection;
  startTime: string;
  endTime: string;
  dsl: string;
  page: number;
  pageSize: number;
  columns?: string;
  networkId?: string;
}

export interface INetworkSegmentParams {
  dsl: string;
  startTime: string;
  endTime: string;
}

export interface INetworkSegmentHistParams {
  dsl: string;
  interval: number;
  networkId: string;
}

/** 获取RTP流 */
export async function queryRTPFlows(params: ITableQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/protocol-rtp-logs/?${stringify(params)}`);
}

/** 获取RTP流总数  */
export async function queryRTPFlowCount(params: ITableQueryParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/protocol-rtp-logs/as-statistics?${stringify(params)}`,
  );
}

/** 获取设备列表流 */
export async function queryIpDevices(params: ITableQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/ip-device?${stringify(params)}`);
}

/**
 * 分段分析页面带颜色的图的接口：
 * 这个接口与性能分析传参一样，dsl里面不需要传时间，只需要传五元组进行过滤就行。
 */
export async function queryNetworkSegmentation(params: INetworkSegmentParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metadata/network-segmentation?${stringify(params)}`);
}

/**
 * 分段分析时间图接口：
 * 这个接口则和IP设备列表类似，通过dsl传report_time过滤时间，需要单独传networkId
 */
export async function queryNetworkSegmentationHistogram(params: INetworkSegmentHistParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/network-segmentation/as-histogram?${stringify(params)}`,
  );
}
