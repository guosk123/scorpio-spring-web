// 流量分析下的所有公共字段

import { EMetricApiType } from '@/common/api/analysis';

export type TFlowAnalysisType = Exclude<
  EMetricApiType,
  EMetricApiType.DHCP | EMetricApiType.netif | EMetricApiType.network
>;

// TODO: 确认注释掉的字段是否需要屏蔽
export const flowCommonFields: readonly string[] = [
  'networkId',
  'serviceId',
  'totalBytes',
  'downstreamBytes',
  'upstreamBytes',
  'totalPackets',
  'downstreamPackets',
  'upstreamPackets',
  'establishedSessions',
  // 'tcpClientNetworkLatency',
  // 'tcpClientNetworkLatencyCounts',
  // 'tcpServerNetworkLatencyCounts',
  // 'serverResponseLatency',
  'bytepsAvg',
  'tcpServerNetworkLatencyAvg',
  'tcpClientNetworkLatencyAvg',
  'serverResponseLatencyAvg',
  // 'serverResponseLatencyCounts',
  'tcpClientZeroWindowPackets',
  'tcpServerZeroWindowPackets',
  'tcpEstablishedSuccessCounts',
  'tcpEstablishedFailCounts',
  'tcpClientRetransmissionPackets',
  'tcpClientPackets',
  'tcpServerRetransmissionPackets',
  'tcpServerPackets',
  'tcpEstablishedSuccessRate',
] as const;

export const flowSubFields: Record<TFlowAnalysisType, readonly string[]> = {
  [EMetricApiType.location]: [
    'countryId',
    'provinceId',
    'cityId',
    'totalPayloadBytes',
    'downstreamPayloadBytes',
    'upstreamPayloadBytes',
    'totalPayloadPackets',
    'downstreamPayloadPackets',
    'upstreamPayloadPackets',
    'tcpSynPackets',
    'tcpSynAckPackets',
    'tcpSynRstPackets',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.application]: [
    'applicationId',
    'categoryId',
    'subcategoryId',
    'type',
    'totalPayloadBytes',
    'downstreamPayloadBytes',
    'upstreamPayloadBytes',
    'totalPayloadPackets',
    'downstreamPayloadPackets',
    'upstreamPayloadPackets',
    'tcpSynPackets',
    'tcpSynAckPackets',
    'tcpSynRstPackets',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.protocol]: [
    'l7ProtocolId',
    'totalPayloadBytes',
    'downstreamPayloadBytes',
    'upstreamPayloadBytes',
    'totalPayloadPackets',
    'downstreamPayloadPackets',
    'upstreamPayloadPackets',
    'tcpSynPackets',
    'tcpSynAckPackets',
    'tcpSynRstPackets',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.port]: [
    'port',
    'ipProtocol',
    'totalPayloadBytes',
    'downstreamPayloadBytes',
    'upstreamPayloadBytes',
    'totalPayloadPackets',
    'downstreamPayloadPackets',
    'upstreamPayloadPackets',
    'tcpSynPackets',
    'tcpSynAckPackets',
    'tcpSynRstPackets',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.hostGroup]: [
    'hostgroupId',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.macAddress]: [
    'macAddress',
    'ethernetType',
    // 'tcpServerNetworkLatency',
  ] as const,
  [EMetricApiType.ipAddress]: [
    // 'macAddress',
    'ipAddress',
    'ipLocality',
    'activeEstablishedSessions',
    'passiveEstablishedSessions',
  ] as const,
  [EMetricApiType.ipConversation]: [
    'ipAAddress',
    'ipBAddress',
    'activeEstablishedSessions',
    'passiveEstablishedSessions',
    // 'tcpServerNetworkLatency',
  ] as const,
};

export type CommonFields = typeof flowCommonFields[number];
export type LocationUniqueFields = typeof flowSubFields[EMetricApiType.location][number];
export type AppUniqueFields = typeof flowSubFields[EMetricApiType.application][number];
export type ProtocolUniqueFields = typeof flowSubFields[EMetricApiType.protocol][number];
export type PortUniqueFields = typeof flowSubFields[EMetricApiType.port][number];
export type HostGroupUniqueFields = typeof flowSubFields[EMetricApiType.hostGroup][number];
export type MacAddressUniqueFields = typeof flowSubFields[EMetricApiType.macAddress][number];
export type IpAddressUniqueFields = typeof flowSubFields[EMetricApiType.ipAddress][number];
export type IpConversationUniqueFields =
  typeof flowSubFields[EMetricApiType.ipConversation][number];
