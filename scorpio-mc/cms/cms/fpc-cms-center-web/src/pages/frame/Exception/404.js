import { Button, Result } from 'antd';
import React from 'react';
import { history, useIntl } from 'umi';

const Exception404 = () => (
  <Result
    status="404"
    title="404"
    subTitle={useIntl().formatMessage({ id: 'app.exception.description.404' })}
    extra={
      <Button type="primary" onClick={() => history.push('/')}>
        {useIntl().formatMessage({ id: 'app.exception.back' })}
      </Button>
    }
  />
);

export default Exception404;
