import { login, logout } from '@/services/frame/global';
import { getPageQuery } from '@/utils/utils';
import { notification } from 'antd';
import type { Effect, Reducer } from 'umi';
import type { ConnectState } from '../connect';

export interface ITempUserInfo {
  username?: string;
  password?: string;
}
export interface LoginModelState {
  tempUserInfo?: ITempUserInfo;
  logoutPath?: string;
}

export interface LoginModelType {
  namespace: string;
  state: LoginModelState;

  effects: {
    login: Effect;
    logout: Effect;
    saveLogoutPath: Effect;
  };
  reducers: {
    updateTempUserInfo: Reducer<LoginModelState>;
    updateLogoutPath: Reducer<LoginModelState>;
  };
}

const Model: LoginModelType = {
  namespace: 'loginModel',

  state: {
    logoutPath: '',
  },

  effects: {
    *saveLogoutPath({ payload }, { put }) {
      yield put({
        type: 'updateLogoutPath',
        payload: {
          logoutPath: payload,
        },
      });
    },
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

    *logout(_, { put, call, select }) {
      // 获得当前登陆人的信息
      const currentUser = yield select((state: ConnectState) => {
        return state.globalModel.currentUser;
      });
      const { id } = currentUser;

      if (id) {
        // 存储当前登陆人的路由信息
        yield localStorage.setItem(
          `LOGOUT_USER_${window.location.host}_${id}`,
          yield select((state: ConnectState) => state.loginModel.logoutPath),
        );
      }

      // 清空登录人信息
      yield put({
        type: 'globalModel/clearCurrentUser',
        payload: {},
      });
      const { redirect } = getPageQuery();

      // 退出登陆
      yield call(logout);
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
    updateLogoutPath(state, { payload }): LoginModelState {
      return {
        ...state,
        logoutPath: payload.logoutPath,
      };
    },
  },
};

export default Model;
