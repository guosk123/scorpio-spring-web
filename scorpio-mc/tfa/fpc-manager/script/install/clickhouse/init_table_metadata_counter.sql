CREATE TABLE IF NOT EXISTS t_fpc_metadata_counter
(
    `start_time` DateTime64(9, 'UTC') COMMENT '时间戳(取整分钟)',
    `network_id` LowCardinality(String) COMMENT '网络ID',
    `service_id` LowCardinality(String) COMMENT '业务ID',
    /* 以上三个是SummingMergeTree数据表的排序Key，三个key不同的记录会被累加合并为一条 */
    /* 筛选时必须包含业务ID字段，如果前端查询条件不包含业务，则按业务ID为空字符串来筛选，否则数据量可能会翻若干倍 */
    /* 以下字段是具体的统计字段，表示 某分钟&某网络ID&某业务ID 对应有多少条记录 */
    `arp_cnt` UInt64 COMMENT 'ARP记录条数',
    `dhcp_cnt` UInt64 COMMENT 'DHCP记录条数',
    `dhcpv6_cnt` UInt64 COMMENT 'DHCPv6记录条数',
    `dns_cnt` UInt64 COMMENT 'DNS记录条数',
    `ftp_cnt` UInt64 COMMENT 'FTP记录条数',
    `http_cnt` UInt64 COMMENT 'HTTP记录条数',
    `icmpv4_cnt` UInt64 COMMENT 'ICMPv4记录条数',
    `icmpv6_cnt` UInt64 COMMENT 'ICMPv6记录条数',
    `pop3_cnt` UInt64 COMMENT 'POP3记录条数',
    `imap_cnt` UInt64 COMMENT 'IMAP记录条数',
    `smtp_cnt` UInt64 COMMENT 'SMTP记录条数',
    `mysql_cnt` UInt64 COMMENT 'MYSQL记录条数',
    `ospf_cnt` UInt64 COMMENT 'OSPF记录条数',
    `postgresql_cnt` UInt64 COMMENT 'POSTGRESQL记录条数',
    `socks5_cnt` UInt64 COMMENT 'SOCKS5记录条数',
    `sip_cnt` UInt64 COMMENT 'SIP记录条数',
    `socks4_cnt` UInt64 COMMENT 'SOCKS4记录条数',
    `db2_cnt` UInt64 COMMENT 'DB2记录条数',
    `ssh_cnt` UInt64 COMMENT 'SSH记录条数',
    `ssl_cnt` UInt64 COMMENT 'SSL记录条数',
    `tds_cnt` UInt64 COMMENT 'TDS记录条数',
    `telnet_cnt` UInt64 COMMENT 'TELNET记录条数',
    `tns_cnt` UInt64 COMMENT 'TNS记录条数',
    `flow_cnt` UInt64 COMMENT '流日志记录条数',
    `ldap_cnt` UInt64 COMMENT 'ldap记录条数',
    INDEX i_network_id (network_id) TYPE bloom_filter GRANULARITY 4,
    INDEX i_service_id (service_id) TYPE bloom_filter GRANULARITY 4
)
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMMDD(start_time)
ORDER BY (start_time, network_id, service_id)
SETTINGS storage_policy = 'policy_cold_0';

/* Meterialized Viewes */
CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_arp
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS arp_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id /* 这里添加一个空业务id, 为了存储不筛选业务id时的数据 */
        FROM t_fpc_protocol_arp_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC') /* 选择一个将来的时间点生效，为了手动生成旧数据的counter数据 */
    )
    ARRAY JOIN service_id   /* 两次ARRAY JOIN后，每个网络id和业务id的组合都会对应一份选中的数据 */
                            /* 同时网络id和业务id会被处理为一个个的元素 */
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_dhcp
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    countIf(isNotNull(src_ipv4)) AS dhcp_cnt,
    countIf(isNull(src_ipv4)) AS dhcpv6_cnt     /* dhcp协议分为v4/v6, 这里用源ip判断 */
FROM
(
    SELECT
        start_time,
        network_id,
        service_id,
        src_ipv4
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id,
            src_ipv4
        FROM t_fpc_protocol_dhcp_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_dns
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS dns_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_dns_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_ftp
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS ftp_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_ftp_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_http
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS http_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_http_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_icmp
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    countIf(isNotNull(src_ipv4)) AS icmpv4_cnt,
    countIf(isNull(src_ipv4)) AS icmpv6_cnt     /* icmp协议分为v4/v6, 这里用源ip判断 */
FROM
(
    SELECT
        start_time,
        network_id,
        service_id,
        src_ipv4
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id,
            src_ipv4
        FROM t_fpc_protocol_icmp_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_mail
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    countIf(equals(protocol, 'imap')) AS imap_cnt,
    countIf(equals(protocol, 'pop3')) AS pop3_cnt,
    countIf(equals(protocol, 'smtp')) AS smtp_cnt   /* 邮件协议分为3种，需要单独统计 */
FROM
(
    SELECT
        start_time,
        network_id,
        service_id,
        protocol
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id,
            protocol
        FROM t_fpc_protocol_mail_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_mysql
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS mysql_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_mysql_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_ospf
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS ospf_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_ospf_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_postgresql
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS postgresql_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_postgresql_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_socks5
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS socks5_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_socks5_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_sip
TO t_fpc_metadata_counter
AS SELECT
              start_time,
              network_id,
              service_id,
              count(1) AS sip_cnt
   FROM
              (
                  SELECT
                      start_time,
                      network_id,
                      service_id
                  FROM
                      (
                          SELECT
                              start_time,
                              network_id,
                              arrayPushBack(service_id, '') AS service_id
                          FROM t_fpc_protocol_sip_log_record
                                   ARRAY JOIN network_id
                          WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
                      )
                          ARRAY JOIN service_id
              )
   GROUP BY
              toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_ssh
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS ssh_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_ssh_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_ssl
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS ssl_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_ssl_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_tds
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS tds_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_tds_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_telnet
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS telnet_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_telnet_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_tns
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS tns_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_tns_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_socks4
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS socks4_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_socks4_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_flow
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS flow_cnt
FROM
(
    SELECT
        report_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            report_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_flow_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC') AND total_packets > 0 /* total_packets > 0 流日志的特殊处理 */
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(report_time) AS start_time,
    network_id,
    service_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_ldap
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS ldap_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_ldap_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;
    
CREATE MATERIALIZED VIEW IF NOT EXISTS t_fpc_metadata_counter_db2
TO t_fpc_metadata_counter
AS SELECT
    start_time,
    network_id,
    service_id,
    count(1) AS db2_cnt
FROM
(
    SELECT
        start_time,
        network_id,
        service_id
    FROM
    (
        SELECT
            start_time,
            network_id,
            arrayPushBack(service_id, '') AS service_id
        FROM t_fpc_protocol_db2_log_record
        ARRAY JOIN network_id
        WHERE start_time >= toDateTime('2000-01-01 00:00:00', 9, 'UTC')
    )
    ARRAY JOIN service_id
)
GROUP BY
    toStartOfMinute(start_time) AS start_time,
    network_id,
    service_id;