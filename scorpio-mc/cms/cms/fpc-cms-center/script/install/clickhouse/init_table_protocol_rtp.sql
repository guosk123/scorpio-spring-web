DROP TABLE IF EXISTS fpc.d_fpc_protocol_rtp_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_rtp_log_record
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT 'RTP流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id` UInt32 COMMENT '应用ID',
`start_time` DateTime64(9, 'UTC') COMMENT '通讯开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '通讯结束时间',
`invite_time` DateTime64(9, 'UTC') COMMENT '通讯邀请时间',
`report_time` DateTime64(9, 'UTC') COMMENT 'rtp流分析数据上报时间',
`from` LowCardinality(String) COMMENT '发送方设备编码',
`src_ip` IPv6 COMMENT '发送方IP',
`src_port` UInt16 COMMENT '发送方端口',
`to` LowCardinality(String) COMMENT '接收方设备编码',
`dest_ip` IPv6 COMMENT '接收方IP',
`dest_port` UInt16 COMMENT '接收方端口',
`from_device_type` UInt16 COMMENT '发送方设备类型',
`to_device_type` UInt16 COMMENT '接收方设备类型',
`ip_protocol` LowCardinality(String) COMMENT 'rtp流传输层协议',
`ssrc` UInt32 COMMENT 'rtp流SSRC',
`status` UInt8 COMMENT '视频流状态',
`rtp_total_packets` UInt32 COMMENT '此次上报的rtp总包数',
`rtp_loss_packets` UInt32 COMMENT '此次上报的rtp丢包数',
`jitter_max` UInt32 COMMENT '此次上报的rtp最大抖动 单位是微秒',
`jitter_mean` UInt32 COMMENT '此次上报的rtp平均抖动 单位是微秒',
`payload` LowCardinality(String) COMMENT 'rtp流负载类型',
`invite_src_ip` IPv6 COMMENT '控制通道源IP',
`invite_src_port` UInt16 COMMENT '控制通道源端口',
`invite_dest_ip` IPv6 COMMENT '控制通道目的IP',
`invite_dest_port` UInt16 COMMENT '控制通道目的端口',
`invite_ip_protocol` LowCardinality(String) COMMENT '控制通道传输层协议',
`sip_flow_id` UInt64 COMMENT '控制通道流ID'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_rtp_log_record)
COMMENT 'RTP流分析';

DROP TABLE IF EXISTS fpc.d_fpc_protocol_rtp_log_record_analysis;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_rtp_log_record_analysis
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id`          UInt64 COMMENT 'RTP流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id`   UInt32 COMMENT '应用ID',
`start_time`       DateTime64(9, 'UTC') COMMENT '通讯开始时间',
`invite_time`      DateTime64(9, 'UTC') COMMENT '通讯邀请时间',
`from` LowCardinality(String) COMMENT '发送方设备编码',
`src_ip`           IPv6 COMMENT '发送方IP',
`src_port`         UInt16 COMMENT '发送方端口',
`to` LowCardinality(String) COMMENT '接收方设备编码',
`dest_ip`          IPv6 COMMENT '接收方IP',
`dest_port`        UInt16 COMMENT '接收方端口',
`ip_protocol` LowCardinality(String) COMMENT 'rtp流传输层协议',
`invite_src_ip`    IPv6 COMMENT '控制通道源IP',
`invite_src_port`  UInt16 COMMENT '控制通道源端口',
`invite_dest_ip`   IPv6 COMMENT '控制通道目的IP',
`invite_dest_port` UInt16 COMMENT '控制通道目的端口',
`invite_ip_protocol` LowCardinality(String) COMMENT '控制通道传输层协议',
`sip_flow_id`      UInt64 COMMENT '控制通道流ID',
`end_time` AggregateFunction(max,DateTime64(9,'UTC')) COMMENT '通讯结束时间',
`ssrc` AggregateFunction(max,UInt32) COMMENT 'rtp流SSRC',
`status` AggregateFunction(max,UInt8) COMMENT '视频流状态',
`rtp_total_packets` AggregateFunction(sum,UInt32) COMMENT '此次上报的rtp总包数',
`rtp_loss_packets` AggregateFunction(sum,UInt32) COMMENT '此次上报的rtp丢包数',
`jitter_max` AggregateFunction(max,UInt32) COMMENT '此次上报的rtp最大抖动 单位是微秒',
`jitter_mean` AggregateFunction(avg,UInt32) COMMENT '此次上报的rtp平均抖动 单位是微秒',
`payload` AggregateFunction(any,String) COMMENT 'rtp流负载类型'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_rtp_log_record_analysis)
COMMENT 'RTP流分析';

