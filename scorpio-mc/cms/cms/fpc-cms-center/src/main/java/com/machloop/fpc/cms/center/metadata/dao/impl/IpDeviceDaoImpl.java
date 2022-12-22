package com.machloop.fpc.cms.center.metadata.dao.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.IpDeviceDao;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
@Repository
public class IpDeviceDaoImpl implements IpDeviceDao {


  private static final String TABLE_NAME = "d_fpc_protocol_rtp_log_record";


  private static final Logger LOGGER = LoggerFactory.getLogger(IpDeviceDaoImpl.class);

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Override
  public Page<Map<String, Object>> queryIpDeviceList(LogRecordQueryVO queryVO, PageRequest page) {


    StringBuilder sql = new StringBuilder();
    sql.append(" select ");
    String sortProperty = page.getSort().iterator().next().getProperty();
    sql.append(buildSelectStatement(queryVO.getColumns(), sortProperty));
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" from ( ");

    StringBuilder innerSql = new StringBuilder();
    innerSql.append(
        " select src_ip as device_ip,from as device_code,sum(rtp_total_packets) as rtp_total_packets,sum(rtp_loss_packets)  as rtp_loss_packets,");
    innerSql.append(
        " max(jitter_max) as jitter_max,avg(jitter_mean) as jitter_mean,min(start_time) as start_time,max(report_time) as report_time ");
    innerSql.append(" from ").append(TABLE_NAME);
    if (!queryVO.getNetworkId().isEmpty()) {
      String networkId = queryVO.getNetworkId();
      innerSql.append(" where 1=1 and ");
      innerSql.append(" has(network_id,:networkId)=1 ");
      params.put("networkId", networkId);
    }
    innerSql.append(" group by device_ip,device_code ");
    innerSql.append(" union all ");
    innerSql.append(
        " select dest_ip as device_ip,to as device_code,sum(rtp_total_packets) as rtp_total_packets,sum(rtp_loss_packets)  as rtp_loss_packets,");
    innerSql.append(
        " max(jitter_max) as jitter_max,avg(jitter_mean) as jitter_mean,min(start_time) as start_time,max(report_time) as report_time ");
    innerSql.append(" from ").append(TABLE_NAME);
    if (!queryVO.getNetworkId().isEmpty()) {
      String networkId = queryVO.getNetworkId();
      innerSql.append(" where 1=1 and ");
      innerSql.append(" has(network_id,:networkId)=1 ");
      params.put("networkId", networkId);
    }
    innerSql.append(" group by device_ip,device_code ");
    sql.append(innerSql).append(" ) ");
    sql.append(" group by device_ip,device_code ");
    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, params);
    sql.append(havingSql);
    String totalSql = sql.toString();

    PageUtils.appendPage(sql, page, Lists.newArrayListWithCapacity(0));

    List<Map<String, Object>> resultList = clickHouseTemplate.getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());
    resultList = resultList.stream().map(item -> convertMap(item, queryVO))
        .collect(Collectors.toList());

    long total = 0;
    if (CollectionUtils.isNotEmpty(resultList)) {
      total = clickHouseTemplate.getJdbcTemplate().query(totalSql, params, new ColumnMapRowMapper())
          .size();
    }

    return new PageImpl<>(resultList, page, total);
  }

  @Override
  public List<Map<String, Object>> queryRtpNetworkSegmentationHistograms(LogRecordQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(report_time), :interval)), :interval), 'UTC') as temp_report_time,");
    sql.append(" src_ip,dest_ip,arrayElement(network_id,1) as network_id,");
    sql.append(
        "sum(rtp_total_packets) as rtp_total_packets, sum(rtp_loss_packets)  as rtp_loss_packets,");
    sql.append("max(jitter_max) as jitter_max,avg(jitter_mean) as jitter_mean");
    sql.append(" from ").append(TABLE_NAME);
    if (!queryVO.getNetworkId().isEmpty()) {
      String networkId = queryVO.getNetworkId();
      sql.append(" where 1=1 and ");
      sql.append(" network_id = :networkId ");
      params.put("networkId", networkId);
    }
    sql.append(" group by report_time,src_ip,dest_ip,network_id ");
    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, params);
    sql.append(havingSql);

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

  }

  private Map<String, Object> convertMap(Map<String, Object> map, LogRecordQueryVO queryVO) {
    Map<String, Object> newMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (map.containsKey("start_time")) {
      newMap.put("startTime", DateUtils.toStringNanoISO8601((OffsetDateTime) map.get("start_time"),
          ZoneId.systemDefault()));
    }
    if (map.containsKey("report_time")) {
      newMap.put("reportTime", DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("report_time"), ZoneId.systemDefault()));
    }
    if (map.containsKey("device_ip")) {
      String deviceIp = MapUtils.getString(map, "device_ip");
      if (StringUtils.isNotBlank(deviceIp)
          && StringUtils.startsWith(deviceIp, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        deviceIp = StringUtils.substringAfter(deviceIp, CenterConstants.IPV4_TO_IPV6_PREFIX);
      }
      newMap.put("deviceIp", deviceIp);
    }
    if (map.containsKey("device_code")) {
      newMap.put("deviceCode", MapUtils.getString(map, "device_code"));
    }
    if (map.containsKey("rtp_total_packets")) {
      newMap.put("rtpTotalPackets", MapUtils.getLongValue(map, "rtp_total_packets"));
    }
    if (map.containsKey("rtp_loss_packets")) {
      newMap.put("rtpLossPackets", MapUtils.getLongValue(map, "rtp_loss_packets"));
    }
    if (map.containsKey("jitter_max")) {
      newMap.put("jitterMax", MapUtils.getLongValue(map, "jitter_max"));
    }
    if (map.containsKey("jitter_mean")) {
      newMap.put("jitterMean", MapUtils.getLongValue(map, "jitter_mean"));
    }
    if (queryVO.getColumns().contains("rtp_loss_packets_rate")) {
      long rtpLossPackets = MapUtils.getLongValue(map, "rtp_loss_packets");
      long rtpTotalPackets = MapUtils.getLongValue(map, "rtp_total_packets");
      String rtpLossPacketsRate = new BigDecimal((double) rtpLossPackets * 100 / rtpTotalPackets)
          .setScale(2, RoundingMode.HALF_UP).doubleValue() + "%";
      newMap.put("rtpLossPacketsRate", rtpLossPacketsRate);
    }
    return newMap;
  }

  private void enrichHavingSql(LogRecordQueryVO queryVO, StringBuilder havingSql,
      Map<String, Object> params) {

    havingSql.append(" having 1=1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(),
                queryVO.getHasAgingTime(), queryVO.getTimePrecision(),
                queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        havingSql.append(" and ");
        String t1 = dsl.getT1();
        if (t1.contains("device_ip")) {
          int toIPv6 = t1.indexOf("toIPv6");
          int a = t1.substring(toIPv6).indexOf(")");
          t1 = t1.substring(0, toIPv6) + "IPv6NumToString("
              + t1.substring(toIPv6).substring(0, a + 1) + ")"
              + t1.substring(toIPv6).substring(a + 1);
        }
        havingSql.append(t1);
        params.putAll(dsl.getT2());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

  }


  private String buildSelectStatement(String columns, String sortProperty) {
    if (StringUtils.equals(columns, "*")) {
      return "IPv6NumToString(device_ip) as device_ip,device_code,sum(rtp_total_packets) as rtp_total_packets,sum(rtp_loss_packets) as rtp_loss_packets,"
          + "max(jitter_max) as jitter_max,avg(jitter_mean) as jitter_mean,min(start_time) as start_time,max(report_time) as report_time ";
    }
    // 必须查询排序字段
    String selectFields = null;
    if (!StringUtils.contains(columns, sortProperty)) {
      selectFields = sortProperty;
    }

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      String result = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
      if (result.contains("rtp_loss_packets_rate")) {
        result = result.replace(",rtp_loss_packets_rate", "");
      }

      return result.replace("device_ip", "IPv6NumToString(device_ip) as device_ip")
          .replace("rtp_total_packets", "sum(rtp_total_packets) as rtp_total_packets")
          .replace("rtp_loss_packets", "sum(rtp_loss_packets) as rtp_loss_packets")
          .replace("jitter_max", "max(jitter_max) as jitter_max")
          .replace("jitter_mean", "avg(jitter_mean) as jitter_mean")
          .replace("start_time", "min(start_time) as start_time")
          .replace("report_time", "max(report_time) as report_time");

    }
  }

}
