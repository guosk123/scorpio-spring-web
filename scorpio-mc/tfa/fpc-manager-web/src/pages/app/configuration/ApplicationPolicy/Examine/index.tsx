import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import type { Dispatch } from 'umi';
import ApplicationPolicyForm from '../components/PolicyForm';
import type { IApplicationPolicy } from '../typings';

interface IUpdateApplicationPolicyProps {
  dispatch: Dispatch;
  queryDetailLoading: boolean;
  applicationPolicyDetail: IApplicationPolicy;
  match: {
    params: {
      policyId: string;
    };
  };
}

class Examine extends PureComponent<IUpdateApplicationPolicyProps> {
  componentDidMount() {
    const {
      params: { policyId },
    } = this.props.match;
    const { dispatch } = this.props;
    dispatch({
      type: 'applicationPolicyModel/queryApplicationPolicyDetail',
      payload: { policyId },
    });
  }

  render() {
    const { queryDetailLoading, applicationPolicyDetail } = this.props;
    if (queryDetailLoading) {
      return <Skeleton />;
    }
    if (!applicationPolicyDetail.id) {
      return <Empty description="应用规则不存在或已被删除" />;
    }
    return <ApplicationPolicyForm detail={applicationPolicyDetail} showMode={true} />;
  }
}

export default connect(
  ({ loading, applicationPolicyModel: { applicationPolicyDetail } }: ConnectState) => ({
    applicationPolicyDetail,
    queryDetailLoading: loading.effects['applicationPolicyModel/queryApplicationPolicyDetail'],
  }),
)(Examine);
