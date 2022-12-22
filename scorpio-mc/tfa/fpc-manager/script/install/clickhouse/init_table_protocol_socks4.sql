CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_socks4_log_record
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id` UInt32 COMMENT '应用ID',
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`src_ip` IPv6 COMMENT '源IP',
`src_port` UInt16 COMMENT '源端口',
`dest_ip` IPv6 COMMENT '目的IP',
`dest_port` UInt16 COMMENT '目的端口',
`cmd` String COMMENT '请求命令',
`request_remote_port` UInt16  COMMENT  '远端端口',
`request_remote_ip` IPv6  COMMENT '远端IP',
`user_id` String  COMMENT  '用户ID',
`domain_name` String  COMMENT  '远端域名',
`cmd_result` String  COMMENT  '结果',
`response_remote_ip` IPv6  COMMENT  '服务器返回的远端IP ',
`response_remote_port` UInt16  COMMENT  '服务器返回的远端端口',
`channel_state` UInt8 COMMENT '连接状态',
INDEX i_policy_name (policy_name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_flow_id (flow_id) TYPE minmax GRANULARITY 4,
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_application_id (application_id) TYPE minmax GRANULARITY 4,
INDEX i_end_time (end_time) TYPE minmax GRANULARITY 4,
INDEX i_src_ip (src_ip) TYPE minmax GRANULARITY 4,
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4,
INDEX i_dest_ip (dest_ip) TYPE minmax GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_cmd (cmd) TYPE bloom_filter GRANULARITY 4,
INDEX i_request_remote_port (request_remote_port) TYPE minmax GRANULARITY 4,
INDEX i_request_remote_ip (request_remote_ip) TYPE minmax GRANULARITY 4,
INDEX i_user_id (user_id) TYPE bloom_filter GRANULARITY 4,
INDEX i_domain_name (domain_name) TYPE bloom_filter GRANULARITY 4,
INDEX i_cmd_result (cmd_result) TYPE bloom_filter GRANULARITY 4,
INDEX i_response_remote_ip (response_remote_ip) TYPE minmax GRANULARITY 4,
INDEX i_response_remote_port (response_remote_port) TYPE minmax GRANULARITY 4,
INDEX i_channel_state (channel_state) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(start_time)
ORDER BY (start_time,flow_id)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '应用层协议详单-SOCKS4协议详单表'