import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import { model } from '@/utils/frame/model';
import { queryAllPerms, updateRolePerm } from '../services/perms';

export default modelExtend(model, {
  namespace: 'permsModel',
  state: {
    allPerms: [],
  },
  subscriptions: {},
  reducers: {},
  effects: {
    // 获取角色列表
    *queryAllPerms(_, { call, put }) {
      const { success, result } = yield call(queryAllPerms);
      yield put({
        type: 'updateState',
        payload: {
          allPerms: success ? result : [],
        },
      });
    },

    // 编辑某个角色的权限
    *updateRolePerm({ payload }, { call }) {
      const { success } = yield call(updateRolePerm, payload);
      if (success) {
        message.success('更新权限成功');
      } else {
        message.error('更新权限失败');
      }
      return success;
    },
  },
});
