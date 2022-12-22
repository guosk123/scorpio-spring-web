import { connect } from 'umi';
import type { ConnectState } from '@/models/connect';
import { Button, Result, Skeleton } from 'antd';
import NetworkForm from '../components/Form';
import type { INetwork } from '../typings';
import { MAX_CUSTOM_NETWORK_LIMIT } from '../typings';
import { history } from 'umi';

interface Props {
  allNetworks: INetwork[];
  queryLoading?: boolean;
}

function CreateNetwork(props: Props) {
  const { allNetworks, queryLoading } = props;
  if (queryLoading) {
    return <Skeleton active />;
  }
  if (allNetworks.length >= MAX_CUSTOM_NETWORK_LIMIT) {
    return (
      <Result
        status="warning"
        title={`最多支持网络${MAX_CUSTOM_NETWORK_LIMIT}个`}
        extra={
          <Button type="primary" key="console" onClick={() => history.goBack()}>
            返回
          </Button>
        }
      />
    );
  }
  return <NetworkForm />;
}
export default connect(({ networkModel: { allNetworks }, loading }: ConnectState) => {
  return { allNetworks, loading: loading.effects['networkModel/queryAllNetworks'] };
})(CreateNetwork);
