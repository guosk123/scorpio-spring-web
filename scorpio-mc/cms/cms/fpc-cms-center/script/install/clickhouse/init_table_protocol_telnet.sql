DROP TABLE IF EXISTS fpc.d_fpc_protocol_telnet_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_telnet_log_record
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
`reply` LowCardinality(String) COMMENT '操作结果'
)  
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_telnet_log_record)
COMMENT '应用层协议详单-TELNET协议详单表';