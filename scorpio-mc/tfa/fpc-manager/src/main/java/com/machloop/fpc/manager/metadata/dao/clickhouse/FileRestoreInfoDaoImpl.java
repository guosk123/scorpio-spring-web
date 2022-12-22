package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.FileRestoreInfoDao;
import com.machloop.fpc.manager.metadata.data.FileRestoreInfoDO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author ChenXiao
 * create at 2022/10/27
 */
@Repository
public class FileRestoreInfoDaoImpl implements FileRestoreInfoDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileRestoreInfoDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_file_restore_info";


  protected static final int QUERY_BY_ID_LIMIT_SIZE = 10000;

  protected static final String START_TIME_DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private PacketAnalysisSubTaskDao packetAnalysisSubTaskDao;

  @Autowired
  private Spl2SqlHelper dslConverter;


  @Override
  public Page<FileRestoreInfoDO> queryFileRestoreInfos(LogRecordQueryVO queryVO, List<String> ids,
      PageRequest page) {

    // 数据源
    String tableName = convertTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 查询符合条件的记录timestamp,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select timestamp, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendPage(innerSelectSql, page, Lists.newArrayList("timestamp", "flow_id"));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, inner sql: {}, params: {}", tableName,
          innerSelectSql.toString(), innerParams);
    }

    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 使用已查到的timestamp,flow_id查找对应记录的全部字段
    List<String> rowIds = Lists.newArrayListWithCapacity(innerResult.size());
    for (Map<String, Object> r : innerResult) {
      OffsetDateTime start_time = (OffsetDateTime) r.get("timestamp");
      rowIds.add(start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
          + MapUtils.getString(r, "flow_id"));
    }

    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, queryVO.getColumns(), rowIds,
        page.getSort());
    List<FileRestoreInfoDO> resultDOList = result.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());

    return new PageImpl<>(resultDOList, page, 0);

  }

  @Override
  public List<FileRestoreInfoDO> queryFileRestoreInfosByIds(String tableName, String columns,
      List<String> ids, Sort sort) {
    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, columns, ids, sort);

    return result.stream().map(this::convertLogMap2LogDO).collect(Collectors.toList());

  }

  private List<Map<String, Object>> queryLogRecordsByIds(String tableName, String columns,
      List<String> ids, Sort sort) {

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> timestampConditions = Lists.newArrayListWithCapacity(ids.size());
    List<String> flowIdConditions = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] filters = StringUtils.split(id, "_");

      timestampConditions.add(String.format("toDateTime64(:timestamp%s, 3, 'UTC')", index));
      params.put("timestamp" + index, filters[0]);

      // flow_id
      flowIdConditions.add(String.format(":flow_id%s", index));
      params.put("flow_id" + index, filters[1]);

      index += 1;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("select * from (");

    // 排序字段
    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Sort.Order> iterator = sort.iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }

    // 嵌套子查询
    StringBuilder innersql = new StringBuilder();
    innersql.append("select ").append(buildSelectStatement(columns, sortProperties));
    innersql.append(" from ").append(tableName);
    // 主键start_time过滤
    innersql.append(" where timestamp in (");
    innersql.append(StringUtils.join(timestampConditions, ",")).append(")");
    sql.append(innersql).append(")");

    // flow_id过滤
    sql.append(" where flow_id in (");
    sql.append(StringUtils.join(flowIdConditions, ",")).append(")");

    PageUtils.appendSort(sql, sort, FlowLogQueryVO.class);
    sql.append(" limit ").append(QUERY_BY_ID_LIMIT_SIZE);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page by ids, ids: {}, sql: {}, params: {}", StringUtils.join(ids),
          sql.toString(), params);
    }

    List<Map<String, Object>> allResult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    List<Map<String, Object>> result = allResult.stream().filter(item -> {
      OffsetDateTime start_time = (OffsetDateTime) item.get("timestamp");
      return ids.contains(start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT))
          + "_" + MapUtils.getString(item, "flow_id"));
    }).collect(Collectors.toList());
    return result;

  }

  @Override
  public FileRestoreInfoDO queryFileRestoreInfo(LogRecordQueryVO queryVO, String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ")
        .append(buildSelectStatement(queryVO.getColumns(), Sets.newHashSet("timestamp")));
    sql.append(" from ").append(convertTableName(queryVO));
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    // id查询
    String[] startTimeAndFlowId = StringUtils.split(id, "_");
    whereSql.append(" where timestamp = toDateTime64(:timestamp, 3, 'UTC') ");
    whereSql.append(" and flow_id = :flow_id ");
    whereSql.append("");
    sql.append(" limit 1 ");
    params.put("timestamp", String.valueOf(startTimeAndFlowId[0]));
    params.put("flow_id", String.valueOf(startTimeAndFlowId[1]));

    sql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} log record by id, sql: {}, params: {}", convertTableName(queryVO), sql,
          params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(result) ? new FileRestoreInfoDO()
        : convertLogMap2LogDO(result.get(0));
  }

  @Override
  public Tuple2<String, List<String>> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids,
      Sort sort, int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select timestamp, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }

    innerSelectSql.append(whereSql);
    PageUtils.appendSort(innerSelectSql, sort, Lists.newArrayList("timestamp", "flow_id"));
    innerSelectSql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, inner sql: {}, params: {}", tableName,
          innerSelectSql.toString(), innerParams);
    }
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    return Tuples.of(tableName,
        innerResult.stream()
            .map(row -> ((OffsetDateTime) row.get("timestamp"))
                .format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
                + MapUtils.getString(row, "flow_id"))
            .collect(Collectors.toList()));
  }

  @Override
  public long countLogRecords(LogRecordQueryVO queryVO, List<String> ids) {
    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return 0;
    }

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, params);
    }

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(convertTableName(queryVO));
    totalSql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(queryVO), totalSql,
          params);
    }

    return queryForLongWithExceptionHandle(totalSql.toString(), params);
  }

  protected String convertTableName(LogRecordQueryVO queryVO) {
    String tableName = TABLE_NAME;
    if (StringUtils.equals(queryVO.getSourceType(), FpcConstants.SOURCE_TYPE_PACKET_FILE)) {
      PacketAnalysisSubTaskDO offlineAnalysisSubTask = packetAnalysisSubTaskDao
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

  private void enrichWhereSql(LogRecordQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1=1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(),
                queryVO.getHasAgingTime(), queryVO.getTimePrecision(),
                queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }
  }

  private void enrichIdCondition(List<String> ids, StringBuilder whereSql,
      Map<String, Object> params) {
    List<String> rowsCondition = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] split = StringUtils.split(id, "_");
      rowsCondition
          .add(String.format(" (timestamp=:timestamp%d and flow_id=:flow_id%d) ", index, index));
      params.put("timestamp" + index, split[0]);
      params.put("flow_id" + index, split[1]);
      index += 1;
    }

    whereSql.append(" and (").append(StringUtils.join(rowsCondition, " or ")).append(")");
  }

  private List<Map<String, Object>> queryWithExceptionHandle(String sql,
      Map<String, Object> paramMap, ColumnMapRowMapper rowMapper) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
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

  private String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return " flow_id,network_id,timestamp,IPv6NumToString(src_ip) as src_ip,src_port,to,IPv6NumToString(dest_ip) as dest_ip,dest_port,"
          + " md5,sha1,sha256,name,size,magic,l7_protocol,state ";
    }
    // 必须查询排序字段
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      String result = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;

      return result.replace(",src_ip", ",IPv6NumToString(src_ip) as src_ip").replace(",dest_ip",
          ",IPv6NumToString(dest_ip) as dest_ip");
    }
  }

  private FileRestoreInfoDO convertLogMap2LogDO(Map<String, Object> map) {

    FileRestoreInfoDO fileRestoreInfoDO = new FileRestoreInfoDO();
    if (map.containsKey("timestamp")) {
      fileRestoreInfoDO.setTimestamp(DateUtils
          .toStringYYYYMMDDHHMMSS((OffsetDateTime) map.get("timestamp"), ZoneId.systemDefault()));
    }

    fileRestoreInfoDO.setFlowId(String.valueOf(MapUtils.getLong(map, "flow_id")));

    if (map.get("network_id") != null) {
      fileRestoreInfoDO.setNetworkId(MapUtils.getString(map, "network_id"));
    }

    if (map.containsKey("src_ip")) {
      String srcIp = MapUtils.getString(map, "src_ip");
      if (StringUtils.isNotBlank(srcIp)
          && StringUtils.startsWith(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        srcIp = StringUtils.substringAfter(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
      }
      fileRestoreInfoDO.setSrcIp(srcIp);
    }

    if (map.containsKey("dest_ip")) {
      String destIp = MapUtils.getString(map, "dest_ip");
      if (StringUtils.isNotBlank(destIp)
          && StringUtils.startsWith(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        destIp = StringUtils.substringAfter(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
      }
      fileRestoreInfoDO.setDestIp(destIp);
    }

    fileRestoreInfoDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    fileRestoreInfoDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
    fileRestoreInfoDO.setMd5(MapUtils.getString(map, "md5"));
    fileRestoreInfoDO.setSha1(MapUtils.getString(map, "sha1"));
    fileRestoreInfoDO.setSha256(MapUtils.getString(map, "sha256"));
    fileRestoreInfoDO.setName(MapUtils.getString(map, "name"));
    fileRestoreInfoDO.setSize(MapUtils.getLongValue(map, "size"));
    fileRestoreInfoDO.setMagic(MapUtils.getString(map, "magic"));
    fileRestoreInfoDO.setL7Protocol(MapUtils.getString(map, "l7_protocol"));
    fileRestoreInfoDO.setState(MapUtils.getIntValue(map, "state"));

    return fileRestoreInfoDO;
  }

}
