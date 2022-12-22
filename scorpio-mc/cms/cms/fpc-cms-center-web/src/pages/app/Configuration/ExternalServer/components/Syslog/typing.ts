export interface ISyslogType {
  /** id */
  id: string;
  /** 名称 */
  name: string;
  /** 收件人 */
  protocol: string;
  /** IP */
  syslogServerIpAddress: string;
  /** 端口 */
  syslogServerPort: string;
  severity: string;
  facility: string;
  encodeType: string;
  separator: string;
}

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