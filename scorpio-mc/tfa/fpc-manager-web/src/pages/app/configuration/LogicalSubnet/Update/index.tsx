import type { ConnectState } from '@/models/connect';
import { Card, Empty, Skeleton } from 'antd';
import React, { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { useParams } from 'umi';
import { connect } from 'dva';
import LogicalSubnetForm from '../components/Form';
import type { ILogicalSubnet } from '../typings';

interface IUpdateLogicalSubnetProps {
  dispatch: Dispatch;
  queryLoading: boolean | undefined;
  logicalSubnetDetail?: ILogicalSubnet;
}

const UpdateLogicalSubnet: React.FC<IUpdateLogicalSubnetProps> = ({
  dispatch,
  queryLoading,
  logicalSubnetDetail = {},
}) => {
  const params: { subnetId: string } = useParams();
  useEffect(() => {
    dispatch({
      type: 'logicSubnetModel/queryLogicalSubnetDetail',
      payload: {
        id: params.subnetId,
      },
    });
  }, [dispatch, params.subnetId]);

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        {logicalSubnetDetail.id ? (
          <LogicalSubnetForm detail={logicalSubnetDetail} type="update" />
        ) : (
          <Empty description="逻辑子网不存在或已被删除" />
        )}
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ loading: { effects }, logicSubnetModel: { logicalSubnetDetail } }: ConnectState) => ({
    queryLoading: effects['logicSubnetModel/queryLogicalSubnetDetail'],
    logicalSubnetDetail,
  }),
)(UpdateLogicalSubnet);
