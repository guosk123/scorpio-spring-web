import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Empty } from 'antd';

import StandardProtocolForm from '../components/Form';

@connect((state) => {
  const {
    loading,
    standardProtocolModel: { standardProtocolDetail },
  } = state;
  const { effects } = loading;
  return {
    detail: standardProtocolDetail,
    queryDetailLoading: effects['standardProtocolModel/queryStandardProtocolDetail'],
    updateLoading: effects['standardProtocolModel/updateStandardProtocol'],
  };
})
class UpdateStandardProtocol extends PureComponent {
  componentDidMount() {
    const { dispatch, match } = this.props;
    const { protocolId } = match.params;
    dispatch({
      type: 'standardProtocolModel/queryStandardProtocolDetail',
      payload: {
        id: protocolId,
      },
    });
  }

  render() {
    const { queryDetailLoading, updateLoading, detail } = this.props;
    return (
      <Card bordered={false} loading={queryDetailLoading}>
        {detail.id ? (
          <StandardProtocolForm
            detail={detail}
            operateType="UPDATE"
            submitLoading={updateLoading}
          />
        ) : (
          <Empty description="标准协议端口配置不存在或已被删除" />
        )}
      </Card>
    );
  }
}

export default UpdateStandardProtocol;
