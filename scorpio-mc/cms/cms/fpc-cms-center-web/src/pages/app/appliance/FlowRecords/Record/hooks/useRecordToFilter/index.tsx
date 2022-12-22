import { EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { URLFilter } from '@/pages/app/Network/Analysis/constant';
import { useParams } from 'umi';

export default function useRecordToFilter(record: any) {
  const { networkId, serviceId }: IUriParams = useParams();
  // const recordToMatadataParam = {
  //   src_ipv4: 'src_ipv4',
  //   src_ipv6: 'src_ipv6',
  //   dest_ipv4: 'dest_ipv4',
  //   dest_ipv6: 'dest_ipv6',
  //   src_port: 'src_port',
  //   dest_port: 'dest_port',

  // }

  // 起止时间+五元组+vlanID
  const packetFilter: URLFilter[] = [];
  // 填充 IP 条件
  if (record.ipv4_initiator) {
    packetFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv4_initiator,
    });
  }
  if (record.ipv4_responder) {
    packetFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv4_responder,
    });
  }
  if (record.ipv6_initiator) {
    packetFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv6_initiator,
    });
  }
  if (record.ipv6_responder) {
    packetFilter.push({
      field: 'ipAddress',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ipv6_responder,
    });
  }
  // 填充端口条件
  if (record.port_initiator !== undefined) {
    packetFilter.push({
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_initiator,
    });
  }
  if (record.port_responder !== undefined) {
    packetFilter.push({
      field: 'port',
      operator: EFilterOperatorTypes.EQ,
      operand: record.port_responder,
    });
  }
  if (
    record.ip_protocol &&
    ['TCP', 'UDP', 'ICMP', 'SCTP'].includes(record.ip_protocol.toLocaleUpperCase())
  ) {
    packetFilter.push({
      field: 'ipProtocol',
      operator: EFilterOperatorTypes.EQ,
      operand: record.ip_protocol.toLocaleUpperCase(),
    });
  }
  // 应用层协议
  if (record.l7_protocol_id) {
    packetFilter.push({
      field: 'l7ProtocolId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.l7_protocol_id,
    });
  }
  // VLADID
  if (record.vlan_id) {
    packetFilter.push({
      field: 'vlanId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.vlan_id,
    });
  }

  // 应用
  if (record.application_id && record.application_id !== ('undefined' as any)) {
    packetFilter.push({
      field: 'applicationId',
      operator: EFilterOperatorTypes.EQ,
      operand: record.application_id,
    });
  }

  // 网络、业务信息
  if (networkId) {
    packetFilter.push({
      field: 'network_id',
      operator: EFilterOperatorTypes.EQ,
      operand: networkId || '',
    });
  }

  if (serviceId) {
    packetFilter.push({
      field: 'service_id',
      operator: EFilterOperatorTypes.EQ,
      operand: serviceId || '',
    });
  }
  return packetFilter;
}
