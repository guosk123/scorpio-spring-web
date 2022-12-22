DROP TABLE IF EXISTS fpc.d_fpc_metric_monitor_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_monitor_data_record
(
`monitored_serial_number` String COMMENT '探针设备序列号',
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`memory_used_ratio` UInt8 COMMENT '内存使用率', 
`cpu_used_ratio` UInt8 COMMENT 'CPU使用率', 
`system_fs_used_ratio` UInt8 COMMENT '系统分区使用率', 
`index_fs_used_ratio` UInt8 COMMENT '索引分区使用率',
`metadata_fs_used_ratio`UInt8 COMMENT '详单冷分区使用率',
`metadata_hot_fs_used_ratio` UInt8 COMMENT '详单热分区使用率',
`packet_fs_used_ratio` UInt8 COMMENT '数据包分区使用率',
`system_fs_free` UInt64 COMMENT '系统分区剩余空间',
`index_fs_free` UInt64 COMMENT '索引分区剩余空间',
`metadata_fs_free` UInt64 COMMENT '详单冷分区剩余空间',
`metadata_hot_fs_free` UInt64 COMMENT '详单热分区剩余空间',
`packet_fs_free` UInt64 COMMENT '数据包分区剩余空间'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_monitor_data_record)
COMMENT '系统状态监控表';