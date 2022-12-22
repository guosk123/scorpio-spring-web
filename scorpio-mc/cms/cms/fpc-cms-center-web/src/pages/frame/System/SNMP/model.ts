import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { model } from '@/utils/frame/model';
import { querySystemSnmpSettings, updateSystemSnmpSettings } from './service';
import type { ISnmpSettings } from './typings';

export interface ISnmpSettingModelState {
  snmpSettings: ISnmpSettings;
}
export interface ISnmpSettingModel {
  namespace: string;
  state: ISnmpSettingModelState;
  effects: {
    querySystemSnmpSettings: Effect;
    updateSystemSnmpSettings: Effect;
  };
}

const snmpSettingModel = modelExtend(model, {
  namespace: 'snmpSettingModel',
  state: {
    snmpSettings: {} as ISnmpSettings,
  },
  reducers: {},
  effects: {
    *querySystemSnmpSettings({ payload }, { call, put }) {
      const { success, result } = yield call(querySystemSnmpSettings, payload);
      yield put({
        type: 'updateState',
        payload: {
          snmpSettings: success ? result : {},
        },
      });
    },
    *updateSystemSnmpSettings({ payload }, { call }) {
      const { success } = yield call(updateSystemSnmpSettings, payload);
      if (success) {
        message.success('保存成功，1s后刷新');
      } else {
        message.error('保存失败');
      }
      return success;
    },
  },
} as ISnmpSettingModel);

export default snmpSettingModel;
