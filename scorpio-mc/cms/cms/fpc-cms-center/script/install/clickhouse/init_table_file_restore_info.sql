DROP TABLE IF EXISTS fpc.d_fpc_file_restore_info;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_file_restore_info
(
`flow_id`   UInt64 COMMENT '流ID',
`network_id` LowCardinality(String) COMMENT '网络ID',
`timestamp` DateTime64(3, 'UTC') COMMENT '还原时间',
`src_ip`    IPv6 COMMENT '源ip',
`src_port`  UInt16 COMMENT '源端口',
`dest_ip`   IPv6 COMMENT '目的ip',
`dest_port` UInt16 COMMENT '目的端口',
`md5`       String COMMENT '文件md5值',
`sha1`      String COMMENT '文件sha1值',
`sha256`    String COMMENT '文件sha256值',
`name`      String COMMENT '文件名称',
`size`      UInt64 COMMENT '文件大小',
`magic` LowCardinality(String) COMMENT '文件类型',
`l7_protocol` LowCardinality(String) COMMENT '应用层协议',
`state`     UInt8 COMMENT '还原状态'
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_file_restore_info)
COMMENT '文件还原详单表';