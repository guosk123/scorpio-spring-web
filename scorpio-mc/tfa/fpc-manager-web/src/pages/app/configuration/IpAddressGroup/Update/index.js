import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Empty } from 'antd';

import InsideHostForm from '../components/Form';

@connect((state) => {
  const {
    loading,
    ipAddressGroupModel: { ipAddressGroupDetail },
  } = state;
  const { effects } = loading;
  return {
    detail: ipAddressGroupDetail,
    queryDetailLoading: effects['ipAddressGroupModel/queryIpAddressGroupDetail'],
  };
})
class UpdateIpAddressGroup extends PureComponent {
  componentDidMount() {
    const { dispatch, match } = this.props;
    const { hostgroupId } = match.params;
    dispatch({
      type: 'ipAddressGroupModel/queryIpAddressGroupDetail',
      payload: {
        id: hostgroupId,
      },
    });
  }

  render() {
    const { queryDetailLoading, detail } = this.props;
    return (
      <Card bordered={false} loading={queryDetailLoading}>
        {detail.id ? (
          <InsideHostForm detail={detail} operateType="UPDATE" />
        ) : (
          <Empty description="地址组不存在或已被删除" />
        )}
      </Card>
    );
  }
}

export default UpdateIpAddressGroup;
