import type { ConnectState } from '@/models/connect';
import { SettingOutlined } from '@ant-design/icons';
import { Button, Skeleton } from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useParams } from 'umi';
import Record from '../../../appliance/FlowRecord/Record';
import type { IUriParams } from '../../typings';
import { ESourceType } from '../../typings';

interface ILongConnectionProps {
  location: {
    pathname: string;
  };
  dispatch: Dispatch;
  longConnectionSeconds: number;
  queryConnectionSettingLoading: boolean;
}
const LongConnection: React.FC<ILongConnectionProps> = ({
  location: { pathname },
  dispatch,
  longConnectionSeconds,
  queryConnectionSettingLoading,
}) => {
  // 加个标识，防止 Record 组件渲染2次
  const [isReady, setIsReady] = useState<boolean>(false);

  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();

  const sourceType: ESourceType = useMemo(() => {
    if (serviceId) {
      return ESourceType.SERVICE;
    }
    if (networkId) {
      return ESourceType.NETWORK;
    }
    return ESourceType.OFFLINE;
  }, [serviceId, networkId]);

  const queryLongConnectionSetting = useCallback(() => {
    dispatch({
      type: 'npmdModel/queryLongConnectionSetting',
      payload: {
        sourceType,
        networkId,
        packetFileId: pcapFileId,
        serviceId,
      },
    });
  }, [dispatch, networkId, pcapFileId, serviceId, sourceType]);

  useEffect(() => {
    if (!isReady) {
      setIsReady(true);
    }
  }, [isReady]);

  useEffect(() => {
    queryLongConnectionSetting();
  }, [queryLongConnectionSetting]);

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
        'ip',
        'ipv4',
        'ipv6',
        'port',
      ]}
      extraAction={
        <Button
          type="primary"
          icon={<SettingOutlined />}
          onClick={() => history.push(`${pathname}/setting`)}
        >
          长连接配置
        </Button>
      }
    />
  );
};

export default connect(
  ({ loading: { effects }, npmdModel: { longConnectionSeconds } }: ConnectState) => ({
    longConnectionSeconds,
    queryConnectionSettingLoading: effects['npmdModel/queryLongConnectionSetting'] || false,
  }),
)(LongConnection);
