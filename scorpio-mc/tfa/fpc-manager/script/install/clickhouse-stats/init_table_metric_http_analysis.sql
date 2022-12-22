CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_http_analysis_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`type` String COMMENT '统计分类', 
`key`String COMMENT '统计指标',
`value` UInt64 COMMENT '统计值',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_type (type) TYPE bloom_filter() GRANULARITY 4,
INDEX i_key (key) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'HTTP分析统计表'