import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Empty } from 'antd';

import UserForm from '../components/UserForm';

@connect((state) => {
  const {
    loading,
    usersModel: { detail },
  } = state;
  const { effects } = loading;
  return {
    detail,
    queryDetailLoading: effects['usersModel/queryDetail'],
    updateLoading: effects['usersModel/updateUser'],
  };
})
class UpdateUser extends PureComponent {
  componentDidMount() {
    const { dispatch, location } = this.props;
    const { id } = location.query;
    dispatch({
      type: 'usersModel/queryDetail',
      payload: {
        id,
      },
    });
  }

  render() {
    const { queryDetailLoading, updateLoading, detail } = this.props;
    return (
      <Card bordered={false} loading={queryDetailLoading}>
        {detail.id ? (
          <UserForm detail={detail} operateType="UPDATE" loading={updateLoading} />
        ) : (
          <Empty description="此用户不存在或已被删除" />
        )}
      </Card>
    );
  }
}

export default UpdateUser;
