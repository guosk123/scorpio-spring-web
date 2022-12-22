package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.manager.metric.dao.MetricDnsDataRecordDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author chenxiao
 * create at 2022/8/22
 */
@Repository
public class MetricDnsDataRecordDaoImpl implements MetricDnsDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDnsDataRecordDaoImpl.class);

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  private static final String TABLE_NAME = "t_fpc_protocol_dns_log_record";


  @Override
  public Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page,
      LogRecordQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    String tableName = convertTableName(queryVO);
    sql.append(
        " select domain , count(dns_rcode) as totalCounts, count(if(dns_rcode!=0,1,null)) as failCounts ");
    sql.append(" from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by ").append(" domain ");

    Sort sort = page.getSort();
    Sort.Order order = sort.iterator().next();
    String sortProperty = order.getProperty();
    sql.append(" having ").append(sortProperty).append(" >0 ");
    PageUtils.appendPage(sql, page, Lists.newArrayListWithCapacity(0));

    List<Map<String, Object>> resultList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    long total = 0;
    return new PageImpl<>(resultList, page, total);

  }

  @Override
  public long countDnsLogStatistics(LogRecordQueryVO queryVO, String sortProperty) {
    StringBuilder sql = new StringBuilder();
    String tableName = convertTableName(queryVO);
    sql.append(" select count(1) from ( ");
    sql.append(
        " select domain , count(dns_rcode) as totalCounts, count(if(dns_rcode!=0,1,null)) as failCounts ");
    sql.append(" from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by ").append(" domain ");

    sql.append(" having ").append(sortProperty).append(" >0 ").append(")");

    return queryForLongWithExceptionHandle(sql.toString(), params);
  }

  protected String convertTableName(LogRecordQueryVO queryVO) {
    String tableName = TABLE_NAME;
    if (StringUtils.equals(queryVO.getSourceType(), FpcConstants.SOURCE_TYPE_PACKET_FILE)) {
      PacketAnalysisSubTaskDO offlineAnalysisSubTask = offlineAnalysisSubTaskDao
          .queryPacketAnalysisSubTask(queryVO.getPacketFileId());
      if (StringUtils.isBlank(offlineAnalysisSubTask.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未查询到离线分析子任务");
      }

      tableName = String.join("_", TABLE_NAME, offlineAnalysisSubTask.getTaskId());

      // 判断离线数据包文件的分析结果是否存在
      List<Map<String, Object>> existResult = jdbcTemplate.getJdbcTemplate()
          .query(String.format("show tables from %s where name = '%s'",
              ManagerConstants.FPC_DATABASE, tableName), new ColumnMapRowMapper());
      if (CollectionUtils.isEmpty(existResult)) {
        LOGGER.warn("search failed, offline packet file metadata not found: {}", tableName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到协议详单");
      }
    }

    return tableName;
  }

  protected void enrichWhereSql(LogRecordQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1=1 ");
    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and has(network_id, :networkId)=1 ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and has(service_id, :serviceId)=1 ");
      params.put("serviceId", queryVO.getServiceId());
    }

    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      enrichIpCondition(queryVO.getSrcIp(), "src_ipv4", "src_ipv6", whereSql, params, "src");
    }
    if (StringUtils.isNotBlank(queryVO.getDestIp())) {
      enrichIpArrayCondition(queryVO.getDestIp(), "domain_ipv4", "domain_ipv6", whereSql, params,
          "domain");
    }


  }

  private static void enrichIpArrayCondition(String ipCondition, String ipv4FieldName,
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

    if (ipConditionList.length > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
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
        ipConditionSqlList.add(
            String.format(" hasAny(%s , [%s]) ", ipv4FieldName, StringUtils.join(tmpList, ",")));
      }

      // 单ipv6
      if (CollectionUtils.isNotEmpty(ipv6List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv6List.size());
        for (String ip : ipv6List) {
          tmpList.add("toIPv6(:ipv6_" + index + paramsIndexIdent + ")");
          params.put("ipv6_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList.add(
            String.format(" hasAny(%s , [%s]) ", ipv6FieldName, StringUtils.join(tmpList, ",")));
      }

      // ipv4掩码
      if (CollectionUtils.isNotEmpty(ipv4CidrList)) {
        for (String ip : ipv4CidrList) {
          ipConditionSqlList.add(String.format(
              " (length(arrayFilter(x -> x between IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).1 and IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).2 , %s)) > 0) ",
              index + paramsIndexIdent, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, ipv4FieldName));
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
              " (length(arrayFilter(x -> x between IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).1 and IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).2 , %s)) > 0) ",
              index + paramsIndexIdent, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, ipv6FieldName));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv6_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }

      // ipv4范围
      for (Tuple2<String, String> range : ipv4RangeList) {
        ipConditionSqlList.add(String.format(
            " (length(arrayFilter(x -> x between toIPv4(:ipv4_start%s) and toIPv4(:ipv4_end%s), %s)) > 0) ",
            index + paramsIndexIdent, index + paramsIndexIdent, ipv4FieldName));
        params.put("ipv4_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv4_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      // ipv6范围
      for (Tuple2<String, String> range : ipv6RangeList) {
        ipConditionSqlList.add(String.format(
            " (length(arrayFilter(x -> x between toIPv6(:ipv6_start%s) and toIPv6(:ipv6_end%s), %s)) > 0) ",
            index + paramsIndexIdent, index + paramsIndexIdent, ipv6FieldName));
        params.put("ipv6_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv6_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      whereSql.append(" and ( ");
      whereSql.append(String.join(" or ", ipConditionSqlList));
      whereSql.append(" ) ");
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

    if (ipConditionList.length > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
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

  protected void enrichContainTimeRangeBetter(Date startTime, Date endTime,
      boolean includeStartTime, boolean includeEndTime, StringBuilder whereSql,
      Map<String, Object> params) {
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  protected <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = jdbcTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
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
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  protected Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = jdbcTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
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
