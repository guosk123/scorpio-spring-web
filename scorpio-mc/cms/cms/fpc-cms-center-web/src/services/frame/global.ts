import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory } from '@/common/typings';
import type { ICurrentUser } from 'umi';

export async function login(params: any) {
  return ajax('/login', {
    type: 'POST',
    data: params,
  });
}

export async function logout() {
  return ajax('/logout', {
    type: 'POST',
  });
}

// 取当前登录人
export async function queryCurrentUser(): Promise<IAjaxResponseFactory<ICurrentUser>> {
  return ajax(`${config.API_VERSION_V1}/boot/current-users`, {
    async: false,
  });
}

// 校验用户的密码
export async function checkCurrentPassword({ password }: Record<string, any>) {
  return ajax(`${config.API_VERSION_V1}/boot/current-password-verifications`, {
    type: 'POST',
    data: {
      password,
    },
  });
}

// 修改个人信息(目前是 appKey 和 appToken)
export async function updateCurrentUserInfo(params: Record<string, any>) {
  return ajax(`${config.API_VERSION_V1}/boot/current-users`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

// 修改个人密码
export async function updateCurrentUserPassword(params: Record<string, any>) {
  return ajax(`${config.API_VERSION_V1}/boot/current-user-passwords`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

// 取菜单配置
export async function queryMenus() {
  return ajax(`${config.API_VERSION_V1}/boot/menus`);
}

export async function queryProductInfos() {
  return ajax(`${config.API_VERSION_V1}/boot/product-infos`);
}

/**
 * 服务运行时间
 */
export async function queryRuntimeEnvironments() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/runtime-environments`);
}
