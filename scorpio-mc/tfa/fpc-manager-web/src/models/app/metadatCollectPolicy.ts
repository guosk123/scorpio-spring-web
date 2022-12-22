import modelExtend from 'dva-model-extend';
import { message } from 'antd';
import { model } from '@/utils/frame/model';
import {
  queryCollectPolicys,
  queryCollectPolicyDetail,
  updateCollectPolicy,
  deleteCollectPolicy,
  changeState,
  createCollectPolicy,
} from '@/pages/app/configuration/MetadataCollectPolicy/service';
import type { Effect } from 'umi';
import type { IMetadataCollectPolicy } from '@/pages/app/configuration/MetadataCollectPolicy/typings';

export interface IMetadatCollectPolicyModelState {
  list: IMetadataCollectPolicy[];
  detail: IMetadataCollectPolicy;
}

export interface IMetadatCollectPolicyModelType {
  namespace: string;
  state: IMetadatCollectPolicyModelState;
  effects: {
    query: Effect;
    queryDetail: Effect;
    create: Effect;
    update: Effect;
    delete: Effect;
    changeState: Effect;
  };
}

export default modelExtend(model, {
  namespace: 'metadatCollectPolicyModel',

  state: {
    list: [],
    detail: {} as IMetadataCollectPolicy,
  },

  effects: {
    *query(_, { call, put }) {
      const { result, success } = yield call(queryCollectPolicys);
      yield put({
        type: 'updateState',
        payload: {
          list: success ? result : [],
        },
      });
    },
    *queryDetail({ payload }, { call, put }) {
      const { result, success } = yield call(queryCollectPolicyDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          detail: success ? result : {},
        },
      });
    },
    *create({ payload }, { call }) {
      const { success } = yield call(createCollectPolicy, payload);
      if (!success) {
        message.error('保存失败');
      } else {
        message.success('保存成功');
      }
      return success;
    },
    *update({ payload }, { call }) {
      const { success } = yield call(updateCollectPolicy, payload);
      if (!success) {
        message.error('保存失败');
      } else {
        message.success('保存成功');
      }
      return success;
    },
    *delete({ payload }, { call, put }) {
      const { success } = yield call(deleteCollectPolicy, payload);
      if (success) {
        message.success('删除成功');
        yield put({
          type: 'query',
        });
      } else {
        message.error('删除失败');
      }
      return success;
    },
    *changeState({ payload }, { call, put }) {
      const { success } = yield call(changeState, payload);
      if (success) {
        message.success('操作成功');
        yield put({
          type: 'query',
        });
      } else {
        message.error('操作失败');
      }
      return success;
    },
  },
} as IMetadatCollectPolicyModelType);
