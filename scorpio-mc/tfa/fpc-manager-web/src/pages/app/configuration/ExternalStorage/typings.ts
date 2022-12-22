// export const EXTERNAL_STORAGE_TYPE_LIST = ['FTP', 'SMB', 'HDFS', 'SFTP', 'TFTP', 'NAS'] as const;
export const EXTERNAL_STORAGE_TYPE_LIST = ['FTP', 'SMB', 'SFTP', 'TFTP', 'NFS'] as const;

export const EXTERNAL_STORAGE_USAGE_MAP = {
  transmit_task: '全流量查询任务',
  packet_file_task: '离线分析任务',
} as const;

export enum EExternalStorageState {
  /** 启用 */
  Open = '1',
  /** 禁用 */
  Closed = '0',
}

export interface IExternalStorage {
  id?: string;
  name: string;
  /** 使用用途 */
  usage: keyof typeof EXTERNAL_STORAGE_USAGE_MAP;
  /**
   * 服务器类型
   */
  type: typeof EXTERNAL_STORAGE_TYPE_LIST[number];
  /**
   * IP地址
   */
  ipAddress: string;
  /**
   * 端口
   *
   * - FTP默认端口21
   * - SMB默认端口445
   */
  port: number;
  /**
   * 用户名
   */
  username: string;
  /**
   * 密码
   */
  password: string;
  /**
   * 存储目录
   */
  directory: string;

  /**
   * 可用容量Bytes
   */
  capacity: number;
  /**
   * 页面显示GB
   */
  capacityGigaByte: number;

  /**
   * 状态
   */
  state: EExternalStorageState;
}
