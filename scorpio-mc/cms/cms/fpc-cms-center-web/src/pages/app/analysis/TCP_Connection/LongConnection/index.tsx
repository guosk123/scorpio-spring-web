import Record from '@/pages/app/appliance/FlowRecords/Record';
import { NetworkTypeContext } from '@/pages/app/Network/Analysis';
import { ConnectionContext } from '@/pages/app/Network/components/Connection';
import { connectSettings } from '@/pages/app/Network/components/Connection/constant';
import { EConnectTabs } from '@/pages/app/Network/components/Connection/typing';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { SettingOutlined } from '@ant-design/icons';
import { Button, Skeleton } from 'antd';
import React, { useContext, useEffect, useMemo, useState } from 'react';
import { connect, useParams } from 'umi';
import type { IUriParams } from '../../typings';
import { EMetricSettingCategory } from '../../typings';
import { ESourceType } from '../../typings';
import { DEFAULT_LONG_CONNECTION_SECONDS } from '../LongConnectionSetting';
import { queryMetricSetting } from '../service';
import { ServiceContext } from '@/pages/app/analysis/Service/index';

const LongConnection: React.FC = () => {
  // 加个标识，防止 Record 组件渲染2次
  const [isReady, setIsReady] = useState<boolean>(false);

  const [longConnectionSeconds, setLongConnectionSeconds] = useState<number>(0);

  const urlIds = useParams<IUriParams>();
  const { serviceId, networkId } = useMemo(() => {
    const tmpNetworkId = urlIds.networkId || '';
    if (tmpNetworkId.includes('^')) {
      return {
        serviceId: urlIds.serviceId,
        networkId: tmpNetworkId.split('^')[1],
      };
    }
    return { serviceId: urlIds.serviceId, networkId: urlIds.networkId };
  }, [urlIds.networkId, urlIds.serviceId]);

  const [queryConnectionSettingLoading, setQueryConnectionSettingLoading] = useState(false);
  const [networkType] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  useEffect(() => {
    setQueryConnectionSettingLoading(true);
    const queryParams = {
      sourceType,
      serviceId,
    };
    queryParams[networkType === ENetowrkType.NETWORK ? 'networkId' : 'netwrokGroupId'] = networkId;
    queryMetricSetting(queryParams).then((res) => {
      const { success, result } = res;
      if (success) {
        const target = result.find(
          (row: any) => row.metric === EMetricSettingCategory.LONG_CONNECTION,
        );
        setLongConnectionSeconds(target?.value || DEFAULT_LONG_CONNECTION_SECONDS);
      }
      setQueryConnectionSettingLoading(false);
    });
  }, [networkId, serviceId, sourceType, networkType]);

  useEffect(() => {
    if (!isReady) {
      setIsReady(true);
    }
  }, [isReady]);

  const [state, dispatch] = useContext(ConnectionContext);

  if (queryConnectionSettingLoading || !isReady) {
    return <Skeleton active loading={queryConnectionSettingLoading} />;
  }

  return (
    <Record
      tableKey="long-connection-table"
      filterHistoryKey="long-connection-filter-history"
      extraDsl={`duration > ${longConnectionSeconds * 1000}`}
      displayMetrics={[
        'network_id',
        'service_id',
        'report_time',
        'start_time',
        'ip_initiator',
        'ipv4_initiator',
        'ipv6_initiator',
        'port_initiator',
        'ip_responder',
        'ipv4_responder',
        'ipv6_responder',
        'port_responder',
        'duration',
        'upstream_bytes',
        'downstream_bytes',
        'upstream_packets',
        'downstream_packets',
        'tcp_session_state',
      ]}
      extraAction={
        <Button
          type="primary"
          icon={<SettingOutlined />}
          onClick={() => {
            connectSettings(state, dispatch, EConnectTabs.LONG_CONNECTION_SETTING, networkType);
          }}
          disabled={networkType === ENetowrkType.NETWORK_GROUP}
        >
          长连接配置
        </Button>
      }
    />
  );
};

export default connect()(LongConnection);
