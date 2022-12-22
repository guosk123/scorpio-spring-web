CREATE TABLE IF NOT EXISTS t_fpc_metric_payload_in_second
(
`timestamp` DateTime64(3, 'UTC'), 
`start_time` DateTime64(3, 'UTC'), 
`network_id` LowCardinality(String),
`service_id` LowCardinality(String),
`total_bytes` UInt64,
`total_packets` UInt64,
`upstream_bytes` UInt64,
`upstream_packets` UInt64,
`downstream_bytes` UInt64,
`downstream_packets` UInt64,
`filter_discard_bytes` UInt64,
`overload_discard_bytes` UInt64,
`deduplication_bytes` UInt64,
`established_sessions` UInt64,
`concurrent_sessions` UInt64,
`unique_ip_counts` UInt64,
INDEX i_start_time (start_time) TYPE minmax GRANULARITY 4, 
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_total_bytes (total_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_total_packets (total_packets) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_bytes (upstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_upstream_packets (upstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_bytes (downstream_bytes) TYPE minmax GRANULARITY 4, 
INDEX i_downstream_packets (downstream_packets) TYPE minmax GRANULARITY 4, 
INDEX i_filter_discard_bytes (filter_discard_bytes) TYPE minmax GRANULARITY 4,
INDEX i_overload_discard_bytes (overload_discard_bytes) TYPE minmax GRANULARITY 4,
INDEX i_deduplication_bytes (deduplication_bytes) TYPE minmax GRANULARITY 4,
INDEX i_established_sessions (established_sessions) TYPE minmax GRANULARITY 4,
INDEX i_concurrent_sessions (concurrent_sessions) TYPE minmax GRANULARITY 4,
INDEX i_unique_ip_counts (unique_ip_counts) TYPE minmax GRANULARITY 4
) 
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)