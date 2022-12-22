import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card } from 'antd';
import MetadataReceiverPolicyForm from '../components/Form';

@connect((state) => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitLoading: effects['ingestPolicyModel/create'],
  };
})
class CreateReceiverSetting extends PureComponent {
  componentDidMount() {}

  render() {
    const { submitLoading } = this.props;
    return (
      <Card bordered={false}>
        <MetadataReceiverPolicyForm type="CREATE" loading={submitLoading} />
      </Card>
    );
  }
}

export default CreateReceiverSetting;
