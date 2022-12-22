import type { Effect, Reducer } from 'umi';
import { login, logout } from '@/services/frame/global';
import { getPageQuery } from '@/utils/utils';
import { notification } from 'antd';

export interface ITempUserInfo {
  username?: string;
  password?: string;
}
export interface LoginModelState {
  tempUserInfo?: ITempUserInfo;
}

export interface LoginModelType {
  namespace: string;
  state: LoginModelState;
  effects: {
    login: Effect;
    logout: Effect;
  };
  reducers: {
    updateTempUserInfo: Reducer<LoginModelState>;
  };
}

const Model: LoginModelType = {
  namespace: 'loginModel',

  state: {},

  effects: {
    *login({ payload }, { call, put }) {
      const { success, result } = yield call(login, payload);
      // Login successfully
      if (success) {
        yield put({
          type: 'updateTempUserInfo',
          payload: {},
        });
      } else {
        const { status, message } = JSON.parse(result);
        if (status === 401) {
          notification.error({
            message: '登录失败',
            description: '用户名或密码错误',
          });
        } else if (status === 403) {
          const ERROR = [
            {
              en: 'Authentication internal failed',
              zh: '',
            },
            {
              en: 'Authentication failed ',
              zh: '',
            },
            {
              en: 'Bad credentials',
              zh: '用户名或密码错误',
            },
            {
              en: 'Verification code failded',
              zh: '验证码错误',
            },
            {
              en: 'User account is locked',
              zh: '用户被锁定',
            },
          ];

          let msg = message || '登录失败';

          if (msg === 'Verification code failded') {
            yield put({
              type: 'updateTempUserInfo',
              payload,
            });
          }
          // eslint-disable-next-line no-shadow
          const err = ERROR.filter((item) => item.en === msg);
          if (err.length > 0) {
            msg = err[0].zh;
          }

          notification.error({
            message: '登录失败',
            description: msg,
          });
        }
      }
      return success;
    },

    *logout(_, { put, call }) {
      yield call(logout);
      // 清空登录人信息
      yield put({
        type: 'globalModel/clearCurrentUser',
        payload: {},
      });
      const { redirect } = getPageQuery();
      // Note: There may be security issues, please note
      if (window.location.pathname !== '/login' && !redirect) {
        // history.replace({
        //   pathname: '/login',
        // });
      }
    },
  },

  reducers: {
    updateTempUserInfo(state, { payload }): LoginModelState {
      return {
        ...state,
        tempUserInfo: {
          username: payload.username,
          password: payload.password,
        },
      };
    },
  },
};

export default Model;
