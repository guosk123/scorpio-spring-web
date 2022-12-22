/**
 * ===========
 *  角色权限管理
 * ===========
 */

import ajax from '@/utils/frame/ajax';
import config from '@/common/applicationConfig';

/**
 * 拉取全部的权限列表
 */
export async function queryAllPerms() {
  return ajax(`${config.API_VERSION_V1}/system/perms`);
}

/**
 * 更新某个角色的权限
 * @param {String} roleId
 * @param {String} permIds
 */
export async function updateRolePerm({ roleId, permIds }) {
  return ajax(`${config.API_VERSION_V1}/system/roles/${roleId}/perm`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      permIds,
    },
  });
}
