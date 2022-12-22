DROP TABLE IF EXISTS fpc.d_fpc_protocol_icmp_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_icmp_log_record
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
`dest_port` UInt16 COMMENT '目的端口(已弃用)',
`version` UInt8 COMMENT '版本',
`result` String COMMENT '描述信息',
`request_data_len` UInt32 COMMENT '请求数据长度',
`response_data_len` UInt32 COMMENT '应答数据长度',
`only_request` UInt8 COMMENT '只有请求',
`only_response` UInt8 COMMENT '只有应答',
`payload_hash_inconsistent` Nullable(UInt8) COMMENT '请求应答payload hash不一致'
) 
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_icmp_log_record)
COMMENT '应用层协议详单-ICMP协议详单表';