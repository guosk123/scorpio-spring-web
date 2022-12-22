import type { EFieldOperandType, EFieldType, IEnumValue } from '@/components/FieldFilter/typings';
import type { ColumnProps } from 'antd/lib/table';

export interface IDashboardData {
  alarmTotalCount: number;
  topTargetHost: Record<string, number>;
  topOriginIp: Record<string, number>;
  topAlarmId: Record<string, number>;
  classificationProportion: Record<string, number>;
  alarmTrend: [number | string, number][];
  mitreTacticProportion: Record<string, number>;
}

export interface IMitreAttack {
  id: string;
  name: string;
  parentId: string;
  ruleSize: number;
  alertSize?: number;
}

export interface IRuleClasstype {
  name: string;
  id: string;
  ruleSize: number;
  alertSize?: number;
}

export interface ISuricataAlertMessage {
  sid: number;
  msg: string;
  classtypeId: string;
  mitreTacticId: string;
  mitreTechniqueId: string;
  cve: string;
  cnnvd: string;
  signatureSeverity: number;
  target: string;
  srcIpv4: string;
  srcIpv6: string;
  srcIp: string;
  srcPort: number;
  destIpv4: string;
  destIpv6: string;
  destIp: string;
  destPort: number;
  protocol: string;
  l7Procotol: string;
  flowId: string;
  domain: string;
  url: string;
  countryIdInitiator: string;
  provinceIdInitiator: string;
  cityIdInitiator: string;
  countryIdResponder: string;
  provinceIdResponder: string;
  cityIdResponder: string;
  source: string;
  tag: string;
  basicTag: string;
  timestamp: string;
}

export interface IColumnProps<Record> extends ColumnProps<Record> {
  /**
   * 搜索时的提示信息
   */
  searchTip?: string;

  /**
   * 是否在表格中显示
   */
  show?: boolean;

  disabled?: boolean;
  /**
   * 是否可以被搜索
   */
  searchable?: boolean;
  enumValue?: IEnumValue[];
  /**
   * 字段的类型
   */
  fieldType?: EFieldType;
  /**
   * 操作数类型
   */
  operandType?: EFieldOperandType;
}

export type SuricataStatisticsType =
  | 'top_target_host'
  | 'top_origin_ip'
  | 'top_alarm_id'
  | 'classification_proportion'
  | 'mitre_tactic_proportion'
  | 'top_mining_host'
  | 'top_mining_domain'
  | 'top_mining_pool_address'
  | 'top_attack_target_area'
  | 'top_attack_origin_area'
  | 'source_alarm_trend'
  | 'signature_severity'
  | 'basic_tag';

export type SuricataHistogramType = 'alarm_trend' | 'mining_alarm_trend';

export interface SuricataStatisticsResult {
  key: string;
  count: number;
}

export interface SuricataAlertEvent {
  srcRole: 'offender' | 'victim';
  destRole: 'offender' | 'victim';
  sid: number;
  timestamp: string;
  msg: string;
}
