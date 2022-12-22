import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Empty } from 'antd';
import MetadataCollectPolicyForm from '../components/Form';

@connect((state) => {
  const {
    loading,
    metadatCollectPolicyModel: { detail },
  } = state;
  const { effects } = loading;
  return {
    detail,
    queryDetailLoading: effects['metadatCollectPolicyModel/queryDetail'],
    updateLoading: effects['metadatCollectPolicyModel/update'],
  };
})
class ClientEdit extends PureComponent {
  componentDidMount() {
    const { dispatch, match } = this.props;
    const { policyId } = match.params;
    dispatch({
      type: 'metadatCollectPolicyModel/queryDetail',
      payload: {
        id: policyId,
      },
    });
  }

  render() {
    const { queryDetailLoading, updateLoading, detail } = this.props;
    return (
      <Card bodyStyle={{ padding: 0 }} bordered={false} loading={queryDetailLoading}>
        {detail.id ? (
          <MetadataCollectPolicyForm values={detail} type="UPDATE" loading={updateLoading} />
        ) : (
          <Empty description="没有找到相关策略" />
        )}
      </Card>
    );
  }
}

export default ClientEdit;
