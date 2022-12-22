import type { IEnumValue } from '../typings';

export const enum2List = (enumValue: any): IEnumValue[] =>
  Object.keys(enumValue)
    .filter((value: any) => isNaN(Number(value)) === false)
    .map((key) => ({
      text: enumValue[key],
      value: key,
    }));

/**
 * tcp会话状态
 */
export enum TCP_SESSION_STATE_ENUM {
  'SYN_SENT' = 0,
  'SYN_RCVD',
  'SYN_RST',
  'ESTABLISHED',
  'FIN_WAIT',
  'RST_CLOSED',
  'CLOSED',
  /** 引擎端把非TCP会话都报 （7：NULL） 状态 */
  'NULL',
}

export const TCP_SESSION_STATE_LIST = enum2List(TCP_SESSION_STATE_ENUM);

/**
 * eth类型
 */
export enum ETHERNET_TYPE_ENUM {
  'ARP' = 0,
  'IEEE802.1x',
  'IPv4',
  'IPv6',
  'IPX',
  'LACP',
  'MPLS',
  'STP',
  'Other',
}
export const ETHERNET_TYPE_LIST = enum2List(ETHERNET_TYPE_ENUM);

/**
 * IP地址的位置
 */
export enum IP_ADDRESS_LOCALITY_ENUM {
  '内网' = 0,
  '外网',
}

export const IP_ADDRESS_LOCALITY_LIST = enum2List(IP_ADDRESS_LOCALITY_ENUM);

/**
 * 应用统计中的应用分类
 */
export enum ANALYSIS_APPLICATION_TYPE_ENUM {
  '分类' = 0,
  '子分类',
  '应用',
}

/**
 * 端口统计中的传输层协议
 */
export enum ANALYSIS_PORT_IP_PROTOCOL_ENUM {
  'TCP' = 0,
  'UDP',
}

export const ANALYSIS_PORT_IP_PROTOCOL_LIST = enum2List(ANALYSIS_PORT_IP_PROTOCOL_ENUM);

/**
 * ICMP版本
 */
export enum ICMP_VERSION_ENUM {
  'ICMPv4' = 0,
  'ICMPv6',
}

/**
 * DHCP版本
 */
export enum DHCP_VERSION_ENUM {
  'DHCP' = 0,
  'DHCPv6',
}

export const DHCP_VERSION_LIST = enum2List(DHCP_VERSION_ENUM);

/**
 * DHCP V6版本下的消息类型
 */
export enum DHCP_V6_MESSAGE_TYPE_ENUM {
  'Solicit' = 1,
  'Advertise',
  'Request',
  'Confirm',
  'Renew',
  'Rebind',
  'Reply',
  'Release',
  'Decline',
  'Reconfigure',
  'Information request',
  'Relay forw',
  'Relay reply',
  'Leasequery',
  'Leasequery reply',
  'Leasequery done',
  'Leasequery data',
  'Reconfigure request',
  'Reconfigure reply',
  'Dhcpv4 query',
  'Dhcpv4 response',
  'Activeleasequery',
  'Starttls',
  'Bndupd',
  'Bndreply',
  'Poolreq',
  'Poolresp',
  'Updreq',
  'Updreqall',
  'Upddone',
  'Connect',
  'Connectreply',
  'Disconnect',
  'State',
  'Contact',
}

/**
 * DHCP V6版本下的消息类型 列表
 */
export const DHCP_V6_MESSAGE_TYPE_LIST = enum2List(DHCP_V6_MESSAGE_TYPE_ENUM);

/**
 * DHCP版本下的消息类型
 */
export enum DHCP_MESSAGE_TYPE_ENUM {
  'Discover' = 1,
  'Offer',
  'Request',
  'Decline',
  'ACK',
  'NAK',
  'Release',
  'Inform',
  'Force Renew',
  'Lease query',
  'Lease Unassigned',
  'Lease Unknown',
  'Lease Active',
  'Bulk Lease Query',
  'Lease Query Done',
  'Active LeaseQuery',
  'Lease Query Status',
  'TLS',
}

/**
 * DHCP版本下的消息类型 列表
 */
export const DHCP_MESSAGE_TYPE_LIST = enum2List(DHCP_MESSAGE_TYPE_ENUM);

/**
 * 支持解析的传输层协议列表
 */
export const IP_PROTOCOL_LIST = [
  'icmp',
  'igmp',
  'ggp',
  'ipip',
  'stream',
  'tcp',
  'cbt',
  'egp',
  'igp',
  'bbn_rcc',
  'nvpii',
  'pup',
  'argus',
  'emcon',
  'xnet',
  'chaos',
  'udp',
  'multiplexing',
  'dcnmeas',
  'hmp',
  'prm',
  'idp',
  'rdp',
  'irt',
  'tp',
  'bulk',
  'mfe-nsp',
  'merit',
  'dccp',
  '3pc',
  'idpr',
  'xtp',
  'ddp',
  'cmtp',
  'tppp',
  'il',
  'sdrp',
  'idrp',
  'rsvp',
  'gre',
  'dsr',
  'bna',
  'esp',
  'ah',
  'i-nslp',
  'swipe',
  'narp',
  'mobile',
  'tlsp',
  'icmpv6',
  'cftp',
  'sat-expak',
  'kryptolan',
  'rvd',
  'ippc',
  'sat-mon',
  'visa',
  'ipcv',
  'cpnx',
  'cphb',
  'wsn',
  'pvp',
  'br-sat-mon',
  'sun-nd',
  'wb-mon',
  'wb-expak',
  'iso-ip',
  'vmtp',
  'svmtp',
  'vines',
  'ttp',
  'nsfnet-igp',
  'dgp',
  'tcf',
  'eigrp',
  'ospf',
  'sprite',
  'larp',
  'mtp',
  'ax.25',
  'ipinip',
  'micp',
  'scc-sp',
  'etherip',
  'encap',
  'gmtp',
  'ifmp',
  'pnni',
  'pim',
  'aris',
  'scps',
  'qnx',
  'a/n',
  'ipcomp',
  'snp',
  'compaq',
  'ipx',
  'vrrp',
  'pgm',
  'l2tp',
  'ddx',
  'iatp',
  'stp',
  'srp',
  'uti',
  'smp',
  'sm',
  'ptp',
  'isis',
  'fire',
  'crtp',
  'crudp',
  'sscopmce',
  'iplt',
  'sps',
  'pipe',
  'sctp',
  'fc',
  'rsvpe2ei',
  'mipv6',
  'udplite',
  'mpls-in-ip',
  'manet',
  'hip',
  'shim6',
  'wesp',
  'rohc',
  'ax/4000',
  'ncs_hearbeat',
];

/** 传输层协议列表转为过滤器中的枚举列表 */
export const IP_PROTOCOL_ENUM_LIST = IP_PROTOCOL_LIST.map((item) => ({
  text: item.toUpperCase(),
  value: item,
}));

/** HTTP 认证方式枚举 */
export enum EHttpAuthType {
  '未知认证类型' = 0,
  '基本认证',
  '摘要认证',
  'WSSE认证',
  '令牌身份验证',
  'JSON网络令牌认证',
  'COOKIE认证',
}
/** HTTP 认证方式 */
export const HTTP_AUTH_TYPE_LIST = enum2List(EHttpAuthType);

/** SSL 认证方式 */
export enum ESslAuthType {
  '单向认证' = 0,
  '双向认证',
}

export enum ESslReuseTag {
  '否' = 0,
  '是',
}

export const SSL_AUTH_TYPE_LIST = enum2List(ESslAuthType);

export const SSL_REUSE_LIST = enum2List(ESslReuseTag);

export enum SEC_PROTO {
  'false' = 0,
  'true' = 1,
}
export const SEC_PROTO_LIST = enum2List(SEC_PROTO);

/** 操作系统类型 */
export const OS_VERSION_LIST = [
  'Chrome OS',
  'Windows NT 3.1',
  'Windows NT 3.5',
  'Windows NT 3.51',
  'Windows NT 4.0',
  'Windows 2000',
  'Windows XP',
  'windows 2003',
  'Windows Vista',
  'windows 7',
  'Windows 8.1',
  'Windows 8',
  'Windows 10',
  'Windows 98',
  'Windows 95',
  'AmigaOS',
  'Mac OS',
  'Linux',
  'OpenBSD',
  'BeOS',
  'Haiku',
  'Solaris',
  'NetBSD',
  'FreeBSD',
  'SymbOS',
  'IOS',
  'BlackBerry OS',
  'IOS',
  'Windows Phone',
  'Android',
];

/** ARP 报文类型 */
export enum EARPType {
  /** 请求报文 */
  'request' = 1,
  /** 回复报文 */
  'reply',
  /** 免费ARP */
  'gratuitous',
}
export const ARP_TYPE_LIST = enum2List(EARPType);
