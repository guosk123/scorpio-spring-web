import React from 'react';
import type { IFlowRecordEmbedProps } from '../../appliance/FlowRecord/Record';
import Record from '../../appliance/FlowRecord/Record';

interface ITcpRetransmissionProps {
  location: {
    query: { filter?: string; [propName: string]: any };
  };
}

const TcpRetransmission: React.FC<ITcpRetransmissionProps> = () => {
  const recordProps: IFlowRecordEmbedProps = {
    tableKey: 'analysis-tcp-retransmission-table',
    filterHistoryKey: 'analysis-tcp-retransmission-filter-history',
    extraDsl: '(tcp_client_retransmission_packets > 0 || tcp_server_retransmission_packets > 0)',
    displayMetrics: [
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
      'upstream_bytes',
      'downstream_bytes',
      'upstream_packets',
      'downstream_packets',
      'tcp_client_retransmission_packets',
      'tcp_client_retransmission_rate',
      'tcp_server_retransmission_packets',
      'tcp_server_retransmission_rate',
      'tcp_established_success_flag',
    ],
  };

  return <Record {...recordProps} />;
};

export default TcpRetransmission;
