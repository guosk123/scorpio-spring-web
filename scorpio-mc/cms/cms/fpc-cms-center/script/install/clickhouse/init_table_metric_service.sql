DROP TABLE IF EXISTS fpc.d_fpc_metric_service_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_service_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间',
`network_id`String COMMENT '网络ID',
`service_id`String COMMENT '业务ID',
`total_bytes`   UInt64 COMMENT '总字节数',
`byteps_peak`   UInt64 COMMENT '峰值带宽(byte/s)',
`downstream_bytes`  UInt64 COMMENT '下行流量总字节数',
`upstream_bytes`UInt64 COMMENT '上行流量总字节数',
`filter_discard_bytes`  UInt64 COMMENT '捕获过滤流量总字节数',
`overload_discard_bytes`UInt64 COMMENT '超规格流量总字节数',
`deduplication_bytes`   UInt64 COMMENT '去重流量总字节数',
`total_packets` UInt64 COMMENT '总包数',
`packetps_peak` UInt64 COMMENT '峰值包数',
`downstream_packets`UInt64 COMMENT '下行流量总包数',
`upstream_packets`  UInt64 COMMENT '上行流量总包数',
`filter_discard_packets`UInt64 COMMENT '捕获过滤总包数',
`overload_discard_packets`  UInt64 COMMENT '超规格流量总包数',
`deduplication_packets` UInt64 COMMENT '去重流量总包数',
`active_sessions`   UInt64 COMMENT '活跃会话数',
`concurrent_sessions`   UInt64 COMMENT '会话最大并发数',
`concurrent_tcp_sessions`   UInt64 COMMENT 'TCP会话最大并发数',
`concurrent_udp_sessions`   UInt64 COMMENT 'UDP会话最大并发数',
`concurrent_arp_sessions`   UInt64 COMMENT 'ARP会话最大并发数',
`concurrent_icmp_sessions`  UInt64 COMMENT 'ICMP会话最大并发数',
`established_sessions`  UInt64 COMMENT '新建会话数',
`destroyed_sessions`UInt64 COMMENT '销毁会话数',
`established_tcp_sessions` UInt64 COMMENT 'TCP会话新建数',
`established_udp_sessions` UInt64 COMMENT 'UDP会话新建数',
`established_icmp_sessions` UInt64 COMMENT 'ICMP会话新建数',
`established_other_sessions` UInt64 COMMENT '其它会话新建数',
`established_upstream_sessions` UInt64 COMMENT '上行会话新建数',
`established_downstream_sessions` UInt64 COMMENT '下行会话新建数',
`fragment_total_bytes` UInt64 COMMENT '分片包总字节数',
`fragment_total_packets` UInt64 COMMENT '分片包总包数',
`tcp_syn_packets` UInt64 COMMENT 'TCP协议SYN包数',
`tcp_syn_ack_packets` UInt64 COMMENT 'TCP协议SYNACK包数',
`tcp_syn_rst_packets` UInt64 COMMENT 'TCP协议RST包数',
`tcp_established_time_avg` UInt64 COMMENT 'TCP会话建连平均时间',
`tcp_zero_window_packets` UInt64 COMMENT 'TCP会话零窗口包数',
`unique_ip_counts`  UInt64 COMMENT '独立用户数(源IP地址数量)',

`server_response_latency` UInt64 COMMENT '全部服务-服务器-响应总时延(ms)',
`server_response_latency_inside_service` UInt64 COMMENT '内部服务-服务器-响应总时延(ms)',
`server_response_latency_outside_service` UInt64 COMMENT '外部服务-服务器-响应总时延(ms)',

`server_response_latency_counts` UInt64 COMMENT '全部服务-服务器-响应时延总次数',
`server_response_latency_counts_inside_service` UInt64 COMMENT '内部服务-服务器-响应时延总次数',
`server_response_latency_counts_outside_service`UInt64 COMMENT '外部服务-服务器-响应时延总次数',

`server_response_latency_peak` UInt64 COMMENT '全部服务-服务器-响应时延峰值',
`server_response_latency_peak_inside_service`   UInt64 COMMENT '内部服务-服务器-响应时延峰值',
`server_response_latency_peak_outside_service`  UInt64 COMMENT '外部服务-服务器-响应时延峰值',

`server_response_fast_counts` UInt64 COMMENT '全部服务-服务器-快速响应次数',
`server_response_fast_counts_inside_service` UInt64 COMMENT '内部服务-服务器-快速响应次数',
`server_response_fast_counts_outside_service`   UInt64 COMMENT '外部服务-服务器-快速响应次数',

`server_response_normal_counts` UInt64 COMMENT '全部服务-服务器-正常响应次数',
`server_response_normal_counts_inside_service`  UInt64 COMMENT '内部服务-服务器-正常响应次数',
`server_response_normal_counts_outside_service` UInt64 COMMENT '外部服务-服务器-正常响应次数',

`server_response_timeout_counts` UInt64 COMMENT '全部服务-服务器-超时响应次数',
`server_response_timeout_counts_inside_service` UInt64 COMMENT '内部服务-服务器-超时响应次数',
`server_response_timeout_counts_outside_service` UInt64 COMMENT '外部服务-服务器-超时响应次数',

`tcp_client_network_latency` UInt64 COMMENT '全部服务-TCP客户端-网络总时延(ms)',
`tcp_client_network_latency_inside_service` UInt64 COMMENT '内部服务-TCP客户端-网络总时延(ms)',
`tcp_client_network_latency_outside_service` UInt64 COMMENT '外部服务-TCP客户端-网络总时延(ms)',

`tcp_client_network_latency_counts` UInt64 COMMENT '全部服务-TCP客户端-网络时延次数',
`tcp_client_network_latency_counts_inside_service`  UInt64 COMMENT '内部服务-TCP客户端-网络时延次数',
`tcp_client_network_latency_counts_outside_service` UInt64 COMMENT '外部服务-TCP客户端-网络时延次数',

`tcp_server_network_latency` UInt64 COMMENT '全部服务-TCP服务端-网络总时延',
`tcp_server_network_latency_inside_service` UInt64 COMMENT '内部服务-TCP服务端-网络总时延',
`tcp_server_network_latency_outside_service` UInt64 COMMENT '外部服务-TCP服务端-网络总时延',

`tcp_server_network_latency_counts` UInt64 COMMENT '全部服务-TCP服务端-网络时延次数',
`tcp_server_network_latency_counts_inside_service`  UInt64 COMMENT '内部服务-TCP服务端-网络时延次数',
`tcp_server_network_latency_counts_outside_service` UInt64 COMMENT '外部服务-TCP服务端-网络时延次数',

`tcp_established_success_counts` UInt64 COMMENT '全部服务-TCP会话建连成功次数',
`tcp_established_success_counts_inside_service` UInt64 COMMENT '内部服务-TCP会话建连成功次数',
`tcp_established_success_counts_outside_service` UInt64 COMMENT '外部服务-TCP会话建连成功次数',

`tcp_established_fail_counts` UInt64 COMMENT '全部服务-TCP会话建连失败次数',
`tcp_established_fail_counts_inside_service` UInt64 COMMENT '内部服务-TCP会话建连失败次数',
`tcp_established_fail_counts_outside_service`   UInt64 COMMENT '外部服务-TCP会话建连失败次数',

`tcp_client_syn_packets` UInt64 COMMENT '全部服务-TCP协议客户端-SYN包数',
`tcp_client_syn_packets_inside_service` UInt64 COMMENT '内部服务-TCP协议客户端-SYN包数',
`tcp_client_syn_packets_outside_service` UInt64 COMMENT '外部服务-TCP协议客户端-SYN包数',

`tcp_server_syn_packets` UInt64 COMMENT '全部服务-TCP协议服务端-SYNACK包数',
`tcp_server_syn_packets_inside_service` UInt64 COMMENT '内部服务-TCP协议服务端-SYNACK包数',
`tcp_server_syn_packets_outside_service` UInt64 COMMENT '外部服务-TCP协议服务端-SYNACK包数',

`tcp_client_retransmission_packets` UInt64 COMMENT '全部服务-TCP客户端-重传包数',
`tcp_client_retransmission_packets_inside_service`  UInt64 COMMENT '内部服务-TCP客户端-重传包数',
`tcp_client_retransmission_packets_outside_service` UInt64 COMMENT '外部服务-TCP客户端-重传包数',

`tcp_client_packets` UInt64 COMMENT '全部服务-TCP客户端-总包数',
`tcp_client_packets_inside_service` UInt64 COMMENT '内部服务-TCP客户端-总包数',
`tcp_client_packets_outside_service` UInt64 COMMENT '外部服务-TCP客户端-总包数',

`tcp_server_retransmission_packets` UInt64 COMMENT '全部服务-TCP服务端-重传包数',
`tcp_server_retransmission_packets_inside_service`  UInt64 COMMENT '内部服务-TCP服务端-重传包数',
`tcp_server_retransmission_packets_outside_service` UInt64 COMMENT '外部服务-TCP服务端-重传包数',

`tcp_server_packets` UInt64 COMMENT '全部服务-TCP服务端-总包数',
`tcp_server_packets_inside_service` UInt64 COMMENT '内部服务-TCP服务端-总包数',
`tcp_server_packets_outside_service` UInt64 COMMENT '外部服务-TCP服务端-总包数',

`tcp_client_zero_window_packets` UInt64 COMMENT '全部服务-TCP客户端-零窗口包数',
`tcp_client_zero_window_packets_inside_service` UInt64 COMMENT '内部服务-TCP客户端-零窗口包数',
`tcp_client_zero_window_packets_outside_service` UInt64 COMMENT '外部服务-TCP客户端-零窗口包数',

`tcp_server_zero_window_packets` UInt64 COMMENT '全部服务-TCP服务端-零窗口包数',
`tcp_server_zero_window_packets_inside_service` UInt64 COMMENT '内部服务-TCP服务端-零窗口包数',
`tcp_server_zero_window_packets_outside_service` UInt64 COMMENT '外部服务-TCP服务端-零窗口包数'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_service_data_record)
COMMENT '业务性能统计表';