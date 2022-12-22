CREATE TABLE IF NOT EXISTS fpc.t_fpc_asset_information
( 
	`timestamp` DateTime64(3, 'UTC') COMMENT '记录时间',
	`ip` IPv6 COMMENT '资产所属IP',
	`account` String COMMENT '账户信息',
	`type` UInt16 COMMENT '统计类型（1：设备类型；2：监听端口；3：服务标签；4：操作系统；）',
	`value1` LowCardinality(String) COMMENT '统计属性值（主属性）',
	`value2` LowCardinality(String) COMMENT '统计属性值（副属性）',
	INDEX i_ip ip TYPE minmax GRANULARITY 4,
	INDEX i_account account TYPE bloom_filter GRANULARITY 4,
	INDEX i_type type TYPE minmax GRANULARITY 4
)  
ENGINE = MergeTree() 
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY timestamp
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '资产表';

/* Meterialized Viewes */
CREATE MATERIALIZED VIEW IF NOT EXISTS fpc.v_fpc_asset_first
TO fpc.t_fpc_asset_first
AS SELECT
    minState(timestamp) AS timestamp,
    ip,
    account
FROM fpc.t_fpc_asset_information
GROUP BY
    ip,
    account;
    
CREATE MATERIALIZED VIEW IF NOT EXISTS fpc.v_fpc_asset_latest 
TO fpc.t_fpc_asset_latest 
AS SELECT
    maxState(timestamp) AS timestamp,
    ip,
    account,
    type,
    value1,
    value2
FROM fpc.t_fpc_asset_information
GROUP BY
    ip,
    account,
    type,
    value1,
    value2;