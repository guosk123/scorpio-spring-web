DROP TABLE IF EXISTS fpc.d_fpc_appliance_alert_message;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_appliance_alert_message
(
`id` String COMMENT 'UUID',
`arise_time` DateTime64(3, 'UTC') COMMENT '告警时间', 
`alert_id` String COMMENT '告警规则ID', 
`network_id` String COMMENT '网络ID',
`service_id` Nullable(String) COMMENT '业务ID',
`name` String COMMENT '告警名称', 
`category` String COMMENT '告警分类',
`level` UInt8 COMMENT '告警级别',
`alert_define` String COMMENT '告警规则定义(JSON)',
`components` String COMMENT '告警子组件(JSON)',
`status` String DEFAULT '0' COMMENT '处理状态',
`solver_id` String COMMENT '告警处理人',
`solve_time` Nullable(DateTime64(3, 'UTC')) COMMENT '处理时间',
`reason` String COMMENT '处理意见'
) 
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_appliance_alert_message)
COMMENT '告警消息记录表';