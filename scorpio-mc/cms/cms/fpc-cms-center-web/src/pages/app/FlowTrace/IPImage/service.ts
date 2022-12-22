import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
// import type {
//   IFlowQueryParams,
// } from "../../typings";
import { stringify } from 'qs';

const { API_VERSION_PRODUCT_V1 } = config;
//查询IP画像数据
export async function queryHistogramData(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/ip-detections/flow-logs?${stringify(params)}`);
}

//查询来访IP，访问IP数量总数
export async function queryHistogramDataTotalNumer(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/ip-detections/flow-logs/as-statistics?${stringify(params)}`,
  );
}

//查询安全告警的数据
export async function queryIpAlarmData(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/suricata/alert-messages/as-graph?${stringify(params)}`);
}

//查询安全告警总数的接口
export async function queryIpAlarmDataTotalNumber(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/suricata/alert-messages/as-statistics?${stringify(params)}`,
  );
}

//查询域名数据
export async function queryIpDominName(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/ip-detections/protocol-dns-logs?${stringify(params)}`,
  );
}

//查询域名总数数据
export async function queryIpDominNameTotalNumber(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/ip-detections/protocol-dns-logs/as-statistics?${stringify(
      params,
    )}`,
  );
}

//查询页面的配置信息
export async function getSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/ip-detections/layouts`);
}

export async function updateSettings(params: {
  layouts: string;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/ip-detections/layouts`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}