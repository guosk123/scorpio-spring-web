import type { ConnectState } from '@/models/connect';
import { Spin } from 'antd';
import { connect } from 'dva';
import React from 'react';
import { Redirect, useAccess } from 'umi';

interface RedirectWrapProps {
  queryUserInfoLoading: boolean | undefined;
}
const RedirectWrap: React.FC<RedirectWrapProps> = ({ queryUserInfoLoading }) => {
  const access = useAccess();

  if (queryUserInfoLoading) {
    return <Spin />;
  }
  if (!access.isLogin) {
    return <Redirect to="/403" />;
  }

  if (access.hasUserPerm) {
    return <Redirect to="/dashboard" />;
  }

  // 如果只有rest api调用的角色，跳转到单独的提示页面
  if (access.onlyRestApiPerm) {
    return <Redirect to="/restapi" />;
  }

  // 只有审计管理员权限
  if (access.onlyAuditPerm) {
    return <Redirect to="/system/log-system/log" />;
  }

  // 管理员权限
  return <Redirect to="/system/monitor" />;
};

export default connect(({ loading }: ConnectState) => ({
  queryUserInfoLoading: loading.effects['globalModel/queryCurrentUser'],
}))(RedirectWrap);
