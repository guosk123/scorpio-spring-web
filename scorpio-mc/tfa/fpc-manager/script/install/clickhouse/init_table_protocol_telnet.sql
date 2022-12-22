CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_telnet_log_record
( 
`level` LowCardinality(String) COMMENT '采集策略等级', 
`policy_name` LowCardinality(String) COMMENT '采集策略名称', 
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id` UInt32 COMMENT '应用ID', 
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6',
`src_port` UInt16 COMMENT '源端口',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`dest_port` UInt16 COMMENT '目的端口',
`username` LowCardinality(String) COMMENT '登录用户名',
`password` LowCardinality(String) COMMENT '登录密码',
`cmd` LowCardinality(String) COMMENT '操作命令',
`reply` LowCardinality(String) COMMENT '操作结果',
INDEX i_policy_name (policy_name) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_flow_id (flow_id) TYPE minmax GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_application_id (application_id) TYPE minmax GRANULARITY 4,
INDEX i_end_time (end_time) TYPE minmax GRANULARITY 4, 
INDEX i_src_ipv4 (src_ipv4) TYPE minmax GRANULARITY 4, 
INDEX i_src_ipv6 (src_ipv6) TYPE minmax GRANULARITY 4, 
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4, 
INDEX i_dest_ipv4 (dest_ipv4) TYPE minmax GRANULARITY 4, 
INDEX i_dest_ipv6 (dest_ipv6) TYPE minmax GRANULARITY 4, 
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4, 
INDEX i_username (username) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_password (password) TYPE bloom_filter() GRANULARITY 4,
INDEX i_cmd (cmd) TYPE bloom_filter() GRANULARITY 4
)  
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(start_time)
ORDER BY (start_time, flow_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '应用层协议详单-TELNET协议详单表'