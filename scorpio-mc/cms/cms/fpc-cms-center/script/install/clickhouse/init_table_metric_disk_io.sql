DROP TABLE IF EXISTS fpc.d_fpc_metric_disk_io_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_disk_io_data_record
(
`monitored_serial_number` String COMMENT '探针设备序列号',
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`partition_name` String COMMENT '磁盘分区',
`read_byteps` UInt64 COMMENT '读速率(Byte/s)',
`read_byteps_peak`UInt64 COMMENT '读峰值速率(Byte/s)',
`write_byteps` UInt64 COMMENT '写速率(Byte/s)',
`write_byteps_peak` UInt64 COMMENT '写峰值速率(Byte/s)'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_disk_io_data_record)
COMMENT '磁盘分区IO统计';