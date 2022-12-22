import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import { querySecuritySettings, updateSecuritySettings } from '@/services/frame/security';

export default modelExtend(model, {
  namespace: 'securityModel',
  state: {
    settings: {},
  },
  reducers: {},
  effects: {
    *querySecuritySettings(_, { call, put }) {
      const { success, result } = yield call(querySecuritySettings);
      yield put({
        type: 'updateState',
        payload: {
          settings: success ? result : {},
        },
      });
    },
    *updateSecuritySettings({ payload }, { call, put }) {
      const { success } = yield call(updateSecuritySettings, payload);
      if (!success) {
        message.error('设置失败');
      } else {
        yield put({
          type: 'querySecuritySettings',
        });
        message.success('设置成功');
      }
      return success;
    },
  },
});
