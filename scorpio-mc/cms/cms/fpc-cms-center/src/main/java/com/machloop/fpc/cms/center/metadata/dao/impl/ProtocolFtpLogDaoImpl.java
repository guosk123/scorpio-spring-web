package com.machloop.fpc.cms.center.metadata.dao.impl;

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
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolFtpLogDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolFtpLogDao")
public class ProtocolFtpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolFtpLogDO>
    implements LogRecordDao<ProtocolFtpLogDO> {

  private static final String TABLE_NAME = "d_fpc_protocol_ftp_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "user");

  private static final ProtocolFtpLogDO EMPTY_DO = new ProtocolFtpLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolFtpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolFtpLogDO protocolFtpLogDO = new ProtocolFtpLogDO();

    convertBaseLogMap2AbstractLogDO(protocolFtpLogDO, map);

    protocolFtpLogDO.setUser(MapUtils.getString(map, "user"));
    protocolFtpLogDO.setCmdSeq(MapUtils.getString(map, "cmd_seq"));
    protocolFtpLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolFtpLogDO.setReply(MapUtils.getString(map, "reply"));
    protocolFtpLogDO.setFilename(MapUtils.getString(map, "filename"));
    String ip = MapUtils.getString(map, "data_channel_ip");
    if (StringUtils.isNotBlank(ip)
        && StringUtils.startsWith(ip, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
      ip = StringUtils.substringAfter(ip, CenterConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolFtpLogDO.setDataChannelIp(ip);
    protocolFtpLogDO.setDataChannelPort(MapUtils.getInteger(map, "data_channel_port"));

    return protocolFtpLogDO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolFtpLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or user = :keyword ");
    return conditionSql.toString();
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildSelectStatement(java.lang.String, java.util.Set)
   */
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return "level,policy_name,flow_id,network_id,service_id,application_id,start_time,end_time,"
          + "src_ipv4,src_ipv6,src_port,dest_ipv4,dest_ipv6,dest_port,user,cmd_seq,cmd,reply,filename,"
          + "IPv6NumToString(data_channel_ip) as data_channel_ip,data_channel_port";
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
      return StringUtils.replace(result, "data_channel_ip",
          "IPv6NumToString(data_channel_ip) as data_channel_ip");
    }
  }

}
