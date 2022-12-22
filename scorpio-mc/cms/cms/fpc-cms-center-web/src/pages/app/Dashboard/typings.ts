export enum EPartitionType {
  'SYSTEM' = 'fs_system_io',
  'METADATA_HOT' = 'fs_metadata_hot_io',
  'METADATA' = 'fs_metadata_io',
  'INDEX' = 'fs_index_io',
  'PACKET' = 'fs_packet_io',
}

export enum EDiskIOType {
  'READBYTES' = 'avg(read_byteps)',
  'WRITEBYTES' = 'avg(write_byteps)',
  'READBYTESPEAK' = 'max(read_byteps_peak)',
  'WRITEBYTESPEAK' = 'max(write_byteps_peak)',
}
