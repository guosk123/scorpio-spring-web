import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card } from 'antd';

import UserForm from '../components/UserForm';

@connect(state => {
  const { loading } = state;
  const { effects } = loading;
  return {
    submitLoading: effects['usersModel/createUser'],
  };
})
class CreateUser extends PureComponent {
  componentDidMount() {}

  render() {
    const { submitLoading } = this.props;
    return (
      <Card bordered={false}>
        <UserForm operateType="CREATE" loading={submitLoading} />
      </Card>
    );
  }
}

export default CreateUser;
