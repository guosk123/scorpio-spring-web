CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_http_request_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`request_counts` UInt64 COMMENT '请求总数',
`response_counts`UInt64 COMMENT '响应总数',
`error_response_counts` UInt64 COMMENT '错误总数(状态码4xx和5xx)',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'HTTP请求状态统计表'