package com.machloop.fpc.manager.asset.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.ArrayList;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.asset.dao.AssetInformationDao;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月5日, fpc-manager
 */
@Repository
public class AssetInformationDaoImpl implements AssetInformationDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetInformationDaoImpl.class);

  private static final String ASSET_FIRST_TABLE = "t_fpc_asset_first";

  private static final String ASSET_LATEST_TABLE = "t_fpc_asset_latest";

  private static final String ASSET_INFORMATION_TABLE = "t_fpc_asset_information";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetInformationDao#queryAssetsWithValueAndFirstTime(java.lang.String, com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.util.List, com.machloop.alpha.common.base.page.Pageable, int)
   */
  @Override
  public List<Map<String, Object>> queryAssetsWithValueAndFirstTime(String queryId,
      AssetInformationQueryVO queryVO, List<String> alarmIpList, Pageable page, int count) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // ip+timestamp可以确定一个资产（可以确定一个ip地址所有的属性）
    StringBuilder firstSql = new StringBuilder(securityQueryId);
    StringBuilder innerSql = new StringBuilder(securityQueryId);
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    firstSql.append("select IPv6NumToString(ip) as ip, minMerge(f.timestamp) AS firstTime ");
    firstSql.append(" from ").append(ASSET_FIRST_TABLE).append(" as f ");
    firstSql.append(" inner join ");

    innerSql.append("(select IPv6NumToString(ip) AS ip, maxMerge(timestamp) as timestamp  ");
    innerSql.append(" from ").append(ASSET_LATEST_TABLE);
    enrichWhereSql(queryVO, innerSql, innerParams, null, alarmIpList);
    innerSql.append(" group by ip ");
    innerSql.append(" ) as l");

    firstSql.append(innerSql);

    firstSql.append(" on l.ip = f.ip ");
    firstSql.append(" group by ip ");

    StringBuilder firstTimeSql = new StringBuilder(securityQueryId);
    // 过滤时间
    if (StringUtils.isNotBlank(queryVO.getStartTime())
        && StringUtils.isNotBlank(queryVO.getEndTime())) {
      firstTimeSql.append(" select ip, firstTime as timestamp ");
      firstTimeSql.append(" from( ").append(firstSql);
      enrichContainTimeRangeBetter(queryVO, true, false, firstSql, innerParams);
      firstTimeSql.append(" )group by ip, timestamp ");
    }
    if (page != null) {
      PageUtils.appendPage(firstSql, page, AssetInformationQueryVO.class);
    }
    if (count != 0) {
      firstSql.append(" limit ").append(count);
    }

    List<Map<String, Object>> firstResult = queryWithExceptionHandle(firstTimeSql.toString(),
        innerParams, new ColumnMapRowMapper());
    List<String> ipList = firstResult.stream().map(item -> MapUtils.getString(item, "ip"))
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(firstResult)) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 根据第一个sql获取所有数据
    StringBuilder secondSql = new StringBuilder(securityQueryId);
    Map<String, Object> secondParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    secondSql.append(
        "select IPv6NumToString(ip) as ip, type, value1, value2, maxMerge(timestamp) as timestamp ");
    secondSql.append(" from ").append(ASSET_LATEST_TABLE);
    secondSql.append(" where ip in (:ipList) ");
    secondSql.append(" group by ip, type, account, value1, value2 ");
    secondParams.put("ipList", ipList);
    List<Map<String, Object>> result = queryWithExceptionHandle(secondSql.toString(), secondParams,
        new ColumnMapRowMapper());

    Map<String, String> ipFirstTimeMap = firstResult.stream().collect(Collectors.toMap(
        item -> MapUtils.getString(item, "ip"), item -> MapUtils.getString(item, "timestamp")));

    result.forEach(item -> {
      item.put("firstTime", MapUtils.getString(ipFirstTimeMap, MapUtils.getString(item, "ip")));
      String ipAddress = MapUtils.getString(item, "ip");
      if (StringUtils.startsWithIgnoreCase(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        item.put("ip",
            StringUtils.substringAfterLast(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX));
      }
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetInformationDao#queryAssetsWithValue(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.util.List, java.util.List, com.machloop.alpha.common.base.page.Pageable, int)
   */
  @Override
  public List<Map<String, Object>> queryAssetsWithValue(AssetInformationQueryVO queryVO,
      List<String> baselineIpList, List<String> alarmIpList, Pageable page, int count) {

    StringBuilder ipSql = new StringBuilder();
    Map<String, Object> ipParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    ipSql.append("select IPv6NumToString(ip) AS ip, maxMerge(timestamp) as timestamp ");
    ipSql.append(" from ").append(ASSET_LATEST_TABLE);

    enrichWhereSql(queryVO, ipSql, ipParams, baselineIpList, alarmIpList);
    ipSql.append(" group by ip ");
    if (page != null) {
      PageUtils.appendPage(ipSql, page, AssetInformationQueryVO.class);
    }
    if (count != 0) {
      ipSql.append(" limit ").append(count);
    }

    List<Map<String, Object>> ipTimeResult = queryWithExceptionHandle(ipSql.toString(), ipParams,
        new ColumnMapRowMapper());

    if (CollectionUtils.isEmpty(ipTimeResult)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    List<String> latestIpList = ipTimeResult.stream().map(item -> MapUtils.getString(item, "ip"))
        .collect(Collectors.toList());

    StringBuilder firstTimeSql = new StringBuilder();
    Map<String,
        Object> firstTimeParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    firstTimeSql.append("select IPv6NumToString(ip) as ip, minMerge(timestamp) as firstTime ");
    firstTimeSql.append(" from ").append(ASSET_FIRST_TABLE);
    firstTimeSql.append(" where ip in (:latestIpList) ");
    firstTimeSql.append(" group by ip ");

    firstTimeParams.put("latestIpList", latestIpList);

    List<Map<String, Object>> firstTimeResult = queryWithExceptionHandle(firstTimeSql.toString(),
        firstTimeParams, new ColumnMapRowMapper());
    Map<String, String> ipFirstTimeMap = firstTimeResult.stream().collect(Collectors.toMap(
        item -> MapUtils.getString(item, "ip"), item -> MapUtils.getString(item, "firstTime")));

    StringBuilder resultSql = new StringBuilder();
    Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    resultSql.append(
        "select IPv6NumToString(ip) as ip, type, maxMerge(timestamp) as timestamp, value1, value2, account ");
    resultSql.append(" from ").append(ASSET_LATEST_TABLE);
    resultSql.append(" where ip in (:latestIpList) ");
    resultSql.append(" group by ip, type, account, value1, value2 ");
    resultParams.put("latestIpList", latestIpList);
    List<Map<String, Object>> result = queryWithExceptionHandle(resultSql.toString(), resultParams,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("firstTime", MapUtils.getString(ipFirstTimeMap, MapUtils.getString(item, "ip")));
      String ipAddress = MapUtils.getString(item, "ip");
      if (StringUtils.startsWithIgnoreCase(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        item.put("ip",
            StringUtils.substringAfterLast(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX));
      }
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetInformationDao#queryAssetsWithFirstTime(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.util.List, com.machloop.alpha.common.base.page.Pageable, int)
   */
  @Override
  public List<Map<String, Object>> queryAssetsWithFirstTime(AssetInformationQueryVO queryVO,
      List<String> alarmIpList, Pageable page, int count) {

    StringBuilder ipFirstTimeSql = new StringBuilder();
    StringBuilder ipFirstTimeInnerSql = new StringBuilder();
    Map<String,
        Object> ipFirstTimeParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    ipFirstTimeSql.append("select ip, firstTime ").append(" from ");
    ipFirstTimeInnerSql
        .append("(select IPv6NumToString(ip) as ip, minMerge(timestamp) as firstTime ");
    ipFirstTimeInnerSql.append(" from ").append(ASSET_FIRST_TABLE);
    ipFirstTimeInnerSql.append(" group by ip ) ");
    ipFirstTimeSql.append(ipFirstTimeInnerSql);
    enrichFirstTimeWhereSql(queryVO, ipFirstTimeSql, ipFirstTimeParams, null, alarmIpList);
    ipFirstTimeSql.append(" group by ip, firstTime ");

    if (page != null) {
      PageUtils.appendPage(ipFirstTimeSql, page, AssetInformationQueryVO.class);
    }
    if (count != 0) {
      ipFirstTimeSql.append(" limit ").append(count);
    }

    List<Map<String, Object>> ipFirstTimeResult = queryWithExceptionHandle(
        ipFirstTimeSql.toString(), ipFirstTimeParams, new ColumnMapRowMapper());

    if (CollectionUtils.isEmpty(ipFirstTimeResult)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    List<String> ipFirstTimeList = ipFirstTimeResult.stream()
        .map(item -> MapUtils.getString(item, "ip")).collect(Collectors.toList());
    Map<String, String> ipFirstTimeMap = ipFirstTimeResult.stream().collect(Collectors.toMap(
        item -> MapUtils.getString(item, "ip"), item -> MapUtils.getString(item, "firstTime")));

    StringBuilder resultSql = new StringBuilder();
    Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    resultSql.append(
        "select IPv6NumToString(ip) as ip, type, value1, value2, maxMerge(timestamp) as timestamp ");
    resultSql.append(" from ").append(ASSET_LATEST_TABLE);
    resultSql.append(" where ip in (:ipFirstTimeList) ");
    resultSql.append(" group by ip, type, value1, value2 ");
    resultParams.put("ipFirstTimeList", ipFirstTimeList);

    List<Map<String, Object>> result = queryWithExceptionHandle(resultSql.toString(), resultParams,
        new ColumnMapRowMapper());
    if (CollectionUtils.isEmpty(result)) {
      result.addAll(ipFirstTimeResult);
    } else {
      // 将ipFirstTimeList与ipList交集之外的ip和firstTime赋给result
      List<String> ipList = result.stream().map(item -> MapUtils.getString(item, "ip"))
          .collect(Collectors.toList());
      ipFirstTimeList.removeAll(ipList);
      for (String ip : ipFirstTimeList) {
        Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        temp.put("ip", ip);
        temp.put("firstTime", ipFirstTimeMap.get(ip));
        result.add(temp);
      }
    }

    result.forEach(item -> {
      item.put("firstTime", MapUtils.getString(ipFirstTimeMap, MapUtils.getString(item, "ip")));
      String ipAddress = MapUtils.getString(item, "ip");
      if (StringUtils.startsWithIgnoreCase(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        item.put("ip",
            StringUtils.substringAfterLast(ipAddress, ManagerConstants.IPV4_TO_IPV6_PREFIX));
      }
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetInformationDao#countAssetInformation(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.util.List)
   */
  @Override
  public long countAssetInformation(AssetInformationQueryVO queryVO, List<String> alarmIpList) {

    StringBuilder countSql = new StringBuilder();
    StringBuilder ipSql = new StringBuilder();
    StringBuilder fullSql = new StringBuilder();

    Map<String, Object> ipParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    countSql.append("select count(1) ");
    countSql.append(" from ");

    ipSql.append(" (select ip ");
    ipSql.append(" from ");

    if (StringUtils.isAllBlank(queryVO.getDeviceType(), queryVO.getOs(), queryVO.getPort(),
        queryVO.getIpAddress())
        && (StringUtils.isNotBlank(queryVO.getStartTime())
            || StringUtils.isNotBlank(queryVO.getEndTime()))) {
      fullSql.append(" (select IPv6NumToString(ip) as ip, minMerge(timestamp) as firstTime ");
      fullSql.append(" from ").append(ASSET_FIRST_TABLE);
      fullSql.append(" group by ip) ");
    } else {
      fullSql.append(
          " (select IPv6NumToString(ip) as ip, type, maxMerge(timestamp) as timestamp, value1, value2, account ");
      fullSql.append(" from ").append(ASSET_LATEST_TABLE);
      fullSql.append(" group by ip, type, account, value1, value2) ");
    }

    ipSql.append(fullSql);
    enrichWhereSql(queryVO, ipSql, ipParams, null, alarmIpList);
    ipSql.append(" group by ip) ");

    countSql.append(ipSql);

    return queryForLongWithExceptionHandle(countSql.toString(), ipParams);
  }

  private void enrichWhereSql(AssetInformationQueryVO queryVO, StringBuilder firstSql,
      Map<String, Object> firstParams, List<String> baselineIpList, List<String> alarmIpList) {

    firstSql.append(" where 1=1 ");

    Map<String, String> filterMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getDeviceType())) {
      filterMap.put("1", queryVO.getDeviceType());
    }
    if (StringUtils.isNotBlank(queryVO.getPort())) {
      filterMap.put("2", queryVO.getPort());
    }
    if (StringUtils.isNotBlank(queryVO.getOs())) {
      filterMap.put("4", queryVO.getOs());
    }

    StringBuilder inSql = new StringBuilder();
    inSql.append("(select IPv6NumToString(ip) AS ip ");
    inSql.append(" from ").append(ASSET_LATEST_TABLE);
    inSql.append(" where type = :inType and value1 = :inValue1 ");
    inSql.append(" group by ip) ");

    StringBuilder middleSql = new StringBuilder();
    middleSql.append("(select IPv6NumToString(ip) AS ip ");
    middleSql.append(" from ").append(ASSET_LATEST_TABLE);
    middleSql.append(" where type = :midType and value1 = :midValue1 ");
    middleSql.append(" and ip in ").append(inSql);
    middleSql.append(" group by ip) ");

    StringBuilder outSql = new StringBuilder();
    outSql.append("(select IPv6NumToString(ip) AS ip ");
    outSql.append(" from ").append(ASSET_LATEST_TABLE);
    outSql.append(" where type = :outType and value1 = :outValue1 ");
    outSql.append(" and ip in ").append(middleSql);
    outSql.append(" group by ip) ");

    if (filterMap.keySet().size() >= 1) {
      List<String> typeList = new ArrayList<String>(filterMap.keySet());
      firstSql.append(" and ip in ");
      firstParams.put("inType", typeList.get(0));
      firstParams.put("inValue1", filterMap.get(typeList.get(0)));
      if (filterMap.keySet().size() >= 2) {
        firstParams.put("midType", typeList.get(1));
        firstParams.put("midValue1", filterMap.get(typeList.get(1)));
        if (filterMap.keySet().size() >= 3) {
          firstParams.put("outType", typeList.get(2));
          firstParams.put("outValue1", filterMap.get(typeList.get(2)));
          firstSql.append(outSql);
        } else {
          firstSql.append(middleSql);
        }
      } else {
        firstSql.append(inSql);
      }
    }

    if (CollectionUtils.isNotEmpty(alarmIpList)) {
      List<String> alarmIps = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (String alarmIp : alarmIpList) {
        alarmIps
            .add(!StringUtils.startsWithIgnoreCase(alarmIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
                ? ManagerConstants.IPV4_TO_IPV6_PREFIX + alarmIp
                : alarmIp);
      }
      firstSql.append(" and ip in (:alarmIps) ");
      firstParams.put("alarmIps", alarmIps);
    }

    if (CollectionUtils.isNotEmpty(baselineIpList)) {
      List<String> baselineIps = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (String baselineIp : baselineIpList) {
        baselineIps
            .add(!StringUtils.startsWithIgnoreCase(baselineIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)
                ? ManagerConstants.IPV4_TO_IPV6_PREFIX + baselineIp
                : baselineIp);
      }
      firstSql.append(" and ip in (:baselineIps) ");
      firstParams.put("baselineIps", baselineIps);
    }

    String ipAddress = queryVO.getIpAddress();
    if (StringUtils.isNotBlank(ipAddress)) {
      ipAddress = NetworkUtils.isInetAddress(ipAddress, IpVersion.V4)
          ? ManagerConstants.IPV4_TO_IPV6_PREFIX + ipAddress
          : ipAddress;
      firstSql.append(" and ip = :ip ");
      firstParams.put("ip", ipAddress);
    }
  }

  private void enrichFirstTimeWhereSql(AssetInformationQueryVO queryVO, StringBuilder firstSql,
      Map<String, Object> firstParams, List<String> baselineIpList, List<String> alarmIpList) {

    firstSql.append(" where 1=1 ");

    // 过滤时间
    if (StringUtils.isNotBlank(queryVO.getStartTime())
        && StringUtils.isNotBlank(queryVO.getEndTime())) {
      enrichContainTimeRangeBetter(queryVO, true, false, firstSql, firstParams);
    }

    String ipAddress = queryVO.getIp();
    if (StringUtils.isNotBlank(ipAddress)) {
      ipAddress = NetworkUtils.isInetAddress(ipAddress, IpVersion.V4)
          ? ManagerConstants.IPV4_TO_IPV6_PREFIX + ipAddress
          : ipAddress;
      firstSql.append(" and ip = :ip ");
      firstParams.put("ip", ipAddress);
    }
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetInformationDao#dropExpiredData(java.lang.String)
   */
  @Override
  public void dropExpiredData(String partitionName) {
    StringBuilder partitionSql = new StringBuilder();
    // 删除partitionName之前的所有分区
    partitionSql.append(" select partition from system.parts ");
    partitionSql.append(" where table = '").append(ASSET_INFORMATION_TABLE).append("'");
    partitionSql.append(" and partition < ").append(partitionName);

    List<Map<String, Object>> partitionMap = queryWithExceptionHandle(partitionSql.toString(), null,
        new ColumnMapRowMapper());

    List<String> partitionList = partitionMap.stream()
        .map(item -> MapUtils.getString(item, "partition")).collect(Collectors.toList());

    StringBuilder dropSql = new StringBuilder();
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
    map.put("dropPartition", partitionList);

    for (String partition : partitionList) {
      dropSql.append("alter table ").append(ASSET_INFORMATION_TABLE);
      dropSql.append(" drop partition :partition ");
      clickHouseTemplate.getJdbcTemplate().update(dropSql.toString(), map);
      LOGGER.info(" drop table {} partition {} success! ", ASSET_INFORMATION_TABLE, partition);

      dropSql.append("alter table ").append(ASSET_LATEST_TABLE);
      dropSql.append(" drop partition :partition ");
      clickHouseTemplate.getJdbcTemplate().update(dropSql.toString(), map);
      LOGGER.info(" drop table {} partition {} success! ", ASSET_LATEST_TABLE, partition);
    }
  }

  private void enrichContainTimeRangeBetter(AssetInformationQueryVO queryVO,
      boolean includeStartTime, boolean includeEndTime, StringBuilder whereSql,
      Map<String, Object> params) {

    if (StringUtils.isAllBlank(queryVO.getDeviceType(), queryVO.getOs(), queryVO.getPort(),
        queryVO.getIpAddress())
        && !StringUtils.equals(queryVO.getSortProperty(), "timestamp")
        && (StringUtils.isNotBlank(queryVO.getStartTime())
            || StringUtils.isNotBlank(queryVO.getEndTime())
            || StringUtils.equals(queryVO.getSortProperty(), "firstTime"))) {
      whereSql.append(String.format(" and firstTime %s toDateTime64(:start_time, 3, 'UTC') ",
          includeStartTime ? ">=" : ">"));
      whereSql.append(String.format(" and firstTime %s toDateTime64(:end_time, 3, 'UTC') ",
          includeEndTime ? "<=" : "<"));
    } else {
      whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 3, 'UTC') ",
          includeStartTime ? ">=" : ">"));
      whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 3, 'UTC') ",
          includeEndTime ? "<=" : "<"));
    }

    params.put("start_time", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
        "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(queryVO.getEndTimeDate(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result.addAll(clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper));
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query flow log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
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
        LOGGER.info("query metadata counter has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata counter failed, error msg: {}",
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
