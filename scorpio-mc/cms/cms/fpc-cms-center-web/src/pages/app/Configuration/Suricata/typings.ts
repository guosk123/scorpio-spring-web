export enum ERuleDirection {
  源到目的 = '->',
  双向 = '<>',
}

export enum ERuleTarget {
  源 = 'src_ip',
  目的 = 'dest_ip',
}

export interface ISuricataRule {
  // 数据库的的自增id
  id: string;
  // suricata 规则id， 由用户填写
  sid: number;
  // 规则描述
  msg: string;
  content: string;
  action: string;
  // 规则分类
  classtypeId: string;
  // 战术策略
  mitreTacticId: string;
  // 技术分类
  mitreTechniqueId?: string;
  // cve编号
  cve?: string;
  // cnnvd编号
  cnnvd?: string;
  // 优先级
  priority: number;
  // 严重级别
  signatureSeverity?: string;
  // 受害方
  target: string;
  protocol?: string;
  srcIp: string;
  srcPort: string;
  destIp: string;
  destPort: string;
  direction: string;
  state?: string;
  source: string;
  parseState?: string;
  threshold?: string;
  parseLog?: string;
  parseStateText?: string;
  operatorId: string;
}

export enum ERuleProtocol {
  IP = 'ip',
  TCP = 'tcp',
  UDP = 'udp',
  ICMP = 'icmp',
  HTTP = 'http',
  FTP = 'ftp',
  TLS = 'tls',
  SMB = 'smb',
  DNS = 'dns',
  DCERPC = 'dcerpc',
  SSH = 'ssh',
  SMTP = 'smtp',
  IMAP = 'imap',
  MODBUS = 'modbus',
  DNP3 = 'dnp3',
  ENIP = 'emip',
  NFS = 'nfs',
  IKEV2 = 'ikev2',
  KRB5 = 'krb5',
  NTP = 'ntp',
  DHCP = 'dhcp',
  RFB = 'rfb',
  RDP = 'rdp',
  SNMP = 'snmp',
  TFTP = 'tftp',
  SIP = 'sip',
  HTTP2 = 'http2',
}

export enum ERuleSource {
  自定义 = '1',
  系统内置 = '0',
}

export const ruleKeywords = ['自定义', '系统内置'];

export enum ERuleState {
  启用 = '1',
  停用 = '0',
}

export enum ERuleParseState {
  未解析 = '0',
  解析成功 = '1',
  解析失败 = '2',
}

export enum ERuleSignatureSeverity {
  紧急 = '0',
  严重 = '1',
  一般 = '2',
  提示 = '3',
}
