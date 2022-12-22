DROP TABLE IF EXISTS fpc.d_fpc_protocol_mail_log_record;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_protocol_mail_log_record
( 
`level` LowCardinality(String) COMMENT '采集策略等级',
`policy_name` LowCardinality(String) COMMENT '采集策略名称',
`flow_id` UInt64 COMMENT '流ID',
`network_id` Array(LowCardinality(String)) COMMENT '网络ID',
`service_id` Array(LowCardinality(String)) COMMENT '业务ID',
`application_id` UInt32 COMMENT '应用ID',
`start_time` DateTime64(9, 'UTC') COMMENT '开始时间', 
`end_time` DateTime64(9, 'UTC') COMMENT '结束时间', 
`src_ipv4` Nullable(IPv4) COMMENT '源IPv4',
`src_ipv6` Nullable(IPv6) COMMENT '源IPv6', 
`src_port` UInt16 COMMENT '源端口',
`dest_ipv4` Nullable(IPv4) COMMENT '目的IPv4', 
`dest_ipv6` Nullable(IPv6) COMMENT '目的IPv6', 
`dest_port` UInt16 COMMENT '目的端口',
`protocol` LowCardinality(String) COMMENT '邮件协议',
`message_id` String COMMENT '邮件ID',
`from` String COMMENT '发件人',
`to` String COMMENT '收件人',
`subject` String COMMENT '邮件主题',
`date` String COMMENT '发送日期',
`cc` String COMMENT '抄送人',
`bcc` String COMMENT '密送人',
`attachment` String COMMENT '附件名称',
`content` String COMMENT '邮件正文',
`url_list` Array(String) COMMENT '邮件正文内包含的链接',
`decrypted` UInt8 COMMENT '加密方式'
)  
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_protocol_mail_log_record)
COMMENT '应用层协议详单-邮件协议(POP3/IMAP/SMTP)详单表';