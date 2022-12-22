import modelExtend from 'dva-model-extend';
import { doPageRequest, pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import type { Effect } from 'umi';
import {
  createApplicationPolicy,
  deleteApplicationPolicy,
  queryApplicationPolicies,
  queryAllApplicationPolicies,
  queryApplicationPolicyDetail,
  updateApplicationPolicy,
} from './service';
import type { IApplicationPolicy } from './typings';

export interface ApplicationPolicyModelState {
  applicationPolicyList: IApplicationPolicy[];
  allApplicationPolicy: IApplicationPolicy[];
  applicationPolicyDetail: IApplicationPolicy;
}

export interface ApplicationPolicyModel {
  namespace: string;
  state: ApplicationPolicyModelState;
  effects: {
    queryApplicationPolicies: Effect;
    queryAllApplicationPolicies: Effect;
    queryApplicationPolicyDetail: Effect;
    createApplicationPolicy: Effect;
    updateApplicationPolicy: Effect;
    deleteApplicationPolicy: Effect;
  };
}

const Model = modelExtend(pageModel, {
  namespace: 'applicationPolicyModel',

  state: {
    // 应用过滤策略分页数据
    applicationPolicyList: [] as IApplicationPolicy[],
    // 所有的应用过滤策略
    allApplicationPolicy: [] as IApplicationPolicy[],
    // 应用过滤策略详情
    applicationPolicyDetail: {} as IApplicationPolicy,
  },

  effects: {
    *queryApplicationPolicies({ payload = {} }, { call, put }) {
      yield doPageRequest({
        api: queryApplicationPolicies,
        payload,
        call,
        put,
        stateKey: 'applicationPolicyList',
      });
    },
    *queryAllApplicationPolicies(_, { call, put }) {
      const { success, result } = yield call(queryAllApplicationPolicies);
      yield put({
        type: 'updateState',
        payload: {
          allApplicationPolicy: success ? result : [],
        },
      });
    },
    *queryApplicationPolicyDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryApplicationPolicyDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          applicationPolicyDetail: success ? result : {},
        },
      });
    },
    *createApplicationPolicy({ payload }, { call }) {
      const response = yield call(createApplicationPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateApplicationPolicy({ payload }, { call }) {
      const response = yield call(updateApplicationPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteApplicationPolicy({ payload }, { call }) {
      const response = yield call(deleteApplicationPolicy, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },

  reducers: {},
} as ApplicationPolicyModel);

export default Model;
