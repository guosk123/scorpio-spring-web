/* eslint-disable no-restricted-syntax */
/**
 * 系统告警指标
 */
export enum ESystemAlertSourceType {
  'CPU' = 'CPU',
  'MEMORY' = 'MEMORY',
  'DISK' = 'DISK',
}

export enum ESystemAlertMetric {
  CPU_USAGE = 'cpu_usage',
  MEMORY_USAGE = 'memory_usage',
  FS_SYSTEM_FREE = 'fs_system_free',
  FS_INDEX_FREE = 'fs_index_free',
  FS_METADATA_FREE = 'fs_metadata_free',
}

export enum EOperandUnit {
  'PERCENT' = 'percent',
  'MB' = 'MB',
}

export enum ESystemAlertState {
  '开启' = '1',
  '关闭' = '0',
}

export type TAlertLevel = '0' | '1' | '2' | '3';
/**
 * 告警级别
 *
 * 存在4种级别，和系统告警保持一致
 *
 * 0: 提示
 * 1: 一般
 * 2: 重要
 * 3: 紧急
 */
export const AlertLevelTypeList = [
  { label: '提示', value: '0' },
  { label: '一般', value: '1' },
  { label: '重要', value: '2' },
  { label: '紧急', value: '3' },
];

export enum EAlertRefireTime {
  '5m' = 300,
  '15m' = 900,
  '30m' = 1800,
  '60m' = 3600,
}

export const AlertRefireTimeList: { label: string; value: any }[] = [];

for (const n in EAlertRefireTime) {
  if (typeof EAlertRefireTime[n] === 'number') {
    AlertRefireTimeList.push({ value: EAlertRefireTime[n], label: n });
  }
}
// export let AlertRefireTimeList;

interface ISystemAlertRuleInfo {
  id: string;
  sourceType: ESystemAlertSourceType;
  name: string;
  /**
   * 告警状态
   * 0- 开启
   * 1- 关闭
   */
  state: ESystemAlertState;
  level: TAlertLevel;
  /**
   * 告警间隔
   */
  refireSeconds: number;
}

export interface ISystemAlertRule extends ISystemAlertRuleInfo {
  fireCriteria: {
    metric: ESystemAlertMetric;
    operand: number;
    operandUnit: EOperandUnit;
  }[];
}

export interface ISystemAlertRuleResponse extends ISystemAlertRuleInfo {
  fireCriteria: string;
}
