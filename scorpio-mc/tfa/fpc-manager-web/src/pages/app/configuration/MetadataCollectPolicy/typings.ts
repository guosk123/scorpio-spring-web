/**
 * 元数据采集策略
 */
export interface IMetadataCollectPolicy {
  id: string;
  name: string;
  ipAddress: string;
  level: EMetadataCollectLevel;
  state: '0' | '1';
  orderNo: number;
  protocol: number;
  operatorId: string;
}

/** 采集策略级别 */
export const METADATA_COLLECT_LEVEL_MAP = {
  '2': '高',
  '1': '中',
  '0': '低',
};

/**
 * 采集策略的级别
 */
export type EMetadataCollectLevel = keyof typeof METADATA_COLLECT_LEVEL_MAP;
