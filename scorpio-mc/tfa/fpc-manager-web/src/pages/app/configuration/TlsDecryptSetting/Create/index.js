import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { history } from 'umi';
import { Card, Result, Button, Skeleton } from 'antd';
import TlsDecryptSettingForm from '../components/Form';
import { MAX_LIMIT } from '../index';

@connect(({ tlsDecryptSettingModel: { allTlsDecryptSettings }, loading: { effects } }) => ({
  allTlsDecryptSettings,
  queryLoading: effects['tlsDecryptSettingModel/queryAllTlsDecryptSettings'],
}))
class CreateTlsDecryptSetting extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'tlsDecryptSettingModel/queryAllTlsDecryptSettings',
    });
  }

  render() {
    const { queryLoading, allTlsDecryptSettings } = this.props;
    if (queryLoading) {
      return <Skeleton active />;
    }
    return (
      <Card bordered={false}>
        {allTlsDecryptSettings.length >= MAX_LIMIT ? (
          <Result
            status="warning"
            title={`最多支持新建${MAX_LIMIT}个TLS协议私钥
            `}
            extra={
              <Button type="primary" key="console" onClick={() => history.goBack()}>
                返回
              </Button>
            }
          />
        ) : (
          <TlsDecryptSettingForm operateType="CREATE" />
        )}
      </Card>
    );
  }
}

export default CreateTlsDecryptSetting;
