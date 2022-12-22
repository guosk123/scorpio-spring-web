DROP TABLE IF EXISTS fpc.d_fpc_protocol_dns_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_dns_log_record
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
`answer` String COMMENT 'DNS应答内容(JSON)',
`domain` String COMMENT 'DNS请求域名',
`domain_ipv4` Array(IPv4) COMMENT '域名对应的IPv4地址',
`domain_ipv6` Array(IPv6) COMMENT '域名对应的IPv6地址',
`domain_intelligence` UInt8 COMMENT 'DNS请求域名情报（0为黑，1为白，2为未知;)',
`dns_rcode` Int32 COMMENT 'DNS协议返回码',
`dns_rcode_name` LowCardinality(String) COMMENT 'DNS协议返回码名称',
`dns_queries` String COMMENT 'DNS请求内容(JSON)',
`subdomain_count` UInt32 COMMENT '请求域名数量',
`transaction_id` String COMMENT '事务ID'
)  
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_dns_log_record)
COMMENT '应用层协议详单-DNS协议详单表';