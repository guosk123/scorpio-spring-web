CREATE TABLE IF NOT EXISTS fpc.t_fpc_beiyou_analysis_result_log_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`flow_id` UInt64 COMMENT '流ID',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6',
`src_port` UInt16 COMMENT '源端口',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`dest_port` UInt16 COMMENT '目的端口',
`issuer` String COMMENT '证书签发人',
`common_name` String COMMENT '证书使用者',
`validity` String COMMENT '证书有效期',
`certs_len` UInt16 COMMENT '证书链长度',
`white_prob` Float COMMENT '预测为白的概率(0.0-1.0)',
`black_prob` Float COMMENT '预测为黑的概率(0.0-1.0)',
`suspicious` UInt8 COMMENT '黑白标记(0:正常流量,1:可疑流量)',
`encryption_classification` String COMMENT '加密分类',
`family` String COMMENT '家族',
`organization` String COMMENT '组织',
`url` String COMMENT '参考描述URL',
`relative_host` Array(String) COMMENT '相关域名',
INDEX i_flow_id (flow_id) TYPE bloom_filter GRANULARITY 4,
INDEX i_src_ipv4 (src_ipv4) TYPE minmax GRANULARITY 4,
INDEX i_src_ipv6 (src_ipv6) TYPE minmax GRANULARITY 4,
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4,
INDEX i_dest_ipv4 (dest_ipv4) TYPE minmax GRANULARITY 4,
INDEX i_dest_ipv6 (dest_ipv6) TYPE minmax GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_family (family) TYPE bloom_filter GRANULARITY 4,
INDEX i_organization (organization) TYPE bloom_filter GRANULARITY 4,
INDEX i_relative_host (relative_host) TYPE bloom_filter GRANULARITY 4
) 
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '加密流量分类表'