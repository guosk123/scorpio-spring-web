CREATE TABLE IF NOT EXISTS fpc.t_fpc_mail_alert_message
(
`timestamp` DateTime64(9, 'UTC') COMMENT '告警生成时间',
`src_ip` IPv6 COMMENT '源ip',
`dest_ip` IPv6 COMMENT '目的ip',
`src_port` UInt16 COMMENT '源端口',
`dest_port` UInt16 COMMENT '目的端口',
`protocol` String COMMENT '协议',
`mail_address` String COMMENT  '邮箱地址',
`country_id` UInt16 COMMENT '国家ID',
`province_id` UInt16 COMMENT '省份ID',
`city_id` UInt16 COMMENT '城市ID',
`login_timestamp` DateTime64(9, 'UTC') COMMENT '登录时间',
`description`  LowCardinality(String) COMMENT '描述',
`flow_id` UInt64 COMMENT '流ID',
`network_id` LowCardinality(String) COMMENT '网络ID',
`rule_id` LowCardinality(String) COMMENT '邮件规则ID',
INDEX i_flow_id (flow_id) TYPE minmax GRANULARITY 4,
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_timestamp (timestamp) TYPE minmax GRANULARITY 4,
INDEX i_src_ip (src_ip) TYPE minmax GRANULARITY 4,
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4,
INDEX i_dest_ip (dest_ip) TYPE minmax GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_mail_address (mail_address) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_protocol (protocol) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_country_id (country_id) TYPE minmax GRANULARITY 4,
INDEX i_province_id (province_id) TYPE minmax GRANULARITY 4,
INDEX i_city_id (city_id) TYPE minmax GRANULARITY 4,
INDEX i_rule_id (rule_id) TYPE bloom_filter() GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0', index_granularity = 8192
COMMENT '邮件异常登录告警表'