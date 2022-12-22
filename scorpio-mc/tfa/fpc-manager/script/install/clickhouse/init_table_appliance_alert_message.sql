CREATE TABLE IF NOT EXISTS fpc.t_fpc_appliance_alert_message
( 
`id` String COMMENT 'UUID',
`arise_time` DateTime64(3, 'UTC') COMMENT '告警时间', 
`alert_id` String COMMENT '告警规则ID', 
`network_id` String COMMENT '网络ID',
`service_id` Nullable(String) COMMENT '业务ID',
`name` String COMMENT '告警名称', 
`category` String COMMENT '告警分类',
`level` UInt8 COMMENT '告警级别',
`alert_define` String COMMENT '告警规则定义(JSON)',
`components` String COMMENT '告警子组件(JSON)',
`status` String DEFAULT '0' COMMENT '处理状态',
`solver_id` String COMMENT '告警处理人',
`solve_time` Nullable(DateTime64(3, 'UTC')) COMMENT '处理时间',
`reason` String COMMENT '处理意见',
INDEX i_id (id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_alert_id (alert_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_name (name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_category (category) TYPE bloom_filter() GRANULARITY 4,
INDEX i_level (level) TYPE minmax GRANULARITY 4,
INDEX i_status (status) TYPE bloom_filter() GRANULARITY 4,
INDEX i_solve_time (solve_time) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMM(arise_time)
ORDER BY (arise_time) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '告警消息记录表'