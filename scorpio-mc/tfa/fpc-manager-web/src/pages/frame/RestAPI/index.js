import React from 'react';
import { Result, Card } from 'antd';

export default () => (
  <Card bordered={false}>
    <Result
      title="您仅限于使用REST API功能"
      subTitle="如需其他功能，请联系系统管理员。"
    />
  </Card>
);
