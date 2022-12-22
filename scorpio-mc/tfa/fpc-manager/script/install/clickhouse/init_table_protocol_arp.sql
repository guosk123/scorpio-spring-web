CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_arp_log_record
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID', 
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_mac` LowCardinality(String) COMMENT '源MAC地址',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_mac` LowCardinality(String) COMMENT '目的MAC地址',
`type` UInt8 COMMENT '报文类型', 
INDEX i_policy_name (policy_name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_end_time (end_time) TYPE minmax GRANULARITY 4,
INDEX i_src_ipv4 (src_ipv4) TYPE minmax GRANULARITY 4,
INDEX i_src_mac (src_mac) TYPE minmax GRANULARITY 4,
INDEX i_dest_ipv4 (dest_ipv4) TYPE minmax GRANULARITY 4,
INDEX i_dest_mac (dest_mac) TYPE minmax GRANULARITY 4,
INDEX i_type (type) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(start_time)
ORDER BY start_time
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '应用层协议详单-ARP协议详单表'