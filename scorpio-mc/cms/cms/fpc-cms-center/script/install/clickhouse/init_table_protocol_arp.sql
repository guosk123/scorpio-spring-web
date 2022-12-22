DROP TABLE IF EXISTS fpc.d_fpc_protocol_arp_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_arp_log_record
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID', 
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_mac` LowCardinality(String) COMMENT '源MAC地址',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_mac` LowCardinality(String) COMMENT '目的MAC地址',
`type` UInt8 COMMENT '报文类型'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_arp_log_record)
COMMENT '应用层协议详单-ARP协议详单表';