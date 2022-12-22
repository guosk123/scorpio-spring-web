package com.machloop.fpc.cms.center.metadata.dao.impl;

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
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolSslLogDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolSslLogDao")
public class ProtocolSslLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolSslLogDO>
    implements LogRecordDao<ProtocolSslLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolSslLogDaoImpl.class);

  private static final String TABLE_NAME = "d_fpc_protocol_ssl_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name",
      "server_name", "ja3_client", "ja3_server", "version", "issuer", "common_name");

  private static final ProtocolSslLogDO EMPTY_DO = new ProtocolSslLogDO();

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
  protected ProtocolSslLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolSslLogDO protocolSslLogDO = new ProtocolSslLogDO();

    convertBaseLogMap2AbstractLogDO(protocolSslLogDO, map);

    protocolSslLogDO.setServerName(MapUtils.getString(map, "server_name"));
    List<String> serverCertsSha1List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("server_certs_sha1") != null) {
      String[] serverCertsSha1Array = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("server_certs_sha1")), new TypeReference<String[]>() {
          }, false);
      if (serverCertsSha1Array != null) {
        for (String item : serverCertsSha1Array) {
          serverCertsSha1List.add(item);
        }
      }
    }
    protocolSslLogDO.setServerCertsSha1(serverCertsSha1List);
    protocolSslLogDO.setJa3Client(MapUtils.getString(map, "ja3_client"));
    protocolSslLogDO.setJa3Server(MapUtils.getString(map, "ja3_server"));
    protocolSslLogDO.setVersion(MapUtils.getString(map, "version"));
    protocolSslLogDO.setCipherSuite(MapUtils.getString(map, "cipher_suite"));
    protocolSslLogDO.setSignatureAlgorithm(MapUtils.getString(map, "signature_algorithm"));
    protocolSslLogDO.setIssuer(MapUtils.getString(map, "issuer"));
    protocolSslLogDO.setCommonName(MapUtils.getString(map, "common_name"));
    protocolSslLogDO.setValidity(MapUtils.getString(map, "validity"));
    protocolSslLogDO.setAuthType(MapUtils.getString(map, "auth_type"));
    List<String> clientCipherSuiteList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("client_cipher_suite") != null) {
      String[] clientCipherSuiteArray = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("client_cipher_suite")), new TypeReference<String[]>() {
          }, false);
      if (clientCipherSuiteArray != null) {
        for (String item : clientCipherSuiteArray) {
          clientCipherSuiteList.add(item);
        }
      }
    }
    protocolSslLogDO.setClientCipherSuite(clientCipherSuiteList);
    List<String> clientExtensionsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("client_extensions") != null) {
      String[] clientExtensionsArray = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("client_extensions")), new TypeReference<String[]>() {
          }, false);
      if (clientExtensionsArray != null) {
        for (String item : clientExtensionsArray) {
          clientExtensionsList.add(item);
        }
      }
    }
    protocolSslLogDO.setClientExtensions(clientExtensionsList);
    List<String> serverExtensionsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("server_extensions") != null) {
      String[] serverExtensionsArray = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("server_extensions")), new TypeReference<String[]>() {
          }, false);
      if (serverExtensionsArray != null) {
        for (String item : serverExtensionsArray) {
          serverExtensionsList.add(item);
        }
      }
    }
    protocolSslLogDO.setServerExtensions(serverExtensionsList);
    protocolSslLogDO.setClientCurVersion(MapUtils.getString(map, "client_cur_version"));
    protocolSslLogDO.setClientMaxVersion(MapUtils.getString(map, "client_max_version"));
    protocolSslLogDO.setCertsLen(MapUtils.getIntValue(map, "certs_len"));
    protocolSslLogDO.setCrlUrls(MapUtils.getString(map, "crl_urls"));
    protocolSslLogDO.setIssuerUrls(MapUtils.getString(map, "issuer_urls"));
    protocolSslLogDO.setOcspUrls(MapUtils.getString(map, "ocsp_urls"));
    protocolSslLogDO.setIsReuse(MapUtils.getIntValue(map, "is_reuse"));
    protocolSslLogDO.setSecProto(MapUtils.getIntValue(map, "sec_proto"));

    return protocolSslLogDO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolSslLogDO buildEmptyLogDO() {
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
    conditionSql.append(" or server_name = :keyword ");
    conditionSql.append(" or ja3_client = :keyword ");
    conditionSql.append(" or ja3_server = :keyword ");
    conditionSql.append(" or version = :keyword ");
    conditionSql.append(" or issuer = :keyword ");
    conditionSql.append(" or common_name = :keyword ");
    conditionSql.append(" or auth_type = :keyword ");
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
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }
}
