CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_dhcp_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`network_id` String COMMENT '网络ID',
`client_ip_address` String COMMENT '客户端IP地址',
`server_ip_address` String COMMENT '服务端IP地址',
`client_mac_address` String COMMENT '客户端MAC地址',
`server_mac_address` String COMMENT '服务端MAC地址',
`message_type` UInt8 COMMENT '消息类型',
`dhcp_version` UInt8 COMMENT 'DHCP版本',
`total_bytes` UInt64 COMMENT '总字节数',
`send_bytes`UInt64 COMMENT '客户端发送字节数',
`receive_bytes` UInt64 COMMENT '客户端接收字节数', 
`total_packets` UInt64 COMMENT '总包数',
`send_packets` UInt64 COMMENT '客户端发送数据包数',
`receive_packets` UInt64 COMMENT '服务端接收数据包数',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_client_ip_address (client_ip_address) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_server_ip_address (server_ip_address) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_client_mac_address (client_mac_address) TYPE bloom_filter() GRANULARITY 4,
INDEX i_server_mac_address (server_mac_address) TYPE bloom_filter() GRANULARITY 4,
INDEX i_message_type (message_type) TYPE minmax GRANULARITY 4,
INDEX i_dhcp_version (dhcp_version) TYPE minmax GRANULARITY 4,
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_send_bytes (send_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_receive_bytes (receive_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4, 
INDEX i_send_packets (send_packets) TYPE minmax GRANULARITY 4, 
INDEX i_receive_packets (receive_packets) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'DHCP统计表'