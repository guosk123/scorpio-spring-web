import { Card } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import MetadataCollectPolicyForm from '../components/Form';

@connect((state) => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitLoading: effects['metadatCollectPolicyModel/create'],
  };
})
class CreateCollectPolicy extends PureComponent {
  componentDidMount() {}

  render() {
    const { submitLoading } = this.props;
    return (
      <Card bordered={false}>
        <MetadataCollectPolicyForm type="CREATE" loading={submitLoading} />
      </Card>
    );
  }
}

export default CreateCollectPolicy;
