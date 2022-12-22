export enum EWeek {
  MONDAY = 'Monday',
  TUESDAY = 'Tuesday',
  WEDNESDAY = 'Wednesday',
  THURSDAY = 'Thursday',
  FRIDAY = 'Friday',
  SATURDAY = 'Saturday',
  SUNDAY = 'Sunday',
}

export const week_Enum = {
  [EWeek.MONDAY]: '星期一',
  [EWeek.TUESDAY]: '星期二',
  [EWeek.WEDNESDAY]: '星期三',
  [EWeek.THURSDAY]: '星期四',
  [EWeek.FRIDAY]: '星期五',
  [EWeek.SATURDAY]: '星期六',
  [EWeek.SUNDAY]: '星期日',
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
