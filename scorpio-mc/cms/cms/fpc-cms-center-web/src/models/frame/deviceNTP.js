import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import { queryDeviceNtps, updateDeviceNtps, queryDeviceNtpState } from '@/services/frame/deviceNTP';

export default modelExtend(model, {
  namespace: 'deviceNTPModel',
  state: {
    ntpInfo: {},
  },

  effects: {
    *queryDeviceNtps(_, { call, put }) {
      const response = yield call(queryDeviceNtps);
      const { success, result } = response;
      yield put({
        type: 'updateState',
        payload: {
          ntpInfo: success ? result : {},
        },
      });
    },
    *updateDeviceNtps({ payload }, { call }) {
      const response = yield call(updateDeviceNtps, payload);
      const { success } = response;
      if (!success) {
        message.error('编辑失败');
      }
      return response;
    },
    *queryDeviceNtpState({ payload }, { call }) {
      const response = yield call(queryDeviceNtpState, payload);
      return response;
    },
  },

  reducers: {},
});
