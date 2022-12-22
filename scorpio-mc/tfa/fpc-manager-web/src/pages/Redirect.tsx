import { DEF_ADM_ACCESS_MAP } from '@/access';
import type { ConnectState } from '@/models/connect';
import { queryCurrentUser } from '@/services/frame/global';
import {
  editIdentificationCode,
  getAuthenticationFn,
  getRouteAccessMap,
} from '@/utils/frame/menuAccess';
import { Spin } from 'antd';
import { connect } from 'dva';
import React, { useEffect, useState } from 'react';
import type { ICurrentUser } from 'umi';
import { Redirect, useAccess, useSelector } from 'umi';

interface RedirectWrapProps {
  queryUserInfoLoading: boolean | undefined;
}
const RedirectWrap: React.FC<RedirectWrapProps> = ({ queryUserInfoLoading }) => {
  const access = useAccess();

  const [menuAccessMapLoading, setMenuAccessMapLoading] = useState(true);

  const authenticationMap = useSelector<ConnectState>(
    (state) => state.globalModel.currentUser.menuPerms,
  );
  // const [menuAccessMap, setMenuAccessMap] = useState({});

  useEffect(() => {
    queryCurrentUser().then((res) => {
      const {
        success,
        result: { menuPerms },
      } = res;
      if (success) {
        // setMenuAccessMap(menuPerms);
      }
      setMenuAccessMapLoading(false);
    });
  }, []);
  const currentUserInfo = useSelector<ConnectState, Required<ICurrentUser>>(
    (state) => state.globalModel.currentUser as any,
  );

  useEffect(() => {
    //
    // window.location.reload();
    // 重新进入用户退出时的页面
    // const path = localStorage.getItem(`LOGOUT_USER_${window.location.host}_${currentUserInfo.id}`);
    // history.replace(path || '/');
    // console.log('re loading');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (queryUserInfoLoading || menuAccessMapLoading) {
    return <Spin />;
  }
  if (!access.isLogin) {
    return <Redirect to="/403" />;
  }

  if (access.hasUserPerm()) {
    let redirectUrl = '/analysis/dashboard/custom';
    const menuAccessMap =
      JSON.stringify(authenticationMap) === DEF_ADM_ACCESS_MAP
        ? getRouteAccessMap(true)
        : authenticationMap;
    const accessObj = getAuthenticationFn(editIdentificationCode(redirectUrl), menuAccessMap || {});
    if (!accessObj?.flag) {
      redirectUrl = '/welcome';
    }

    return <Redirect to={redirectUrl} />;
  }

  // 如果只有rest api调用的角色，跳转到单独的提示页面
  if (access.onlyRestApiPerm) {
    return <Redirect to="/restapi" />;
  }

  // 只有审计管理员权限
  if (access.onlyAuditPerm) {
    return <Redirect to="/system/log-alarm/log" />;
  }

  // 管理员权限
  if (access.hasAdminPerm) {
    return <Redirect to="/system/monitor" />;
  }

  return <Redirect to="/" />;
};

export default connect(({ loading }: ConnectState) => ({
  queryUserInfoLoading: loading.effects['globalModel/queryCurrentUser'],
}))(RedirectWrap);
