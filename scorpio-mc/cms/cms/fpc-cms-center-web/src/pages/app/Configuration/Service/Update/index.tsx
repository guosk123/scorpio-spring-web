import type { ConnectState } from '@/models/connect';
import { Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React from 'react';
import type { Dispatch } from 'umi';
import ServiceForm from '../components/Form';
import type { IService } from '../typings';

interface IUpdateServiceProps {
  dispatch: Dispatch;
  queryDetailLoading: boolean | undefined;
  serviceDetail: IService;
  match: {
    params: {
      serviceId: string;
    };
  };
}

class UpdateService extends React.PureComponent<IUpdateServiceProps> {
  componentDidMount() {
    const {
      params: { serviceId },
    } = this.props.match;
    const { dispatch } = this.props;
    dispatch({
      type: 'serviceModel/queryServiceDetail',
      payload: serviceId,
    });
  }

  render() {
    const { queryDetailLoading, serviceDetail } = this.props;
    if (queryDetailLoading) {
      return <Skeleton />;
    }
    if (!serviceDetail.id) {
      return <Empty description="业务配置不存在或已被删除" />;
    }
    return <ServiceForm detail={serviceDetail} />;
  }
}

export default connect(({ loading, serviceModel: { serviceDetail } }: ConnectState) => ({
  serviceDetail,
  queryDetailLoading: loading.effects['serviceModel/queryServiceDetail'],
}))(UpdateService);
