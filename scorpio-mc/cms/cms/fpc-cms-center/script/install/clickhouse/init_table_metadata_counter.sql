DROP TABLE IF EXISTS fpc.d_fpc_metadata_counter;
CREATE TABLE IF NOT EXISTS fpc.d_fpc_metadata_counter
(
`start_time` DateTime64(9, 'UTC'),
`network_id` LowCardinality(String),
`service_id` LowCardinality(String),
`arp_cnt` UInt64,
`dhcp_cnt` UInt64,
`dhcpv6_cnt` UInt64,
`dns_cnt` UInt64,
`ftp_cnt` UInt64,
`http_cnt` UInt64,
`icmpv4_cnt` UInt64,
`icmpv6_cnt` UInt64,
`pop3_cnt` UInt64,
`imap_cnt` UInt64,
`smtp_cnt` UInt64,
`mysql_cnt` UInt64,
`ospf_cnt` UInt64,
`postgresql_cnt` UInt64,
`socks5_cnt` UInt64,
`socks4_cnt` UInt64,
`db2_cnt` UInt64,
`ssh_cnt` UInt64,
`ssl_cnt` UInt64,
`sip_cnt` UInt64,
`tds_cnt` UInt64,
`telnet_cnt` UInt64,
`tns_cnt` UInt64,
`flow_cnt` UInt64,
`ldap_cnt` UInt64
)
ENGINE = Distributed(clickhouse_servers, fpc, t_fpc_metadata_counter)
COMMENT '详单数量统计表';