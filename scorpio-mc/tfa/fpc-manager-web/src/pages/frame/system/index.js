import React from 'react';
import { Redirect, useAccess } from 'umi';

export default () => {
  const access = useAccess();
  if (access.onlyAuditPerm) {
    return <Redirect to="/system/log-alarm/log" />;
  }

  // 管理员权限
  return <Redirect to="/system/monitor" />;
};
