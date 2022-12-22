import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React from 'react';
import type { Dispatch } from 'umi';
import IngestPolicyForm from '../components/IngestPolicyForm';
import type { IIngestPolicy } from '../typings';

interface IUpdateIngestPolicyProps {
  dispatch: Dispatch;
  queryDetailLoading: boolean;
  ingestPolicyDeatil: IIngestPolicy;
  match: {
    params: {
      policyId: string;
    };
  };
}

class Examine extends React.PureComponent<IUpdateIngestPolicyProps> {
  componentDidMount() {
    const {
      params: { policyId },
    } = this.props.match;
    const { dispatch } = this.props;
    dispatch({
      type: 'ingestPolicyModel/queryIngestPolicyDetail',
      payload: policyId,
    });
  }

  render() {
    const { queryDetailLoading, ingestPolicyDeatil } = this.props;
    if (queryDetailLoading) {
      return <Skeleton />;
    }
    if (!ingestPolicyDeatil.id) {
      return <Empty description="业务配置不存在或已被删除" />;
    }
    return <IngestPolicyForm detail={ingestPolicyDeatil} showMode={true} />;
  }
}

export default connect(({ loading, ingestPolicyModel: { ingestPolicyDeatil } }: ConnectState) => ({
  ingestPolicyDeatil,
  queryDetailLoading: loading.effects['ingestPolicyModel/queryIngestPolicyDetail'],
}))(Examine);
