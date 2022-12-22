import type { ConnectState } from '@/models/connect';
import { Button, Result, Skeleton } from 'antd';
import { connect } from 'dva';
import LogicalSubnetForm from '../components/Form';
import type { ILogicalSubnet } from '../typings';
import { MAX_CUSTOM_SUBNETWORK_LIMIT } from '../typings';
import { history } from 'umi';

interface Props {
  allLogicalSubnets: ILogicalSubnet[];
  loading?: boolean;
}

function CreateLogicalSubnet(props: Props) {
  const { allLogicalSubnets, loading } = props;
  if (loading) {
    return <Skeleton active />;
  }
  if (allLogicalSubnets.length >= MAX_CUSTOM_SUBNETWORK_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持子网络${MAX_CUSTOM_SUBNETWORK_LIMIT}个`}
        extra={
          <Button type="primary" key="console" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }
  return <LogicalSubnetForm type={'create'} />;
}

export default connect(({ logicSubnetModel: { allLogicalSubnets }, loading }: ConnectState) => ({
  allLogicalSubnets,
  loading: loading.effects['logicSubnetModel/queryAllLogicalSubnets'],
}))(CreateLogicalSubnet);
