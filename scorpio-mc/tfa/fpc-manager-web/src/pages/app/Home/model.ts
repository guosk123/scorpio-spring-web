/* eslint-disable no-param-reassign */
/* eslint-disable no-return-assign */
import { EMetricApiType } from '@/common/api/analysis';
import type { ANALYSIS_APPLICATION_TYPE_ENUM } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import {
  countApplicationFlow,
  countL7protocolFlow,
  queryMetricAnalysysHistogram,
} from '@/services/app/analysis';
import { model } from '@/utils/frame/model';
import { timeFormatter } from '@/utils/utils';
import modelExtend from 'dva-model-extend';
import moment from 'moment';
import type { Reducer } from 'redux';
import type { Effect } from 'umi';
import type {
  IAnalysisParams,
  ICountApplication,
  ICountL7Procolcol,
  INetworkAnalysis,
} from './typings';
import type { THomeMetricType } from './typings';

export interface HomeModelState {
  analysisResultMap: Record<THomeMetricType, INetworkAnalysis[]>;
  analysisHistogramMap: Record<THomeMetricType, INetworkAnalysis[]>;
  saRankHistogramMap: Record<ANALYSIS_APPLICATION_TYPE_ENUM, INetworkAnalysis[]>;
  /**
   * 应用流量Top
   */
  applicationFlowCount: ICountApplication[];

  /**
   * 协议流量 Top
   */
  l7ProtocolFlowCount: ICountL7Procolcol[];

  /**
   * 获取某一个记录的时间直方图
   */
  detailHistogram: INetworkAnalysis[];
  /** 所有网络的总流量统计 */
  allNetworkFlowHistogram: INetworkAnalysis[];
}

export interface HomeModelType {
  namespace: string;
  state: HomeModelState;
  effects: {
    /** 最近平均总带宽 */
    queryLatelyBandwidth: Effect;
    /**
     * 应用流量Top
     */
    queryApplicationFlowCount: Effect;
    queryMetricAnalysysHistogram: Effect;
    /**
     * 协议流量Top
     */
    queryL7ProtocolFlowCount: Effect;
    /** 获取所有网络的流量统计 */
    queryAllNetworkFlowHistogram: Effect;
  };
  reducers: {
    clearData: Reducer<HomeModelState>;
    clearDetailHistogram: Reducer<HomeModelState>;
  };
}

const Model = modelExtend(model, {
  namespace: 'homeModel',

  state: {
    analysisResultMap: {} as HomeModelState['analysisResultMap'],
    analysisHistogramMap: {} as HomeModelState['analysisHistogramMap'],
    saRankHistogramMap: {} as HomeModelState['saRankHistogramMap'],

    detailHistogram: [],

    applicationFlowCount: [],
    l7ProtocolFlowCount: [],
    allNetworkFlowHistogram: [],

    // 最近30秒的平均带宽
    currentAvgBitsps: 0,
  },

  effects: {
    *queryLatelyBandwidth(_, { call, put }) {
      const time1 = moment().subtract(10, 'minutes').format();
      const time2 = moment().format();
      const { startTime, endTime, interval = 30 } = timeFormatter(time1, time2) || {};
      const { success, result = [] } = yield call(queryMetricAnalysysHistogram, {
        metricApi: EMetricApiType.network,
        startTime,
        endTime,
        interval,
      });
      const data = success ? result : [];
      // 去查询时间的最后一个时间点，作为最近的时间
      const target = data.find(
        (row: any) =>
          new Date(row.timestamp).valueOf() === new Date(endTime).valueOf() - interval * 1000,
      );

      yield put({
        type: 'updateState',
        payload: {
          currentAvgBitsps: ((target?.latelyTotalBytes || 0) * 8) / interval,
        },
      });
    },

    *queryAllNetworkFlowHistogram({ payload }, { call, put }) {
      const { success, result = [] } = yield call(queryMetricAnalysysHistogram, {
        ...payload,
        metricApi: EMetricApiType.network,
      });
      yield put({
        type: 'updateState',
        payload: {
          allNetworkFlowHistogram: success ? result : [],
        },
      });
    },

    *queryMetricAnalysysHistogram({ payload }, { call, put, select }) {
      const { metricApi } = payload as IAnalysisParams;
      const { success, result = [] } = yield call(queryMetricAnalysysHistogram, payload);
      const analysisHistogramMap = yield select(
        (state: ConnectState) => state.homeModel.analysisHistogramMap,
      );
      const data = success ? result : [];

      yield put({
        type: 'updateState',
        payload: {
          analysisHistogramMap: {
            ...analysisHistogramMap,
            [metricApi]: data,
          },
        },
      });
    },

    *queryApplicationFlowCount({ payload }, { call, put }) {
      const { success, result = [] } = yield call(countApplicationFlow, {
        ...payload,
      });

      yield put({
        type: 'updateState',
        payload: {
          applicationFlowCount: success ? result : [],
        },
      });
    },
    *queryL7ProtocolFlowCount({ payload }, { call, put }) {
      const { success, result = [] } = yield call(countL7protocolFlow, {
        ...payload,
      });

      yield put({
        type: 'updateState',
        payload: {
          l7ProtocolFlowCount: success ? result : [],
        },
      });
    },
  },

  reducers: {
    clearData: (state) => {
      return {
        ...state,
        analysisResultMap: {} as HomeModelState['analysisResultMap'],
        analysisHistogramMap: {} as HomeModelState['analysisHistogramMap'],
        saRankHistogramMap: {} as HomeModelState['saRankHistogramMap'],
      };
    },
    clearDetailHistogram: (state) => {
      return {
        ...state,
        detailHistogram: [],
      };
    },
  },
} as HomeModelType);

export default Model;
