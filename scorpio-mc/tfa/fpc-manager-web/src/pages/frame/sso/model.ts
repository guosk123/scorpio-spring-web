import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { pageModel, doPageRequest } from '@/utils/frame/model';
import {
  querySsoPlatforms,
  querySsoPlatformDetail,
  createSsoPlatform,
  updateSsoPlatform,
  queryAllSsoPlatforms,
  querySsoUserDetail,
  createSsoUser,
  deleteSsoUser,
  updateSsoUser,
  querySsoUsers,
  deleteSsoPlatform,
  queryAllSystemUsers,
} from './service';
import type { ISsoPlatform, ISsoUser, ISystemUser } from './typings';
import type { PaginationProps } from 'antd/lib/pagination';

export interface ISsoModelStateType {
  ssoPlatformList: ISsoPlatform[];
  allSsoPlatforms: ISsoPlatform[];
  ssoPlatformDetail: ISsoPlatform;
  ssoUserList: ISsoUser[];
  ssoUserDetail: ISsoUser;
  pagination: PaginationProps;
  allSystemUsers: ISystemUser[];
}

export interface ISsoModelType {
  namespace: string;
  state: ISsoModelStateType;
  effects: {
    querySsoPlatforms: Effect;
    queryAllSsoPlatforms: Effect;
    querySsoPlatformDetail: Effect;
    createSsoPlatform: Effect;
    updateSsoPlatform: Effect;
    deleteSsoPlatform: Effect;

    querySsoUsers: Effect;
    querySsoUserDetail: Effect;
    createSsoUser: Effect;
    updateSsoUser: Effect;
    deleteSsoUser: Effect;

    queryAllSystemUsers: Effect;
  };
}

const Model = modelExtend(pageModel, {
  namespace: 'ssoModel',
  state: {
    ssoPlatformList: [],
    allSsoPlatforms: [],
    ssoPlatformDetail: {} as ISsoPlatform,
    ssoUserList: [],
    ssoUserDetail: {} as ISsoUser,
    allSystemUsers: [],
    pagination: { current: 1 } as PaginationProps,
  },
  reducers: {},
  effects: {
    *querySsoPlatforms({ payload = {} }, { call, put }) {
      yield doPageRequest({
        api: querySsoPlatforms,
        payload,
        call,
        put,
        stateKey: 'ssoPlatformList',
      });
    },

    *queryAllSsoPlatforms({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllSsoPlatforms, payload);
      yield put({
        type: 'updateState',
        payload: {
          allSsoPlatforms: success ? result : [],
        },
      });
    },

    *querySsoPlatformDetail({ payload }, { call, put }) {
      const { success, result } = yield call(querySsoPlatformDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          ssoPlatformDetail: success ? result : {},
        },
      });
    },

    *createSsoPlatform({ payload }, { call }) {
      const { success } = yield call(createSsoPlatform, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateSsoPlatform({ payload }, { call }) {
      const { success } = yield call(updateSsoPlatform, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteSsoPlatform({ payload }, { call }) {
      const { success } = yield call(deleteSsoPlatform, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },

    *querySsoUsers({ payload = {} }, { call, put }) {
      yield doPageRequest({
        api: querySsoUsers,
        payload,
        call,
        put,
        stateKey: 'ssoUserList',
      });
    },

    *querySsoUserDetail({ payload }, { call, put }) {
      const { success, result } = yield call(querySsoUserDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          ssoUserDetail: success ? result : {},
        },
      });
    },

    *createSsoUser({ payload }, { call }) {
      const { success } = yield call(createSsoUser, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    *updateSsoUser({ payload }, { call }) {
      const { success } = yield call(updateSsoUser, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    *deleteSsoUser({ payload }, { call }) {
      const { success } = yield call(deleteSsoUser, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },

    *queryAllSystemUsers({ payload }, { call, put }) {
      const { success, result } = yield call(queryAllSystemUsers, payload);
      yield put({
        type: 'updateState',
        payload: {
          allSystemUsers: success ? result : [],
        },
      });
    },
  },
} as ISsoModelType);

export default Model;
