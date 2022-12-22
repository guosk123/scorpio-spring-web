CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_monitor_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`monitored_serial_number` String COMMENT '被监控设备序列号',
`memory_used_ratio` UInt8 COMMENT '内存使用率', 
`cpu_used_ratio` UInt8 COMMENT 'CPU使用率', 
`system_fs_used_ratio` UInt8 COMMENT '系统分区使用率',
`system_fs_free` UInt64 COMMENT '系统分区剩余空间（byte）',
`index_fs_used_ratio` UInt8 COMMENT '索引分区使用率',
`index_fs_free` UInt64 COMMENT '索引分区剩余空间（byte）',
`metadata_fs_used_ratio`UInt8 COMMENT '详单冷分区使用率',
`metadata_fs_free` UInt64 COMMENT '详单冷分区剩余空间（byte）',
`metadata_hot_fs_used_ratio` UInt8 COMMENT '详单热分区使用率',
`metadata_hot_fs_free` UInt64 COMMENT '详单热分区剩余空间（byte）',
`packet_fs_used_ratio` UInt8 COMMENT '数据包分区使用率',
`packet_fs_free` UInt64 COMMENT '数据包分区剩余空间（byte）'
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '系统状态监控表'