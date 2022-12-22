DROP TABLE IF EXISTS fpc.d_fpc_protocol_dict;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_dict
(
`protocol` LowCardinality(String) COMMENT '协议',
`field` LowCardinality(String) COMMENT 'map类型字段',
`key` String COMMENT 'key值'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_dict)
COMMENT '协议字典表';