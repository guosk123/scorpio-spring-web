import Record from '@/pages/app/appliance/FlowRecords/Record';
import { EFlag } from '@/pages/app/appliance/FlowRecords/typings';
import { ConnectionContext } from '@/pages/app/Network/components/Connection';
import { clearShareInfo } from '@/pages/app/Network/components/EditTabs';
import { useContext, useEffect } from 'react';

export default function TcpConnectError() {
  const [state, analysisDispatch] = useContext<any>(ConnectionContext);

  useEffect(() => {
    clearShareInfo(analysisDispatch);
  }, [analysisDispatch]);
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
        'tcp_session_state',
      ]}
      filterObj={state.shareInfo}
    />
  );
}
