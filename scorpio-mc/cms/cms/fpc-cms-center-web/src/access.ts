import type { ICurrentUser } from 'umi';

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
  // 业务用户 + 业务管理员
  let hasUserPerm = false;
  // 业务管理员权限
  let hasServiceUserPerm = false;
  // restAPI 调用人
  let hasRestApiPerm = false;
  // 只是 restAPI 调用人
  let onlyRestApiPerm = false;

  // console.log('currentUser', currentUser);
  if (currentUser && currentUser.id) {
    isLogin = true;

    // 解析权限
    const { authorities } = currentUser;

    authorities.forEach((el) => {
      if (el.authority === 'PERM_SYS_USER') {
        hasAdminPerm = true;
      }
      if (el.authority === 'PERM_AUDIT_USER') {
        hasAuditPerm = true;
      }
      if (el.authority === 'PERM_SERVICE_USER') {
        hasServiceUserPerm = true;
        hasUserPerm = true;
      }
      if (el.authority === 'PERM_USER') {
        hasUserPerm = true;
      }
      if (el.authority === 'PERM_RESTAPI_USER') {
        hasRestApiPerm = true;
      }
    });

    if (hasAuditPerm && authorities.length === 1) {
      onlyAuditPerm = true;
    }

    if (hasRestApiPerm && authorities.length === 1) {
      onlyRestApiPerm = true;
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
    /** 业务用户 + 业务管理员 */
    hasUserPerm,
    /** 业务管理员 */
    hasServiceUserPerm,
    /** rest api */
    hasRestApiPerm,
    onlyRestApiPerm,
  };
}
