import { Button, Result } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { history, Redirect, useLocation } from 'umi';
import { queryAllNetworks } from '@/pages/app/configuration/Network/service';
import type { INetwork } from '../../../configuration/Network/typings';
import type { IAnalysisTaskParams } from '..';
import { packetUrl } from '..';
import { getLinkUrl } from '@/utils/utils';
import PageLoading from '@/components/PageLoading';

export default function JumpToPacket() {
  const [allNetworks, setAllNetworks] = useState<INetwork[]>([]);
  const [loading, setLoading] = useState(true);
  const location = useLocation() as any as {
    pathname: string;
    query: {
      ipInitiator: string;
      portInitiator: string;
      ipResponder: string;
      portResponder: string;
      ipProtocol: string;
      startTime: string;
      endTime: string;
    } & IAnalysisTaskParams;
  };

  const jumpUrl = useMemo(() => {
    let networkId = null;
    if (allNetworks.length) {
      networkId = allNetworks[0].id;
    }
    let resUrl = `${packetUrl}`;
    const {
      ipInitiator,
      portInitiator,
      ipResponder,
      portResponder,
      ipProtocol,
      startTime,
      endTime,
    } = location.query;

    const filter: any = [];

    [ipInitiator, ipResponder].forEach((item) => {
      if (item) {
        filter.push({
          field: 'ipAddress',
          operator: '=',
          operand: item,
        });
      }
    });

    [portInitiator, portResponder].forEach((item) => {
      if (item) {
        filter.push({
          field: 'port',
          operator: '=',
          operand: item,
        });
      }
    });

    if (ipProtocol && ['tcp', 'udp', 'icmp', 'sctp'].includes(ipProtocol)) {
      filter.push({
        field: 'ipProtocol',
        operator: '=',
        operand: ipProtocol.toUpperCase(),
      });
    }

    if (networkId) {
      filter.push({
        field: 'network_id',
        operator: '=',
        operand: networkId,
      });
    }

    resUrl = `${resUrl}?filter=${encodeURIComponent(JSON.stringify(filter))}`;

    if (startTime && endTime) {
      resUrl = `${resUrl}&from=${startTime}&to=${endTime}`;
    }

    return resUrl;
  }, [allNetworks, location.query]);

  useEffect(() => {
    queryAllNetworks().then((res) => {
      const { success, result } = res;
      if (success) {
        setAllNetworks(result);
      }
      setLoading(false);
    });
  }, []);

  if (loading) {
    return <PageLoading />;
  }

  if (!allNetworks.length) {
    return (
      <Result
        status="info"
        title="还没有配置网络"
        extra={
          <Button
            type="primary"
            onClick={() => history.push(getLinkUrl('/configuration/network-netif/network'))}
          >
            配置网络
          </Button>
        }
      />
    );
  }

  return <Redirect to={jumpUrl} />;
}
