import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { EDrilldown } from './typing';
import { stringify } from 'qs';

/** 查询参数 */
// dashboard查询参数
interface IDashboardDataParams {
  count: number;
  startTime: string;
  endTime: string;
  deviceName: string;
  netifNo?: string;
  netifSpeed?: number;
  interval: number;
}

// 设备列表查询参数
interface ISourcesParams {
  pageNumber: number;
  pageSize: number;
  startTime: string;
  endTime: string;
}

// 编辑数据源表单参数
interface IEditSourceParams {
  id: string;
  deviceType: string;
  alias: string;
  description: string;
  netifSpeed: number;
}

// 会话详单查询参数
interface ISessionDetailParams {
  deviceName: string;
  startTime: string;
  endTime: string;
  sortProperty: string;
  sortDirection: string;
  page: number;
  pageSize: number;
}

// flow折线图查询参数
export interface IFlowHistParams {
  deviceName: string;
  netifNo: string;
  startTime: string;
  endTime: string;
  sortProperty: string;
  sortDirection: string;
  drilldown: EDrilldown;
  interval: number;
  dsl?: string;
}

// 会话详单查询总数据量查询参数
interface ISessionTotalElementParams {
  queryId: string;
  startTime: string;
  endTime: string;
  deviceName: string;
  netifNo?: string;
  dsl?: string;
  drilldown: EDrilldown;
}

/** ajax请求函数 */
/** 获取设备列表信息 */
export async function querySources(params: ISourcesParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/netflow-sources?${stringify(params)}`);
}

/** 编辑netflow源 */
export async function editSource(params: IEditSourceParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/netflow-sources`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...{ netflowListJson: JSON.stringify([params]) },
    },
  });
}

export async function queryDashboard(params: IDashboardDataParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/netflows/dashboard?${stringify(params)}`);
}

// 用来处理模糊搜索
export async function Search(params: { keywords: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/netflow-sources/as-list?${stringify(params)}`);
}

/** 查询流量分析Ip折线图 */
export async function queryFlowIpHist(params: IFlowHistParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/netflows/ip/as-histogram?${stringify(params)}`);
}

/** 查询流量分析发送IP折线图 */
export async function queryFlowTransmitIpHist(params: IFlowHistParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/netflows/transmit-ip/as-histogram?${stringify(params)}`,
  );
}

/** 查询流量分析接收IP折线图 */
export async function queryFlowIngestIpHist(params: IFlowHistParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/netflows/ingest-ip/as-histogram?${stringify(params)}`,
  );
}

/** 查询流量分析端口折线图 */
export async function queryFlowPortHist(params: IFlowHistParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/netflows/protocol-port/as-histogram?${stringify(params)}`,
  );
}

/** 查询流量分析会话折线图 */
export async function queryFlowSessionHist(params: IFlowHistParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/netflows/session/as-histogram?${stringify(params)}`,
  );
}

/** 查询会话详单 */
export async function querySessionDetail(params: ISessionDetailParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/netflows/session-detail?${stringify(params)}`);
}

/** 查询会话详单分页数量 */
export async function querySessionTotalElement(params: ISessionTotalElementParams) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/netflows/session-total-element?${stringify(params)}`,
  );
}
