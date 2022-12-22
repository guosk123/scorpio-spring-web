import { DEF_ADM_ACCESS_MAP } from '@/access';
import type { ConnectState } from '@/models/connect';
import { getAuthenticationFn, getRouteAccessMap } from '@/utils/frame/menuAccess';
import { useMemo } from 'react';
import type { ICurrentUser } from 'umi';
import { useSelector } from 'umi';

/**
 *
 * @param authenticationCode 唯一路径
 * @param authenticationMap 权限表
 * @returns 是否拥有权限
 */
export default function useAuthentication(
  authenticationCode: string = '',
  authenticationMap: any = {},
) {
  // const test = 'aa/bb/cc/dd/ee'
  const currentUserInfo = useSelector<ConnectState, Required<ICurrentUser>>(
    (state) => state.globalModel.currentUser as any,
  );
  const AuthenticationFlag = useMemo(() => {
    const routeAccessMap =
      JSON.stringify(currentUserInfo?.menuPerms) === DEF_ADM_ACCESS_MAP
        ? getRouteAccessMap(true)
        : authenticationMap;
    return getAuthenticationFn(authenticationCode, routeAccessMap);
  }, [authenticationCode, authenticationMap, currentUserInfo?.menuPerms]);

  return AuthenticationFlag;
}
