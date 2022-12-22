export interface ILicenseInfo {
  /** 设备采集时间 */
  collectTime?: string;
  /** 过期时间 */
  expiryTime?: string;
  /** License 文件名称 */
  fileName: string;
  /** 设备列表 */
  licenseItemList: { fpcId: string; serialNo: string; fpcIp: string }[];
  /** License用途 */
  licenseType: ELicenseType;
  /** 当前设备序列号 */
  localSerialNo: string;
  /** License 签发时间 */
  signTime: string;
  /** License 版本号 */
  version: number;
}

/**
 * License用途
 */
export enum ELicenseType {
  /** Demo 演示版本 */
  Demo = 'demo',
  /** 正式商用版本 */
  Commercial = 'commercial',
}
