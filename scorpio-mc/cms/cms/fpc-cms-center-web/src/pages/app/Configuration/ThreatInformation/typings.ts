export interface IThreatIntelligence {
  /** ID */
  id: string;

  iocType: string;

  iocRaw: string;

  basicTag: string;

  tag: string;

  intelType: string;
  /** 来源 */
  source: string;
  /** 时间 */
  time: string;
}

export enum IocType {
  Domain = 'domain',
  Ipv4 = 'ipv4',
}

export const IocTypeNameMap = {
  [IocType.Domain]: '域名',
  [IocType.Ipv4]: 'IPv4',
};
