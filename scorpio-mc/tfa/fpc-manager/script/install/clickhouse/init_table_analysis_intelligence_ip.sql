CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_intelligence_ip
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`ip_address` String COMMENT '命中的IP',
INDEX i_ip_address (ip_address) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-威胁IP情报结果表'