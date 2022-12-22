import { EMetricApiType } from '@/common/api/analysis';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import { countAbnormalEvent } from '@/pages/app/configuration/AbnormalEvent/service';
import { countApplicationFlow, queryMetricAnalysysHistogram } from '@/services/app/analysis';
import { model } from '@/utils/frame/model';
import modelExtend from 'dva-model-extend';
import moment from 'moment';
import type { Effect, Reducer } from 'umi';
import { ERealTimeStatisticsFlag } from 'umi';
import { queryServiceStatList, queryNetworkFlow, queryPayloadHistogram } from '../analysis/service';
import type { IServiceStatData, IFlowAnalysisData } from '../analysis/typings';
import { ANALYSIS_APPLICATION_TYPE_ENUM, ESortDirection, ESourceType } from '../analysis/typings';
import type { INetifAnalysis } from '../Home/typings';
import {
  queryIpConversationTop,
  queryServiceHistogram,
  queryTopUserFlow,
  queryTotalPayload,
} from './service';

export interface ISituationModelState {
  /** 接口统计直方图数据 */
  flowHistogramData: INetifAnalysis[];
  /** 某个业务的统计详情 */
  serviceStatData: IServiceStatData;
  /** 业务汇总数据 */
  serviceFlowTableData: IFlowAnalysisData[];
  startTime: string;
  endTime: string;
}

export interface ISituationModelType {
  namespace: string;
  state: ISituationModelState;
  effects: {
    /** 获取网络流量时间直方图数据 */
    queryNetworkPayloadHistogram: Effect;
    /** 获取某个网络下IP总流量Top排行 */
    queryIpTop: Effect;
    /** 获取某个网络下通讯地址对总流量Top排行 */
    queryIpConversationTop: Effect;
    /** 查询应用详情 */
    queryApplicationDetailHistogram: Effect;
    /** 获取某个业务的汇总统计数据 */
    queryServiceStat: Effect;

    /** 业务态势 */
    /** 获取某个网络下某个应用的统计图数据 */
    queryServiceHistogram: Effect;
    /** 总流量 */
    queryTotalPayload: Effect;
    /** 获取应用汇总数据 */
    queryApplicationFlowTableData: Effect;
    /** 应用会话相关数据 */
    queryApplicationSession: Effect;

    /* 统计聚合异常事件 */
    countAbnormalEvent: Effect;
    /* 存储最新时间 */
    setSituationRefreshTimeInfo: Effect;
  };
  reducers: {
    saveSituationRefreshTimeInfo: Reducer<any>;
  };
}

export default modelExtend(model, {
  namespace: 'situationModel',
  state: {
    flowHistogramData: [],
    serviceStatData: <IServiceStatData>{},
    serviceFlowTableData: [],
    startTime: moment(moment().add(-1, 'h')).format(),
    endTime: moment().format(),
  },
  effects: {
    *queryNetworkPayloadHistogram({ payload }, { call, put }) {
      const { success, result } = yield call(queryPayloadHistogram, {
        ...payload,
        sourceType: ESourceType.NETWORK,
        serviceId: '',
        packetFileId: '',
        realTime: ERealTimeStatisticsFlag.CLOSED,
      });

      yield put({
        type: 'updateState',
        payload: {
          flowHistogramData: success ? result : [],
        },
      });
    },
    *countAbnormalEvent({ payload }, { call }) {
      const { success, result } = yield call(countAbnormalEvent, payload);
      return success ? result : [];
    },
    *queryIpTop({ payload }, { call }) {
      const { success, result } = yield call(queryTopUserFlow, {
        ...payload,
        sortProperty: 'total_bytes',
        sortDirection: ESortDirection.DESC,
      });
      return success ? result : [];
    },
    *queryIpConversationTop({ payload }, { call }) {
      const { success, result } = yield call(queryIpConversationTop, {
        ...payload,
        sortProperty: 'total_bytes',
        sortDirection: ESortDirection.DESC,
        metricApi: EMetricApiType.ipConversation,
      });
      return success ? result : [];
    },
    *queryApplicationDetailHistogram({ payload }, { call }) {
      const { success, result } = yield call(queryMetricAnalysysHistogram, {
        ...payload,
        sortProperty: 'total_bytes',
        sortDirection: ESortDirection.DESC,
        metricApi: EMetricApiType.application,
        type: ANALYSIS_APPLICATION_TYPE_ENUM.应用,
        isDetail: 1,
      });
      return success ? result : [];
    },

    *queryApplicationFlowTableData({ payload }, { call }) {
      const { success, result } = yield call(queryNetworkFlow, payload);
      return success ? result : [];
    },
    *queryServiceHistogram({ payload }, { call }) {
      const { success, result } = yield call(queryServiceHistogram, payload);
      return success ? result : [];
    },
    *queryTotalPayload({ payload }, { call }) {
      const { success, result } = yield call(queryTotalPayload, payload);
      return success ? result : [];
    },
    *queryServiceStat({ payload = {} }, { call, put }) {
      const { success, result }: IAjaxResponseFactory<IPageFactory<IServiceStatData>> = yield call(
        queryServiceStatList,
        { ...payload, page: 0, pageSize: 1 },
      );
      let serviceStatData = {};
      // 业务统计分页，所以需要调整下返回值
      if (success && Array.isArray(result?.content) && result?.content?.length > 0) {
        serviceStatData = result.content[0] as IServiceStatData;
      }
      yield put({
        type: 'updateState',
        payload: {
          serviceStatData,
        },
      });
    },
    *queryApplicationSession({ payload }, { call }) {
      const { success, result } = yield call(countApplicationFlow, payload);
      return success ? result : [];
    },
    *setSituationRefreshTimeInfo({ payload }, { put }) {
      yield put({ type: 'saveSituationRefreshTimeInfo', payload });
    },
  },
  reducers: {
    saveSituationRefreshTimeInfo(state, { payload }) {
      return { ...state, ...payload };
    },
  },
} as ISituationModelType);
