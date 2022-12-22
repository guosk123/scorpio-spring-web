CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_disk_io_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`monitored_serial_number` String COMMENT '被监控设备序列号',
`partition_name` String COMMENT '磁盘分区',
`read_byteps` UInt64 COMMENT '读速率(Byte/s)',
`read_byteps_peak`UInt64 COMMENT '读峰值速率(Byte/s)',
`write_byteps` UInt64 COMMENT '写速率(Byte/s)',
`write_byteps_peak` UInt64 COMMENT '写峰值速率(Byte/s)', 
INDEX i_partition_name (partition_name) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_read_byteps (read_byteps) TYPE minmax GRANULARITY 4, 
INDEX i_read_byteps_peak (read_byteps_peak) TYPE minmax GRANULARITY 4, 
INDEX i_write_byteps (write_byteps) TYPE minmax GRANULARITY 4, 
INDEX i_write_byteps_peak (write_byteps_peak) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '磁盘分区IO统计'