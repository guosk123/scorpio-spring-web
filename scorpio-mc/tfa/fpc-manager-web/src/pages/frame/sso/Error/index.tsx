import { Button, Result } from 'antd';
import React from 'react';
import { history } from 'umi';

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
  return (
    <Result
      status="error"
      title="登录异常"
      subTitle={error}
      extra={[
        <Button
          type="primary"
          onClick={() => {
            history.replace('/login');
          }}
        >
          返回登录页
        </Button>,
      ]}
    />
  );
};

export default SsoError;
