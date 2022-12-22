DROP TABLE IF EXISTS fpc.d_fpc_flow_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_flow_log_record
(
`interface` LowCardinality(String) COMMENT '流量接收物理网口名称', 
`flow_id` UInt64 COMMENT '流ID', 
`network_id` Array(LowCardinality(String)) COMMENT '网络ID(可能归属于多个网络)', 
`service_id` Array(LowCardinality(String)) COMMENT '业务ID(可能归属于多个业务)', 
`start_time` DateTime64(9, 'UTC') COMMENT '会话开始时间', 
`report_time` DateTime64(9, 'UTC') COMMENT '会话记录时间', 
`duration` UInt32 COMMENT '会话持续时间', 
`flow_continued` UInt8 COMMENT '是否还有后续会话详单生成', 
`packet_sigseq` String COMMENT '前100个包的特征序列',
`upstream_bytes` UInt64 COMMENT '正向字节数', 
`downstream_bytes` UInt64 COMMENT '反向字节数', 
`total_bytes` UInt64 COMMENT '总字节数', 
`upstream_packets` UInt32 COMMENT '正向包数', 
`downstream_packets` UInt32 COMMENT '反向包数', 
`total_packets` UInt32 COMMENT '总包数', 
`upstream_payload_bytes` UInt64 COMMENT '正向payload载荷字节数', 
`downstream_payload_bytes` UInt64 COMMENT '反向payload载荷字节数', 
`total_payload_bytes` UInt64 COMMENT '总payload载荷字节数', 
`upstream_payload_packets` UInt32 COMMENT '正向payload载荷包数', 
`downstream_payload_packets` UInt32 COMMENT '反向payload载荷包数', 
`total_payload_packets` UInt32 COMMENT '总payload载荷包数',
`tcp_client_network_latency` UInt32 COMMENT '客户端网络时延(ms)', 
`tcp_client_network_latency_flag` UInt8 COMMENT '标记该条会话详单中的客户端网络时延是否有效', 
`tcp_server_network_latency` UInt32 COMMENT '服务端网络时延(ms)', 
`tcp_server_network_latency_flag` UInt8 COMMENT '标记该条会话详单中的服务端网络时延是否有效', 
`server_response_latency` UInt32 COMMENT '服务器响应时延(ms)', 
`server_response_latency_flag` UInt8 COMMENT '标记该条会话详单中的服务端响应时延是否有效',
`tcp_client_loss_bytes` UInt32 COMMENT 'TCP客户端丢包字节数',
`tcp_server_loss_bytes` UInt32 COMMENT 'TCP服务端丢包字节数',
`tcp_client_zero_window_packets` UInt32 COMMENT 'TCP客户端零窗口包数', 
`tcp_server_zero_window_packets` UInt32 COMMENT 'TCP服务端零窗口包数', 
`tcp_session_state` Nullable(UInt8) COMMENT 'TCP会话状态', 
`tcp_established_success_flag` UInt8 COMMENT '标记该条会话详单是否建连成功', 
`tcp_established_fail_flag` UInt8 COMMENT '标记该条会话详单是否建连失败', 
`established_sessions` UInt32 COMMENT '新建会话数', 
`tcp_syn_packets` UInt32 COMMENT 'TCP同步包数', 
`tcp_syn_ack_packets` UInt32 COMMENT 'TCP同步确认包数', 
`tcp_syn_rst_packets` UInt32 COMMENT 'TCP同步重置包数', 
`tcp_client_packets` UInt32 COMMENT 'TCP客户端包数',
`tcp_server_packets` UInt32 COMMENT 'TCP服务端包数',
`tcp_client_retransmission_packets` UInt32 COMMENT 'TCP客户端重传包数', 
`tcp_server_retransmission_packets` UInt32 COMMENT 'TCP服务端重传包数', 
`ethernet_type` UInt8 COMMENT '以太网类型', 
`ethernet_initiator` LowCardinality(String) COMMENT '源MAC地址', 
`ethernet_responder` LowCardinality(String) COMMENT '目的MAC地址', 
`ethernet_protocol` LowCardinality(String) COMMENT '三层协议类型', 
`vlan_id` UInt16 COMMENT 'VLANID', 
`hostgroup_id_initiator` LowCardinality(Nullable(String)) COMMENT '源IP归属的IP地址组ID',
`hostgroup_id_responder` LowCardinality(Nullable(String)) COMMENT '目的IP归属的IP地址组ID',
`ip_locality_initiator` UInt8 COMMENT '源IP所在位置(0:内网,1:外网)', 
`ip_locality_responder` UInt8 COMMENT '目的IP所在位置(0:内网,1:外网)', 
`ipv4_initiator` Nullable(IPv4) COMMENT '源IPv4(会话为IPv6时为空)', 
`ipv4_responder` Nullable(IPv4) COMMENT '目的IPv4(会话为IPv6时为空)', 
`ipv6_initiator` Nullable(IPv6) COMMENT '源IPv6(会话为IPv4时为空)', 
`ipv6_responder` Nullable(IPv6) COMMENT '目的IPv6(会话为IPv4时为空)', 
`ip_protocol` LowCardinality(String) COMMENT '四层协议类型', 
`port_initiator` UInt16 COMMENT '源端口', 
`port_responder` UInt16 COMMENT '目的端口', 
`l7_protocol_id` UInt16 COMMENT '应用层协议ID', 
`application_category_id` UInt16 COMMENT 'DPI应用分类ID', 
`application_subcategory_id` UInt16 COMMENT 'DPI应用子分类ID', 
`application_id` UInt32 COMMENT 'DPI应用ID', 
`malicious_application_id` UInt32 COMMENT '僵木蠕应用ID', 
`country_id_initiator` Nullable(UInt16) COMMENT '源IP归属地国家ID', 
`province_id_initiator` Nullable(UInt16) COMMENT '源IP归属地省份ID', 
`city_id_initiator` Nullable(UInt16) COMMENT '源IP归属地城市ID', 
`district_initiator` LowCardinality(String) COMMENT '源IP归属区县', 
`aoi_type_initiator` LowCardinality(String) COMMENT '源的AOI类型', 
`aoi_name_initiator` LowCardinality(String) COMMENT '目的的AOI名称', 
`country_id_responder` Nullable(UInt16) COMMENT '目的IP归属地国家ID', 
`province_id_responder` Nullable(UInt16) COMMENT '目的IP归属地省份ID', 
`city_id_responder` Nullable(UInt16) COMMENT '目的IP归属地城市ID', 
`district_responder` LowCardinality(String) COMMENT '目的IP归属区县', 
`aoi_type_responder` LowCardinality(String) COMMENT '流应答方的AOI类型', 
`aoi_name_responder` LowCardinality(String) COMMENT '流应答方的AOI名称'
)  
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_flow_log_record)
COMMENT '会话详单表';