CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_brute_force
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间', 
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间', 
`inner_host` String COMMENT '内网IP', 
`record_max_hit_every_1minutes` UInt64 COMMENT '每分钟最大暴破次数', 
`record_max_hit_every_3minutes` UInt64 COMMENT '每3分钟最大暴破次数',
INDEX i_start_time (start_time) TYPE minmax GRANULARITY 4, 
INDEX i_end_time (end_time) TYPE minmax GRANULARITY 4, 
INDEX i_inner_host (inner_host) TYPE bloom_filter() GRANULARITY 4
)  
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-SSH/RDP暴破检测'