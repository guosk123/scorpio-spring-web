export interface ISystemAlarm {
  ariseTime: string;
  category: string;
  component: string;
  content: string;
  id: string;
  keyword: string;
  level: string;
  nodeId: string;
  reason: string;
  solveTime: string;
  solver: string;
  /**
   *
   */
  status: ESystemAlarmStatus;
}

export enum ESystemAlarmStatus {
  /** 未解决 */
  UnResolved = '0',
  /** 已解决 */
  Resolved = '1',
}
