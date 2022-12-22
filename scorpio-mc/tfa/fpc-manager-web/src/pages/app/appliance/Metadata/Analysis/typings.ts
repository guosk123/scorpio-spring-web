import type {
  DHCP_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_ENUM,
  DHCP_VERSION_ENUM,
  EARPType,
  EHttpAuthType,
  ESslAuthType,
} from '@/common/app';

/**
 * 元数据协议类型
 */
export enum EMetadataProtocol {
  'HTTP' = 'http',
  'DNS' = 'dns',
  'FTP' = 'ftp',
  'MAIL' = 'mail',
  'SMTP' = 'smtp',
  'POP3' = 'pop3',
  'IMAP' = 'imap',
  'TELNET' = 'telnet',
  'SSL' = 'ssl',
  'SSH' = 'ssh',
  'MYSQL' = 'mysql',
  'POSTGRESQL' = 'postgresql',
  'TNS' = 'tns',
  'ICMP' = 'icmp',
  'ICMPV4' = 'icmpv4',
  'ICMPV6' = 'icmpv6',
  'SOCKS5' = 'socks5',
  'SOCKS4' = 'socks4',
  'DHCP' = 'dhcp',
  'DHCPV6' = 'dhcpv6',
  'TDS' = 'tds',
  'ARP' = 'arp',
  'OSPF' = 'ospf',
}

export enum EMetadataTabType {
  OVERVIEW = 'overview',
  HTTP = 'http',
  HTTPANALYSIS = 'httpAnalysis',
  DNS = 'dns',
  FTP = 'ftp',
  MAIL = 'mail',
  SMTP = 'smtp',
  POP3 = 'pop3',
  IMAP = 'imap',
  TELNET = 'telnet',
  SIP = 'sip',
  SSL = 'ssl',
  SSH = 'ssh',
  MYSQL = 'mysql',
  POSTGRESQL = 'postgresql',
  TNS = 'tns',
  ICMP = 'icmp',
  ICMPV4 = 'icmpv4',
  ICMPV6 = 'icmpv6',
  ICMPV6ANALYSIS = 'icmpv6Analysis',
  SOCKS5 = 'socks5',
  SOCKS4 = 'socks4',
  DHCP = 'dhcp',
  DHCPANALYSIS = 'dhcpAnalysis',
  DHCPV6 = 'dhcpv6',
  DHCPV6ANALYSIS = 'dhcpv6Analysis',
  TDS = 'tds',
  ARP = 'arp',
  OSPF = 'ospf',
  LDAP = 'Ldap',
  DB2 = 'db2',
  FILE = 'file',
}

// 全部改为用ID判断
export const matadataDetailKV = {
  '1': EMetadataTabType.HTTP,
  '2': EMetadataTabType.DNS,
  '3': EMetadataTabType.SMTP,
  '4': EMetadataTabType.POP3,
  '5': EMetadataTabType.IMAP,
  '6': EMetadataTabType.FTP,
  '7': EMetadataTabType.TELNET,
  '8': EMetadataTabType.SSH,
  '9': EMetadataTabType.MYSQL,
  '10': EMetadataTabType.TNS,
  '11': EMetadataTabType.ICMPV4,
  '12': EMetadataTabType.SSL,
  '13': EMetadataTabType.SOCKS5,
  '14': EMetadataTabType.POSTGRESQL,
  '16': EMetadataTabType.DHCP,
  '19': EMetadataTabType.ICMPV6,
  '21': EMetadataTabType.DHCPV6,
  '22': EMetadataTabType.OSPF,
  '23': EMetadataTabType.ARP,
  '307': EMetadataTabType.SIP,
  '596': EMetadataTabType.LDAP,
  '645': EMetadataTabType.TDS,
  '650': EMetadataTabType.DB2,
  '843': EMetadataTabType.SOCKS4,
};

/**
 * 查询流日志列表的参数
 */
export interface IQueryMetadataParams {
  id?: string;
  protocol: EMetadataProtocol;
  keyword?: string;
  packetFileId?: string;
  page?: number;
  pageSize?: number;
  sortProperty?: string;
  sortDirection?: 'desc' | 'asc';
  startTime?: string;
  endTime?: string;
  /**
   * DSL 表达式查询
   */
  dsl?: string;
  sourceType?: string;
  entry?: string;
}

/**
 * 查询流日志详情的参数
 */
export interface IQueryMetadataDetailParams {
  id: string;
  protocol: EMetadataProtocol;
}

/**
 * 元数据协议
 */
export interface IL7Protocol {
  /** 协议 ID */
  protocolId: string;
  /** 英文名称 */
  name: string;
  /** 中文显示名称 */
  nameText: string;
  /** 英文备注 */
  description: string;
  /** 中文备注 */
  descriptionText: string;
}

export type IL7ProtocolMap = Record<string, IL7Protocol>;

/** 采集策略级别 */
export const METADATA_COLLECT_LEVEL_MAP = {
  '2': '高',
  '1': '中',
  '0': '低',
};

export type EMetadataCollectLevel = keyof typeof METADATA_COLLECT_LEVEL_MAP;

export interface IMetadataCommonField {
  /** 记录ID */
  id: string;
  /** 流ID */
  flowId: string;
  /**
   * 原业务中查询数据包可以使用，现在已经没有什么用处了
   * @deprecated
   */
  flowPacketId: string;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 归属网络 ID */
  networkId: string;
  /** 源IP */
  srcIp: string;
  /** 源端口 */
  srcPort: string;
  /** 目的IP */
  destIp: string;
  /** 目的端口 */
  destPort: string;
  /** 应用ID */
  applicationId: string;
  /** 采集策略名称 */
  policyName?: string;
  /** 采集策略级别 */
  level: EMetadataCollectLevel;
}

export const FILE_FLAG_MAP = {
  0: '非文件传输',
  1: '上传',
  2: '下载',
};
/** HTTP 文件传输方式 */
export type HttpFileFlag = keyof typeof FILE_FLAG_MAP;

/** HTTP加密方式 */
export const HTTP_DECRYPTED_MAP = {
  '0': '明文',
  '1': '密文',
};
/** HTTP加密方式 */
export type HttpDecrypted = keyof typeof HTTP_DECRYPTED_MAP;

export interface IMetadataHttp extends IMetadataCommonField {
  /** URL */
  uri: string;
  /** 方法 */
  method: string;
  /** status */
  status: string;

  // resquest header
  // ---------
  /** Accept-Encoding */
  acceptEncoding: string;
  /** Accept-Language */
  acceptLanguage: string;
  /** cookie */
  cookie: string;
  /** host */
  host: string;
  /** origin */
  origin: string;
  /** referer */
  referer: string;
  /** xff */
  xff: string;
  /** HTTP请求头内容 */
  requestHeader: string;
  /**
   * HTTP请求 Payload
   * @description 目前在界面上不显示
   */
  requestBody: string;

  // response header
  // ---------
  /** Content-Type */
  contentType: string;
  /** User-Agent */
  userAgent: string;
  /** 操作系统 */
  osVersion: string;
  /** 重定向至的地址 */
  location: string;
  /** set_cookie */
  setCookie: string;
  /**
   * HTTP应答头内容
   * @description 目前在界面上不显示
   */
  responseHeader: string;
  /**
   * HTTP应答内容
   * @description 目前在界面上不显示
   */
  responseBody: string;

  // 其他字段
  // --------
  /** 文件传输方式 */
  fileFlag: HttpFileFlag;
  /** 传输文件名称 */
  fileName: string;
  /** 传输文件类型 */
  fileType: string;
  /** Authorization */
  authorization: string;
  /** 认证方式 */
  authType: EHttpAuthType;
  /**
   * 加密方式
   * @value 0 - 明文
   * @value 1 - 密文
   */
  decrypted: HttpDecrypted;
}
export interface IMetadataDns extends IMetadataCommonField {
  /**
   * 应答
   * @value "[{\"name\":\"wenda.jisuanke.com\",\"ttl\":105,\"CNAME\":\"wenda.jisuanke.com.wswebpic.com\"}]"
   */
  answer: string;
  /**
   * DNS查询内容
   * @value '[{"domain":"wenda.jisuanke.com","len":18,"entropy":3.73,"class":1,"class_name":"IN","type":28,"type_name":"AAAA","valid":true,"abues":false}]'
   */
  dnsQueries: string;
  /** DNS协议返回码 */
  dnsRcode: string;
  /** DNS协议返回码名称 */
  dnsRcodeName: string;
  /**
   * 域名解析地址
   * @value ["52.209.139.119", "34.251.151.178", "54.171.224.102"]
   */
  domainAddress: string[];
  /** 子域名数量 */
  subdomainCount: number;
  /**
   * 事务ID - 目前界面没有展示
   * @description 五元组相同时请求/应答 对应的唯一标识
   */
  transactionId: string;
}
export interface IMetadataFtp extends IMetadataCommonField {
  /** 操作命令 */
  cmd: string;
  /** 操作序号 */
  cmdSeq: string;
  /** 操作结果 */
  reply: string;
  /** 登录用户 */
  user: string;
}
export interface IMetadataIcmp extends IMetadataCommonField {
  /** 详细信息 */
  result: string;
}

export enum EMailProtocol {
  'IMAP' = 'imap',
  'POP3' = 'pop3',
  'SMTP' = 'smtp',
}

export interface IMetadataMail extends IMetadataCommonField {
  /** 邮件ID */
  messageId: string;
  /** 协议 */
  protocol: EMailProtocol;
  /** 发送日期 */
  date: string;
  /** 主题 */
  subject: string;
  /** 发件人 */
  from: string;
  /** 收件人 */
  to: string;
  /** 抄送人 */
  cc: string;
  /** 密送人 */
  bcc: string;
  /** 邮件正文 */
  // content: string;
  /**
   * 附件名称
   * @description "名称:格式;名称:格式"
   */
  attachment: string;
}
export interface IMetadataMysql extends IMetadataCommonField {
  /** 用户名 */
  username: string;
  /** 数据库名称 */
  databaseName: string;
  /** sql命令 */
  cmd: string;
  /** 错误 */
  error: string;
  /** 响应时间(ms) */
  delaytime: number;

  // 下述字段目前界面上不展示了
  // ------
  /** 执行结果 */
  result: string;
  /** SSL加密套件 */
  cipherSuite: string;
  /** 客户端编码格式 */
  clientCharset: string;
  /** 证书使用者 */
  commonName: string;
  /** 证书发布者 */
  issuer: string;
  /** 客户端指纹 */
  ja3Client: string;
  /** 服务端指纹 */
  ja3Server: string;
  /** 密码  */
  password: string;
  /** 服务器编码格式 */
  serverCharset: string;
  /** 服务器名称 */
  serverName: string;
  /** 数据库版本 */
  serverVersion: string;
  /** 证书签名算法 */
  signatureAlgorithm: string;
  /** SSL版本 */
  sslVersion: string;
  /** 证书有效期 */
  validity: string;
}
export interface IMetadataPostgresql extends IMetadataCommonField {
  /** 用户名 */
  username: string;
  /** 数据库名称 */
  databaseName: string;
  /** sql命令 */
  cmd: string;
  /** 错误 */
  error: string;
  /** 响应时间(ms) */
  delaytime: number;

  // 下述字段目前界面上不展示了
  // ------
  /** 密码 */
  password: string;
  /** 执行结果 */
  result: string;
  /** 数据库版本 */
  serverVersion: string;
  /** 服务器编码格式 */
  serverCharset: string;
  /** 客户端编码格式 */
  clientCharset: string;
  /** 客户端指纹 */
  ja3Client: string;
  /** 服务端指纹 */
  ja3Server: string;
  /** SSL版本 */
  sslVersion: string;
  /** SSL加密套件 */
  cipherSuite: string;
  /** 证书签名算法 */
  signatureAlgorithm: string;
  /** 证书发布者 */
  issuer: string;
  /** 证书使用者 */
  commonName: string;
  /** 证书有效期 */
  validity: string;
}
export interface IMetadataSocks5 extends IMetadataCommonField {
  /** 密码 */
  password: string;
  /** 用户名 */
  username: string;
  /** 验证方式 */
  authMethod: string;
  /** 验证结果 */
  authResult: string;
  /** 操作命令 */
  cmd: string;
  /** 执行结果 */
  cmdResult: string;
  /** 地址类型 */
  atyp: string;
  /** 请求服务器地址 */
  bindAddr: string;
  /** 请求服务器端口 */
  bindPort: number;
}
export interface IMetadataSsh extends IMetadataCommonField {
  /** 客户端请求附带信息 */
  clientComments: string;
  /** 客户端软件 */
  clientSoftware: string;
  /** 客户端版本 */
  clientVersion: string;
  /** 服务器应答附带信息 */
  serverComments: string;
  /** 服务器密钥 */
  serverKey: string;
  /** 服务器密钥类 */
  serverKeyType: string;
  /** 服务器软件 */
  serverSoftware: string;
  /** 服务器版本 */
  serverVersion: string;
}
export interface IMetadataSsl extends IMetadataCommonField {
  /** 认证方式 */
  authType: ESslAuthType;
  /** 证书链长度 */
  certsLen: number;
  /** SSL加密套件 */
  cipherSuite: string;
  /** 客户端支持的加密套件 */
  clientCipherSuite: string[];
  /** 客户端SSL当前版本 */
  clientCurVersion: string;
  /** 客户端拓展支持类型 */
  clientExtensions: string[];
  /** 客户端支持最高版本 */
  clientMaxVersion: string;
  /** 证书使用者 */
  commonName: string;
  /** 证书发布者 */
  issuer: string;
  /** 客户端指纹 */
  ja3Client: string;
  /** 服务端指纹 */
  ja3Server: string;
  /** 服务器证书 Sha1值 */
  serverCertsSha1: string[];
  /** 服务器拓展支持类型 */
  serverExtensions: string[];
  /** 服务器名称 */
  serverName: string;
  /** 证书签名算法 */
  signatureAlgorithm: string;
  /** 证书有效期 */
  validity: string;
  /** SSL版本 */
  version: string;
}
export interface IMetadataTelnet extends IMetadataCommonField {
  /** 登录用户 */
  username: string;
  /** 登录密码 */
  password: string;
  /** 操作命令 */
  cmd: string;
  /** 操作结果 */
  reply: string;
}
export interface IMetadataTns extends IMetadataCommonField {
  /** 连接信息 */
  connectData: string;
  /** 连接结果 */
  connectResult: string;
  /** 数据库版本 */
  version: number | string;
  /** sql命令 */
  cmd: string;
  /** 错误信息 */
  error: string;
  /** 响应时间(ms) */
  delaytime: number;
}
export interface IMetadataDhcp extends IMetadataCommonField {
  /**
   *  dhcp版本
   */
  version: DHCP_VERSION_ENUM;
  /** 源IPv4 */
  srcIpv4: string;
  /** 源IPv6 */
  srcIpv6: string;
  /** 目的IPv4 */
  destIpv4: string;
  /** 目的IPv6 */
  destIpv6: string;
  /**
   * 源MAC地址
   */
  srcMac: string;
  /**
   * 目的MAC地址
   */
  destMac: DHCP_V6_MESSAGE_TYPE_ENUM | DHCP_MESSAGE_TYPE_ENUM;
  /**
   * 消息类型
   */
  messageType: string;
  /**
   * 事务ID
   */
  transactionId: number;
  /**
   * 请求参数列表
   */
  parameters: number[];
  /**
   * 分配的IPv4地址
   */
  offeredIpv4Address: string;
  /**
   * 分配的IPv6地址
   */
  offeredIpv6Address: string;
  /**
   * 请求字节数
   */
  upstreamBytes: number;
  /**
   * 应答字节数
   */
  downstreamBytes: number;
}
export interface IMetadataArp extends IMetadataCommonField {
  /** 源MAC地址 */
  srcMac: string;
  /** 目的MAC地址 */
  destMac: string;
  /** 报文类型 */
  type: EARPType;
}
export interface IMetadataTds extends IMetadataCommonField {
  /** sql命令 */
  cmd: string;
  /** 错误信息 */
  error: string;
  /** 响应时间(ms) */
  delaytime: number;
}

/** OSPF 消息类型 */
export enum EOspfMessageType {
  'Hello Packet' = 1,
  'DB Description',
  'LS Request',
  'LS Update',
  'LS Acknowledgement',
}
export interface IMetadataOspf extends IMetadataCommonField {
  version: '2' | '3';
  messageType: EOspfMessageType;
  packetLength: number;
  sourceOspfRouter: string;
  areaId: number;
  /** 通告IPv4地址 */
  linkStateIpv4Address: string[];
  /** 通告IPv6地址 */
  linkStateIpv6Address: string[];
  message: string;
}

/**
 * 元数据详情
 */
export type IMetadataLog =
  | IMetadataHttp
  | IMetadataDns
  | IMetadataFtp
  | IMetadataIcmp
  | IMetadataMail
  | IMetadataMysql
  | IMetadataPostgresql
  | IMetadataSocks5
  | IMetadataSsh
  | IMetadataSsl
  | IMetadataTelnet
  | IMetadataTns
  | IMetadataDhcp
  | IMetadataArp
  | IMetadataTds
  | IMetadataOspf;

/**
 * entry 字段
 */
export enum EMetadataNetworkProtocolEntry {
  'HTTP' = '应用层协议分析-HTTP详单',
  'DNS' = '应用层协议分析-DNS详单',
  'FTP' = '应用层协议分析-FTP详单',
  'MAIL' = '应用层协议分析-MAIL详单',
  'TELNET' = '应用层协议分析-TELNET详单',
  'SSL' = '应用层协议分析-SSL详单',
  'SSH' = '应用层协议分析-SSH详单',
  'MYSQL' = '应用层协议分析-MySQL详单',
  'POSTGRESQL' = '应用层协议分析-PostgreSQL详单',
  'TNS' = '应用层协议分析-TNS详单',
  'ICMP' = '应用层协议分析-ICMP详单',
  'SOCKS5' = '应用层协议分析-SOCKS5详单',
  'DHCP' = '应用层协议分析-DHCP详单',
  'DHCPV6' = '应用层协议分析-DHCPv6详单',
  'TDS' = '应用层协议分析-TDS详单',
  'ARP' = '应用层协议分析-ARP详单',
  'OSPF' = '应用层协议分析-OSPF详单',
}

export enum EMetadataScenarioProtocolEntry {
  'HTTP' = '场景分析结果-HTTP详单',
  'DNS' = '场景分析结果-DNS详单',
  'FTP' = '场景分析结果-FTP详单',
  'MAIL' = '场景分析结果-MAIL详单',
  'TELNET' = '场景分析结果-TELNET详单',
  'SSL' = '场景分析结果-SSL详单',
  'SSH' = '场景分析结果-SSH详单',
  'MYSQL' = '场景分析结果-MySQL详单',
  'POSTGRESQL' = '场景分析结果-PostgreSQL详单',
  'TNS' = '场景分析结果-TNS详单',
  'ICMP' = '场景分析结果-ICMP详单',
  'SOCKS5' = '场景分析结果-SOCK5详单',
  'DHCP' = '场景分析结果-DHCP详单',
  'DHCPV6' = '场景分析结果-DHCPv6详单',
  'TDS' = '场景分析结果-TDS详单',
  'ARP' = '场景分析结果-ARP详单',
  'OSPF' = '场景分析结果-OSPF详单',
}
