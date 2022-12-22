CREATE TABLE IF NOT EXISTS fpc.t_netflow_ip_statistics
(
`report_time` DateTime64(9, 'UTC') COMMENT '上报时间',
`device_name` String COMMENT '设备名称',
`netif_no` String COMMENT '接口编号',
`ipv4_address` Nullable(IPv4) COMMENT 'IPv4地址',
`ipv6_address` Nullable(IPv6) COMMENT 'IPv6地址',
`total_bytes` UInt64 COMMENT '总字节数',
`transmit_bytes` UInt64 COMMENT '发送字节数',
`ingest_bytes` UInt64 COMMENT '接收字节数',
`total_packets` UInt64 COMMENT '总数据包数',
`ingest_packets` UInt64 COMMENT '接收包数',
`transmit_packets` UInt64 COMMENT '发送包数',
INDEX i_device_name (device_name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4,
INDEX i_transmit_bytes (transmit_bytes) TYPE minmax GRANULARITY 4,
INDEX i_ingest_bytes (ingest_bytes) TYPE minmax GRANULARITY 4,
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(report_time)
ORDER BY (report_time)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'FLOW流数据IP统计表'