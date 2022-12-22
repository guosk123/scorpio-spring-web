export enum EWeek {
  // 'Monday' = '1',
  // 'Tuesday' = '2',
  // 'Wednesday' = '3',
  // 'Thursday' = '4',
  // 'Friday' = '5',
  // 'Saturday' = '6',
  // 'Sunday' = '7',

  MONDAY = 'Monday',
  Tuesday = 'Tuesday',
  Wednesday = 'Wednesday',
  Thursday = 'Thursday',
  Friday = 'Friday',
  Saturday = 'Saturday',
  Sunday = 'Sunday',
}

export const week_Enum = {
  [EWeek.MONDAY]: '星期一',
  [EWeek.Tuesday]: '星期二',
  [EWeek.Wednesday]: '星期三',
  [EWeek.Thursday]: '星期四',
  [EWeek.Friday]: '星期五',
  [EWeek.Saturday]: '星期六',
  [EWeek.Sunday]: '星期日',
};

export enum ECustomTimeType {
  'PeriodicTime' = '0',
  'DisposableTime' = '1',
}

export const time_Enum = {
  [ECustomTimeType.PeriodicTime]: '周期性时间',
  [ECustomTimeType.DisposableTime]: '一次性时间',
};

export const timeTypeOptions = [
  { label: time_Enum[ECustomTimeType.PeriodicTime], value: ECustomTimeType.PeriodicTime },
  { label: time_Enum[ECustomTimeType.DisposableTime], value: ECustomTimeType.DisposableTime },
];

// 表格中的数据项
export interface TimeConfigItem {
  id: string;
  name: string;
  type: ECustomTimeType;
  period: EWeek[];
  customTimeSetting: any;
}

export interface ICustomTime {
  name: string;
  id?: string;
  period?: EWeek[];
  type: ECustomTimeType;
  customTimeSetting: string;
}
