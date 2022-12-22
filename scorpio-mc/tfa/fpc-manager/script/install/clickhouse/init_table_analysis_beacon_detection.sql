CREATE TABLE IF NOT EXISTS fpc.t_fpc_analysis_beacon_detection
( 
`id` String COMMENT 'UUID',
`task_id` String COMMENT '场景分析任务ID', 
`record_total_hit` UInt64 COMMENT '命中流日志总数量', 
`record_id_list` String COMMENT '命中流日志ID集合', 
`src_ip` String COMMENT '源IP',
`dest_ip` String COMMENT '目的IP',
`dest_port` UInt16 COMMENT '目的端口', 
`upstream_bytes` UInt64 COMMENT '上行字节数', 
`period` UInt16 COMMENT '每个IP对(相同源IP+目的IP+目的端口)的所有流中具有重复字节数的流总数/IP对的所有流总数', 
`protocol` String COMMENT '协议', 
INDEX i_src_ip (src_ip) TYPE bloom_filter() GRANULARITY 4,
INDEX i_dest_ip (dest_ip) TYPE bloom_filter() GRANULARITY 4,
INDEX i_dest_port (dest_port) TYPE minmax GRANULARITY 4,
INDEX i_protocol (protocol) TYPE bloom_filter() GRANULARITY 4
)  
ENGINE = MergeTree() 
ORDER BY (task_id) 
SETTINGS storage_policy = 'policy_hot_cold_0'
COMMENT '场景分析-beacon查询结果表'