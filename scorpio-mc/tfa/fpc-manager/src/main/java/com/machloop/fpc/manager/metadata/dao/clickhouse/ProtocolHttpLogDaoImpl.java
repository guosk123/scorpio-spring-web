package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolHttpLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolHttpLogDao")
public class ProtocolHttpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolHttpLogDO>
    implements LogRecordDao<ProtocolHttpLogDO> {

  private static final String TABLE_NAME = "t_fpc_protocol_http_log_record";

  private static List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "method",
      "host", "uri", "file_name", "file_type"/* , "file_flag" */, "xff");

  private static final List<String> WILDCARD_FIELD_NAMES = Lists.newArrayList("uri", "host");

  private static final ProtocolHttpLogDO EMPTY_DO = new ProtocolHttpLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getOfflineAnalysisSubTaskDao()
   */
  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getTableName(com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO)
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolHttpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolHttpLogDO protocolHttpLogDO = new ProtocolHttpLogDO();

    convertBaseLogMap2AbstractLogDO(protocolHttpLogDO, map);

    protocolHttpLogDO.setMethod(MapUtils.getString(map, "method"));
    protocolHttpLogDO.setHost(MapUtils.getString(map, "host"));
    protocolHttpLogDO.setUri(MapUtils.getString(map, "uri"));

    protocolHttpLogDO.setOrigin(MapUtils.getString(map, "origin"));
    protocolHttpLogDO.setCookie(MapUtils.getString(map, "cookie"));
    protocolHttpLogDO.setUserAgent(MapUtils.getString(map, "user_agent"));
    protocolHttpLogDO.setReferer(MapUtils.getString(map, "referer"));
    protocolHttpLogDO.setXff(MapUtils.getString(map, "xff"));
    protocolHttpLogDO.setStatus(MapUtils.getString(map, "status"));
    protocolHttpLogDO.setSetCookie(MapUtils.getString(map, "set_cookie"));
    protocolHttpLogDO.setContentType(MapUtils.getString(map, "content_type"));
    protocolHttpLogDO.setAcceptLanguage(MapUtils.getString(map, "accept_language"));
    protocolHttpLogDO.setRequestHeader(MapUtils.getString(map, "request_header"));
    protocolHttpLogDO.setRequestBody(MapUtils.getString(map, "request_body"));
    protocolHttpLogDO.setResponseHeader(MapUtils.getString(map, "response_header"));
    protocolHttpLogDO.setResponseBody(MapUtils.getString(map, "response_body"));

    protocolHttpLogDO.setFileName(MapUtils.getString(map, "file_name"));
    protocolHttpLogDO.setFileType(MapUtils.getString(map, "file_type"));
    protocolHttpLogDO.setFileFlag(MapUtils.getString(map, "file_flag"));
    protocolHttpLogDO.setAcceptEncoding(MapUtils.getString(map, "accept_encoding"));
    protocolHttpLogDO.setLocation(MapUtils.getString(map, "location"));
    protocolHttpLogDO.setDecrypted(MapUtils.getString(map, "decrypted"));
    protocolHttpLogDO.setAuthorization(MapUtils.getString(map, "authorization"));
    protocolHttpLogDO.setAuthType(MapUtils.getString(map, "auth_type"));
    protocolHttpLogDO.setOsVersion(MapUtils.getString(map, "os_version"));
    protocolHttpLogDO.setChannelState(MapUtils.getInteger(map, "channel_state"));
    String xffFirst = MapUtils.getString(map, "xff_first");
    if (StringUtils.isNotBlank(xffFirst)
        && StringUtils.startsWith(xffFirst, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      xffFirst = StringUtils.substringAfter(xffFirst, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolHttpLogDO.setXffFirst(xffFirst);
    String xffLast = MapUtils.getString(map, "xff_last");
    if (StringUtils.isNotBlank(xffLast)
        && StringUtils.startsWith(xffLast, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      xffLast = StringUtils.substringAfter(xffLast, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolHttpLogDO.setXffLast(xffLast);
    protocolHttpLogDO.setXffFirstAlias(MapUtils.getString(map, "xff_first_alias"));
    protocolHttpLogDO.setXffLastAlias(MapUtils.getString(map, "xff_last_alias"));
    return protocolHttpLogDO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolHttpLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or method = :keyword ");
    conditionSql.append(" or host = :keyword ");
    conditionSql.append(" or uri = :keyword ");
    conditionSql.append(" or file_name = :keyword ");
    conditionSql.append(" or file_type = :keyword ");
    conditionSql.append(" or xff = :keyword ");
    conditionSql.append(" or auth_type = :keyword ");
    conditionSql.append(" or os_version = :keyword ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" host like :keyword_like ");
    conditionSql.append(" or uri like :keyword_like ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return "level,policy_name,flow_id,network_id,application_id,start_time,end_time,src_ipv4,"
          + "src_ipv6,src_port,dest_ipv4,dest_ipv6,dest_port,file_name,file_type,file_flag,"
          + "accept_encoding,location,host,uri,origin,cookie,user_agent,referer,xff,status,"
          + "set_cookie,content_type,accept_language,location,method,host,uri,origin,cookie,"
          + "user_agent,referer,xff,status,set_cookie,content_type,request_header,request_body,"
          + "decrypted,authorization,auth_type,os_version,channel_state,IPv6NumToString(xff_first) as xff_first,"
          + "IPv6NumToString(xff_last) as xff_last,xff_first_alias,xff_last_alias";
    }
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      String result = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
      List<String> resultUpdateXffFirstAndXffLast = CsvUtils.convertCSVToList(result).stream()
          .map(filed -> {
            if (StringUtils.equals(filed, "xff_first")) {
              filed = "IPv6NumToString(xff_first) as xff_first";
            }

            if (StringUtils.equals(filed, "xff_last")) {
              filed = "IPv6NumToString(xff_last) as xff_last";
            }
            return filed;

          }).collect(Collectors.toList());
      return StringUtils.join(resultUpdateXffFirstAndXffLast, ",");
    }
  }
}
