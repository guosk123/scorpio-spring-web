import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card } from 'antd';

import StandardProtocolForm from '../components/Form';

@connect(state => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitLoading: effects['insideHostsModel/createInsideHost'],
  };
})
class CreateStandardProtocol extends PureComponent {
  componentDidMount() {}

  render() {
    const { submitLoading } = this.props;
    return (
      <Card bordered={false}>
        <StandardProtocolForm operateType="CREATE" submitLoading={submitLoading} />
      </Card>
    );
  }
}

export default CreateStandardProtocol;
