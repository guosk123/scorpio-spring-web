DROP TABLE IF EXISTS fpc.d_fpc_protocol_dhcp_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_dhcp_log_record
(
`flow_id` UInt64 COMMENT '流ID', 
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间', 
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID', 
`application_id` UInt32 COMMENT '应用ID',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6', 
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4', 
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6', 
`src_port` UInt16 COMMENT '源端口',
`dest_port` UInt16 COMMENT '目的端口',
`version` UInt8 COMMENT 'DHCP版本', 
`src_mac` LowCardinality(String) COMMENT '源MAC地址', 
`dest_mac` LowCardinality(String) COMMENT '目的MAC地址', 
`message_type` UInt8 COMMENT 'DHCP消息类型', 
`transaction_id` UInt32 COMMENT '事务ID', 
`parameters` Array(UInt16) COMMENT '请求参数列表', 
`offered_ipv4_address`  Nullable(IPv4) COMMENT '分配的IPv4地址', 
`offered_ipv6_address` Nullable(IPv6) COMMENT '分配的IPv6地址', 
`upstream_bytes` UInt32 COMMENT '请求字节数', 
`downstream_bytes` UInt32 COMMENT '应答字节数'
) 
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_dhcp_log_record)
COMMENT '应用层协议详单-DHCP协议详单表';