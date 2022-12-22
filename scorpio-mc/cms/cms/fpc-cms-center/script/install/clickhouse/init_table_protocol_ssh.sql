DROP TABLE IF EXISTS fpc.d_fpc_protocol_ssh_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_ssh_log_record
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
`client_version` LowCardinality(String) COMMENT '客户端版本',
`client_software` LowCardinality(String) COMMENT '客户端软件',
`client_comments` LowCardinality(String) COMMENT '客户端请求附带信息',
`server_version` LowCardinality(String) COMMENT '服务器版本',
`server_software` LowCardinality(String) COMMENT '服务器软件',
`server_comments` LowCardinality(String) COMMENT '服务器应答附带信息',
`server_key_type` LowCardinality(String) COMMENT '服务器秘钥类',
`server_key` String COMMENT '服务器秘钥'
) 
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_ssh_log_record)
COMMENT '应用层协议详单-SSH协议详单表';