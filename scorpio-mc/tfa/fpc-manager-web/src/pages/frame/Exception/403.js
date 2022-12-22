import { Button, Result } from 'antd';
import React from 'react';
import { history, useIntl } from 'umi';

const Exception403 = () => (
  <Result
    status="403"
    title="403"
    subTitle={useIntl().formatMessage({ id: 'app.exception.description.403' })}
    extra={
      <Button type="primary" onClick={() => history.push('/')}>
        {useIntl().formatMessage({ id: 'app.exception.back' })}
      </Button>
    }
  />
);

export default Exception403;
