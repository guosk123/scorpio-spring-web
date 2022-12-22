package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolDnsLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolDnsLogDao")
public class ProtocolDnsLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolDnsLogDO>
    implements LogRecordDao<ProtocolDnsLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolDnsLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_dns_log_record";

  private static List<String> IP_FIELD_NAMES = Lists.newArrayList("domain_address");

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "domain", "dns_rcode_name");

  private static final List<String> WILDCARD_FIELD_NAMES = Lists.newArrayList("domain");

  private static final ProtocolDnsLogDO EMPTY_DO = new ProtocolDnsLogDO();

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
  protected ProtocolDnsLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolDnsLogDO protocolDnsLogDO = new ProtocolDnsLogDO();

    convertBaseLogMap2AbstractLogDO(protocolDnsLogDO, map);

    // domain ip address
    List<String> domainAddressList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("domain_ipv4") != null) {
      String[] v4Array = JsonHelper.deserialize(JsonHelper.serialize(map.get("domain_ipv4")),
          new TypeReference<String[]>() {
          }, false);
      if (v4Array != null) {
        for (String item : v4Array) {
          domainAddressList.add(item);
        }
      }
    }
    if (map.get("domain_ipv6") != null) {
      String[] v6Array = JsonHelper.deserialize(JsonHelper.serialize(map.get("domain_ipv6")),
          new TypeReference<String[]>() {
          }, false);
      if (v6Array != null) {
        for (String item : v6Array) {
          domainAddressList.add(item);
        }
      }
    }

    protocolDnsLogDO.setAnswer(MapUtils.getString(map, "answer"));
    protocolDnsLogDO.setDomain(MapUtils.getString(map, "domain"));
    protocolDnsLogDO.setDomainIntelligence(MapUtils.getIntValue(map, "domain_intelligence"));
    protocolDnsLogDO.setDomainAddress(domainAddressList);
    protocolDnsLogDO.setDnsRcode(MapUtils.getString(map, "dns_rcode"));
    protocolDnsLogDO.setDnsRcodeName(MapUtils.getString(map, "dns_rcode_name"));
    protocolDnsLogDO.setDnsQueries(MapUtils.getString(map, "dns_queries"));
    protocolDnsLogDO.setSubdomainCount(MapUtils.getLongValue(map, "subdomain_count"));
    protocolDnsLogDO.setTransactionId(MapUtils.getString(map, "transaction_id"));

    return protocolDnsLogDO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolDnsLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    if (NetworkUtils.isCidr(keyword) || NetworkUtils.isInetAddress(keyword)) {
      if (NetworkUtils.isInetAddress(keyword, IpVersion.V4)) {
        conditionSql.append(" has(domain_ipv4, toIPv4(:keyword))=1 ");
      } else {
        conditionSql.append(" has(domain_ipv6, toIPv6(:keyword))=1 ");
      }
    } else {
      conditionSql.append(" policy_name = :keyword ");
      conditionSql.append(" or domain = :keyword ");
      conditionSql.append(" or dns_rcode_name = :keyword ");
    }
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" domain like :keyword_like ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }
}
