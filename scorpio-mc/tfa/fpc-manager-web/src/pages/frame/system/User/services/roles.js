/**
 * ===========
 *  角色管理
 * ===========
 */

import { stringify } from 'qs';
import ajax from '@/utils/frame/ajax';
import { API_VERSION_V1 } from '@/common/applicationConfig';

/**
 * 查角色列表（分页）
 * @param {Object} params size || page
 */
export async function queryRoles(params) {
  return ajax(`${API_VERSION_V1}/system/roles${params && `?${stringify(params)}`}`);
}

/**
 * 查角色列表(全部)
 */
export async function queryAllRoles() {
  return ajax(`${API_VERSION_V1}/system/roles/as-list`);
}

/**
 * 添加角色
 * @param {Object} params 角色VO
 */
export async function createRole(params) {
  return ajax(`${API_VERSION_V1}/system/roles`, {
    type: 'POST',
    data: params,
  });
}

/**
 * 更新角色
 * @param {Object} params 角色VO
 */
export async function updateRole(params) {
  return ajax(`${API_VERSION_V1}/system/roles/${params.id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...params,
    },
  });
}

/**
 * 删除角色
 * @param {String} id 角色ID
 */
export async function deleteRole(id) {
  return ajax(`${API_VERSION_V1}/system/roles/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}
