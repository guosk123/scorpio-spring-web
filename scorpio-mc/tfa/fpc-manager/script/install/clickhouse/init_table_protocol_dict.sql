CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_dict
(
    `protocol` LowCardinality(String) COMMENT '协议',
    `field` LowCardinality(String) COMMENT 'map类型字段',
    `key` String COMMENT 'key值'
)
ENGINE = SummingMergeTree()
ORDER BY (protocol,field,key)
SETTINGS storage_policy = 'policy_hot_cold_0';
