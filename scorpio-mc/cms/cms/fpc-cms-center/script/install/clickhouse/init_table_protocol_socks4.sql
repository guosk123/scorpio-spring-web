DROP TABLE IF EXISTS fpc.d_fpc_protocol_socks4_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_socks4_log_record
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
`channel_state` UInt8 COMMENT '连接状态'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_socks4_log_record)
COMMENT '应用层协议详单-SOCKS4协议详单表';