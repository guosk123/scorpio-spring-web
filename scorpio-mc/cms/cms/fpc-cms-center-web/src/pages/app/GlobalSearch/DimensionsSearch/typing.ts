export enum EDimensionsSearchType {
  IPADDRESS = 'ip',
  APPLICATION = 'application',
  L7PROTOCOLID = 'protocol',
  PORT = 'port',
  IPCONVERSATION = 'ipConversation',
  IPSEGMENT = 'ipSegment',
  LOCATION = 'location',
}

export const DimensionsTypeToFlowFilterMap = {
  [EDimensionsSearchType.IPADDRESS]: 'ip_address',
  [EDimensionsSearchType.APPLICATION]: 'application_id',
  [EDimensionsSearchType.L7PROTOCOLID]: 'l7_protocol_id',
  [EDimensionsSearchType.PORT]: 'port',
  [EDimensionsSearchType.IPCONVERSATION]: 'ip_address',
  [EDimensionsSearchType.LOCATION]: ['country_id', 'province_id', 'city_id'],
};

export interface ISearchType {
  name: EDimensionsSearchType;
  title: string;
}

export const DimensionsSearchMapping: Record<string, ISearchType> = {
  ipAddress: { name: EDimensionsSearchType.IPADDRESS, title: 'IP地址' },
  application: { name: EDimensionsSearchType.APPLICATION, title: '应用' },
  l7ProtocolId: { name: EDimensionsSearchType.L7PROTOCOLID, title: '应用层协议' },
  port: { name: EDimensionsSearchType.PORT, title: '端口' },
  ipConversation: { name: EDimensionsSearchType.IPCONVERSATION, title: 'IP会话' },
  // ipSegment: { name: EDimensionsSearchType.IPSEGMENT, title: ' IP地址段' },
  location: { name: EDimensionsSearchType.LOCATION, title: '地区' },
};

export enum EDRILLDOWN {
  NOTDRILLDOWN = '0',
  ISDRILLDOWN = '1',
}
