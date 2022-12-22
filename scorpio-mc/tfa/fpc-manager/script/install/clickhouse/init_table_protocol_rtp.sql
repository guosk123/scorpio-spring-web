CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_rtp_log_record
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
    `sip_flow_id` UInt64 COMMENT '控制通道流ID',
    INDEX i_policy_name policy_name TYPE bloom_filter GRANULARITY 4,
    INDEX i_flow_id flow_id TYPE minmax GRANULARITY 4,
    INDEX i_network_id network_id TYPE bloom_filter GRANULARITY 4,
    INDEX i_service_id service_id TYPE bloom_filter GRANULARITY 4,
    INDEX i_application_id application_id TYPE minmax GRANULARITY 4,
    INDEX i_start_time start_time TYPE minmax GRANULARITY 4,
    INDEX i_end_time end_time TYPE minmax GRANULARITY 4,
    INDEX i_invite_time invite_time TYPE minmax GRANULARITY 4,
    INDEX i_from from TYPE bloom_filter GRANULARITY 4,
    INDEX i_to to TYPE bloom_filter GRANULARITY 4,
    INDEX i_src_ip src_ip TYPE minmax GRANULARITY 4,
    INDEX i_src_port src_port TYPE minmax GRANULARITY 4,
    INDEX i_dest_ip dest_ip TYPE minmax GRANULARITY 4,
    INDEX i_dest_port dest_port TYPE minmax GRANULARITY 4,
    INDEX i_from_device_type from_device_type TYPE minmax GRANULARITY 4,
    INDEX i_to_device_type to_device_type TYPE minmax GRANULARITY 4,
    INDEX i_ip_protocol ip_protocol TYPE bloom_filter GRANULARITY 4,
    INDEX i_ssrc ssrc TYPE minmax GRANULARITY 4,
    INDEX i_status status TYPE minmax GRANULARITY 4,
    INDEX i_rtp_total_packets rtp_total_packets TYPE minmax GRANULARITY 4,
    INDEX i_rtp_loss_packets rtp_loss_packets TYPE minmax GRANULARITY 4,
    INDEX i_jitter_max jitter_max TYPE minmax GRANULARITY 4,
    INDEX i_jitter_mean jitter_mean TYPE minmax GRANULARITY 4,
    INDEX i_payload payload TYPE bloom_filter GRANULARITY 4,
    INDEX i_invite_src_ip invite_src_ip TYPE minmax GRANULARITY 4,
    INDEX i_invite_src_port invite_src_port TYPE minmax GRANULARITY 4,
    INDEX i_invite_dest_ip invite_dest_ip TYPE minmax GRANULARITY 4,
    INDEX i_invite_dest_port invite_dest_port TYPE minmax GRANULARITY 4,
    INDEX i_invite_ip_protocol invite_ip_protocol TYPE bloom_filter GRANULARITY 4,
    INDEX i_sip_flow_id sip_flow_id TYPE minmax GRANULARITY 4
)
    ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(report_time)
ORDER BY report_time
SETTINGS storage_policy = 'policy_hot_cold_0' COMMENT 'RTP流分析';

CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_rtp_log_record_analysis
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
    `payload` AggregateFunction(any,String) COMMENT 'rtp流负载类型',
    INDEX i_level level TYPE bloom_filter GRANULARITY 4,
    INDEX i_policy_name policy_name TYPE bloom_filter GRANULARITY 4,
    INDEX i_flow_id flow_id TYPE minmax GRANULARITY 4,
    INDEX i_network_id network_id TYPE bloom_filter GRANULARITY 4,
    INDEX i_service_id service_id TYPE bloom_filter GRANULARITY 4,
    INDEX i_application_id application_id TYPE minmax GRANULARITY 4,
    INDEX i_start_time start_time TYPE minmax GRANULARITY 4,
    INDEX i_invite_time invite_time TYPE minmax GRANULARITY 4,
    INDEX i_from from TYPE bloom_filter GRANULARITY 4,
    INDEX i_src_ip src_ip TYPE minmax GRANULARITY 4,
    INDEX i_src_port src_port TYPE minmax GRANULARITY 4,
    INDEX i_to to TYPE bloom_filter GRANULARITY 4,
    INDEX i_dest_ip dest_ip TYPE minmax GRANULARITY 4,
    INDEX i_dest_port dest_port TYPE minmax GRANULARITY 4,
    INDEX i_ip_protocol ip_protocol TYPE bloom_filter GRANULARITY 4,
    INDEX i_invite_src_ip invite_src_ip TYPE minmax GRANULARITY 4,
    INDEX i_invite_src_port invite_src_port TYPE minmax GRANULARITY 4,
    INDEX i_invite_dest_ip invite_dest_ip TYPE minmax GRANULARITY 4,
    INDEX i_invite_dest_port invite_dest_port TYPE minmax GRANULARITY 4,
    INDEX i_invite_ip_protocol invite_ip_protocol TYPE bloom_filter GRANULARITY 4,
    INDEX i_sip_flow_id sip_flow_id TYPE minmax GRANULARITY 4
)
    ENGINE = AggregatingMergeTree()
        PARTITION BY toYYYYMMDD(start_time)
        ORDER BY (level, policy_name, flow_id, network_id, service_id, application_id, start_time, invite_time, from,
                  src_ip,
                  src_port, to, dest_ip, dest_port, ip_protocol, invite_src_ip, invite_src_port, invite_dest_ip,
                  invite_dest_port, invite_ip_protocol, sip_flow_id)
        SETTINGS storage_policy = 'policy_hot_cold_0' COMMENT 'RTP流分析';

/* Meterialized Viewes*/
CREATE MATERIALIZED VIEW IF NOT EXISTS v_fpc_protocol_rtp_log_record_analysis
    TO t_fpc_protocol_rtp_log_record_analysis
AS
select level,
       policy_name,
       flow_id,
       network_id,
       service_id,
       application_id,
       start_time,
       invite_time,
from,
    src_ip,
    src_port,
    to,
    dest_ip,
    dest_port,
    ip_protocol,
    invite_src_ip,
    invite_src_port,
    invite_dest_ip,
    invite_dest_port,
    invite_ip_protocol,
    sip_flow_id,
    maxState(end_time)          as end_time,
    maxState(ssrc)              as ssrc,
    maxState(status)            as status,
    sumState(rtp_total_packets) as rtp_total_packets,
    sumState(rtp_loss_packets)  as rtp_loss_packets,
    maxState(jitter_max)        as jitter_max,
    avgState(jitter_mean)       as jitter_mean,
    anyState(payload)           as payload
from t_fpc_protocol_rtp_log_record
group by level, policy_name, flow_id, network_id, service_id, application_id, start_time, invite_time, from, src_ip,
    src_port, to, dest_ip, dest_port, ip_protocol, invite_src_ip, invite_src_port, invite_dest_ip,
    invite_dest_port, invite_ip_protocol, sip_flow_id;