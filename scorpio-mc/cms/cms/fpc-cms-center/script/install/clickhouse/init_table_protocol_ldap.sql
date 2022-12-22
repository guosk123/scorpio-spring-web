DROP TABLE IF EXISTS fpc.d_fpc_protocol_ldap_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_ldap_log_record
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
`op_type` UInt16 COMMENT '请求类型',
`res_status` UInt16 COMMENT '回复状态',
`req_content` Map(String,String) COMMENT '请求内容',
`res_content` Map(String,String) COMMENT '回复内容'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_ldap_log_record)
COMMENT '应用层协议详单-Ldap协议详单表';
