import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Empty } from 'antd';

import TlsDecryptSettingForm from '../components/Form';

@connect((state) => {
  const {
    loading,
    tlsDecryptSettingModel: { tlsSettingDetail },
  } = state;
  const { effects } = loading;
  return {
    detail: tlsSettingDetail,
    queryDetailLoading: effects['tlsDecryptSettingModel/queryTlsDecryptSettingsDetail'],
  };
})
class UpdateTlsDecryptSetting extends PureComponent {
  componentDidMount() {
    const { dispatch, match } = this.props;
    const { settingId } = match.params;
    dispatch({
      type: 'tlsDecryptSettingModel/queryTlsDecryptSettingsDetail',
      payload: {
        id: settingId,
      },
    });
  }

  render() {
    const { queryDetailLoading, detail } = this.props;
    return (
      <Card bordered={false} loading={queryDetailLoading}>
        {detail.id ? (
          <TlsDecryptSettingForm detail={detail} operateType="UPDATE" />
        ) : (
          <Empty description="TLS协议私钥配置不存在或已被删除" />
        )}
      </Card>
    );
  }
}

export default UpdateTlsDecryptSetting;
