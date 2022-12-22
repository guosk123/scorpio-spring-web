import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { pageModel, doPageRequest } from '@/utils/frame/model';
import { queryRoles, queryAllRoles, createRole, updateRole, deleteRole } from '../services/roles';

export default modelExtend(pageModel, {
  namespace: 'rolesModel',
  state: {
    allRoles: [],
    roles: [],
  },
  subscriptions: {
    setupHistory({ dispatch, history }) {
      history.listen(location => {
        if (location.pathname === '/system/role') {
          const payload = { ...location.query };
          dispatch({
            type: 'queryRoles',
            payload,
          });
        }
      });
    },
  },
  reducers: {},
  effects: {
    *queryAllRoles(_, { call, put }) {
      const { success, result } = yield call(queryAllRoles);
      yield put({
        type: 'updateState',
        payload: {
          allRoles: success ? result : [],
        },
      });
    },
    *queryRoles({ payload = {} }, { call, put }) {
      yield doPageRequest({ api: queryRoles, payload, call, put, stateKey: 'roles' });
    },

    // 增加角色
    *createRole({ payload }, { call }) {
      const { success } = yield call(createRole, payload);
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    // 编辑角色
    *updateRole({ payload }, { call }) {
      const { success } = yield call(updateRole, payload);
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    // 删除角色
    *deleteRole({ payload }, { call }) {
      const { success } = yield call(deleteRole, payload);
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
  },
});
