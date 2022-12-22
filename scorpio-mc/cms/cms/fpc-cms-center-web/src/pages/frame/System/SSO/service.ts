/**
 * ===========
 *  单点登录
 * ===========
 */
import config from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { ISsoPlatform, ISsoUser } from './typings';

const { API_VERSION_V1 } = config;

export async function querySsoPlatforms(
  params: any,
): Promise<IAjaxResponseFactory<IPageFactory<ISsoPlatform>>> {
  return ajax(`${API_VERSION_V1}/system/sso/platforms${params && `?${stringify(params)}`}`);
}
export async function queryAllSsoPlatforms() {
  return ajax(`${API_VERSION_V1}/system/sso/platforms/as-list`);
}
export async function querySsoPlatformDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/sso/platforms/${id}`);
}
export async function createSsoPlatform(data: ISsoPlatform[]) {
  return ajax(`${API_VERSION_V1}/system/sso/platforms`, {
    type: 'POST',
    data: {
      platforms: JSON.stringify(data),
    },
  });
}
export async function updateSsoPlatform(params: ISsoPlatform) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_V1}/system/sso/platforms/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}
export async function deleteSsoPlatform({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/sso/platforms/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function querySsoUsers(
  params: any,
): Promise<IAjaxResponseFactory<IPageFactory<ISsoUser>>> {
  return ajax(`${API_VERSION_V1}/system/sso/users${params && `?${stringify(params)}`}`);
}
export async function querySsoUserDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/sso/users/${id}`);
}
export async function createSsoUser(data: ISsoUser[]) {
  return ajax(`${API_VERSION_V1}/system/sso/users`, {
    type: 'POST',
    data: {
      users: JSON.stringify(data),
    },
  });
}
export async function updateSsoUser(params: ISsoUser) {
  const { id, ...restParams } = params;
  return ajax(`${API_VERSION_V1}/system/sso/users/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restParams,
    },
  });
}
export async function deleteSsoUser({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/sso/users/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function queryAllSystemUsers() {
  return ajax(`${API_VERSION_V1}/system/users/as-list`);
}
export async function queryDeviceInfo() {
  return ajax(`${API_VERSION_V1}/system/custom-infos`);
}

export async function updateDeviceInfo(params: any) {
  return ajax(`${API_VERSION_V1}/system/custom-infos`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

export async function queryFpcLoginSetting() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/device-login-settings`);
}

export async function queryDeviceList() {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/central/devices/as-list`);
}

export async function updateFpcLoginSetting(params: any) {
  return ajax(`${config.API_VERSION_PRODUCT_V1}/system/device-login-settings`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}
