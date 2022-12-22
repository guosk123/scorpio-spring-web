import Record from '@/pages/app/appliance/FlowRecords/Record';
import type { IFlowRecordEmbedProps } from '@/pages/app/appliance/FlowRecords/Record/typing';
import React from 'react';
import { clearShareInfo } from '@/pages/app/Network/components/EditTabs';
import { useContext, useEffect } from 'react';
import { RetransmissionContext } from '@/pages/app/Network/components/Retransmission';

interface ITcpRetransmissionProps {}

const TcpRetransmission: React.FC<ITcpRetransmissionProps> = () => {
  const [state, analysisDispatch] = useContext<any>(RetransmissionContext);

  useEffect(() => {
    clearShareInfo(analysisDispatch);
  }, [analysisDispatch]);
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

  return <Record {...recordProps} filterObj={state.shareInfo} />;
};

export default TcpRetransmission;
