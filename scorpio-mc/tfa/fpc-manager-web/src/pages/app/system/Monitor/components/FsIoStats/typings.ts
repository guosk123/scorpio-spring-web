export interface IFsIoMetric {
  timestamp: string;
  partitionName: TPartitionName;
  /** 读速率 bytes/s */
  readByteps: number;
  /** 读峰值速率 bytes/s */
  readBytepsPeak: number;
  /** 写速率 bytes/s */
  writeByteps: number;
  /** 写峰值速率 bytes/s */
  writeBytepsPeak: number;
}

export const PARTITION_NAME_MAP = {
  fs_system_io: '系统分区',
  fs_index_io: '索引分区',
  fs_metadata_io: '详单冷分区',
  fs_metadata_hot_io: '详单热分区',
  fs_packet_io: '全流量存储分区',
};

export type TPartitionName = keyof typeof PARTITION_NAME_MAP;
