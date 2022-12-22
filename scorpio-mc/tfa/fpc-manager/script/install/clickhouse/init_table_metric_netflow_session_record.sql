CREATE TABLE IF NOT EXISTS fpc.t_netflow_session_record (
`device_name` String COMMENT '设备名称',
`in_netif` String COMMENT '入接口',
`out_netif` String COMMENT '出接口',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`src_port` UInt16 COMMENT '源端口',
`dest_port` UInt16 COMMENT '目的端口',
`protocol` String COMMENT '传输层协议',
`total_packets` UInt32 COMMENT '总包数',
`ingest_packets` UInt32 COMMENT '接收包数',
`transmit_packets` UInt32 COMMENT '发送包数',
`total_bytes` UInt64 COMMENT '总字节数',
`transmit_bytes` UInt64 COMMENT '发送字节数',
`ingest_bytes` UInt64 COMMENT '接收字节数',
`tcp_flag` UInt32 COMMENT 'TCP标识',
`dscp_flag` UInt32 COMMENT 'DSCP标识',
`duration` UInt32 COMMENT '会话持续时间',
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`report_time` DateTime64(9, 'UTC') COMMENT '上报时间',
`session_id` UInt64 COMMENT 'SESSION ID',
INDEX i_device_name (device_name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4,
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4,
INDEX i_transmit_bytes (transmit_bytes) TYPE minmax GRANULARITY 4,
INDEX i_transmit_packets (transmit_packets) TYPE minmax GRANULARITY 4,
INDEX i_ingest_bytes (ingest_bytes) TYPE minmax GRANULARITY 4,
INDEX i_ingest_packets (ingest_packets) TYPE minmax GRANULARITY 4,
INDEX i_duration (duration) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(report_time)
ORDER BY (report_time)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'FLOW流数据会话详单表'