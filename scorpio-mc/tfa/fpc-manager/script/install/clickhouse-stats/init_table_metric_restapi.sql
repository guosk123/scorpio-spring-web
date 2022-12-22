CREATE TABLE IF NOT EXISTS  fpc.t_fpc_metric_restapi_data_record
(
    `timestamp` DateTime('UTC') COMMENT '访问时间',
    `api_name` String COMMENT 'API名称',
    `uri` String COMMENT 'URI',
    `method` String COMMENT '请求类型',
    `user_ip` String COMMENT '来访IP',
    `user_id` String COMMENT '用户ID',
    `status` UInt8 COMMENT '访问结果',
    `response` String COMMENT '应答详情',
    INDEX i_api_name (api_name) TYPE bloom_filter() GRANULARITY 4,
    INDEX i_uri (uri) TYPE bloom_filter() GRANULARITY 4,
    INDEX i_method (method) TYPE bloom_filter() GRANULARITY 4,
    INDEX i_user_ip (user_ip) TYPE bloom_filter() GRANULARITY 4,
    INDEX i_user_id (user_id) TYPE bloom_filter() GRANULARITY 4,
    INDEX i_status (status) TYPE minmax GRANULARITY 4,
    INDEX i_response (response) TYPE bloom_filter() GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT 'restAPI接口调用统计表'