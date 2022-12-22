import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import { queryReceiverSettings, updateReceiverSettings, deleteReceiverSettings } from './service';
import type { Effect } from 'umi';

type IReceiverSettings = Record<string, any>;

export interface StateType {
  detail: IReceiverSettings;
}

export interface ModelType {
  namespace: string;
  state: StateType;
  effects: {
    query: Effect;
    update: Effect;
    delete: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'receiverSettingsModel',

  state: {
    detail: {},
  },

  effects: {
    *query(_, { call, put }) {
      const { result, success } = yield call(queryReceiverSettings);
      yield put({
        type: 'updateState',
        payload: {
          detail: success ? result : {},
        },
      });
    },
    *update({ payload }, { call, put }) {
      const { success } = yield call(updateReceiverSettings, payload);
      if (!success) {
        message.error('保存失败');
      } else {
        message.success('保存成功');
        yield put({
          type: 'query',
        });
      }
      return success;
    },
    *delete({ payload }, { call }) {
      const { success } = yield call(deleteReceiverSettings, payload);
      if (!success) {
        message.error('重置失败');
      } else {
        message.success('重置成功');
      }
      return success;
    },
  },
} as ModelType);
