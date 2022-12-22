--TOP10目标主机
DROP VIEW IF EXISTS t_fpc_suricata_top_target_host;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_target_host to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    signature_severity,
    basic_tag,
    'top_target_host' as type,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        target as key
    from
        t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--TOP10源主机
DROP VIEW IF EXISTS t_fpc_suricata_top_origin_ip;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_origin_ip to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_origin_ip' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        if(IPv4StringToNum(target AS target_ip) = 0,
        if(target_ip = IPv6NumToString(src_ipv6),
        IPv6NumToString(dest_ipv6),
        IPv6NumToString(src_ipv6)),
        if(target_ip = IPv4NumToString(src_ipv4),
        IPv4NumToString(dest_ipv4) ,
        IPv4NumToString(src_ipv4))) as key
    from
        t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--TOP10告警
DROP VIEW IF EXISTS t_fpc_suricata_top_alarm_id;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_alarm_id to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_alarm_id' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        sid as key
    from
        t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--分类占比
DROP VIEW IF EXISTS t_fpc_suricata_classification_proportion;
CREATE MATERIALIZED VIEW t_fpc_suricata_classification_proportion to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
    classtype_id,
    signature_severity,
    basic_tag,
    'classification_proportion' as type,
    classtype_id as key,
    count(1) as value
from
    (
    select
    timestamp,
    source,
    signature_severity,
    basic_tag,
    classtype_id
    from
    t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
    signature_severity,
    basic_tag,
    source,
    key;


--战术占比
DROP VIEW IF EXISTS t_fpc_suricata_mitre_tactic_proportion;
CREATE MATERIALIZED VIEW t_fpc_suricata_mitre_tactic_proportion to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'mitre_tactic_proportion' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        mitre_tactic_id as key
    from
        t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--告警趋势
DROP VIEW IF EXISTS t_fpc_suricata_alarm_trend;
CREATE MATERIALIZED VIEW t_fpc_suricata_alarm_trend to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'alarm_trend' as type,
    signature_severity,
    basic_tag,
    'alarm_trend' as key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
        signature_severity,
        basic_tag,
		classtype_id
    from
        t_fpc_analysis_suricata_alert_message)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
    signature_severity,
    basic_tag,
	classtype_id;

--TOP10挖矿主机
DROP VIEW IF EXISTS t_fpc_suricata_top_mining_host;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_mining_host to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_mining_host' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        if(IPv4StringToNum(target AS target_ip) = 0,
        if(target_ip = IPv6NumToString(src_ipv6),
        IPv6NumToString(dest_ipv6),
        IPv6NumToString(src_ipv6)),
        if(target_ip = IPv4NumToString(src_ipv4),
        IPv4NumToString(dest_ipv4) ,
        IPv4NumToString(src_ipv4))) as key
    from
        t_fpc_analysis_suricata_alert_message
    where
        classtype_id = '1')
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--TOP10挖矿域名
DROP VIEW IF EXISTS t_fpc_suricata_top_mining_domain;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_mining_domain to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_mining_domain' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        domain as key
    from
        t_fpc_analysis_suricata_alert_message
    where
        classtype_id = '2'
        and length(domain) > 0)
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
    signature_severity,
    basic_tag,
	classtype_id,
    key;

--TOP10矿池地址
DROP VIEW IF EXISTS t_fpc_suricata_top_mining_pool_address;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_mining_pool_address to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_mining_pool_address' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
		classtype_id,
        signature_severity,
        basic_tag,
        target as key
    from
        t_fpc_analysis_suricata_alert_message
    where
        classtype_id = '1')
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

--告警趋势
DROP VIEW IF EXISTS t_fpc_suricata_mining_alarm_trend;
CREATE MATERIALIZED VIEW t_fpc_suricata_mining_alarm_trend to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'mining_alarm_trend' as type,
    signature_severity,
    basic_tag,
    'mining_alarm_trend' as key,
    count(1) as value
from
    (
    select
        timestamp,
        source,
        signature_severity,
        basic_tag,
		classtype_id
    from
        t_fpc_analysis_suricata_alert_message
    where
        classtype_id in ('1', '2'))
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
    signature_severity,
    basic_tag,
	classtype_id;

-- 攻击地区
DROP VIEW IF EXISTS t_fpc_suricata_top_attack_origin_area;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_attack_origin_area to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_attack_origin_area' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
        select
            timestamp,
            source,
			classtype_id,
            signature_severity,
            basic_tag,
            if ( target = IPv6NumToString(dest_ipv6) or target = IPv4NumToString(dest_ipv4),
                 if ( province_id_initiator = 255, toString(country_id_initiator), concat(toString(country_id_initiator), '_', toString(province_id_initiator)) ),
                 if ( province_id_responder = 255, toString(country_id_responder), concat(toString(country_id_responder), '_', toString(province_id_responder)) )
                ) as key
        from
            t_fpc_analysis_suricata_alert_message
        where isNotNull(country_id_initiator) or isNotNull(country_id_responder))
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

-- 目标地区
DROP VIEW IF EXISTS t_fpc_suricata_top_attack_target_area;
CREATE MATERIALIZED VIEW t_fpc_suricata_top_attack_target_area to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'top_attack_target_area' as type,
    signature_severity,
    basic_tag,
    key,
    count(1) as value
from
    (
        select
            timestamp,
            source,
			classtype_id,
            signature_severity,
            basic_tag,
            if ( target = IPv6NumToString(dest_ipv6) or target = IPv4NumToString(dest_ipv4),
                 if ( province_id_responder = 255, toString(country_id_responder), concat(toString(country_id_responder), '_', toString(province_id_responder)) ),
                 if ( province_id_initiator = 255, toString(country_id_initiator), concat(toString(country_id_initiator), '_', toString(province_id_initiator)) )
                ) as key
        from
            t_fpc_analysis_suricata_alert_message
        where isNotNull(country_id_initiator) or isNotNull(country_id_responder))
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
	classtype_id,
    signature_severity,
    basic_tag,
    key;

-- 告警规则来源
DROP VIEW IF EXISTS t_fpc_suricata_source_alarm_trend;
CREATE MATERIALIZED VIEW t_fpc_suricata_source_alarm_trend to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
	classtype_id,
    'source_alarm_trend' as type,
    signature_severity,
    basic_tag,
    source as key,
    count(1) as value
from
    t_fpc_analysis_suricata_alert_message
group by
    toStartOfMinute(timestamp) as timestamp,
	source,
    signature_severity,
    basic_tag,
	classtype_id;

-- 告警严重级别
DROP VIEW IF EXISTS t_fpc_suricata_signature_severity;
CREATE MATERIALIZED VIEW t_fpc_suricata_signature_severity to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
    classtype_id,
    'signature_severity' as type,
    signature_severity,
    basic_tag,
    signature_severity as key,
    count(1) as value
from
    t_fpc_analysis_suricata_alert_message
group by
    toStartOfMinute(timestamp) as timestamp,
    source,
    signature_severity,
    basic_tag,
    classtype_id;

-- 基础标签分布
DROP VIEW IF EXISTS t_fpc_suricata_basic_tag;
CREATE MATERIALIZED VIEW t_fpc_suricata_basic_tag to t_fpc_analysis_suricata_alert_statistics as
select
    timestamp,
    source,
    classtype_id,
    'basic_tag' as type,
    signature_severity,
    basic_tag,
    basic_tag as key,
    count(1) as value
from
    t_fpc_analysis_suricata_alert_message
where basic_tag != ''
group by
    toStartOfMinute(timestamp) as timestamp,
    source,
    signature_severity,
    basic_tag,
    classtype_id;