export interface IPktAnalysis {
  id: string;
  fileName: string;
  protocol: string;
  description: string;
  createTime: string;
  parseStatus: string;
  parseLog: string;
}

export enum EParseStatus {
  'UNRESOLVED' = '0',
  'SUCCESS' = '1',
  'ERROR' = '2',
}
