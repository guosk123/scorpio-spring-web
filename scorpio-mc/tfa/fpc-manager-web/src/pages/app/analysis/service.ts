import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type {
  INetworkStatParams,
  IMetricSettingQueryParams,
  IBaselineSettingData,
  IFlowQueryParams,
  IMetricQueryParams,
  IMetricSettingData,
  IPerformanceSettingData,
  IServiceStatParams,
  INetworkStatData,
  IServiceStatData,
  IIpConversationGraph,
  IIpConversationGraphParams,
  IAlertMsgCntParams,
  IIpConversationHistoryParams,
} from './typings';
import type { BusinessPanelSettings } from './Service/List/typings';

/**
 * 所有网络的统计分析列表
 */
export async function queryAllNetworkStat(
  params: INetworkStatParams,
): Promise<IAjaxResponseFactory<INetworkStatData[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/networks?${stringify(params)}`);
}

/**
 * 网络概览
 */
export async function queryNetworkDashboard(params: IMetricQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/networks/dashboard?${stringify(params)}`);
}

/**
 * 告警消息
 */
export async function queryAlertMsgCnt(params: IAlertMsgCntParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/alert-messages/as-count?${stringify(params)}`);
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
 * 负载量统计
 */
export async function queryPayloadHistogram(params: IMetricQueryParams) {
  const { serviceId } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${serviceId ? 'services' : 'networks'}/payload?${stringify(
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

// 查询网络流量表格数据
export async function queryNetworkFlow(params: IFlowQueryParams) {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}?${restParams && stringify(restParams)}`,
  );
}

// 查询网络流量图表数据
export async function queryNetworkFlowHistogram(params: IFlowQueryParams) {
  const { metricApi, ...restParams } = params;
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/${metricApi}/as-histogram?${
      restParams && stringify(restParams)
    }`,
  );
}
/** 查询 IP 访问关系图 */
export async function queryIpConversationGraph(
  params: IIpConversationGraphParams,
): Promise<IAjaxResponseFactory<IIpConversationGraph[]>> {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metric/ip-conversations/as-graph?${
      params &&
      stringify({
        /** 默认不懈怠应用协议 */
        aggApplication: '0',
        ...params,
      })
    }`,
  );
}

/** 查询 IP 访问关系图历史列表 */
export async function queryIpConversationGraphHistory() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history`);
}

/** 查询 IP 访问关系图 */
export async function queryIpConversationGraphHistoryById(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${id}`);
}

export async function createGraphHistory(params: IIpConversationHistoryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function alterGraphHistory(params: IIpConversationHistoryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${params.id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function deleteHistoryRelations({
  id,
  ...params
}: {
  id: string;
  name: string;
  data: string;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversation/history/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
      ...params,
    },
  });
}

export async function deleteGraphHistory(id: string) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/ip-conversations/history/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

// ====================================================
/**
 * 业务统计分析分页列表
 */
export async function queryServiceStatList(
  params: IServiceStatParams,
): Promise<IAjaxResponseFactory<IPageFactory<IServiceStatData>>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services?${stringify(params)}`);
}
/**
 * 业务概览
 */
export async function queryServiceDashboard(params: IMetricQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/dashboard?${stringify(params)}`);
}
/**
 * 业务流量统计数据
 */
export async function queryServiceFlowHistogram(params: IMetricQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/payload?${stringify(params)}`);
}
/**
 * 业务配置项查询
 */
export async function queryServiceDashboardSettings() {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/dashboard-settings`);
}

/**
 * 更新业务配置项
 */
export async function updateServiceDashboardSettings(params: BusinessPanelSettings) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/services/dashboard-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
/**
 * 查询告警分布图中的数据
 */
export async function queryAlarmDashboardMegs(params: any) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/appliance/alert-messages/as-histogram?${stringify(params)}`,
  );
}

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

// --------------
/**
 *  通用的统计指标参数设置
 */
export async function queryMetricSetting(params: IMetricSettingQueryParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/metric-settings?${stringify(params)}`);
}

/**
 * 更新通用的统计指标
 */
export async function updataMetricSetting(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/metric-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryHttpAnalysis(params: { dsl: string; interval: number }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/https?${stringify(params)}`);
}

export async function queryEstablishedFail(params: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/metric/l3-devices/established-fail?${stringify(params)}`);
}
