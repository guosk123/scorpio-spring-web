DROP TABLE IF EXISTS fpc.d_fpc_metric_netif_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_netif_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`netif_name` String COMMENT '网口名称',
`total_bytes` UInt64 COMMENT '总字节数',
`downstream_bytes`UInt64 COMMENT '下行流量总字节数',
`upstream_bytes` UInt64 COMMENT '上行流量总字节数',
`transmit_bytes` UInt64 COMMENT '发送流量总字节数',
`total_packets` UInt64 COMMENT '总包数',
`downstream_packets` UInt64 COMMENT '下行流量总包数',
`upstream_packets` UInt64 COMMENT '上行流量总包数',
`transmit_packets` UInt64 COMMENT '发送流量总包数'
)
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_netif_data_record)
COMMENT '网口统计表';
