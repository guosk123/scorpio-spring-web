import PageLoading from '@/components/PageLoading';
import type { ConnectState } from '@/models/connect';
import { pageIsEmbed } from '@/utils/frame/utils';
import { Button, Result } from 'antd';
import { useEffect } from 'react';
import { connect, history } from 'umi';
import type { INetwork } from '../../configuration/Network/typings';

interface INetworkRedirectProps {
  allNetworks: INetwork[];
}

const NetworkRedirect: React.FC<INetworkRedirectProps> = ({ allNetworks }) => {
  useEffect(() => {
    if (allNetworks.length > 0) {
      if (pageIsEmbed()) {
        history.replace(`/embed/analysis/performance/network/${allNetworks[0].id}/dashboard`);
      } else {
        history.replace(`/analysis/performance/network/${allNetworks[0].id}/dashboard`);
      }
    }
  }, [allNetworks, allNetworks.length]);

  if (allNetworks.length === 0) {
    return (
      <Result
        status="info"
        title="还没有配置网络"
        extra={
          <Button
            type="primary"
            onClick={() => history.push('/configuration/network-netif/network')}
          >
            配置网络
          </Button>
        }
      />
    );
  }

  return <PageLoading />;
};

export default connect(({ networkModel: { allNetworks } }: ConnectState) => ({
  allNetworks,
}))(NetworkRedirect);
