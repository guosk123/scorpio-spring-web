// import React from 'react';
import { connect } from 'dva';
import type { ConnectState } from '@/models/connect';
import type { Dispatch } from 'umi';
import { Button, Card, Result, Skeleton } from 'antd';
import IpAddressGroupForm from '../components/Form';
import { MAX_IP_ADDRESS_GROUP_LIMIT } from '../index';
import { history } from 'umi';
import { useCallback, useEffect } from 'react';

interface Props {
  dispatch: Dispatch;
  queryAllLoading: boolean | undefined;
  allIpAddressGroupList: any;
}

function CreateIpAddressGroup(props: Props) {


  const { queryAllLoading, allIpAddressGroupList, dispatch } = props;
  const queryAllIpAddressGroup = useCallback(()=>{
    if(dispatch){
      dispatch({
        type: 'ipAddressGroupModel/queryAllIpAddressGroup',
      });
    }
  },[dispatch]);

  useEffect(()=>{
    queryAllIpAddressGroup();
  },[queryAllIpAddressGroup]);

  console.log(queryAllLoading, 'loading');
  console.log(allIpAddressGroupList, 'List');
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
        <IpAddressGroupForm operateType="CREATE"/>
      )}
    </Card>
  );
}

export default connect(
  ({ loading: { effects }, ipAddressGroupModel: { allIpAddressGroupList } }: ConnectState) => ({
    queryAllLoading: effects['ipAddressGroupModel/queryAllIpAddressGroup'],
    allIpAddressGroupList,
  }),
)(CreateIpAddressGroup);
