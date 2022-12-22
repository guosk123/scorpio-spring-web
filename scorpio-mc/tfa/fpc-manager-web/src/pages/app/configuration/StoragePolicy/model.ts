import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import { message } from 'antd';
import * as policyService from './service';
import type { Effect } from 'umi';
import type { IStoragePolicy } from './typings';

export interface IStoragePolicyModelState {
  policy: IStoragePolicy;
}

export interface IStoragePolicyModel {
  namespace: string;
  state: IStoragePolicyModelState;
  effects: {
    query: Effect;
    update: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'storagePolicyModel',

  state: {
    policy: {} as IStoragePolicy,
  },

  effects: {
    *query(_, { call, put }) {
      const response = yield call(policyService.query);
      const { success, result } = response;
      if (success) {
        yield put({
          type: 'updateState',
          payload: {
            policy: result,
          },
        });
      }
    },
    *update({ payload }, { call, put }) {
      const response = yield call(policyService.update, payload);
      const { success } = response;
      if (success) {
        message.success('保存成功');
        yield put({
          type: 'query',
        });
      } else {
        message.error('保存失败');
      }
    },
  },
} as IStoragePolicyModel);
