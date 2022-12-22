package com.machloop.fpc.cms.npm.analysis.dao.clickhouse;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.*;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.*;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataAlertMessageDao;
import com.machloop.fpc.cms.npm.analysis.data.SuricataAlertMessageDO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
@Repository
public class SuricataAlertMessageDaoImpl implements SuricataAlertMessageDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuricataAlertMessageDaoImpl.class);

  private static final String TABLE_ANALYSIS_SURICATA_ALERT = "d_fpc_analysis_suricata_alert_message";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataAlertMessageDao#querySuricataAlerts(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO)
   */
  @Override
  public Page<SuricataAlertMessageDO> querySuricataAlerts(Pageable page,
      SuricataRuleQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, SuricataAlertMessageDO.class);

    List<Map<String, Object>> suricataAlerts = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(TABLE_ANALYSIS_SURICATA_ALERT);
    totalSql.append(whereSql);

    Long total = queryForLongWithExceptionHandle(totalSql.toString(), params);

    List<SuricataAlertMessageDO> resultList = suricataAlerts.stream()
        .map(item -> convertSuricataAlertMap2DO(item)).collect(Collectors.toList());
    return new PageImpl<>(resultList, page, total);
  }


  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataAlertMessageDao#statisticsMitreAttack(com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO)
   */
  @Override
  public List<Map<String, Object>> statisticsMitreAttack(SuricataRuleQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append("select mitre_tactic_id as mitreTacticId, mitre_technique_id as mitreTechniqueId, ");
    sql.append(" count(1) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);
    sql.append(whereSql);

    sql.append(" group by mitre_tactic_id, mitre_technique_id ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
      PageRequest page) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        " select msg,signature_severity as signatureSeverity,max(timestamp) as max_timestamp,count(sid) as counts ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT);
    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    queryVO.setDsl("");
    String srcIp = queryVO.getSrcIp();
    String destIp = queryVO.getDestIp();
    queryVO.setSrcIp("");
    queryVO.setDestIp("");
    whereSql(queryVO, whereSql, params);

    if (StringUtils.isNotBlank(srcIp) && StringUtils.isNotBlank(destIp)) {
      StringBuilder srcIpWhereSql = new StringBuilder();
      enrichIpCondition(srcIp, "src_ipv4", "src_ipv6", srcIpWhereSql, params, "src");
      StringBuilder destIpWhereSql = new StringBuilder();
      enrichIpCondition(destIp, "dest_ipv4", "dest_ipv6", destIpWhereSql, params, "dest");
      whereSql.append(srcIpWhereSql.toString().replace("and (", "and (("));
      whereSql.append(destIpWhereSql.toString().replace("and (", "or (")).append(")");
    }

    sql.append(whereSql);
    sql.append(" group by msg,signatureSeverity ");
    PageUtils.appendPage(sql, page, Lists.newArrayListWithCapacity(0));
    List<Map<String, Object>> suricataAlertMessages = queryWithExceptionHandle(sql.toString(),
        params, new ColumnMapRowMapper());
    for (Map<String, Object> map : suricataAlertMessages) {
      map.put("timestamp", DateUtils.toStringYYYYMMDDHHMMSS(
          (OffsetDateTime) map.get("max_timestamp"), ZoneId.systemDefault()));
      map.remove("max_timestamp");
    }

    long total = 0;
    return new PageImpl<>(suricataAlertMessages, page, total);
  }

  @Override
  public long SuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select count(1) from ( ");
    sql.append(
        " select msg,signature_severity as signatureSeverity,max(timestamp) as max_timestamp,count(sid) as counts ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT);
    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    queryVO.setDsl("");
    String srcIp = queryVO.getSrcIp();
    String destIp = queryVO.getDestIp();
    queryVO.setSrcIp("");
    queryVO.setDestIp("");
    whereSql(queryVO, whereSql, params);

    if (StringUtils.isNotBlank(srcIp) && StringUtils.isNotBlank(destIp)) {
      StringBuilder srcIpWhereSql = new StringBuilder();
      enrichIpCondition(srcIp, "src_ipv4", "src_ipv6", srcIpWhereSql, params, "src");
      StringBuilder destIpWhereSql = new StringBuilder();
      enrichIpCondition(destIp, "dest_ipv4", "dest_ipv6", destIpWhereSql, params, "dest");
      whereSql.append(srcIpWhereSql.toString().replace("and (", "and (("));
      whereSql.append(destIpWhereSql.toString().replace("and (", "or (")).append(")");
    }

    sql.append(whereSql);
    sql.append(" group by msg,signatureSeverity ").append(")");

    return queryForLongWithExceptionHandle(sql.toString(), params);

  }

  @Override
  public List<Map<String, Object>> queryAlterMessagesRelation(String ip, Date startTimeDate,
      Date endTimeDate) {

    StringBuilder sql = new StringBuilder();
    sql.append(" select ");
    if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V4)) {
      sql.append(" src_ipv4, dest_ipv4, ");
    } else if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V6)) {
      sql.append(" src_ipv6, dest_ipv6, ");
    }
    sql.append(" min(timestamp) as minTimestamp, sid, msg, target from ")
        .append(TABLE_ANALYSIS_SURICATA_ALERT);
    sql.append(" where 1 = 1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichContainTimeRangeBetter(startTimeDate, endTimeDate, true, true, sql, params);
    if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V4)) {
      sql.append(
          " and (src_ipv4 = toIPv4(:keyword) or dest_ipv4 = toIPv4(:keyword)) group by sid, msg, src_ipv4, dest_ipv4, target ");
    } else if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V6)) {
      sql.append(
          " and (src_ipv6 = toIPv6(:keyword) or dest_ipv6 = toIPv6(:keyword)) group by sid, msg, src_ipv6, dest_ipv6, target ");
    }
    params.put("keyword", ip);

    List<Map<String, Object>> suricataRelation = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return suricataRelation;
  }

  @Override
  public List<Map<String, Object>> querySuricataAlertsWithoutTotal(PageRequest page,
      SuricataRuleQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, SuricataAlertMessageDO.class);

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public List<Object> querySuricataAlertFlowIds(SuricataRuleQueryVO queryVO, Sort sort, int size) {
    StringBuilder sql = new StringBuilder();

    sql.append("select distinct flow_id from ").append(TABLE_ANALYSIS_SURICATA_ALERT);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql(queryVO, whereSql, params);
    whereSql.append(" order by ").append(sort.iterator().next().getProperty());
    whereSql.append(" ").append(sort.iterator().next().getDirection());
    whereSql.append(" limit ").append(size);

    sql.append(whereSql);
    List<Object> flowIds = queryForObjectWithExceptionHandle(sql.toString(), params);
    return CollectionUtils.isEmpty(flowIds)
        ? Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)
        : flowIds;
  }

  @Override
  public List<Object> querySuricataTopHundredFlowIds(String sid, Date startTimeDate,
      Date endTimeDate) {
    StringBuilder sql = new StringBuilder();
    sql.append("select flow_id from ").append(TABLE_ANALYSIS_SURICATA_ALERT);

    sql.append(" where sid = :sid ");
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sid", sid);
    enrichContainTimeRangeBetter(startTimeDate, endTimeDate, true, true, whereSql, params);

    sql.append(whereSql);
    sql.append(" order by timestamp limit 100 ");

    List<Object> flowIds = queryForObjectWithExceptionHandle(sql.toString(), params);
    return CollectionUtils.isEmpty(flowIds)
        ? Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)
        : flowIds;
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp, sid, msg, network_id, classtype_id, mitre_tactic_id, ");
    sql.append(" mitre_technique_id, cve, cnnvd, signature_severity, target, ");
    sql.append(" src_ipv4, src_ipv6, src_port, dest_ipv4, dest_ipv6, dest_port, ");
    sql.append(" protocol, l7_protocol, flow_id, domain, url, country_id_initiator, ");
    sql.append(" province_id_initiator, city_id_initiator, country_id_responder, ");
    sql.append(" province_id_responder, city_id_responder, source, tag, basic_tag ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT);
    return sql;
  }

  private void enrichWhereSql(SuricataRuleQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1=1 ");
    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and network_id  =:networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      enrichIpCondition(queryVO.getSrcIp(), "src_ipv4", "src_ipv6", whereSql, params, "src");
    }
    if (StringUtils.isNotBlank(queryVO.getDestIp())) {
      enrichIpCondition(queryVO.getDestIp(), "dest_ipv4", "dest_ipv6", whereSql, params, "dest");
    }

  }

  private static void enrichIpCondition(String ipCondition, String ipv4FieldName,
      String ipv6FieldName, StringBuilder whereSql, Map<String, Object> params,
      String paramsIndexIdent) {
    List<String> ipv4List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> ipv4CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String[] ipConditionList = StringUtils.split(ipCondition, ",");

    if (ipConditionList.length > FpcCmsConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "查询的IP地址条目数过多, 请修改查询。");
    }

    // 按ip类型分类
    for (String ip : ipConditionList) {
      if (StringUtils.contains(ip, "-")) {
        // ip范围 10.0.0.1-10.0.0.100
        String[] ipRange = StringUtils.split(ip, "-");
        if (ipRange.length != 2) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }

        String ipStart = ipRange[0];
        String ipEnd = ipRange[1];

        // 起止都是正确的ip
        if (!NetworkUtils.isInetAddress(StringUtils.trim(ipStart))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipEnd))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ipStart, NetworkUtils.IpVersion.V4)
            && NetworkUtils.isInetAddress(ipEnd, NetworkUtils.IpVersion.V4)) {
          ipv4RangeList.add(Tuples.of(ipStart, ipEnd));
        } else if (NetworkUtils.isInetAddress(ipStart, NetworkUtils.IpVersion.V6)
            && NetworkUtils.isInetAddress(ipEnd, NetworkUtils.IpVersion.V6)) {
          ipv6RangeList.add(Tuples.of(ipStart, ipEnd));
        } else {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
      } else {
        // 单个IP或CIDR格式
        ip = StringUtils.trim(ip);
        if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V4)) {
          ipv4List.add(ip);
        } else if (NetworkUtils.isCidr(ip, NetworkUtils.IpVersion.V4)) {
          ipv4CidrList.add(ip);
        } else if (NetworkUtils.isInetAddress(ip, NetworkUtils.IpVersion.V6)
            || NetworkUtils.isCidr(ip, NetworkUtils.IpVersion.V6)) {
          ipv6List.add(ip);
        } else if (NetworkUtils.isCidr(ip, NetworkUtils.IpVersion.V6)) {
          ipv6CidrList.add(ip);
        }
      }
    }

    List<String> ipConditionSqlList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 拼接sql
    if (CollectionUtils.isNotEmpty(ipv4List) || CollectionUtils.isNotEmpty(ipv6List)
        || CollectionUtils.isNotEmpty(ipv4CidrList) || CollectionUtils.isNotEmpty(ipv6CidrList)
        || CollectionUtils.isNotEmpty(ipv4RangeList) || CollectionUtils.isNotEmpty(ipv6RangeList)) {
      int index = 0;

      // 单ipv4
      if (CollectionUtils.isNotEmpty(ipv4List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv4List.size());
        for (String ip : ipv4List) {
          tmpList.add("toIPv4(:ipv4_" + index + paramsIndexIdent + ")");
          params.put("ipv4_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv4FieldName, StringUtils.join(tmpList, ",")));
      }

      // 单ipv6
      if (CollectionUtils.isNotEmpty(ipv6List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv6List.size());
        for (String ip : ipv6List) {
          tmpList.add("toIPv6(:ipv6_" + index + paramsIndexIdent + ")");
          params.put("ipv6_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv6FieldName, StringUtils.join(tmpList, ",")));
      }

      // ipv4掩码
      if (CollectionUtils.isNotEmpty(ipv4CidrList)) {
        for (String ip : ipv4CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).1 and IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).2) ",
              ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv4_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }
      // ipv6掩码
      if (CollectionUtils.isNotEmpty(ipv6CidrList)) {
        for (String ip : ipv6CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).1 and IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).2) ",
              ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv6_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }

      // ipv4范围
      for (Tuple2<String, String> range : ipv4RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv4(:ipv4_start%s) and toIPv4(:ipv4_end%s)) ",
                ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv4_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv4_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      // ipv6范围
      for (Tuple2<String, String> range : ipv6RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv6(:ipv6_start%s) and toIPv6(:ipv6_end%s)) ",
                ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv6_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv6_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      whereSql.append(" and ( ");
      whereSql.append(String.join(" or ", ipConditionSqlList));
      whereSql.append(" ) ");
    }
  }

  private void whereSql(SuricataRuleQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1=1 ");

    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String, Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(), false,
            queryVO.getTimePrecision(), queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and network_id  =:networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and network_id in (:networkIds) ");
      params.put("networkIds", queryVO.getNetworkIds());
    }
    if (StringUtils.isNotBlank(queryVO.getSid())) {
      List<Integer> sidList = Lists.newArrayListWithCapacity(0);
      try {
        sidList = CsvUtils.convertCSVToList(queryVO.getSid()).stream()
            .map(sid -> Integer.parseInt(sid)).collect(Collectors.toList());
      } catch (NumberFormatException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "规则ID参数异常");
      }
      whereSql.append(" and sid in (:sid) ");
      params.put("sid", sidList);
    }
    if (StringUtils.isNotBlank(queryVO.getProtocol())) {
      whereSql.append(" and protocol = :protocol ");
      params.put("protocol", queryVO.getProtocol());
    }
    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      enrichIpCondition(queryVO.getSrcIp(), "src_ipv4", "src_ipv6", whereSql, params, "src");
    }
    if (StringUtils.isNotBlank(queryVO.getSrcPort())) {
      List<String> srcPorts = CsvUtils.convertCSVToList(queryVO.getSrcPort());
      if (srcPorts.size() == 1) {
        whereSql.append(" and src_port = srcPort ");
        params.put("srcPort", queryVO.getSrcPort());
      } else {
        whereSql.append(" and src_port in (srcPorts) ");
        params.put("srcPorts", srcPorts);
      }
    }
    if (StringUtils.isNotBlank(queryVO.getDestIp())) {
      enrichIpCondition(queryVO.getDestIp(), "dest_ipv4", "dest_ipv6", whereSql, params, "dest");
    }
    if (StringUtils.isNotBlank(queryVO.getDestPort())) {
      List<String> destPorts = CsvUtils.convertCSVToList(queryVO.getDestPort());
      if (destPorts.size() == 1) {
        whereSql.append(" and dest_port = destPort ");
        params.put("destPort", queryVO.getDestPort());
      } else {
        whereSql.append(" and dest_port in (destPorts) ");
        params.put("destPorts", destPorts);
      }
    }
    if (StringUtils.isNotBlank(queryVO.getMsg())) {
      whereSql.append(" and msg like :msg ");
      params.put("msg", "%" + queryVO.getMsg() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getClasstypeIds())) {
      whereSql.append(" and classtype_id in (:classtypeIds) ");
      params.put("classtypeIds", CsvUtils.convertCSVToList(queryVO.getClasstypeIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getMitreTacticIds())) {
      whereSql.append(" and mitre_tactic_id in (:mitreTacticIds) ");
      params.put("mitreTacticIds", CsvUtils.convertCSVToList(queryVO.getMitreTacticIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getMitreTechniqueIds())) {
      whereSql.append(" and mitre_technique_id in (:mitreTechniqueIds) ");
      params.put("mitreTechniqueIds", CsvUtils.convertCSVToList(queryVO.getMitreTechniqueIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getCve())) {
      whereSql.append(" and cve = :cve ");
      params.put("cve", queryVO.getCve());
    }
    if (StringUtils.isNotBlank(queryVO.getCnnvd())) {
      whereSql.append(" and cnnvd = :cnnvd ");
      params.put("cnnvd", queryVO.getCnnvd());
    }
    if (StringUtils.isNotBlank(queryVO.getSignatureSeverity())) {
      whereSql.append(" and signature_severity = :signatureSeverity ");
      params.put("signatureSeverity", queryVO.getSignatureSeverity());
    }
    if (StringUtils.isNotBlank(queryVO.getTarget())) {
      whereSql.append(" and target = :target ");
      params.put("target", queryVO.getTarget());
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append(" and source = :source ");
      params.put("source", queryVO.getSource());
    }
    if (StringUtils.isNotBlank(queryVO.getTag())) {
      whereSql.append(" and has(tag, :tag)=1 ");
      params.put("tag", queryVO.getTag());
    }
    if (StringUtils.isNotBlank(queryVO.getBasicTag())) {
      whereSql.append(" and basic_tag = :basicTag ");
      params.put("basicTag", queryVO.getBasicTag());
    }
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 3, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 3, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private SuricataAlertMessageDO convertSuricataAlertMap2DO(Map<String, Object> map) {
    SuricataAlertMessageDO suricataAlertMessageDO = new SuricataAlertMessageDO();
    suricataAlertMessageDO.setTimestamp(MapUtils.getString(map, "timestamp"));
    suricataAlertMessageDO.setSid(MapUtils.getIntValue(map, "sid"));
    suricataAlertMessageDO.setMsg(MapUtils.getString(map, "msg"));
    suricataAlertMessageDO.setNetworkId(MapUtils.getString(map, "network_id"));
    suricataAlertMessageDO.setClasstypeId(MapUtils.getString(map, "classtype_id"));
    suricataAlertMessageDO.setMitreTacticId(MapUtils.getString(map, "mitre_tactic_id"));
    suricataAlertMessageDO.setMitreTechniqueId(MapUtils.getString(map, "mitre_technique_id"));
    suricataAlertMessageDO.setCve(MapUtils.getString(map, "cve"));
    suricataAlertMessageDO.setCnnvd(MapUtils.getString(map, "cnnvd"));
    suricataAlertMessageDO.setSignatureSeverity(MapUtils.getInteger(map, "signature_severity"));
    suricataAlertMessageDO.setTarget(MapUtils.getString(map, "target"));
    Inet4Address srcIpv4 = (Inet4Address) map.get("src_ipv4");
    suricataAlertMessageDO.setSrcIpv4(srcIpv4 != null ? srcIpv4.getHostAddress() : null);
    Inet6Address srcIpv6 = (Inet6Address) map.get("src_ipv6");
    suricataAlertMessageDO.setSrcIpv6(srcIpv6 != null ? srcIpv6.getHostAddress() : null);
    suricataAlertMessageDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    Inet4Address destIpv4 = (Inet4Address) map.get("dest_ipv4");
    suricataAlertMessageDO.setDestIpv4(destIpv4 != null ? destIpv4.getHostAddress() : null);
    Inet6Address destIpv6 = (Inet6Address) map.get("dest_ipv6");
    suricataAlertMessageDO.setDestIpv6(destIpv6 != null ? destIpv6.getHostAddress() : null);
    suricataAlertMessageDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
    suricataAlertMessageDO.setProtocol(MapUtils.getString(map, "protocol"));
    suricataAlertMessageDO.setL7Protocol(MapUtils.getString(map, "l7_protocol"));
    suricataAlertMessageDO.setFlowId(MapUtils.getString(map, "flow_id"));
    suricataAlertMessageDO.setDomain(MapUtils.getString(map, "domain"));
    suricataAlertMessageDO.setUrl(MapUtils.getString(map, "url"));
    suricataAlertMessageDO.setCountryIdInitiator(MapUtils.getInteger(map, "country_id_initiator"));
    suricataAlertMessageDO
        .setProvinceIdInitiator(MapUtils.getInteger(map, "province_id_initiator"));
    suricataAlertMessageDO.setCityIdInitiator(MapUtils.getInteger(map, "city_id_initiator"));
    suricataAlertMessageDO.setCountryIdResponder(MapUtils.getInteger(map, "country_id_responder"));
    suricataAlertMessageDO
        .setProvinceIdResponder(MapUtils.getInteger(map, "province_id_responder"));
    suricataAlertMessageDO.setCityIdResponder(MapUtils.getInteger(map, "city_id_responder"));
    suricataAlertMessageDO.setSource(MapUtils.getString(map, "source"));
    List<String> tags = JsonHelper.deserialize(JsonHelper.serialize(map.get("tag")),
        new TypeReference<List<String>>() {
        });
    suricataAlertMessageDO.setTag(CsvUtils.convertCollectionToCSV(tags));
    suricataAlertMessageDO.setBasicTag(MapUtils.getString(map, "basic_tag"));

    return suricataAlertMessageDO;
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
                                               RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
              ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
              : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
                errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
                "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  private Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
              ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
              : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
                errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
                "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  protected List<Object> queryForObjectWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    List<Object> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForList(sql, paramMap, Object.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        throw e;
      }
    }
    return result;
  }

}
