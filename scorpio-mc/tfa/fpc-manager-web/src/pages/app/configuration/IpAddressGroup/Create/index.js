import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Skeleton, Result, Button } from 'antd';
import { history } from 'umi';

import IpAddressGroupForm from '../components/Form';
import { MAX_IP_ADDRESS_GROUP_LIMIT } from '../index';

@connect(({ ipAddressGroupModel: { allIpAddressGroupList }, loading: { effects } }) => ({
  allIpAddressGroupList,
  queryAllLoading: effects['ipAddressGroupModel/queryAllIpAddressGroup'],
}))
class CreateIpAddressGroup extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'ipAddressGroupModel/queryAllIpAddressGroup',
    });
  }

  render() {
    const { queryAllLoading, allIpAddressGroupList } = this.props;
    if (queryAllLoading) {
      return <Skeleton active />;
    }

    return (
      <Card bordered={false}>
        {allIpAddressGroupList.length >= MAX_IP_ADDRESS_GROUP_LIMIT ? (
          <Result
            status="warning"
            title={`最多支持新建${MAX_IP_ADDRESS_GROUP_LIMIT}个TLS协议私钥
            `}
            extra={
              <Button type="primary" key="console" onClick={() => history.goBack()}>
                返回
              </Button>
            }
          />
        ) : (
          <IpAddressGroupForm operateType="CREATE" />
        )}
      </Card>
    );
  }
}

export default CreateIpAddressGroup;
