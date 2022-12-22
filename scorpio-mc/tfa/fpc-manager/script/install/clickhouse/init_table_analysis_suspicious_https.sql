CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_suspicious_https
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`ja3` String COMMENT 'ja3指纹'
) 
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-异常ja3指纹结果表'