CREATE TABLE IF NOT EXISTS fpc.t_fpc_metric_os_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`type` String COMMENT '操作系统类型',
`ip_list` Array(String) COMMENT 'IP地址列表',
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4, 
INDEX i_type (type) TYPE bloom_filter() GRANULARITY 4
) 
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '操作系统分布统计表'