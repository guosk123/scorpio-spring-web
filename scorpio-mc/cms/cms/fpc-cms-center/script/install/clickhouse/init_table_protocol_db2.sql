DROP TABLE IF EXISTS fpc.d_fpc_protocol_db2_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_db2_log_record
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
`code_point` LowCardinality(String) COMMENT 'DRDA用途',
`data` Map(String,String) COMMENT 'DRDA数据'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_db2_log_record)
COMMENT '应用层协议详单-DB2协议详单';