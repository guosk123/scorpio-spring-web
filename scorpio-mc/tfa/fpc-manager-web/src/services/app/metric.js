import { stringify } from 'qs'
import ajax from '@/utils/frame/ajax'
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig'

/**
 * 系统监控统计
 */
export async function queryMonitorMetrics(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/system/monitor-metrics/as-histogram${params &&
      `?${stringify(params)}`}`
  )
}

/**
 * 流量协议占比
 */
export async function queryFlowProtocolHistogram(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/flows/as-histogram${params &&
      `?${stringify(params)}`}`
  )
}

/**
 * 统计各种协议统计数量
 */
export async function countFlowProtocol(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/flow-logs/as-protocol-count${params &&
      `?${stringify(params)}`}`
  )
}

/**
 * 全局统计：会话统计
 */
export async function queryProbeMetricsHistogram(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/probe-metrics/as-histogram${params &&
      `?${stringify(params)}`}`
  )
}

/**
 * 接口流量统计
 */
export async function queryNetifMetricsHistogram(params) {
  return ajax(
    `${API_VERSION_PRODUCT_V1}/metadata/netif-metrics/as-histogram${params &&
      `?${stringify(params)}`}`
  )
}

/**
 * 服务运行时间
 */
export async function queryRuntimeEnvironments() {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/runtime-environments`)
}
