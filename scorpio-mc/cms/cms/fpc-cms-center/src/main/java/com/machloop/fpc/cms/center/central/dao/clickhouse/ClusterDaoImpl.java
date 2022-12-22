package com.machloop.fpc.cms.center.central.dao.clickhouse;

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
import org.springframework.stereotype.Repository;

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseBPJdbcTemplate;
import com.machloop.fpc.cms.center.central.dao.ClusterDao;
import com.machloop.fpc.cms.center.helper.ClickhouseRemoteServerHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2022年4月28日, fpc-cms-center
 */
@Repository
public class ClusterDaoImpl implements ClusterDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterDaoImpl.class);

  /*private static final Pattern IP_PATTERN = Pattern
      .compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");*/

  @Autowired
  private ClickHouseBPJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.ClusterDao#queryClusterNodes(boolean, java.util.Date)
   */
  @Override
  public List<Map<String, Object>> queryClusterNodes(boolean exceptionNode, Date afterTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select host_name as hostName, sum(errors_count) as errorCount ");
    sql.append(" from system.clusters ");
    sql.append(" where cluster in (:clusters) ");
    sql.append(" and host_name != 'localhost' ");
    sql.append(" group by host_name ");
    if (exceptionNode) {
      sql.append(" having errorCount > 0 ");
    }

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("clusters", Lists.newArrayList(ClickhouseRemoteServerHelper.CLICKHOUSE_SERVER_NAME,
        ClickhouseRemoteServerHelper.CH_STATS_SERVER_NAME));

    List<Map<String, Object>> nodes = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    if (exceptionNode && CollectionUtils.isNotEmpty(nodes)) {
      /*errors_count 是单个节点在集群中出现异常的数量，该计数不仅仅包含连接异常（比如查询方面的异常），
       * 因此不能通过该计数就确认是集群连接异常，需要进一步判断,通过remoteSecure判断*/

      return nodes.stream()
          .filter(node -> StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL,
              queryNodeConnectState(MapUtils.getString(node, "hostName"))))
          .collect(Collectors.toList());
    }

    return nodes;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.ClusterDao#queryNodeConnectState(java.lang.String)
   */
  @Override
  public String queryNodeConnectState(String nodeIp) {
    StringBuilder sql = new StringBuilder();
    sql.append("select name from ");
    sql.append(" remoteSecure(:host, system.tables, :username, :password) ");
    sql.append(" where database = 'fpc' limit 1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("host", nodeIp + ":" + HotPropertiesHelper.getProperty("clickhouse.tcp.ssl.port")
        + "," + nodeIp + ":" + HotPropertiesHelper.getProperty("ch_stats.tcp.ssl.port"));
    params.put("username", HotPropertiesHelper.getProperty("clickhouse.remote.server.username"));
    params.put("password", HotPropertiesHelper.getProperty("clickhouse.remote.server.password"));

    String status = FpcCmsConstants.CONNECT_STATUS_NORMAL;
    try {
      clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params, new ColumnMapRowMapper());
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      String errorMsg = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getMessage()
          : ((BadSqlGrammarException) e).getSQLException().getMessage();
      if (errorCode == 519 || errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 519 (All attempts to get table structure failed)
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        status = FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
        LOGGER.warn("Attempt to use remote connection failed, errorCode: {}.", errorCode);
      } else {
        // 其他异常
        LOGGER.warn(
            "A remote connection error is reported due to a non-connection problem, errorCode: {}, errorMsg: {}.",
            errorCode, errorMsg);
      }
    }

    return status;
  }

}
