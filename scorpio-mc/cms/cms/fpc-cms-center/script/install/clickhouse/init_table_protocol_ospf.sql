DROP TABLE IF EXISTS fpc.d_fpc_protocol_ospf_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_ospf_log_record
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
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`version` UInt8 COMMENT '版本',
`message_type` UInt8 COMMENT '消息类型',
`packet_length` UInt16 COMMENT '包长',
`source_ospf_router` UInt32 COMMENT '源路由器',
`area_id` UInt32 COMMENT '区域ID',
`link_state_ipv4_address` Array(String) COMMENT '通告IPv4地址',
`link_state_ipv6_address` Array(String) COMMENT '通告IPv6地址',
`message` String COMMENT '消息详情'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_ospf_log_record)
COMMENT '应用层协议详单-OSPF协议详单表';