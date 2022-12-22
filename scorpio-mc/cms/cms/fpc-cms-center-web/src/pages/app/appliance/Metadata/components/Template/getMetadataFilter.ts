import type { IFilter } from '@/components/FieldFilter/typings';
import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { isIpv4 } from '@/utils/utils';
import { EMetadataProtocol } from '../../typings';

export function getMetadataFilter(params: {
  srcIp?: string;
  destIp?: string;
  srcPort?: number;
  destPort?: number;
  protocol: EMetadataProtocol;
}) {
  const { srcIp, destIp, srcPort, destPort, protocol } = params;
  const filter: IFilter[] = [];
  // 不区分v4,v6的源、目的ip
  if (
    [
      EMetadataProtocol.FILE,
      EMetadataProtocol.LDAP,
      EMetadataProtocol.SIP,
      EMetadataProtocol.SOCKS4,
    ].includes(protocol)
  ) {
    if (srcIp) {
      filter.push({
        field: 'src_ip',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      });
    }
    if (destIp) {
      filter.push({
        field: 'dest_ip',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      });
    }
  } else {
    // 区分v4,v6的源目地ip
    if (srcIp) {
      filter.push({
        field: isIpv4(srcIp) ? 'src_ipv4' : 'src_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: srcIp,
      });
    }
    if (destIp) {
      filter.push({
        field: isIpv4(destIp) ? 'dest_ipv4' : 'dest_ipv6',
        operator: EFilterOperatorTypes.EQ,
        operand: destIp,
      });
    }
  }

  // 源端口
  if (srcPort) {
    filter.push({
      field: 'src_port',
      operator: EFilterOperatorTypes.EQ,
      operand: srcPort,
    });
  }

  // 目的端口
  if (destPort) {
    filter.push({
      field: 'dest_port',
      operator: EFilterOperatorTypes.EQ,
      operand: destPort,
    });
  }

  return filter;
}
