CREATE TABLE IF NOT EXISTS fpc.t_fpc_flow_log_est_fail_record
(
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`report_time` DateTime64(9, 'UTC') COMMENT '入库时间',
`start_time` DateTime64(9, 'UTC') COMMENT '会话开始时间',
`ipv4_initiator` Nullable(IPv4) COMMENT '源IPv4',
`ipv4_responder` Nullable(IPv4) COMMENT '目的IPv4',
`ipv6_initiator` Nullable(IPv6) COMMENT '源IPv6',
`ipv6_responder` Nullable(IPv6) COMMENT '目的IPv6',
`port_initiator` UInt16 COMMENT '源端口',
`port_responder` UInt16 COMMENT '目的端口',
`ip_locality_initiator` UInt8 COMMENT '源IP所在位置(0:内网,1外网)',
`ip_locality_responder` UInt8 COMMENT '目的IP所在位置(0:内网,1外网)',
`tcp_session_state` UInt8 COMMENT 'TCP会话状态',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_ipv4_initiator (ipv4_initiator) TYPE bloom_filter() GRANULARITY 4,
INDEX i_ipv4_responder (ipv4_responder) TYPE bloom_filter() GRANULARITY 4,
INDEX i_ipv6_initiator (ipv6_initiator) TYPE bloom_filter() GRANULARITY 4,
INDEX i_ipv6_responder (ipv6_responder) TYPE bloom_filter() GRANULARITY 4,
INDEX i_tcp_session_state (tcp_session_state) TYPE bloom_filter() GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(report_time)
ORDER BY report_time
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '会话详单-建连失败表'