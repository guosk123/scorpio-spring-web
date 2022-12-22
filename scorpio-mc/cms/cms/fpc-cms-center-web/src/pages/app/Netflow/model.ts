import { model } from '@/utils/frame/model';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import type { ConnectState } from '@/models/connect';
import { IDashbroad, IFlowRecord } from './typing';
import { queryDashboard, querySessionDetail, querySessionTotalElement, Search } from './service';

export interface INetflowModel extends ConnectState {
  netflowModel: INetflowModelState;
}

export interface INetflowModelState {
  selectedNetifSpeed: number;
  dashboardData: IDashbroad;
  sessionDetails: IFlowRecord[];
}

export interface INetflowModelType {
  namespace: string;
  state: INetflowModelState;
  effects: {
    /** 设置速率 */
    setSelectedNetifSpeed: Effect;
    /** dashboard信息 */
    queryDashboard: Effect;
    /** 获取详单信息 */
    querySessionDetail: Effect;
    /** 获取详单数据总量 */
    querySessionTotalElement: Effect;
    /** 模糊搜索 */
    Search: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'netflowModel',
  state: {
    selectedNetifSpeed: 0,
    dashboardData: {} as IDashbroad,
    sessionDetails: [] as IFlowRecord[],
  },
  effects: {
    *setSelectedNetifSpeed({ payload }, { put }) {
      yield put({
        type: 'updateState',
        payload: {
          selectedNetifSpeed: payload,
        },
      });
    },
    *queryDashboard({ payload }, { put, call }) {
      const { success, result } = yield call(queryDashboard, payload);
      if (!success) {
        return {};
      }
      yield put({
        type: 'updateState',
        payload: {
          dashboardData: result,
        },
      });
      return result;
    },
    *querySessionDetail({ payload }, { put, call }) {
      const { success, result } = yield call(querySessionDetail, payload);
      if (!success) {
        return [];
      }
      yield put({
        type: 'updateState',
        payload: {
          sessionDetails: result.content,
        },
      });
      return result;
    },
    *querySessionTotalElement({ payload }, { call }) {
      const { success, result } = yield call(querySessionTotalElement, payload);
      if (!success) {
        return;
      }
      return result.total;
    },
    *Search({ payload }, { call }) {
      const { success, result } = yield call(Search, payload);
      if (!success) {
        return [];
      }
      return result;
    },
  },
} as INetflowModelType);
