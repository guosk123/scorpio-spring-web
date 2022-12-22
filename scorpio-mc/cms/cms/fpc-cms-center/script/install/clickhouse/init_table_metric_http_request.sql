DROP TABLE IF EXISTS fpc.d_fpc_metric_http_request_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_http_request_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`request_counts` UInt64 COMMENT '请求总数',
`response_counts`UInt64 COMMENT '响应总数',
`error_response_counts` UInt64 COMMENT '错误总数(状态码4xx和5xx)'
)
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_http_request_data_record)
COMMENT 'HTTP请求状态统计表';