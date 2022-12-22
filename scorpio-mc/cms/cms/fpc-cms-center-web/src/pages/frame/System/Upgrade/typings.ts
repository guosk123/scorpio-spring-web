/**
 * 更新包的版本信息
 */
 export interface IUpgradeInfo {
  /**
   * 安装包的版本
   */
  installVersion: string;
  /**
   * 安装包的文件名
   */
  installFileName: string;
  /**
   * 安装包的发布时间
   */
  installReleaseTime: string;

  /**
   * 升级包的版本
   */
  upgradeVersion: string;
  /**
   * 升级包的文件名
   */
  upgradeFileName: string;
  /**
   * 升级包的发布
   */
  upgradeReleaseTime: string;
  /**
   * 开始升级时间
   */
  upgradeStartTime?: string;
  /**
   * 升级结束时间
   */
  upgradeEndTime?: string;
  /**
   * 升级状态
   */
  upgradeState?: EUpgradeState;
}

/**
 * 升级状态
 */
export enum EUpgradeState {
  'RUNNING' = 'running',
  'SUCCESS' = 'success',
  'FAILED' = 'failed',
  /**
   * 异常退出
   */
  'EXCEPTION' = 'exception',
}

/**
 * 升级日志
 */
export interface IUpgradeLog {
  /**
   * 增量的升级日志
   */
  logs: string[];
  /**
   * 游标
   */
  cursor: number;
  /**
   * 升级状态
   */
  state: EUpgradeState;
}
