CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_custom_template
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`group_by` String COMMENT '分组名称',
`record_start_time` DateTime64(9, 'UTC') COMMENT '最早流日志的时间', 
`record_end_time` DateTime64(9, 'UTC') COMMENT '最晚流日志的时间', 
`function_result` UInt64 COMMENT '计算方法(模板中配置)的结果', 
`time_avg_hit` UInt64 COMMENT '每X秒(模板中配置)的均值', 
`time_slice_list` String COMMENT '时间切片列表结果',
INDEX i_group_by (group_by) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_record_start_time (record_start_time) TYPE minmax GRANULARITY 4, 
INDEX i_record_end_time (record_end_time) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-自定义模板分析结果'