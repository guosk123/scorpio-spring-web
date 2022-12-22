import { pageModel } from '@/utils/frame/model';
import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import {
  changeUserLocked,
  createUser,
  deleteUser,
  queryDetail,
  updateUser,
} from '../services/users';

export default modelExtend(pageModel, {
  namespace: 'usersModel',

  state: {
    detail: {},
  },

  effects: {
    *queryDetail({ payload }, { call, put }) {
      const { success, result } = yield call(queryDetail, payload);
      yield put({
        type: 'updateState',
        payload: {
          detail: success ? result : {},
        },
      });
    },

    // 增加用户
    *createUser({ payload }, { call }) {
      const response = yield call(createUser, payload);
      const { success } = response;
      if (success) {
        message.success('添加成功');
      } else {
        message.error('添加失败');
      }
      return success;
    },
    // 编辑用户
    *updateUser({ payload }, { call }) {
      const response = yield call(updateUser, payload);
      const { success } = response;
      if (success) {
        message.success('编辑成功');
      } else {
        message.error('编辑失败');
      }
      return success;
    },
    // 删除用户
    *deleteUser({ payload }, { call }) {
      const response = yield call(deleteUser, payload);
      const { success } = response;
      if (success) {
        message.success('删除成功');
      } else {
        message.error('删除失败');
      }
      return success;
    },
    // 锁定用户 / 解锁用户
    *changeUserLocked({ payload }, { call }) {
      const response = yield call(changeUserLocked, payload);
      const { success } = response;
      if (success) {
        message.success('状态更改成功');
      } else {
        message.error('状态更改失败');
      }
      return success;
    },
  },
});
