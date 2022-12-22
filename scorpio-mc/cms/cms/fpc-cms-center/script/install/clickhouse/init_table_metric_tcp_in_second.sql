CREATE TABLE IF NOT EXISTS t_fpc_metric_tcp_in_second
(
`timestamp` DateTime64(3, 'UTC'), 
`start_time` DateTime64(3, 'UTC'), 
`network_id` LowCardinality(String),
`service_id` LowCardinality(String),
`tcp_client_syn_packets` UInt64,
`tcp_server_syn_packets` UInt64,
`tcp_established_success_counts` UInt64,
`tcp_established_fail_counts` UInt64,
`tcp_client_retransmission_packets` UInt64,
`tcp_server_retransmission_packets` UInt64,
`tcp_client_zero_window_packets` UInt64,
`tcp_server_zero_window_packets` UInt64,
INDEX i_start_time (start_time) TYPE minmax GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_tcp_client_syn_packets (tcp_client_syn_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_syn_packets (tcp_server_syn_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_established_success_counts (tcp_established_success_counts) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_established_fail_counts (tcp_established_fail_counts) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_retransmission_packets (tcp_client_retransmission_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_retransmission_packets (tcp_server_retransmission_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_client_zero_window_packets (tcp_client_zero_window_packets) TYPE minmax GRANULARITY 4, 
INDEX i_tcp_server_zero_window_packets (tcp_server_zero_window_packets) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)