/** 异常事件来源 */
export enum EAbnormalEventRuleSource {
  /** 系统内置 */
  'Default' = '0',
  /** 自定义 */
  'Custom' = '1',
}

/** 异常事件启用状态 */
export enum EAbnormalEventRuleStatus {
  /** 停用 */
  'Closed' = '0',
  /** 启用 */
  'Open' = '1',
}

export enum EABNORMAL_EVENT {
  /** 违规域名访问 */
  VIOLATION_DOMAIN_NAME = 1,
  /** 违规IP访问 */
  VIOLATION_IP = 11,
  /** 违规URL访问 */
  VIOLATION_URL = 21,

  /** 可疑域名 */
  SUSPICIOUS_DOMAIN_NAME = 2,
  /** 可疑IP */
  SUSPICIOUS_IP = 12,
  /** 可疑JA3 */
  SUSPICIOUS_JA3 = 41,

  /** 文件泄密风险 */
  RISK_DOCUMENT = 31,

  /** 非标准协议 */
  NON_STANDARD_PROTOCOL = 200,
  /** 共享上网 */
  SHARING_INTERNET = 201,
  /** ARP-扫描 */
  ARP_SCANNING = 202,
  /** ARP-欺骗 */
  ARP_DECEPTION = 203,

  /** DHCP-Dos攻击 */
  DHCP_DOS_ATTACK = 204,
  /** DHCP-假冒服务器 */
  DHCP_FAKE_SERVER = 205,
  /** DNS-Dos攻击 */
  DNS_DOS_ATTACK = 206,
  /** DNS-回复报文超长 */
  DNS_REPLY_LONG_MESSAGE = 207,
}

/** 异常事件消息的类别 */
export const ABNORMAL_EVENT_TYPE_ENUM = {
  [EABNORMAL_EVENT.VIOLATION_DOMAIN_NAME]: {
    label: '违规域名访问',
    placeholder: '请输入一个域名，例如：google.com',
  },
  [EABNORMAL_EVENT.VIOLATION_IP]: {
    label: '违规IP访问',
    placeholder: '请输入一个IPv4，例如：8.8.8.8',
  },
  [EABNORMAL_EVENT.VIOLATION_URL]: {
    label: '违规URL访问',
    placeholder: '请输入一个URL，例如：/index.php',
  },

  [EABNORMAL_EVENT.SUSPICIOUS_DOMAIN_NAME]: {
    label: '可疑域名',
    placeholder: '请输入一个域名，例如：google.com',
  },
  [EABNORMAL_EVENT.SUSPICIOUS_IP]: {
    label: '可疑IP',
    placeholder: '请输入一个IPv4，例如：8.8.8.8',
  },
  [EABNORMAL_EVENT.SUSPICIOUS_JA3]: {
    label: '可疑JA3',
    placeholder: '请输入一个JA3指纹，例如：ebf950a16f650142d0bbf8b663dfeb4a',
  },

  [EABNORMAL_EVENT.RISK_DOCUMENT]: {
    label: '文件泄密风险',
    placeholder: '请输入一个文件名称，例如：xxx研讨会议内容.docx',
  },
  // 以下的是
  [EABNORMAL_EVENT.NON_STANDARD_PROTOCOL]: { label: '非标准协议' },
  [EABNORMAL_EVENT.SHARING_INTERNET]: { label: '共享上网' },
  [EABNORMAL_EVENT.ARP_SCANNING]: { label: 'ARP-扫描' },
  [EABNORMAL_EVENT.ARP_DECEPTION]: { label: 'ARP-欺骗' },
  [EABNORMAL_EVENT.DHCP_DOS_ATTACK]: { label: 'DHCP-Dos攻击' },
  [EABNORMAL_EVENT.DHCP_FAKE_SERVER]: {
    label: 'DHCP-假冒服务器',
    placeholder: '请输入DHCP服务器的IPv4地址，用半角逗号分隔，最多支持10个',
  },
  [EABNORMAL_EVENT.DNS_DOS_ATTACK]: { label: 'DNS-Dos攻击' },
  [EABNORMAL_EVENT.DNS_REPLY_LONG_MESSAGE]: { label: 'DNS-回复报文超长' },
};

/** 异常事件消息的类别 */
export type AbnormalEventType = keyof typeof ABNORMAL_EVENT_TYPE_ENUM;

// export enum EAbnormalEventType {
//   '违规域名访问' = 1,
//   '可疑域名' = 2,
//   '违规IP访问' = 11,
//   '违规URL访问' = 21,
//   '文件泄密风险' = 31,
//   '可疑JA3' = 41,

//   '非标准协议' = 200,
//   '共享上网' = 201,
//   'ARP-扫描' = 202,
//   'ARP-欺骗' = 203,
//   'DHCP-Dos攻击' = 204,
//   'DHCP-假冒服务器' = 205,
//   'DNS-Dos攻击' = 206,
//   'DNS-回复报文超长' = 207,
// }

/** 异常事件规则 */
export interface IAbnormalEventRule {
  /** ID */
  id: string;
  /** 事件分类 */
  type: AbnormalEventType;
  /** 事件内容 */
  content: string;
  /** 事件来源（0：预置；1：自定义） */
  source: EAbnormalEventRuleSource;
  /** 启用状态（0：停用，1：启用） */
  status: EAbnormalEventRuleStatus;
  /** 描述 */
  description: string;
  /** 更新时间 */
  timestamp: string;
  /** 操作人ID */
  operatorId: string;
  /** 内容 */
  typeText: string;
}

/** 异常事件触发的消息 */
export interface IAbnormalEventMessage {
  /** 网络ID */
  networkId: string;
  /** 触发时间 */
  startTime: string;
  /** 类别 */
  type: AbnormalEventType;
  /** 内容 */
  content: string;
  /** 描述 */
  description: string;
  /** 源IP */
  srcIp: string;
  /** 目的IP */
  destIp: string;
  /** 目的端口 */
  destPort: number;
  /** 应用层协议ID */
  l7ProtocolId: number;
  /** 源国家ID */
  countryIdInitiator: number;
  /** 源省份ID */
  provinceIdInitiator: number;
  /** 源城市ID */
  cityIdInitiator: number;
  /** 目的国家ID */
  countryIdResponder: number;
  /** 目的省份ID */
  provinceIdResponder: number;
  /** 目的城市ID */
  cityIdResponder: number;
}

/** 异常事件统计维度 */
export enum EAbnormalEventMetricType {
  'TYPE' = 'type',
  'LOCATION_INITIATOR' = 'locationInitiator',
  'LOCATION_RESPONDER' = 'locationResponder',
}

/** 异常事件类型统计图返回值 */
export interface IAbnormalEventCountType {
  type: AbnormalEventType;
  count: number;
}

/** 异常事件攻击源目的所在地统计图返回值 */
export interface IAbnormalEventCountLocationResponder {
  /** 源城市ID */
  cityIdResponder: number;
  /** 目的国家ID */
  countryIdResponder: number;
  /** 目的省份ID */
  provinceIdResponder: number;
  count: number;
}

/** 异常事件攻击源所在地统计图返回值 */
export interface IAbnormalEventCountLocationInitiator {
  /** 源国家ID */
  countryIdInitiator: number;
  /** 源省份ID */
  provinceIdInitiator: number;
  /** 源城市ID */
  cityIdInitiator: number;
  count: number;
}
