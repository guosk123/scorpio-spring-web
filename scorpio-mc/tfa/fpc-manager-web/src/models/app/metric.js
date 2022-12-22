import {
  countFlowProtocol,
  queryFlowProtocolHistogram,
  queryMonitorMetrics,
  queryNetifMetricsHistogram,
  queryProbeMetricsHistogram,
} from '@/services/app/metric';
import { model } from '@/utils/frame/model';
import modelExtend from 'dva-model-extend';

export default modelExtend(model, {
  namespace: 'metricModel',

  state: {
    netifMetricsHistogram: {},
    flowProtocolHistogram: [],

    flowProtocolCount: 0,
    flowProtocol: {}, // 各个协议的占比

    probeMetricsHistogram: [],
    monitorMetricsHistogram: [],
  },

  effects: {
    // 流量接口统计
    *queryNetifMetricsHistogram({ payload }, { call, put }) {
      const { result, success } = yield call(queryNetifMetricsHistogram, payload);

      const histogram = success ? result : {};
      yield put({
        type: 'updateState',
        payload: {
          netifMetricsHistogram: histogram,
        },
      });
      return histogram;
    },
    // 流量协议占比统计
    *queryFlowProtocolHistogram({ payload }, { call, put }) {
      const { success, result } = yield call(queryFlowProtocolHistogram, payload);
      const histogram = success ? result : [];
      yield put({
        type: 'updateState',
        payload: {
          flowProtocolHistogram: histogram,
        },
      });
      return histogram;
    },
    // 统计各种协议统计数量
    *countFlowProtocol({ payload }, { call, put }) {
      const { success, result } = yield call(countFlowProtocol, payload);
      const collectProtocol = success ? result : {};

      const totalCount = collectProtocol.TOTAL || 0;
      // 删除总数
      delete collectProtocol.TOTAL;

      yield put({
        type: 'updateState',
        payload: {
          flowProtocolCount: totalCount,
          flowProtocol: collectProtocol,
        },
      });
    },
    // 会话统计
    *queryProbeMetricsHistogram({ payload }, { call, put }) {
      const { success, result } = yield call(queryProbeMetricsHistogram, payload);
      const histogram = success ? result : [];
      yield put({
        type: 'updateState',
        payload: {
          probeMetricsHistogram: histogram,
        },
      });
      return histogram;
    },
    // 系统状态统计
    *queryMonitorMetrics({ payload }, { call, put }) {
      const { success, result } = yield call(queryMonitorMetrics, payload);
      const histogram = success ? result : [];
      yield put({
        type: 'updateState',
        payload: {
          monitorMetricsHistogram: histogram,
        },
      });
      return histogram;
    },
  },
});
