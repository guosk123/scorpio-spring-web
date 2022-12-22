/**
 * ===========
 *  单点登录
 * ===========
 */
import config from '@/common/applicationConfig';
import ajax from '@/utils/frame/ajax';
import type { ISsoLoginData, ISsoUserBindData, ISsoUserRegisterData } from './typings';

const { API_VERSION_V1 } = config;

export async function ssoLogin(data: ISsoLoginData) {
  return ajax(`${API_VERSION_V1}/sso/login`, {
    type: 'POST',
    data: { ...data },
  });
}

export async function ssoUserBind({ token, ...restData }: ISsoUserBindData) {
  return ajax(`${API_VERSION_V1}/sso/user-bind`, {
    type: 'POST',
    headers: {
      token,
    },
    data: { ...restData },
  });
}

export async function ssoUserRegister({ token, ...restData }: ISsoUserRegisterData) {
  return ajax(`${API_VERSION_V1}/sso/user-register`, {
    type: 'POST',
    headers: {
      token,
    },
    data: { ...restData },
  });
}
