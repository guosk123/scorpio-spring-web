export interface IRestAPIRecord {
  apiName: string;
  uri: string;
  userIp: string;
  userId: string;
  timestamp: string;
  status: number;
  method: string;
  response: string;
}

export enum ERestStatType {
  'User' = 'user',
  'API' = 'api',
}

export interface IRestMetric {
  timestamp: string;
  [key: string]: number | string;
}

export interface ITimeInfo {
  fromTime: string;
  toTime: string;
}
