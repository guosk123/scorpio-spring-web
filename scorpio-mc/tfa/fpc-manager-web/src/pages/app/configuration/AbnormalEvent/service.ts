import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { EAbnormalEventMetricType, IAbnormalEventRule } from './typings';
import { EAbnormalEventRuleStatus } from './typings';
/**
 * 查询异常事件规则分页列表
 */
export async function queryAbnormalEventRules() {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules`);
}

/**
 * 获取异常事件规则详情
 */
export async function queryAbnormalEventRuleDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/${id}`);
}

/**
 * 新建异常事件规则
 */
export async function createAbnormalEventRule(params: IAbnormalEventRule) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

/**
 * 更新异常事件规则
 */
export async function updateAbnormalEventRule({ id, ...restParams }: IAbnormalEventRule) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}

/**
 * 删除异常事件规则
 */
export async function deleteAbnormalEventRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/** 启用异常事件规则 */
export async function enableAbnormalEventRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/${id}/status`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: EAbnormalEventRuleStatus.Open,
    },
  });
}

/** 禁用异常事件规则 */
export async function disableAbnormalEventRule({ id }: { id: string }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/${id}/status`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      status: EAbnormalEventRuleStatus.Closed,
    },
  });
}

/**
 * 导入异常事件规则
 */
export async function importAbnormalEventRule(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-event-rules/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

/**
 * 导入威胁情报
 */
export async function importThreatIntelligenceRule(formData: any) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/threat-intelligences/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}

export async function queryAbnormalEventMessage(params: {
  startTime: string;
  endTime: string;
  page: number;
  pageSize: number;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-events?${stringify(params)}`); // 服务端数据
}

/**
 * 异常事件聚合
 */
export async function countAbnormalEvent(params: {
  startTime: string;
  endTime: string;
  metricType: EAbnormalEventMetricType;
  count?: number;
}) {
  return ajax(`${API_VERSION_PRODUCT_V1}/analysis/abnormal-events/as-count?${stringify(params)}`);
}
