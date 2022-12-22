CREATE TABLE IF NOT EXISTS fpc.t_fpc_protocol_ldap_log_record
(
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id` UInt32 COMMENT '应用ID',
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间',
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间',
`src_ip` IPv6 COMMENT '源IP',
`src_port` UInt16 COMMENT '源端口',
`dest_ip` IPv6 COMMENT '目的IP',
`dest_port` UInt16 COMMENT '目的端口',
`op_type` UInt16 COMMENT '请求类型',
`res_status` UInt16 COMMENT '回复状态',
`req_content` Map(String,String) COMMENT '请求内容',
`res_content` Map(String,String) COMMENT '回复内容',
INDEX i_policy_name (policy_name) TYPE bloom_filter() GRANULARITY 4,
INDEX i_flow_id (flow_id) TYPE minmax GRANULARITY 4,
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_service_id (service_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_src_ip (src_ip) TYPE minmax GRANULARITY 4,
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4,
INDEX i_dest_ip (dest_ip) TYPE minmax GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_application_id (application_id) TYPE minmax GRANULARITY 4,
INDEX i_end_time (end_time) TYPE minmax GRANULARITY 4,
INDEX i_op_type (op_type) TYPE bloom_filter GRANULARITY 4,
INDEX i_res_status (res_status) TYPE bloom_filter GRANULARITY 4
)
ENGINE = MergeTree
PARTITION BY toYYYYMMDD(start_time)
ORDER BY start_time
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '应用层协议详单-LDAP协议详单表';

CREATE MATERIALIZED VIEW IF NOT EXISTS v_fpc_protocol_dict_ldap_req_content
TO t_fpc_protocol_dict
AS SELECT
  'ldap' as protocol,
  'req_content' as field,
  arrayJoin(mapKeys(req_content)) as key
from t_fpc_protocol_ldap_log_record;

CREATE MATERIALIZED VIEW IF NOT EXISTS v_fpc_protocol_dict_ldap_res_content
TO t_fpc_protocol_dict
AS SELECT
  'ldap' as protocol,
  'res_content' as field,
  arrayJoin(mapKeys(res_content)) as key
from t_fpc_protocol_ldap_log_record;