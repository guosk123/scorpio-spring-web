DROP TABLE IF EXISTS fpc.d_fpc_metric_dscp_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_dscp_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`service_id` String COMMENT '业务ID',
`type` String COMMENT 'DSCP类型',
`total_bytes` UInt64 COMMENT '总字节数',
`total_packets` UInt64 COMMENT '总包数'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_dscp_data_record)
COMMENT 'DSCP统计表';