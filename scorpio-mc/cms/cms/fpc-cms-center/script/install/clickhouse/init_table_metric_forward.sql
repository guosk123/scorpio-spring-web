DROP TABLE IF EXISTS fpc.d_fpc_metric_forward_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_forward_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '上报时间',
`policy_id` String COMMENT '策略ID',
`network_id` String COMMENT '网络ID',
`netif_name` String COMMENT '网口名称',
`forward_total_bytes` UInt64 COMMENT '转发总字节数',
`forward_success_bytes` UInt64 COMMENT '转发成功字节数',
`forward_fail_bytes` UInt64 COMMENT '转发失败字节数',
`forward_total_packets` UInt64 COMMENT '转发总包数',
`forward_success_packets`UInt64 COMMENT '转发成功包数',
`forward_fail_packets` UInt64 COMMENT '转发失败包数'
)
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_forward_data_record)
COMMENT '转发流量统计表';
