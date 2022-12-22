CREATE TABLE IF NOT EXISTS t_fpc_metric_ip_conversation_in_second
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`network_id` LowCardinality(String) COMMENT '网络ID',
`service_id` LowCardinality(String) COMMENT '业务ID',
`total_bytes_top` Map(String, UInt64) COMMENT '总字节数排行',
`total_sessions_top` Map(String, UInt64) COMMENT '总会话数排行',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
COMMENT 'IP通讯对秒级统计表'