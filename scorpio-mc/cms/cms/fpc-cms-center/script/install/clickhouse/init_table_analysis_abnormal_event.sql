DROP TABLE IF EXISTS fpc.d_fpc_analysis_abnormal_event;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_analysis_abnormal_event
(
`id` String COMMENT 'UUID',
`start_time` DateTime64(3, 'UTC') COMMENT '生成时间',
`network_id` String COMMENT '网络ID',
`type` UInt16 COMMENT '异常事件类型',
`content` String COMMENT '异常事件内容',
`description` String COMMENT '异常事件描述信息',
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4',
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6',
`dest_port` UInt16 COMMENT '目的端口',
`l7_protocol_id` UInt16 COMMENT '应用层协议ID',
`country_id_initiator` Nullable(UInt16) COMMENT '源国家ID',
`province_id_initiator` Nullable(UInt16) COMMENT '源省份ID',
`city_id_initiator` Nullable(UInt16) COMMENT '源城市ID',
`country_id_responder` Nullable(UInt16) COMMENT '目的国家ID',
`province_id_responder` Nullable(UInt16) COMMENT '目的省份ID',
`city_id_responder` Nullable(UInt16) COMMENT '目的城市ID'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_analysis_abnormal_event)
COMMENT '异常事件记录表';