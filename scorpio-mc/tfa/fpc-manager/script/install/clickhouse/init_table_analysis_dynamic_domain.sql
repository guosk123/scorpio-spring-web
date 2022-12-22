CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_dynamic_domain
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`inner_host` String COMMENT 'DNS请求源IP', 
`dynamic_domain` String COMMENT 'DNS请求的域名',
INDEX i_inner_host (inner_host) TYPE bloom_filter() GRANULARITY 4,
INDEX i_dynamic_domain (dynamic_domain) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-动态域名查询结果表'