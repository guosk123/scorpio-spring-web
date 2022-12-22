import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { pageModel, doPageRequest } from '@/utils/frame/model';
import {
  queryAlarms,
  countGroupbyLevel,
  queryAlarmDetail,
  solveAlerm,
} from '@/services/frame/alarm';

export default modelExtend(pageModel, {
  namespace: 'alarmModel',

  state: {
    alarms: [],
    countAlarmByLevel: [],
    alarmDetail: {},
    currentItem: {},
    modalVisible: false,
  },

  effects: {
    *queryAlarms({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: queryAlarms, payload, call, put, stateKey: 'alarms' });
    },
    *countGroupbyLevel(_, { call, put }) {
      const { success, result } = yield call(countGroupbyLevel);
      yield put({
        type: 'updateState',
        payload: {
          countAlarmByLevel: success ? result : [],
        },
      });
    },
    *queryAlarmDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryAlarmDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          alarmDetail: success ? result : {},
        },
      });
    },
    *solveAlerm({ payload }, { call }) {
      const response = yield call(solveAlerm, payload);
      const { success } = response;
      if (success) {
        message.success('解决成功');
      } else {
        message.error('解决失败');
      }
      return success;
    },
  },

  reducers: {
    showModal(state, { payload }) {
      return { ...state, ...payload, modalVisible: true };
    },

    hideModal(state) {
      return { ...state, modalVisible: false, currentItem: {} };
    },
  },
});
