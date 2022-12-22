import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { ConnectState } from '@/models/connect';
import type { IL7ProtocolMap } from '@/pages/app/appliance/Metadata/typings';
import type { URLFilter } from '@/pages/app/Network/Analysis/constant';
import { isIpv4 } from '@/utils/utils';
import { useSelector } from 'umi';

export default function useRecordToMetadataFilter(record: any) {
  const metadataFilter: URLFilter[] = [];
  const allL7ProtocolMap = useSelector<ConnectState, IL7ProtocolMap>(
    (state) => state.metadataModel.allL7ProtocolMap,
  );
  const srcIp = record.ipv4_initiator || record.ipv6_initiator;
  const destIp = record.ipv4_responder || record.ipv6_responder;
  if (record.network_id.length > 0) {
    metadataFilter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: record.network_id[0],
    });
  }

  // 如果目标元数据不区分v4、v6
  if (['SOCKS4', 'SIP'].includes(allL7ProtocolMap[record.l7_protocol_id]?.name)) {
    if (srcIp) {
      metadataFilter.push({
        field: 'src_ip',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      });
    }
    if (destIp) {
      metadataFilter.push({
        field: 'dest_ip',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      });
    }
  } else {
    if (srcIp) {
      metadataFilter.push({
        field: isIpv4(srcIp) ? 'src_ipv4' : 'src_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      });
    }
    if (destIp) {
      metadataFilter.push({
        field: isIpv4(destIp) ? 'dest_ipv4' : 'dest_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      });
    }
  }

  // 填充端口条件
  if (record.port_initiator) {
    metadataFilter.push({
      field: 'src_port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_initiator,
    });
  }
  if (record.port_responder) {
    metadataFilter.push({
      field: 'dest_port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_responder,
    });
  }
  return metadataFilter;
}
