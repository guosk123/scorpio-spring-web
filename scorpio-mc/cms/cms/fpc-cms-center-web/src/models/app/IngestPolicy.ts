import type { IIngestPolicy } from '@/pages/app/Configuration/IngestPolicy/typings';
import {
  createIngestPolicy,
  deleteIngestPolicy,
  queryAllIngestPolicies,
  queryIngestPolicies,
  queryIngestPolicyDetail,
  updateIngestPolicy,
} from '@/pages/app/Configuration/IngestPolicy/service';
import { doPageRequest, pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';

export interface IngestPolicyModelState {
  ingestPolicyData: IIngestPolicy[];
  allIngestPolicy: IIngestPolicy[];
  ingestPolicyDeatil: IIngestPolicy;
}

export interface IngestPolicyModelType {
  namespace: string;
  state: IngestPolicyModelState;
  effects: {
    queryIngestPolicies: Effect;
    queryAllIngestPolicies: Effect;
    queryIngestPolicyDetail: Effect;
    createIngestPolicy: Effect;
    updateIngestPolicy: Effect;
    deleteIngestPolicy: Effect;
  };
}

export default modelExtend(pageModel, {
  namespace: 'ingestPolicyModel',

  state: {
    ingestPolicyData: [] as IIngestPolicy[],
    allIngestPolicy: [] as IIngestPolicy[],
    ingestPolicyDeatil: {} as IIngestPolicy,
  },

  effects: {
    *queryIngestPolicies({ payload = {} }, { call, put }) {
      yield doPageRequest({
        api: queryIngestPolicies,
        payload,
        call,
        put,
        stateKey: 'ingestPolicyData',
      });
    },
    *queryAllIngestPolicies(_, { call, put }) {
      const { success, result } = yield call(queryAllIngestPolicies);
      yield put({
        type: 'updateState',
        payload: {
          allIngestPolicy: success ? result : [],
        },
      });
    },
    *queryIngestPolicyDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryIngestPolicyDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          ingestPolicyDeatil: success ? result : {},
        },
      });
    },
    *createIngestPolicy({ payload }, { call }) {
      const response = yield call(createIngestPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateIngestPolicy({ payload }, { call }) {
      const response = yield call(updateIngestPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteIngestPolicy({ payload }, { call }) {
      const response = yield call(deleteIngestPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
} as IngestPolicyModelType);
