CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_nonstandard_protocol
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`standard_l7_protocol_id` String COMMENT '非标准协议的七层协议',
`standard_ip_protocol` String COMMENT '非标准协议的IP层协议',
`standard_port` UInt16 COMMENT '非标准协议的端口号',
INDEX i_standard_l7_protocol_id (standard_l7_protocol_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_standard_ip_protocol (standard_ip_protocol) TYPE bloom_filter() GRANULARITY 4,
INDEX i_standard_port (standard_port) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-非标准协议表'