import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React from 'react';
import type { Dispatch } from 'umi';
import type { INetwork } from '../typings';
import NetworkForm from '../components/Form';

interface IUpdateNetworkProps {
  dispatch: Dispatch;
  queryDetailLoading: boolean;
  networkDetail: INetwork;
  match: {
    params: {
      networkId: string;
    };
  };
}

class UpdateNetwork extends React.PureComponent<IUpdateNetworkProps> {
  componentDidMount() {
    const {
      params: { networkId },
    } = this.props.match;
    const { dispatch } = this.props;
    dispatch({
      type: 'networkModel/queryNetworkDetail',
      payload: networkId,
    });
  }

  render() {
    const { queryDetailLoading, networkDetail } = this.props;
    if (queryDetailLoading) {
      return <Skeleton />;
    }
    if (!networkDetail.id) {
      return <Empty description="网络配置不存在或已被删除" />;
    }
    return <NetworkForm detail={networkDetail} />;
  }
}

export default connect(({ loading, networkModel: { networkDetail } }: ConnectState) => ({
  networkDetail,
  queryDetailLoading: loading.effects['networkModel/queryNetworkDetail'],
}))(UpdateNetwork);
