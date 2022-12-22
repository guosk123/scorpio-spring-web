CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_netif_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`netif_name` String COMMENT '网口名称',
`total_bytes` UInt64 COMMENT '总字节数',
`downstream_bytes`UInt64 COMMENT '下行流量总字节数',
`upstream_bytes` UInt64 COMMENT '上行流量总字节数',
`transmit_bytes` UInt64 COMMENT '发送流量总字节数',
`total_packets` UInt64 COMMENT '总包数',
`downstream_packets` UInt64 COMMENT '下行流量总包数',
`upstream_packets` UInt64 COMMENT '上行流量总包数',
`transmit_packets` UInt64 COMMENT '发送流量总包数',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_netif_name (netif_name) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_bytes (downstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_bytes (upstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_transmit_bytes (transmit_bytes) TYPE minmax GRANULARITY 4,  
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_packets (downstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_packets (upstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_transmit_packets (transmit_packets) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '网口统计表'