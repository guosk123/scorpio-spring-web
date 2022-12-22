import type { ICurrentUser } from 'umi';
import {
  editIdentificationCode,
  getAuthenticationFn,
  getRouteAccessMap,
} from './utils/frame/menuAccess';

export const DEF_ADM_ACCESS_MAP = '{"all":1}'

export default function access(initialState: { currentUser?: ICurrentUser | undefined }) {
  const { currentUser } = initialState || {};

  // 登录标志
  let isLogin = false;
  // 系统管理员
  let hasAdminPerm = false;
  // 审计管理员
  let hasAuditPerm = false;
  // 只有审计管理员一个角色
  let onlyAuditPerm = false;
  // 业务管理员
  let hasUserPerm = false;
  // restAPI 调用人
  let hasRestApiPerm = false;
  // 只是 restAPI 调用人
  let onlyRestApiPerm = false;
  // 无权限页面
  const disablePath = false;

  if (currentUser && currentUser.id) {
    isLogin = true;

    // 解析权限
    const { authorities } = currentUser;
    if (authorities.find((el) => el.authority === 'PERM_SYS_USER')) {
      hasAdminPerm = true;
    }
    if (authorities.find((el) => el.authority === 'PERM_AUDIT_USER')) {
      hasAuditPerm = true;
      if (authorities.length === 1) {
        onlyAuditPerm = true;
      }
    }
    if (authorities.find((el) => el.authority === 'PERM_USER')) {
      hasUserPerm = true;
    }
    if (authorities.find((el) => el.authority === 'PERM_RESTAPI_USER')) {
      hasRestApiPerm = true;
      if (authorities.length === 1) {
        onlyRestApiPerm = true;
      }
    }
  }

  return {
    /** 是否登录 */
    isLogin,
    /** 系统管理员 */
    hasAdminPerm,
    /** 审计管理员 */
    hasAuditPerm,
    /** 只有审计管理员一个角色 */
    onlyAuditPerm,
    /** 系统管理员或审计管理员 */
    hasAdminOrAuditPerm: hasAdminPerm || hasAuditPerm,
    /** 业务管理员 */
    hasUserPerm: (route?: any) => {
      if (route && hasUserPerm && currentUser?.menuPerms) {
        const routeAccessMap =
          JSON.stringify(currentUser?.menuPerms) === DEF_ADM_ACCESS_MAP
            ? getRouteAccessMap(true)
            : currentUser?.menuPerms;
        const res = getAuthenticationFn(
          editIdentificationCode(route.path || '/welcome'),
          routeAccessMap || {},
        );
        return res.flag;
      }
      return hasUserPerm;
    },
    /** rest api */
    hasRestApiPerm,
    onlyRestApiPerm,
    disablePath,
  };
}
