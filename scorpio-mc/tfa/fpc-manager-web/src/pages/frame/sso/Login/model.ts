import { message } from 'antd';
import modelExtend from 'dva-model-extend';
import type { Effect } from 'umi';
import { history } from 'umi';
import { pageModel } from '@/utils/frame/model';
import { ssoLogin, ssoUserRegister, ssoUserBind } from './service';

export interface ISsoOauthStateType {}
export interface ISsoOauthModelType {
  namespace: string;
  state: ISsoOauthStateType;
  effects: {
    ssoLogin: Effect;
    ssoUserBind: Effect;
    ssoUserRegister: Effect;
  };
}

const Model = modelExtend(pageModel, {
  namespace: 'ssoLoginModel',
  state: {},
  reducers: {},
  effects: {
    *ssoLogin({ payload }, { call }) {
      const response = yield call(ssoLogin, payload);
      return response;
    },
    *ssoUserBind({ payload }, { call }) {
      const response = yield call(ssoUserBind, payload);
      const { success, msg, status } = response;
      if (success) {
        message.success('用户关联成功');
      } else {
        if (status === 412) {
          history.push(`/sso/error?error=${msg}`);
          return {};
        }
        message.error('用户关联失败');
      }
      return response;
    },
    *ssoUserRegister({ payload }, { call }) {
      const response = yield call(ssoUserRegister, payload);
      const { success, msg } = response;
      if (success) {
        message.success('用户注册成功');
      } else {
        message.error(msg);
      }
      return response;
    },
  },
} as ISsoOauthModelType);

export default Model;
