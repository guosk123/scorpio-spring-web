import { Button, Result } from 'antd';
import React from 'react';
import { history, useIntl } from 'umi';

const Exception500 = () => (
  <Result
    status="500"
    title="500"
    subTitle={useIntl().formatMessage({ id: 'app.exception.description.500' })}
    extra={
      <Button type="primary" onClick={() => history.push('/')}>
        {useIntl().formatMessage({ id: 'app.exception.back' })}
      </Button>
    }
  />
);

export default Exception500;
