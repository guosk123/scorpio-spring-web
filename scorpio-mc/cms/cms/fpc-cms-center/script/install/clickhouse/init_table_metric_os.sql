DROP TABLE IF EXISTS fpc.d_fpc_metric_os_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_os_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id` String COMMENT '网络ID',
`type` String COMMENT '操作系统类型',
`ip_list` Array(String) COMMENT 'IP地址列表'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_os_data_record)
COMMENT '操作系统分布统计表';