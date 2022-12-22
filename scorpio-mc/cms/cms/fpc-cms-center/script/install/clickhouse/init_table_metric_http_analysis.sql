DROP TABLE IF EXISTS fpc.d_fpc_metric_http_analysis_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_http_analysis_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`type` String COMMENT '统计分类', 
`key`String COMMENT '统计指标',
`value` UInt64 COMMENT '统计值'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_http_analysis_data_record)
COMMENT 'HTTP分析统计表';