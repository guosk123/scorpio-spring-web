import type { ISegmentItemBaseType } from "../components/Segment/typing";
import { ESegmentItemUnit } from "../components/Segment/typing";

export enum EsegmentAnalysisSearchType {
  IPADDRESS = 'ip',
  APPLICATION = 'application',
  PORT = 'port',
  IPCONVERSATION = 'ipConversation',
  BUSINESS = 'business',
}


export const EsegmentAnalysisInterfaceMap = {
  [EsegmentAnalysisSearchType.IPADDRESS]: 'l3-devices',
  [EsegmentAnalysisSearchType.APPLICATION]: 'applications',
  [EsegmentAnalysisSearchType.PORT]: 'ports',
  [EsegmentAnalysisSearchType.IPCONVERSATION]: 'ip-conversations',
  [EsegmentAnalysisSearchType.BUSINESS]: 'services',
};

export const EsegmentAnalysisTypeToFlowFilterMap = {
  [EsegmentAnalysisSearchType.IPADDRESS]: 'ip_address',
  [EsegmentAnalysisSearchType.APPLICATION]: 'application_id',
  [EsegmentAnalysisSearchType.PORT]: 'port',
  [EsegmentAnalysisSearchType.IPCONVERSATION]: 'ip_address',
  [EsegmentAnalysisSearchType.BUSINESS]: 'service_id',
};


export interface ISearchType {
  name: EsegmentAnalysisSearchType;
  title: string;
}

export const SegmentAnalysisSearchMapping: Record<string, ISearchType> = {
  ipAddress: { name: EsegmentAnalysisSearchType.IPADDRESS, title: 'IP地址' },
  application: { name: EsegmentAnalysisSearchType.APPLICATION, title: '应用' },
  port: { name: EsegmentAnalysisSearchType.PORT, title: '端口' },
  ipConversation: { name: EsegmentAnalysisSearchType.IPCONVERSATION, title: 'IP会话' },
  business: { name: EsegmentAnalysisSearchType.BUSINESS, title: '业务' },
};

export interface INetworkDetails {
  //网络ID
  networkId: string;
  //网络组ID
  networkGroupId: string;
  //总字节数
  totalBytes: number;
  //总包数
  totalPackets: number;
  //新建会话数
  establishedSessions: number;
  //TCP客户端网络总时延
  tcpClientNetworkLatency: number;
  //TCP客户端网络时延统计次数
  tcpClientNetworkLatencyCounts: number;
  //TCP服务端网络总时延
  tcpServerNetworkLatency: number;
  //TCP服务端网络时延统计次数
  tcpServerNetworkLatencyCounts: number;
  //服务端响应总时延
  serverResponseLatency: number;
  //服务端响应时延统计次数
  serverResponseLatencyCounts: number;
  //TCP客户端总包数
  tcpClientPackets: number;
  // TCP客户端重传包数
  tcpClientRetransmissionPackets: number;
  //TCP服务端总包数
  tcpServerPackets: number;
  // TCP服务端重传包数
  tcpServerRetransmissionPackets: number;
  //客户端零窗口包数
  tcpClientZeroWindowPackets: number;
  //服务端零窗口包数
  tcpServerZeroWindowPackets: number;
  // 客户端TCP重传率
  tcpClientRetransmissionRate: number;  
  // 服务端TCP重传率
  tcpServerRetransmissionRate: number;
  //TCP建连成功数
  tcpEstablishedSuccessCounts: number;
  //TCP建连失败数
  tcpEstablishedFailCounts: number;
}

export interface INetworkSegmentDetails {
  tcpClientRetransmissionPackets: number;
  tcpClientRetransmissionRate: number;
  tcpServerRetransmissionPackets: number;
  tcpServerRetransmissionRate: number;
  tcpClientNetworkLatency: number;
  tcpServerNetworkLatency: number;
}

export const NetworkDetailNameMap: Record<string, string> = {
  tcpClientRetransmissionPackets: '客户端TCP重传包数',
  tcpClientRetransmissionRate: '客户端TCP重传率',
  tcpServerRetransmissionPackets: '服务端TCP重传包数',
  tcpServerRetransmissionRate: '服务端TCP重传率',
  tcpClientNetworkLatency: '客户端网络时延',
  tcpServerNetworkLatency: '服务端网络时延',
};

export const SEGMENT_ITEM_LIST: ISegmentItemBaseType[] = [
  {
    label: '客户端TCP重传包数',
    index: 'tcpClientRetransmissionPackets',
    unit: ESegmentItemUnit.COUNT,
  },
  {
     label: '客户端TCP重传率',
     index: 'tcpClientRetransmissionRate',
     unit: ESegmentItemUnit.RATE,
  },
  {
    label: '服务端TCP重传包数',
    index: 'tcpServerRetransmissionPackets',
    unit: ESegmentItemUnit.COUNT,
  },
  {
    label: '服务端TCP重传率',
    index: 'tcpServerRetransmissionRate',
    unit: ESegmentItemUnit.RATE,
  },
  {
    label: '客户端网络时延',
    index: 'tcpClientNetworkLatency',
    unit: ESegmentItemUnit.MILLI,
  },
  {
    label: '服务端网络时延',
    index: 'tcpServerNetworkLatency',
    unit: ESegmentItemUnit.MILLI
  },
];