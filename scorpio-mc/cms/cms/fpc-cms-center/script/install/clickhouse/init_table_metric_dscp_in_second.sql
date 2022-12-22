CREATE TABLE IF NOT EXISTS t_fpc_metric_dscp_in_second
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`start_time` DateTime64(3, 'UTC') COMMENT '开始时间', 
`network_id` LowCardinality(String) COMMENT '网络ID',
`service_id` LowCardinality(String) COMMENT '业务ID',
`type` LowCardinality(String) COMMENT 'DSCP类型',
`total_bytes` UInt64 COMMENT '总字节数',
INDEX i_start_time (start_time) TYPE minmax GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_type (type) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
COMMENT 'DSCP秒级统计表'