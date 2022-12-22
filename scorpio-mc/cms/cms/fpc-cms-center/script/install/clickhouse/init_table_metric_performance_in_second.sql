CREATE TABLE IF NOT EXISTS t_fpc_metric_performance_in_second
(
`timestamp` DateTime64(3, 'UTC'), 
`start_time` DateTime64(3, 'UTC'), 
`network_id` LowCardinality(String),
`service_id` LowCardinality(String),
`tcp_client_network_latency` UInt64,
`tcp_client_network_latency_counts` UInt32,
`tcp_server_network_latency` UInt64,
`tcp_server_network_latency_counts` UInt32,
`server_response_latency` UInt64,
`server_response_latency_counts` UInt32,
`server_response_fast_counts` UInt64,
`server_response_normal_counts` UInt64,
`server_response_timeout_counts` UInt64,
`tcp_client_retransmission_packets` UInt64,
`tcp_server_retransmission_packets` UInt64,
INDEX i_start_time (start_time) TYPE minmax GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_tcp_client_network_latency (tcp_client_network_latency) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_network_latency_counts (tcp_client_network_latency_counts) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_network_latency (tcp_server_network_latency) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_network_latency_counts (tcp_server_network_latency_counts) TYPE minmax GRANULARITY 4,
INDEX i_server_response_latency (server_response_latency) TYPE minmax GRANULARITY 4,
INDEX i_server_response_latency_counts (server_response_latency_counts) TYPE minmax GRANULARITY 4,
INDEX i_server_response_fast_counts (server_response_fast_counts) TYPE minmax GRANULARITY 4,
INDEX i_server_response_normal_counts (server_response_normal_counts) TYPE minmax GRANULARITY 4,
INDEX i_server_response_timeout_counts (server_response_timeout_counts) TYPE minmax GRANULARITY 4,
INDEX i_tcp_client_retransmission_packets (tcp_client_retransmission_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_retransmission_packets (tcp_server_retransmission_packets) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)