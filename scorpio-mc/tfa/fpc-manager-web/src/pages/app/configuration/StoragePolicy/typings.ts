/**
 * 压缩策略
 */
export enum ECompressAction {
  /**
   * 压缩
   */
  YES = '0',
  /**
   * 不压缩
   */
  NO = '1',
}

/**
 * 加密策略
 */
export enum EEncryptAction {
  /**
   * 加密
   */
  YES = '0',
  /**
   * 不加密
   */
  NO = '1',
}

/**
 * 流量存储策略
 */
export interface IStoragePolicy {
  id: string;
  /**
   * 压缩策略
   */
  compressAction: ECompressAction;
  /**
   * 加密策略
   */
  encryptAction: EEncryptAction;
  /**
   * 加密算法
   */
  encryptAlgorithm: string;
}
