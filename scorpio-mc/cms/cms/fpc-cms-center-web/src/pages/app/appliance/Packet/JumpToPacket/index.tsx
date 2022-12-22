import { queryNetWorkTree } from '@/pages/app/Network/service';
import { Button, message, Result } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { history, Redirect, useLocation } from 'umi';
import type { IAnalysisTaskParams } from '..';

export default function JumpToPacket() {
  const [allNetworks, setAllNetworks] = useState<any>([]);
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
      networkId = allNetworks[0].key;
    }
    let resUrl = `/analysis/network/${networkId}/packet`;
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

    resUrl = `${resUrl}?filter=${encodeURIComponent(JSON.stringify(filter))}`;

    if (startTime && endTime) {
      resUrl = `${resUrl}&from=${startTime}&to=${endTime}`;
    }

    return resUrl;
  }, [allNetworks, location.query]);

  useEffect(() => {
    queryNetWorkTree
      .then((result: any) => {
        setAllNetworks(result);
      })
      .catch((err: string) => {
        message.error(err);
      });
  }, []);

  if (!allNetworks.length) {
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

  return <Redirect to={jumpUrl} />;
}
