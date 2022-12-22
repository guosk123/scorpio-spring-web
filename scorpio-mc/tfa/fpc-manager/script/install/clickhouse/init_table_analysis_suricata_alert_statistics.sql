CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_suricata_alert_statistics
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '记录时间',
`source` LowCardinality(String) COMMENT '来源',
`classtype_id` LowCardinality(String) COMMENT '规则分类ID',
`signature_severity` LowCardinality(String) COMMENT '严重级别',
`basic_tag` LowCardinality(String) COMMENT '基础标签',
`type` LowCardinality(String) COMMENT '统计类型',
`key` String COMMENT '统计指标',
`value` UInt64 COMMENT '统计值'
)
ENGINE = SummingMergeTree(`value`)
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp, source, classtype_id, signature_severity, basic_tag, type, key)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '安全分析-安全告警统计'