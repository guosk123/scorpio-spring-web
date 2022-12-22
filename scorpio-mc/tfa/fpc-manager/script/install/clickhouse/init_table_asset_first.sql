CREATE TABLE IF NOT EXISTS fpc.t_fpc_asset_first
(
    `timestamp` AggregateFunction(min, DateTime64(3, 'UTC')) COMMENT '首次上报时间',
    `ip` IPv6 COMMENT '资产所属IP',
    `account` String COMMENT '账户信息',
    INDEX i_ip ip TYPE minmax GRANULARITY 4,
	INDEX i_account account TYPE bloom_filter GRANULARITY 4
)
ENGINE = AggregatingMergeTree
ORDER BY (ip, account)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '资产首次上报时间表';
