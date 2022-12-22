DROP TABLE IF EXISTS fpc.d_fpc_metric_dhcp_data_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metric_dhcp_data_record
( 
`timestamp` DateTime64(3, 'UTC') COMMENT '时间', 
`network_id` String COMMENT '网络ID',
`client_ip_address` String COMMENT '客户端IP地址',
`server_ip_address` String COMMENT '服务端IP地址',
`client_mac_address` String COMMENT '客户端MAC地址',
`server_mac_address` String COMMENT '服务端MAC地址',
`message_type` UInt8 COMMENT '消息类型',
`dhcp_version` UInt8 COMMENT 'DHCP版本',
`total_bytes` UInt64 COMMENT '总字节数',
`send_bytes`UInt64 COMMENT '客户端发送字节数',
`receive_bytes` UInt64 COMMENT '客户端接收字节数', 
`total_packets` UInt64 COMMENT '总包数',
`send_packets` UInt64 COMMENT '客户端发送数据包数',
`receive_packets` UInt64 COMMENT '服务端接收数据包数'
) 
ENGINE = Distributed(ch_stats_servers, fpc, t_fpc_metric_dhcp_data_record)
COMMENT 'DHCP统计表';