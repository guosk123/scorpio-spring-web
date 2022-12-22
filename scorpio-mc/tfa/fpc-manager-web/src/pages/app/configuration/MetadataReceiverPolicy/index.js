import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Alert } from 'antd';
import MetadataReceiverPolicyForm from './components/Form';

@connect((state) => {
  const {
    loading,
    receiverSettingsModel: { detail },
  } = state;
  const { effects } = loading;
  return {
    detail,
    queryDetailLoading: effects['receiverSettingsModel/query'],
    updateLoading: effects['receiverSettingsModel/update'],
  };
})
class Receiver extends PureComponent {
  componentDidMount() {
    this.querySetting();
  }

  querySetting = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'receiverSettingsModel/query',
    });
  };

  render() {
    const { queryDetailLoading, updateLoading, detail } = this.props;
    return (
      <Card bordered={false} loading={queryDetailLoading}>
        <div style={{ width: '90%', margin: '0 auto 20px' }}>
          <Alert showIcon type="info" message="当前只支持 Kafka 接收。" />
        </div>
        <MetadataReceiverPolicyForm values={detail} loading={updateLoading} />
      </Card>
    );
  }
}

export default Receiver;
