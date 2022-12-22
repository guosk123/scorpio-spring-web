CREATE TABLE fpc.t_fpc_file_restore_info
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
`state`     UInt8 COMMENT '还原状态',
INDEX i_flow_id (flow_id) TYPE minmax GRANULARITY 4,
INDEX i_network_id (network_id) TYPE bloom_filter() GRANULARITY 4,
INDEX i_timestamp (timestamp) TYPE minmax GRANULARITY 4,
INDEX i_src_ip (src_ip) TYPE minmax GRANULARITY 4,
INDEX i_src_port (src_port) TYPE minmax GRANULARITY 4,
INDEX i_dest_ip (dest_ip) TYPE minmax GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_md5 (md5) TYPE bloom_filter GRANULARITY 4,
INDEX i_sha1 (sha1) TYPE bloom_filter GRANULARITY 4,
INDEX i_sha256 (sha256) TYPE bloom_filter GRANULARITY 4,
INDEX i_name (name) TYPE bloom_filter GRANULARITY 4,
INDEX i_size(size) TYPE minmax GRANULARITY 4,
INDEX i_l7_protocol(l7_protocol) TYPE bloom_filter GRANULARITY 4,
INDEX i_magic(magic) TYPE bloom_filter GRANULARITY 4,
INDEX i_state(state) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(timestamp)
ORDER BY (timestamp)
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '文件还原详单表'