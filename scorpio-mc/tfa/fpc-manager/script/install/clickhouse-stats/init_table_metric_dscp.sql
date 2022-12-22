CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_dscp_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`service_id` String COMMENT '业务ID',
`type` String COMMENT 'DSCP类型',
`total_bytes` UInt64 COMMENT '总字节数',
`total_packets` UInt64 COMMENT '总包数',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_type (type) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'DSCP统计表'