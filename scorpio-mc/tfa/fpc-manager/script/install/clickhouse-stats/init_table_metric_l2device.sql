CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_l2device_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '记录时间',
`network_id` String COMMENT '网络ID',
`service_id` String COMMENT '业务ID',
`mac_address` String COMMENT 'MAC地址',
`ethernet_type` UInt8 COMMENT '三层协议类型',
`total_bytes` UInt64 COMMENT '总字节数',
`downstream_bytes`UInt64 COMMENT '下行流量总字节数',
`upstream_bytes` UInt64 COMMENT '上行流量总字节数',
`total_packets` UInt64 COMMENT '总包数',
`downstream_packets` UInt64 COMMENT '下行包数',
`upstream_packets` UInt64 COMMENT '上行包数',
`established_sessions` UInt64 COMMENT '新建会话数',
`tcp_client_network_latency` UInt64 COMMENT 'TCP客户端网络总时延(ms)',
`tcp_client_network_latency_counts` UInt64 COMMENT 'TCP客户端网络时延次数',
`tcp_server_network_latency` UInt64 COMMENT 'TCP服务端网络总时延(ms)',
`tcp_server_network_latency_counts` UInt64 COMMENT 'TCP服务端网络时延次数',
`server_response_latency` UInt64 COMMENT '服务器响应总时延(ms)',
`server_response_latency_counts` UInt64 COMMENT '服务器响应时延总次数',
`tcp_client_retransmission_packets` UInt64 COMMENT 'TCP客户端重传包数',
`tcp_client_packets` UInt64 COMMENT 'TCP客户端包数',
`tcp_server_retransmission_packets` UInt64 COMMENT 'TCP服务端重传包数',
`tcp_server_packets` UInt64 COMMENT 'TCP服务端包数',
`tcp_client_zero_window_packets` UInt64 COMMENT 'TCP客户端零窗口包数',
`tcp_server_zero_window_packets` UInt64 COMMENT 'TCP服务端零窗口包数',
`tcp_established_fail_counts` UInt64 COMMENT 'TCP会话建连失败次数',
`tcp_established_success_counts` UInt64 COMMENT 'TCP会话建连成功次数',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_mac_address (mac_address) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_ethernet_type (ethernet_type) TYPE minmax GRANULARITY 4, 
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_bytes (downstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_bytes (upstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_packets (downstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_packets (upstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_established_sessions (established_sessions) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_network_latency (tcp_client_network_latency) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_network_latency_counts (tcp_client_network_latency_counts) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_network_latency (tcp_server_network_latency) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_network_latency_counts (tcp_server_network_latency_counts) TYPE minmax GRANULARITY 4, 
INDEX i_server_response_latency (server_response_latency) TYPE minmax GRANULARITY 4, 
INDEX i_server_response_latency_counts (server_response_latency_counts) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_retransmission_packets (tcp_client_retransmission_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_packets (tcp_client_packets) TYPE minmax GRANULARITY 4,  
INDEX i_tcp_server_retransmission_packets (tcp_server_retransmission_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_packets (tcp_server_packets) TYPE minmax GRANULARITY 4,  
INDEX i_tcp_client_zero_window_packets (tcp_client_zero_window_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_zero_window_packets (tcp_server_zero_window_packets) TYPE minmax GRANULARITY 4,  
INDEX i_tcp_established_fail_counts (tcp_established_fail_counts)  TYPE minmax GRANULARITY 4, 
INDEX i_tcp_established_success_counts (tcp_established_success_counts)  TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'MAC地址统计表'