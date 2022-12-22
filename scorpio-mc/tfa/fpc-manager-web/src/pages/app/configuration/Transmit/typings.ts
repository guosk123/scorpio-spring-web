export enum EFacilityType {
  'kern' = 0,
  'user' = 1,
  'mail' = 2,
  'daemon' = 3,
  'auth' = 4,
  'syslog' = 5,
  'lpr' = 6,
  'news' = 7,
  'uucp' = 8,
}

export enum ESeverity {
  'emerg' = 0,
  'alert' = 1,
  'crit' = 2,
  'err' = 3,
  'warning' = 4,
  'notice' = 5,
  'info' = 6,
  'debug' = 7,
}

export enum EEncodeType {
  'UTF-8' = '0',
  'GB2312' = '1',
}
export enum ESyslogTransmitType {
  'Instant' = '0',
  'Timing' = '1',
  'Inhibit' = '2',
}

export enum ESyslogTransmitName {
  '即时发送' = 0,
  '定时发送' = 1,
  '抑制发送' = 2,
}

export const networkAlertMsgName = {
  networkName: '网络名称',
  name: '告警名称',
  category: '告警分类',
  level: '告警级别',
  alertDefine: '告警详情',
  ariseTime: '触发时间',
};

export const serviceAlertMsgName = {
  ...networkAlertMsgName,
  serviceName: '业务名称',
};
/** 网络告警消息内容 */
export const networkAlertMsgContentMap = new Map([
  ['networkName', '办公室流量'],
  ['name', 'test21_copy'],
  ['category', '阈值告警'],
  ['level', '提示'],
  [
    'alertDefine',
    '在2021-10-28 14:48:00 ~ 2021-10-28 15:48:00内，TCP新建会话数量指标数据计数为25,667',
  ],
  ['ariseTime', '2021-10-28 15:48:00'],
]);

/** 网络告警消息内容 */
export const serviceAlertMsgContentMap = new Map([
  ['serviceName', 'OA系统-10网段'],
  ['networkName', '办公室流量'],
  ['name', 'OA系统告警'],
  ['category', '阈值告警'],
  ['level', '紧急'],
  [
    'alertDefine',
    '在2021-11-12 15:55:00 ~ 2021-11-12 15:56:00内，流量大小（字节）指标数据计数为3,055。',
  ],
  ['ariseTime', '2021-11-12 15:56:00'],
]);

/** syslog外发配置 */
export interface ITransmitSyslog {
  id: string;
  name: string;
  syslogServerAddress: string;
  sendType: ESyslogTransmitType;
  interval?: number;
  threshold?: number;
  severity: ESeverity;
  facility: EFacilityType;
  encodeType: EEncodeType;
  networkAlertContent: string;
  serviceAlertContent: string;
  systemAlarmContent: string;
  systemLogContent: string;
  sendTime?: string;
}

/** mail外发配置 */
export interface ITransmitMail {
  id: string;
  mailTitle: string;
  receiver: string;
  cc: string;
  interval: number;
  networkAlertContent: string;
  serviceAlertContent: string;
  systemLogContent: string;
  systemAlarmContent: string;
}

/** SMTP表单参数 */
export interface ITransmitSmtp {
  id?: string;
  mailUsername: string;
  mailAddress: string;
  smtpServer: string;
  serverPort: number;
  /** (0-否；1-是) */
  encrypt: 0 | 1;
  loginUser: string;
  loginPassword: string;
}

export type TableField = {
  name: string;
  id: string;
  comment: string;
};
