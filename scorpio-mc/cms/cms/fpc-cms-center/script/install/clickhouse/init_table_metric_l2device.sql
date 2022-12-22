DROP TABLE IF EXISTS fpc.d_fpc_metric_l2device_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_l2device_data_record
(
`timestamp` DateTime64(3, 'UTC') COMMENT '记录时间',
`network_id` String COMMENT '网络ID',
`service_id` String COMMENT '业务ID',
`mac_address` String COMMENT 'MAC地址',
`ethernet_type` UInt8 COMMENT '三层协议类型',
`total_bytes` UInt64 COMMENT '总字节数',
`downstream_bytes`UInt64 COMMENT '下行流量总字节数',
`upstream_bytes` UInt64 COMMENT '上行流量总字节数',
`total_packets` UInt64 COMMENT '总包数',
`downstream_packets` UInt64 COMMENT '下行包数',
`upstream_packets` UInt64 COMMENT '上行包数',
`established_sessions` UInt64 COMMENT '新建会话数',
`tcp_client_network_latency` UInt64 COMMENT 'TCP客户端网络总时延(ms)',
`tcp_client_network_latency_counts` UInt64 COMMENT 'TCP客户端网络时延次数',
`tcp_server_network_latency` UInt64 COMMENT 'TCP服务端网络总时延(ms)',
`tcp_server_network_latency_counts` UInt64 COMMENT 'TCP服务端网络时延次数',
`server_response_latency` UInt64 COMMENT '服务器响应总时延(ms)',
`server_response_latency_counts` UInt64 COMMENT '服务器响应时延总次数',
`tcp_client_retransmission_packets` UInt64 COMMENT 'TCP客户端重传包数',
`tcp_client_packets` UInt64 COMMENT 'TCP客户端包数',
`tcp_server_retransmission_packets` UInt64 COMMENT 'TCP服务端重传包数',
`tcp_server_packets` UInt64 COMMENT 'TCP服务端包数',
`tcp_client_zero_window_packets` UInt64 COMMENT 'TCP客户端零窗口包数',
`tcp_server_zero_window_packets` UInt64 COMMENT 'TCP服务端零窗口包数',
`tcp_established_fail_counts` UInt64 COMMENT 'TCP会话建连失败次数',
`tcp_established_success_counts` UInt64 COMMENT 'TCP会话建连成功次数'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_l2device_data_record)
COMMENT 'MAC地址统计表';