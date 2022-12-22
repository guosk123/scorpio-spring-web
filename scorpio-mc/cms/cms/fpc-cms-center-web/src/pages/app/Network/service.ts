import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import { stringify } from 'qs';
import { queryNetworkGroups, queryNetworkSensors } from '../Configuration/Network/service';
import { queryAllLogicalSubnets } from '../Configuration/LogicalSubnet/service';
import type { INetworkGroup, INetworkSensor } from '../Configuration/Network/typings';
import { ESensorStatus } from '../Configuration/Network/typings';
import type { INetworkTreeItem } from './typing';
import { ENetowrkType } from './typing';
import _ from 'lodash';
import type { IMetricQueryParams } from '../analysis/typings';
import type { IAjaxResponseFactory } from '@/common/typings';

const { API_VERSION_PRODUCT_V1 } = config;

/**
 * 所有网络的统计分析列表
 */
export async function queryAllNetworkStat(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/networks?${stringify(params)}`);
}

/**
 * @description 3层top统计
 */
export async function queryL3Top(params: IMetricQueryParams): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/l3-devices/as-count?${stringify(params)}`);
}

/**
 * @description 3层会话对top
 */
export async function queryIpConversationTop(
  params: IMetricQueryParams,
): Promise<IAjaxResponseFactory<any>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-count?${stringify(params)}`);
}

/**
 * 网络概览
 */
export async function queryNetworkDashboard(params: IMetricQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/networks/dashboard?${stringify(params)}`);
}

/**
 * 业务概览
 */
export async function queryServiceDashboard(params: IMetricQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/dashboard?${stringify(params)}`);
}

/**
 * 一次获取网络组，探针网络，逻辑子网的信息
 */
export const queryNetWorkTree = () => {
  return new Promise<INetworkTreeItem[]>((resolve, reject) => {
    Promise.all([queryNetworkGroups(), queryNetworkSensors(), queryAllLogicalSubnets()]).then(
      (res) => {
        const [groupsRes, sensorsRes, logicalRes] = res;
        const networkArr: INetworkTreeItem[] = [];
        if (groupsRes.success && sensorsRes.success && logicalRes.success) {
          // 添加网络组
          if (groupsRes.result.length > 0) {
            groupsRes.result.forEach((item: INetworkGroup) => {
              networkArr.push({
                title: item.name,
                value: item.id,
                key: item.id,
                networkInSensorIds: item.networkInSensorIds,
                recordId: item.id,
                type: ENetowrkType.NETWORK_GROUP,
                status: item.status,
                statusDetail: item.statusDetail,
              });
            });
          }
          sensorsRes.result.forEach((item: INetworkSensor) => {
            networkArr.push({
              title: `${item.name}${item.status === ESensorStatus.OFFLINE ? '(离线)' : ''}`,
              value: item.networkInSensorId,
              key: item.networkInSensorId,
              recordId: item.networkInSensorId,
              type: ENetowrkType.NETWORK,
              status: item.status,
              statusDetail: item.statusDetail,
            });
          });
          // 添加逻辑子网
          logicalRes.result.forEach((item: any) => {
            networkArr.push({
              title: `${item.name}${item.status === ESensorStatus.OFFLINE ? '(离线)' : ''}`,
              value: item.id,
              key: item.id,
              recordId: item.id,
              type: ENetowrkType.NETWORK,
              status: item.status,
              statusDetail: item.statusDetail,
              logicNetwork: true,
            });
          });
          resolve(networkArr);
        } else {
          reject('获取网络信息失败');
        }
      },
    );
  });
};

/**
 * 负载量统计
 */
export async function queryPayloadHistogram(params: any) {
  const { serviceId } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${serviceId ? 'services' : 'networks'}/payload?${stringify(
      params,
    )}`,
  );
}

/**
 * TCP指标统计
 */
export async function queryTcpHistogram(params: IMetricQueryParams) {
  const { serviceId } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${serviceId ? 'services' : 'networks'}/tcp?${stringify(
      params,
    )}`,
  );
}

/**
 * 性能统计
 */
export async function queryPerformanceHistogram(params: IMetricQueryParams) {
  const { serviceId } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${
      serviceId ? 'services' : 'networks'
    }/performance?${stringify(params)}`,
  );
}

/**
 * 所有网络的统计分析列表
 */
export async function queryOldestTime(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/central/packet-oldest-time?${stringify(params)}`);
}
