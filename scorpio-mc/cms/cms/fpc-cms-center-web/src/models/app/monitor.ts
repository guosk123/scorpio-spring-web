import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import { queryMetrics, queryCpuAndMemoryUsages } from '@/services/app/monitor';
import type { Effect } from 'umi';

export type IMonitorMetricName =
  /** cpu使用率 */
  | 'cpu_used_pct'
  /**  内存使用率 */
  | 'memory_used_pct'
  /** 系统盘使用率 */
  | 'fs_system_used_pct'
  /** 索引盘使用率 */
  | 'fs_index_used_pct'
  /** 数据盘使用率 */
  | 'fs_data_used_pct'
  /** 数据盘使用大小（Bytes） */
  | 'fs_data_used_byte'
  /** 数据盘总大小（Bytes） */
  | 'fs_data_total_byte'
  /** 缓存使用率 */
  | 'fs_cache_used_pct'
  /** 缓存使用的大小（Bytes） */
  | 'fs_cache_used_byte'
  /** 缓存总大小（Bytes） */
  | 'fs_cache_total_byte'
  /** 数据存储空间总大小（Bytes） */
  | 'fs_store_total_byte'
  /** 最早报文时间 */
  | 'data_oldest_time'
  /** 近24小时存储报文的大小（Bytes） */
  | 'data_last24_total_byte'
  /** 预计可存储的总天数 */
  | 'data_predict_total_day'
  /** 查询缓存任务的平均大小（Bytes） */
  | 'cache_file_avg_byte';

export interface IMonitorMetric {
  /** 统计类型 */
  metricName: IMonitorMetricName;
  /** 统计上报时间 */
  metricTime: string;
  /** 统计值 */
  metricValue: string;
}

export type IMonitorMetricMap = Record<IMonitorMetricName, IMonitorMetric>;

export interface MoitorModelState {
  /** 系统监控指标 */
  metrics: IMonitorMetric[];
  /** 系统监控指标Map */
  metricsMap: IMonitorMetricMap;
  /** CPU使用率 */
  cupUsages: any;
  /** 内存使用率 */
  memoryUsages: any;
}

interface MoitorModelType {
  namespace: string;
  state: MoitorModelState;
  effects: {
    queryMetrics: Effect;
    queryCpuAndMemoryUsages: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'moitorModel',
  state: {
    metrics: [],
    metricsMap: <IMonitorMetricMap>{},
    cupUsages: {},
    memoryUsages: {},
  },

  effects: {
    *queryMetrics(_, { call, put }) {
      const { success, result } = yield call(queryMetrics);
      const metrics: IMonitorMetric[] = success && Array.isArray(result) ? result : [];
      const metricsMap = <IMonitorMetricMap>{};

      metrics.forEach((el) => {
        metricsMap[el.metricName] = el;
      });

      yield put({
        type: 'updateState',
        payload: {
          metrics,
          metricsMap,
        },
      });
    },
    *queryCpuAndMemoryUsages({ payload }, { call, put }) {
      const { success, result } = yield call(queryCpuAndMemoryUsages, payload);

      let cupUsages = {};
      let memoryUsages = {};

      if (success) {
        cupUsages = result.cpu || {};
        memoryUsages = result.memory || {};
      }

      yield put({
        type: 'updateState',
        payload: {
          cupUsages,
          memoryUsages,
        },
      });
    },
  },
} as MoitorModelType);
