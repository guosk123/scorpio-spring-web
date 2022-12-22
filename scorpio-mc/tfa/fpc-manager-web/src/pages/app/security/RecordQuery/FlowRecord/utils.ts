import type { IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IFlowRecordData } from '@/pages/app/appliance/FlowRecord/typings';
import { isIpv4 } from '@/utils/utils';

export const jumpPacketFromFlowRecord = (record: IFlowRecordData) => {
  const filter: IFilter[] = [];
  // 网络
  if (record.network_id.length > 0) {
    filter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: record.network_id[0],
    });
  }
  // 业务
  if (record.service_id.length > 0) {
    filter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: record.service_id[0],
    });
  }
  // IP地址
  if (record.ipv4_initiator) {
    filter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv4_initiator,
    });
  }
  if (record.ipv4_responder) {
    filter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv4_responder,
    });
  }
  if (record.ipv6_initiator) {
    filter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv6_initiator,
    });
  }
  if (record.ipv6_responder) {
    filter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv6_responder,
    });
  }

  if (
    record.ip_protocol &&
    ['TCP', 'UDP', 'ICMP', 'SCTP'].includes(record.ip_protocol.toLocaleUpperCase())
  ) {
    filter.push({
      field: 'ipProtocol',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ip_protocol.toLocaleUpperCase(),
    });
  }
  // 应用层协议
  if (record.l7_protocol_id) {
    filter.push({
      field: 'l7ProtocolId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.l7_protocol_id,
    });
  }
  // VLADID
  if (record.vlan_id) {
    filter.push({
      field: 'vlanId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.vlan_id,
    });
  }
  // 应用
  if (record.application_id) {
    // console.log(record.application_id, typeof record.application_id, record);
    filter.push({
      field: 'applicationId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.application_id,
    });
  }
  // 端口
  if (record.port_initiator) {
    filter.push({
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_initiator,
    });
  }
  if (record.port_responder) {
    filter.push({
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_responder,
    });
  }

  return filter;
};

export const getFlowRecordFilterString = (
  params: {
    srcIp?: string;
    destIp?: string;
    session?: { ipAAddress: string; ipBAddress: string };
    networkId?: string;
    serviceId?: string;
    ip?: string;
  } = {},
) => {
  const { srcIp, destIp, session, networkId, serviceId, ip } = params;

  const filter: IFilterCondition = [];

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

  if (networkId) {
    filter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId,
    });
  }

  if (serviceId) {
    filter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId,
    });
  }

  return encodeURIComponent(JSON.stringify(filter));
};
