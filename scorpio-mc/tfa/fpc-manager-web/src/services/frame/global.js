import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';

export async function login(params) {
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
export async function queryCurrentUser() {
  return ajax(`${API_VERSION_V1}/boot/current-users`, {
    async: false,
  });
}

// 校验用户的密码
export async function checkCurrentPassword({ password }) {
  return ajax(`${API_VERSION_V1}/boot/current-password-verifications`, {
    type: 'POST',
    data: {
      password,
    },
  });
}

// 修改个人信息(目前是 appKey 和 appToken)
export async function updateCurrentUserInfo(params) {
  return ajax(`${API_VERSION_V1}/boot/current-users`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

// 修改个人密码
export async function updateCurrentUserPassword(params) {
  return ajax(`${API_VERSION_V1}/boot/current-user-passwords`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

// 取菜单配置
export async function queryMenus() {
  return ajax(`${API_VERSION_V1}/boot/menus`);
}

export async function queryProductInfos() {
  return ajax(`${API_VERSION_V1}/boot/product-infos`);
}
