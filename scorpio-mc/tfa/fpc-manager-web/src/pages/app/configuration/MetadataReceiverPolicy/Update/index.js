import { Card, Empty } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import MetadataReceiverPolicyForm from '../components/Form';

@connect((state) => {
  const {
    loading,
    ingestPolicyModel: { detail },
  } = state;
  const { effects } = loading;
  return {
    detail,
    queryDetailLoading: effects['ingestPolicyModel/queryDetail'],
    updateLoading: effects['ingestPolicyModel/update'],
  };
})
class UpdateReceiverSetting extends PureComponent {
  componentDidMount() {
    const { dispatch, location } = this.props;
    dispatch({
      type: 'ingestPolicyModel/queryDetail',
      payload: {
        id: location.query.id,
      },
    });
  }

  queryDetail = () => {};

  render() {
    const { queryDetailLoading, updateLoading, detail } = this.props;
    return (
      <Card bodyStyle={{ padding: 0 }} bordered={false} loading={queryDetailLoading}>
        {detail.clientId ? (
          <MetadataReceiverPolicyForm
            values={detail}
            type="UPDATE"
            loading={updateLoading}
            onDelete={this.handleDelete}
          />
        ) : (
          <Empty description="没有找到相关策略" />
        )}
      </Card>
    );
  }
}

export default UpdateReceiverSetting;
