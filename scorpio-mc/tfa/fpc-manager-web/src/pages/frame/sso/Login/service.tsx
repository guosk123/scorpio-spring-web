/**
 * ===========
 *  单点登录
 * ===========
 */
import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';
import type { ISsoLoginData, ISsoUserBindData, ISsoUserRegisterData } from './typings';

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
