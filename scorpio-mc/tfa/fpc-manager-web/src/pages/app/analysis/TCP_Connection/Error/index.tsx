import React from 'react';
import Record from '../../../appliance/FlowRecord/Record';
import { EFlag } from '../../../appliance/FlowRecord/typings';

interface ITcpConnectErrorProps {
  location: {
    query: { filter?: string; [propName: string]: any };
  };
}

const TcpConnectError: React.FC<ITcpConnectErrorProps> = () => {
  return (
    <Record
      tableKey="tcp-connection-error-table"
      filterHistoryKey="tcp-connection-error-filter-history"
      extraDsl={`tcp_established_fail_flag=${EFlag.True}`}
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
        'ip',
        'ipv4',
        'ipv6',
        'port',
        'tcp_session_state',
      ]}
    />
  );
};

export default TcpConnectError;
