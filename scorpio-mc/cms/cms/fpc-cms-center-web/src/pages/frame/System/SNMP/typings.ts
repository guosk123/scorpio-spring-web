/**
 * SNMP 版本
 */
export enum ESnmpVersion {
  'v1' = 'v1',
  'v2c' = 'v2c',
  'v3' = 'v3',
}

/**
 * SNMP认证算法
 */
export enum ESnmpAuthAlgorithm {
  'MD5' = 'MD5',
  'SHA' = 'SHA',
}

/**
 * SNMP加密算法
 */
export enum ESnmpEncryptAlgorithm {
  'DES' = 'DES',
  'AES' = 'AES',
}

export interface ISnmpSettings {
  /**
   * 启用状态
   * 0 - 关闭
   * 1 - 启用
   */
  state: '0' | '1';
  /**
   * SNMP版本
   */
  version: ESnmpVersion;
  /**
   * SNMP只读团体名
   */
  roCommunity?: string;
  /**
   * 设备位置
   */
  sysLocation?: string;
  /**
   * 联系信息
   */
  sysContact?: string;

  /**
   * 安全用户名
   */
  username?: string;

  /**
   * 认证密码
   */
  authPassword: string;
  /**
   * 认证算法
   */
  authAlgorithm: ESnmpAuthAlgorithm;
  /**
   * 加密密码
   */
  encryptPassword: string;
  /**
   * 加密算法
   */
  encryptAlgorithm: ESnmpEncryptAlgorithm;
}
