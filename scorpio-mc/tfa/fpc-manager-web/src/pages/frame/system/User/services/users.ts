/**
 * ===========
 *  用户管理
 * ===========
 */
import { API_VERSION_V1 } from '@/common/applicationConfig';
import type { IAjaxResponseFactory, IPageFactory, IPageParms } from '@/common/typings';
import ajax from '@/utils/frame/ajax';
import { stringify } from 'qs';
import type { IUser } from '../tpyings';

export async function queryUsers(
  params: IPageParms,
): Promise<IAjaxResponseFactory<IPageFactory<IUser>>> {
  return ajax(`${API_VERSION_V1}/system/users${params && `?${stringify(params)}`}`);
}

export async function queryUserList(): Promise<IAjaxResponseFactory<IUser[]>> {
  return ajax(`${API_VERSION_V1}/system/users/as-list`);
}

export async function queryDetail({ id }: { id: string }) {
  return ajax(`${API_VERSION_V1}/system/users/${id}`);
}

export async function createUser(params: any) {
  return ajax(`${API_VERSION_V1}/system/users`, {
    type: 'POST',
    data: {
      ...params,
    },
  });
}

export async function updateUser(params: any) {
  const { id, ...userVo } = params;
  return ajax(`${API_VERSION_V1}/system/users/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...userVo,
    },
  });
}

export async function deleteUser(id: string) {
  return ajax(`${API_VERSION_V1}/system/users/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

export async function changeUserLocked(params: any) {
  const { id, isLocked } = params;
  return ajax(`${API_VERSION_V1}/system/users/${id}/lock`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      isLocked,
    },
  });
}
