export enum SpaceEnum {
  /**
   * 实时流量
   */
  FS_DATA = 'fs_data',
  /**
   * 离线文件
   */
  OFFLINE_PCAP = 'offline_pcap',
  /**
   * 缓存空间
   */
  FS_CACHE = 'fs_cache',
  /**
   * 单文件最大落盘大小限制
   */
  TRANSMIT_TASK_FILE_LIMIT = 'transmit_task_file_limit',
}

/**
 * 流量存储策略
 */
export interface IStorageSpace {
  /**
   * 存储空间分类
   */
  spaceType: SpaceEnum;
  /**
   * 容量限制
   */
  capacity: number;
}
