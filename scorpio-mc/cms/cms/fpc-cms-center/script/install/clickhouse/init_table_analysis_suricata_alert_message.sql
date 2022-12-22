DROP TABLE IF EXISTS fpc.d_fpc_analysis_suricata_alert_message;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_analysis_suricata_alert_message
(
`timestamp` DateTime64(3, 'UTC') COMMENT '记录时间',
`sid` UInt32 COMMENT '检测规则ID',
`msg` String COMMENT '规则描述',
`network_id` LowCardinality(String) COMMENT '网络ID',
`classtype_id` String COMMENT '规则分类ID',
`mitre_tactic_id` String COMMENT '战术分类ID',
`mitre_technique_id` String COMMENT '技术分类ID',
`cve` String COMMENT 'CVE',
`cnnvd` String COMMENT 'CNNVD',
`signature_severity` String COMMENT '严重级别',
`target` String COMMENT '受害者',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6',
`src_port` UInt16 COMMENT '源端口',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`dest_port` UInt16 COMMENT '目的端口',
`protocol` LowCardinality(String) COMMENT '传输层协议',
`l7_protocol` String COMMENT '应用协议',
`flow_id` UInt64 COMMENT '流ID',
`domain` String COMMENT '域名',
`url` String COMMENT 'URL',
`country_id_initiator` Nullable(UInt16) COMMENT '源国家ID',
`province_id_initiator` Nullable(UInt16) COMMENT '源省份ID',
`city_id_initiator` Nullable(UInt16) COMMENT '源城市ID',
`country_id_responder` Nullable(UInt16) COMMENT '目的国家ID',
`province_id_responder` Nullable(UInt16) COMMENT '目的省份ID',
`city_id_responder` Nullable(UInt16) COMMENT '目的城市ID',
`source` LowCardinality(String) COMMENT '来源',
`tag` Array(String) COMMENT '标签',
`basic_tag` LowCardinality(String) COMMENT '基础标签'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_analysis_suricata_alert_message)
COMMENT '安全分析-安全告警消息';