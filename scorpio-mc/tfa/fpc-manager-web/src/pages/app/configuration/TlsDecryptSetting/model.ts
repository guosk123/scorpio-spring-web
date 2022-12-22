import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import {
  queryAllTlsDecryptSettings,
  queryTlsDecryptSettingsDetail,
  createTlsDecryptSetting,
  updateTlsDecryptSetting,
  deleteTlsDecryptSetting,
} from './service';
import type { Effect } from 'umi';

export interface StateType {
  allTlsDecryptSettings: any[];
  tlsSettingDetail: any;
}

export interface ModelType {
  namespace: string;
  state: StateType;
  effects: {
    queryAllTlsDecryptSettings: Effect;
    queryTlsDecryptSettingsDetail: Effect;
    createTlsDecryptSetting: Effect;
    updateTlsDecryptSetting: Effect;
    deleteTlsDecryptSetting: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'tlsDecryptSettingModel',
  state: {
    allTlsDecryptSettings: [],
    tlsSettingDetail: {},
  },
  reducers: {},
  effects: {
    *queryAllTlsDecryptSettings({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllTlsDecryptSettings, payload);
      const allTlsDecryptSettings = success && Array.isArray(result) ? result : [];
      yield put({
        type: 'updateState',
        payload: {
          allTlsDecryptSettings,
        },
      });

      return allTlsDecryptSettings;
    },

    *queryTlsDecryptSettingsDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryTlsDecryptSettingsDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          tlsSettingDetail: success ? result : {},
        },
      });
    },

    *createTlsDecryptSetting({ payload }, { call }) {
      const { success } = yield call(createTlsDecryptSetting, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateTlsDecryptSetting({ payload }, { call }) {
      const { success } = yield call(updateTlsDecryptSetting, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteTlsDecryptSetting({ payload }, { call }) {
      const { success } = yield call(deleteTlsDecryptSetting, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
} as ModelType);
