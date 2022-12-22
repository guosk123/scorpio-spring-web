export enum EMailRuleAction {
  'permission' = '0',
  'alert' = '1',
}

export enum EMailRuleStatus {
  'enable' = '1',
  'disable' = '0',
}

export enum EWeek {
  'Monday' = '1',
  'Tuesday' = '2',
  'Wednesday' = '3',
  'Thursday' = '4',
  'Friday' = '5',
  'Saturday' = '6',
  'Sunday' = '7',
}

export const WeekLabel = {
  [EWeek.Monday]: '星期一',
  [EWeek.Tuesday]: '星期二',
  [EWeek.Wednesday]: '星期三',
  [EWeek.Thursday]: '星期四',
  [EWeek.Friday]: '星期五',
  [EWeek.Saturday]: '星期六',
  [EWeek.Sunday]: '星期日',
};

export const RuleActionLabel = {
  [EMailRuleAction.permission]: '允许',
  [EMailRuleAction.alert]: '告警',
};

export const RuleStatusLabel = {
  [EMailRuleStatus.enable]: '启用',
  [EMailRuleStatus.disable]: '停用',
};

export interface IMailLoginRule {
  id: string;
  mailAddress: string;
  countryId: string;
  provinceId?: string;
  cityId?: string;
  startTime: string;
  endTime: string;
  action: EMailRuleAction;
  period?: string;
  state: EMailRuleStatus;
}
