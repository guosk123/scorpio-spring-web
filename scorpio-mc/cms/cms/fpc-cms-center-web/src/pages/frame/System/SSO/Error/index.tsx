import { Result } from 'antd';
import React from 'react';

interface ISsoErrorProps {
  location: {
    query: {
      error: string;
    };
  };
}

const SsoError: React.SFC<ISsoErrorProps> = ({ location }) => {
  const {
    query: { error },
  } = location;
  return <Result status="error" title="登录异常" subTitle={error} />;
};

export default SsoError;
