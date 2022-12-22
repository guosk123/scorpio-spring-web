import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { URLFilter, URLFilterGroup } from '@/pages/app/Network/Analysis/constant';
import { isIpv4 } from '@/utils/utils';

export const getFlowRecordFilter = (
  params: {
    srcIp?: string;
    destIp?: string;
    session?: { ipAAddress: string; ipBAddress: string };
    ip?: string;
  } = {},
) => {
  const { srcIp, destIp, session, ip } = params;

  const filter: (URLFilter | URLFilterGroup)[] = [];

  if (ip) {
    const isV4 = isIpv4(ip);

    filter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          field: isV4 ? 'ipv4_initiator' : 'ipv6_initiator',
          operator: EFilterOperatorTypes.EQ,
          operand: ip,
        },
        {
          field: isV4 ? 'ipv4_responder' : 'ipv6_responder',
          operator: EFilterOperatorTypes.EQ,
          operand: ip,
        },
      ],
    });
  }

  if (srcIp) {
    const isV4 = isIpv4(srcIp);

    filter.push({
      field: isV4 ? 'ipv4_initiator' : 'ipv6_initiator',
      operator: EFilterOperatorTypes.EQ,
      operand: srcIp,
    });
  }

  if (destIp) {
    const isV4 = isIpv4(destIp);

    filter.push({
      field: isV4 ? 'ipv4_responder' : 'ipv6_responder',
      operator: EFilterOperatorTypes.EQ,
      operand: destIp,
    });
  }

  if (session) {
    const { ipAAddress, ipBAddress } = session;

    const aIsV4 = isIpv4(ipAAddress);
    const bIsv4 = isIpv4(ipBAddress);

    filter.push({
      operator: EFilterGroupOperatorTypes.OR,
      group: [
        {
          operator: EFilterGroupOperatorTypes.AND,
          group: [
            {
              field: aIsV4 ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: ipAAddress,
            },
            {
              field: bIsv4 ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: ipBAddress,
            },
          ],
        },
        {
          operator: EFilterGroupOperatorTypes.AND,
          group: [
            {
              field: aIsV4 ? 'ipv4_responder' : 'ipv6_responder',
              operator: EFilterOperatorTypes.EQ,
              operand: ipAAddress,
            },
            {
              field: bIsv4 ? 'ipv4_initiator' : 'ipv6_initiator',
              operator: EFilterOperatorTypes.EQ,
              operand: ipBAddress,
            },
          ],
        },
      ],
    });
  }

  return filter;
};
