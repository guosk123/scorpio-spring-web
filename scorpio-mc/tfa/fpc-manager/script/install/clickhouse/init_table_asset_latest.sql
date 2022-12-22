DROP TABLE IF EXISTS fpc.t_fpc_asset_latest;
CREATE TABLE IF NOT EXISTS fpc.t_fpc_asset_latest
(
    `timestamp` AggregateFunction(max, DateTime64(3, 'UTC')) COMMENT '最新上报时间',
    `ip` IPv6 COMMENT '资产所属IP',
    `account` String COMMENT '账户信息',
    `type` UInt16 COMMENT '统计类型（1：设备类型；2：监听端口；3：服务标签；4：操作系统；）',
    `value1` LowCardinality(String) COMMENT '统计属性值（主属性）',
    `value2` LowCardinality(String) COMMENT '统计属性值（副属性）',
    INDEX i_ip ip TYPE minmax GRANULARITY 4,
    INDEX i_account account TYPE bloom_filter GRANULARITY 4,
    INDEX i_type type TYPE minmax GRANULARITY 4,
    INDEX i_value1 value1 TYPE bloom_filter GRANULARITY 4
)
ENGINE = AggregatingMergeTree
ORDER BY (ip, account, type, value1)
SETTINGS storage_policy = 'policy_hot_cold_0', index_granularity = 8192
COMMENT '资产最新信息表';