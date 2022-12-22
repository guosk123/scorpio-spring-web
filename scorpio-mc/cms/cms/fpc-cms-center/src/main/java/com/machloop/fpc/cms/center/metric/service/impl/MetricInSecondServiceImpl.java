package com.machloop.fpc.cms.center.metric.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao;
import com.machloop.fpc.cms.center.metric.service.MetricInSecondService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2022年2月17日, fpc-cms-center
 */
@Service
public class MetricInSecondServiceImpl implements MetricInSecondService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricInSecondServiceImpl.class);

  // <sensor, startTime, currentRecordTime>
  private static final ConcurrentMap<String,
      Tuple3<String, Date, Date>> queryMap = Maps.newConcurrentMap();

  private static final Map<String, Long> heartbeatMap = Maps.newConcurrentMap();

  private static final int AGEING_SECOND = Constants.ONE_HOUR_SECONDS;

  private static final String REALTIME_STATISTICS_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXXX";

  @Autowired
  private MetricInSecondDao metricInSecondDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorLogicalSubnetService sensorLogicalSubnetService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${fpc.engine.rest.server.protocol}")
  private String fileServerProtocol;
  @Value("${fpc.engine.rest.server.port}")
  private String fileServerPort;

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#asyncCollection(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public synchronized boolean asyncCollection(MetricQueryVO queryVO, String metricType, String path,
      HttpServletRequest request) {
    // 心跳 (如果是第一次心跳，则流程继续向下走，如果非第一次心跳则刷新心跳返回)
    long currentTimeMillis = System.currentTimeMillis();
    if (heartbeatMap.containsKey(queryVO.getQueryId())) {
      heartbeatMap.put(queryVO.getQueryId(), currentTimeMillis);
      return true;
    } else {
      heartbeatMap.put(queryVO.getQueryId(), currentTimeMillis);
    }

    // 清除1小时前的查询状态
    Iterator<Entry<String, Tuple3<String, Date, Date>>> iterator = queryMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, Tuple3<String, Date, Date>> next = iterator.next();
      if ((currentTimeMillis - next.getValue().getT2().getTime()) > AGEING_SECOND
          * Constants.NUM_1000) {
        iterator.remove();
        heartbeatMap.remove(next.getKey());
      }
    }

    // 获取本次需要请求的探针，以及具体的网络、业务
    List<Tuple2<String, String>> configForSensors = querySourceInSensorId(queryVO).getT3();
    if (CollectionUtils.isEmpty(configForSensors)) {
      return false;
    }

    // 获取探针的管理IP
    Set<String> needSearchFpcSerialNumbers = configForSensors.stream().map(item -> item.getT2())
        .collect(Collectors.toSet());
    Map<String,
        String> sensorIpMap = fpcService
            .queryFpcBySerialNumbers(Lists.newArrayList(needSearchFpcSerialNumbers), false).stream()
            .filter(fpc -> StringUtils.equals(fpc.getConnectStatus(),
                FpcCmsConstants.CONNECT_STATUS_NORMAL))
            .collect(Collectors.toMap(FpcBO::getSerialNumber, FpcBO::getIp));

    for (Tuple2<String, String> configForSensor : configForSensors) {
      String serialNumber = configForSensor.getT2();

      // 获取本次请求的探针IP
      String fpcIp = sensorIpMap.get(serialNumber);
      if (StringUtils.isNotBlank(fpcIp)) {
        new Thread() {

          @Override
          public void run() {
            String requestUri = "";
            String[] split = StringUtils.split(configForSensor.getT1(), "^");
            String networkId = "";
            String serviceId = "";
            if (split.length == 2) {
              networkId = split[0];
              serviceId = split[1];
              requestUri = String.format(path, serviceId, networkId);
            } else {
              networkId = split[0];
              requestUri = String.format(path, networkId);
            }

            while (heartbeatMap.containsKey(queryVO.getQueryId()) && (System.currentTimeMillis()
                - heartbeatMap.get(queryVO.getQueryId())) <= Constants.HALF_MINUTE_SECONDS) {
              Map<String, Object> result = queryRealTimeStatistics(fpcIp, requestUri, request);
              if (MapUtils.isEmpty(result)) {
                continue;
              }

              Tuple3<String, Date,
                  Date> queryTuple = queryMap.getOrDefault(queryVO.getQueryId(), Tuples
                      .of(serialNumber, new Date(currentTimeMillis), new Date(currentTimeMillis)));

              try {
                Date currentRecordTime = null;
                switch (metricType) {
                  case METRIC_NETWORK:
                    // 网络统计
                    if (result.get("histogram") != null) {
                      List<Map<String, Object>> histogram = JsonHelper.deserialize(
                          JsonHelper.serialize(result.get("histogram"), false),
                          new TypeReference<List<Map<String, Object>>>() {
                          }, false);
                      if (CollectionUtils.isNotEmpty(histogram)) {
                        // 获取上次记录之后的数据
                        Tuple2<Date, List<Map<String, Object>>> latestData = getLatestData(
                            histogram, queryTuple.getT3());
                        metricInSecondDao.batchSaveNetworkMetrics(latestData.getT2(),
                            queryTuple.getT2());
                        currentRecordTime = latestData.getT1();
                      }
                    }
                    // 三层主机
                    parseL3DeviceAndSave(result);
                    // IP通讯对
                    parseIpConversationAndSave(result);
                    // dscp
                    parseDscpAndSave(result, queryTuple.getT2());
                    break;
                  case METRIC_SERVICE:
                    // 业务统计
                    if (result.get("histogram") != null) {
                      List<Map<String, Object>> histogram = JsonHelper.deserialize(
                          JsonHelper.serialize(result.get("histogram"), false),
                          new TypeReference<List<Map<String, Object>>>() {
                          }, false);
                      if (CollectionUtils.isNotEmpty(histogram)) {
                        // 获取上次记录之后的数据
                        Tuple2<Date, List<Map<String, Object>>> latestData = getLatestData(
                            histogram, queryTuple.getT3());
                        metricInSecondDao.batchSaveServiceMetrics(latestData.getT2(),
                            queryTuple.getT2());
                        currentRecordTime = latestData.getT1();
                      }
                    }
                    // 三层主机
                    parseL3DeviceAndSave(result);
                    // IP通讯对
                    parseIpConversationAndSave(result);
                    // dscp
                    parseDscpAndSave(result, queryTuple.getT2());
                    break;
                  case METRIC_PAYLOAD:
                    if (result.get("histogram") != null) {
                      List<Map<String, Object>> histogram = JsonHelper.deserialize(
                          JsonHelper.serialize(result.get("histogram"), false),
                          new TypeReference<List<Map<String, Object>>>() {
                          }, false);
                      if (CollectionUtils.isNotEmpty(histogram)) {
                        // 获取上次记录之后的数据
                        Tuple2<Date, List<Map<String, Object>>> latestData = getLatestData(
                            histogram, queryTuple.getT3());
                        metricInSecondDao.batchSavePayloadMetrics(latestData.getT2(),
                            queryTuple.getT2(), networkId, serviceId);
                        currentRecordTime = latestData.getT1();
                      }
                    }
                    break;
                  case METRIC_PERFORMANCE:
                    if (result.get("histogram") != null) {
                      List<Map<String, Object>> histogram = JsonHelper.deserialize(
                          JsonHelper.serialize(result.get("histogram"), false),
                          new TypeReference<List<Map<String, Object>>>() {
                          }, false);
                      if (CollectionUtils.isNotEmpty(histogram)) {
                        // 获取上次记录之后的数据
                        Tuple2<Date, List<Map<String, Object>>> latestData = getLatestData(
                            histogram, queryTuple.getT3());
                        metricInSecondDao.batchSavePerformanceMetrics(latestData.getT2(),
                            queryTuple.getT2(), networkId, serviceId);
                        currentRecordTime = latestData.getT1();
                      }
                    }
                    break;
                  case METRIC_TCP:
                    if (result.get("histogram") != null) {
                      List<Map<String, Object>> histogram = JsonHelper.deserialize(
                          JsonHelper.serialize(result.get("histogram"), false),
                          new TypeReference<List<Map<String, Object>>>() {
                          }, false);
                      if (CollectionUtils.isNotEmpty(histogram)) {
                        // 获取上次记录之后的数据
                        Tuple2<Date, List<Map<String, Object>>> latestData = getLatestData(
                            histogram, queryTuple.getT3());
                        metricInSecondDao.batchSaveTcpMetrics(latestData.getT2(),
                            queryTuple.getT2(), networkId, serviceId);
                        currentRecordTime = latestData.getT1();
                      }
                    }
                    break;
                  default:
                    break;
                }

                // 更新查询状态
                queryMap.put(queryVO.getQueryId(),
                    Tuples.of(queryTuple.getT1(), queryTuple.getT2(), currentRecordTime));
              } catch (UnsupportedOperationException e) {
                LOGGER.warn("result parse failed.", e);
              } catch (UncategorizedSQLException e) {
                LOGGER.warn("result save failed.", e);
              }
            }
          }

          @Override
          public void interrupt() {
            super.interrupt();
            throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "实时刷新请求异常");
          }

        }.start();
      }
    }

    return true;
  }

  private Map<String, Object> queryRealTimeStatistics(String serverIp, String path,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append(String.format("%sX-Machloop-Date=", StringUtils.contains(path, "?") ? "&" : "?"));
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      requestUrl = url.toString();
      LOGGER.debug("invoke rest api:{}", url);

      String resultStr = restTemplate.getForObject(url.toString(), String.class);
      if (StringUtils.isBlank(resultStr)) {
        LOGGER.warn("system error, statistics not available.");
        return result;
      }

      result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
      }, false);
    } catch (Exception e) {
      LOGGER.warn("failed to query realTime statistics [" + requestUrl + "].", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "实时刷新请求异常");
    }

    return result;
  }

  private Tuple2<Date, List<Map<String, Object>>> getLatestData(List<Map<String, Object>> histogram,
      Date currentRecordTime) {
    List<Map<String, Object>> collect = histogram.stream().filter(item -> {
      Date timestamp = parseDate(MapUtils.getString(item, "timestamp"));

      return timestamp.after(currentRecordTime);
    }).sorted(new Comparator<Map<String, Object>>() {

      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Date o1Timestamp = parseDate(MapUtils.getString(o1, "timestamp"));
        Date o2Timestamp = parseDate(MapUtils.getString(o2, "timestamp"));
        return o2Timestamp.compareTo(o1Timestamp);
      }

    }).collect(Collectors.toList());

    return Tuples.of(parseDate(MapUtils.getString(collect.get(0), "timestamp")), collect);
  }

  private static Date parseDate(String strDate) {
    ZonedDateTime dateTime = ZonedDateTime.parse(strDate,
        DateTimeFormatter.ofPattern(REALTIME_STATISTICS_TIME_FORMAT));
    return Date.from(dateTime.toInstant());
  }

  private void parseDscpAndSave(Map<String, Object> result, Date startTime) {
    if (result.get("dscp") != null) {
      Map<String,
          List<Map<String, Object>>> dscp = JsonHelper.deserialize(
              JsonHelper.serialize(result.get("dscp"), false),
              new TypeReference<Map<String, List<Map<String, Object>>>>() {
              }, false);
      if (MapUtils.isNotEmpty(dscp)) {
        List<Map<String, Object>> dscpHistogram = JsonHelper.deserialize(
            JsonHelper.serialize(dscp.get("histogram"), false),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        if (CollectionUtils.isNotEmpty(dscpHistogram)) {
          metricInSecondDao.batchSaveDscpMetrics(dscpHistogram, startTime,
              MapUtils.getString(result, "networkId"), MapUtils.getString(result, "serviceId", ""));
        }
      }
    }
  }

  private void parseL3DeviceAndSave(Map<String, Object> result) {
    if (result.get("l3DevicesTop") != null) {
      Map<String,
          List<Map<String, Object>>> l3DevicesTop = JsonHelper.deserialize(
              JsonHelper.serialize(result.get("l3DevicesTop"), false),
              new TypeReference<Map<String, List<Map<String, Object>>>>() {
              }, false);
      if (MapUtils.isNotEmpty(l3DevicesTop)) {
        Map<String,
            Object> totalBytesTop = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        // totalBytes
        List<Map<String, Object>> totalBytes = JsonHelper.deserialize(
            JsonHelper.serialize(l3DevicesTop.get("totalBytes"), false),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        if (CollectionUtils.isNotEmpty(totalBytes)) {
          totalBytes.forEach(item -> totalBytesTop.put(MapUtils.getString(item, "ip"),
              MapUtils.getLong(item, "value", 0L)));
        }
        // totalSessions
        Map<String,
            Object> totalSessionsTop = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        List<Map<String, Object>> totalSessions = JsonHelper.deserialize(
            JsonHelper.serialize(l3DevicesTop.get("totalSessions"), false),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        if (CollectionUtils.isNotEmpty(totalSessions)) {
          totalSessions.forEach(item -> totalSessionsTop.put(MapUtils.getString(item, "ip"),
              MapUtils.getLong(item, "value", 0L)));
        }

        Map<String,
            Object> l3DeviceTop = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        l3DeviceTop.put("timestamp", result.get("timestamp"));
        l3DeviceTop.put("networkId", result.get("networkId"));
        l3DeviceTop.put("serviceId", result.getOrDefault("serviceId", ""));
        l3DeviceTop.put("totalBytesTop", totalBytesTop);
        l3DeviceTop.put("totalSessionsTop", totalSessionsTop);
        metricInSecondDao.saveL3DeviceMetric(l3DeviceTop);
      }
    }
  }

  private void parseIpConversationAndSave(Map<String, Object> result) {
    if (result.get("ipConversationTop") != null) {
      Map<String,
          List<Map<String, Object>>> ipConversationTop = JsonHelper.deserialize(
              JsonHelper.serialize(result.get("ipConversationTop"), false),
              new TypeReference<Map<String, List<Map<String, Object>>>>() {
              }, false);
      if (MapUtils.isNotEmpty(ipConversationTop)) {
        // totalBytes
        Map<String,
            Object> totalBytesTop = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        List<Map<String, Object>> totalBytes = JsonHelper.deserialize(
            JsonHelper.serialize(ipConversationTop.get("totalBytes"), false),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        if (CollectionUtils.isNotEmpty(totalBytes)) {
          totalBytes.forEach(
              item -> totalBytesTop.put(StringUtils.joinWith("_", MapUtils.getString(item, "ipA"),
                  MapUtils.getString(item, "ipB")), MapUtils.getLong(item, "value", 0L)));
        }
        // totalSessions
        Map<String,
            Object> totalSessionsTop = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        List<Map<String, Object>> totalSessions = JsonHelper.deserialize(
            JsonHelper.serialize(ipConversationTop.get("totalSessions"), false),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        if (CollectionUtils.isNotEmpty(totalSessions)) {
          totalSessions.forEach(item -> totalSessionsTop.put(StringUtils.joinWith("_",
              MapUtils.getString(item, "ipA"), MapUtils.getString(item, "ipB")),
              MapUtils.getLong(item, "value", 0L)));
        }

        Map<String, Object> ipConversationsTop = Maps
            .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        ipConversationsTop.put("timestamp", result.get("timestamp"));
        ipConversationsTop.put("networkId", result.get("networkId"));
        ipConversationsTop.put("serviceId", result.getOrDefault("serviceId", ""));
        ipConversationsTop.put("totalBytesTop", totalBytesTop);
        ipConversationsTop.put("totalSessionsTop", totalSessionsTop);
        metricInSecondDao.saveIpConversationMetric(ipConversationsTop);
      }
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#queryNetworkDashboard(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public Map<String, Object> queryNetworkDashboard(MetricQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取实际查询网络集合
    List<String> networkIds = querySourceInSensorId(queryVO).getT1();
    if (CollectionUtils.isEmpty(networkIds)) {
      return result;
    }

    Tuple3<String, Date, Date> queryTime = queryMap.get(queryVO.getQueryId());

    // 网络统计
    Date currentTime = null;
    result.put("networkId", queryVO.getNetworkId());
    result.put("networkGroupId", queryVO.getNetworkGroupId());
    List<Map<String, Object>> networkHistogram = metricInSecondDao
        .queryNetworkStatistics(queryTime.getT2(), networkIds);
    result.put("histogram", networkHistogram);
    if (CollectionUtils.isNotEmpty(networkHistogram)) {
      Map<String, Object> currentNetworkMetric = networkHistogram.get(networkHistogram.size() - 1);
      currentTime = Date.from(((OffsetDateTime) currentNetworkMetric.get("timestamp")).toInstant());
      result.putAll(currentNetworkMetric);
    }

    // DSCP
    List<Map<String, Object>> dscpsHistogram = metricInSecondDao.queryDscpStatistics(
        queryTime.getT2(), FpcCmsConstants.SOURCE_TYPE_NETWORK, networkIds, null);
    Map<String, List<Map<String, Object>>> dscpResult = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    dscpResult.put("histogram", dscpsHistogram);
    if (CollectionUtils.isEmpty(dscpsHistogram)) {
      dscpResult.put("volumn", dscpsHistogram);
    } else {
      Map<String, Object> currentDscpMetric = dscpsHistogram.get(dscpsHistogram.size() - 1);
      Date timestamp = Date.from(((OffsetDateTime) currentDscpMetric.get("timestamp")).toInstant());
      dscpResult.put("volumn",
          dscpsHistogram.stream()
              .filter(item -> timestamp
                  .compareTo(Date.from(((OffsetDateTime) item.get("timestamp")).toInstant())) == 0)
              .collect(Collectors.toList()));

    }
    result.put("dscp", dscpResult);

    // 三层主机
    Map<String,
        List<Map<String, Object>>> l3DeviceStatistics = metricInSecondDao.queryL3DeviceStatistics(
            currentTime, FpcCmsConstants.SOURCE_TYPE_NETWORK, networkIds, null);
    result.put("l3DevicesTop", l3DeviceStatistics);

    // 三层主机通讯对
    Map<String,
        List<Map<String, Object>>> ipConversationStatistics = metricInSecondDao
            .queryIpConversationStatistics(currentTime, FpcCmsConstants.SOURCE_TYPE_NETWORK,
                networkIds, null);
    result.put("ipConversationTop", ipConversationStatistics);

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#queryServiceDashboard(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取实际查询业务
    List<Tuple2<String, String>> serviceNetworkIds = querySourceInSensorId(queryVO).getT2();
    if (CollectionUtils.isEmpty(serviceNetworkIds)) {
      return result;
    }

    Tuple3<String, Date, Date> queryTime = queryMap.get(queryVO.getQueryId());

    // 业务统计
    Date currentTime = null;
    result.put("networkId", queryVO.getNetworkId());
    result.put("networkGroupId", queryVO.getNetworkGroupId());
    result.put("serviceId", queryVO.getServiceId());
    List<Map<String, Object>> serviceHistogram = metricInSecondDao
        .queryServiceStatistics(queryTime.getT2(), serviceNetworkIds);
    result.put("histogram", serviceHistogram);
    if (CollectionUtils.isNotEmpty(serviceHistogram)) {
      Map<String, Object> currentServiceMetric = serviceHistogram.get(serviceHistogram.size() - 1);
      currentTime = Date.from(((OffsetDateTime) currentServiceMetric.get("timestamp")).toInstant());
      result.putAll(currentServiceMetric);
    }

    // DSCP
    List<Map<String, Object>> dscpsHistogram = metricInSecondDao.queryDscpStatistics(
        queryTime.getT2(), FpcCmsConstants.SOURCE_TYPE_SERVICE, null, serviceNetworkIds);
    Map<String, List<Map<String, Object>>> dscpResult = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    dscpResult.put("histogram", dscpsHistogram);
    if (CollectionUtils.isEmpty(dscpsHistogram)) {
      dscpResult.put("volumn", dscpsHistogram);
    } else {
      Map<String, Object> currentDscpMetric = dscpsHistogram.get(dscpsHistogram.size() - 1);
      Date timestamp = Date.from(((OffsetDateTime) currentDscpMetric.get("timestamp")).toInstant());
      dscpResult.put("volumn",
          dscpsHistogram.stream()
              .filter(item -> timestamp
                  .compareTo(Date.from(((OffsetDateTime) item.get("timestamp")).toInstant())) == 0)
              .collect(Collectors.toList()));
    }
    result.put("dscp", dscpResult);

    // 三层主机
    Map<String,
        List<Map<String, Object>>> l3DeviceStatistics = metricInSecondDao.queryL3DeviceStatistics(
            currentTime, FpcCmsConstants.SOURCE_TYPE_SERVICE, null, serviceNetworkIds);
    result.put("l3DevicesTop", l3DeviceStatistics);

    // 三层主机通讯对
    Map<String,
        List<Map<String, Object>>> ipConversationStatistics = metricInSecondDao
            .queryIpConversationStatistics(currentTime, FpcCmsConstants.SOURCE_TYPE_SERVICE, null,
                serviceNetworkIds);
    result.put("ipConversationTop", ipConversationStatistics);

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#queryPayloadStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Tuple3<List<String>, List<Tuple2<String, String>>,
        List<Tuple2<String, String>>> sourceInSensorId = querySourceInSensorId(queryVO);
    if (CollectionUtils.isEmpty(sourceInSensorId.getT1())
        && CollectionUtils.isEmpty(sourceInSensorId.getT2())) {
      return result;
    }

    Tuple3<String, Date, Date> queryTime = queryMap.get(queryVO.getQueryId());

    return metricInSecondDao.queryPayloadStatistics(queryTime.getT2(), queryVO.getSourceType(),
        sourceInSensorId.getT1(), sourceInSensorId.getT2());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#queryPerformanceStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Tuple3<List<String>, List<Tuple2<String, String>>,
        List<Tuple2<String, String>>> sourceInSensorId = querySourceInSensorId(queryVO);
    if (CollectionUtils.isEmpty(sourceInSensorId.getT1())
        && CollectionUtils.isEmpty(sourceInSensorId.getT2())) {
      return result;
    }

    Tuple3<String, Date, Date> queryTime = queryMap.get(queryVO.getQueryId());

    return metricInSecondDao.queryPerformanceStatistics(queryTime.getT2(), queryVO.getSourceType(),
        sourceInSensorId.getT1(), sourceInSensorId.getT2());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricInSecondService#queryTcpStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Tuple3<List<String>, List<Tuple2<String, String>>,
        List<Tuple2<String, String>>> sourceInSensorId = querySourceInSensorId(queryVO);
    if (CollectionUtils.isEmpty(sourceInSensorId.getT1())
        && CollectionUtils.isEmpty(sourceInSensorId.getT2())) {
      return result;
    }

    Tuple3<String, Date, Date> queryTime = queryMap.get(queryVO.getQueryId());

    return metricInSecondDao.queryTcpStatistics(queryTime.getT2(), queryVO.getSourceType(),
        sourceInSensorId.getT1(), sourceInSensorId.getT2());
  }

  private Tuple3<List<String>, List<Tuple2<String, String>>,
      List<Tuple2<String, String>>> querySourceInSensorId(MetricQueryVO queryVO) {
    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> serviceNetworkIds = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> configForSensor = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<String>, List<Tuple2<String, String>>, List<Tuple2<String, String>>> tuple3 = Tuples
        .of(networkIds, serviceNetworkIds, configForSensor);

    Map<String, String> physicalNetworks = fpcNetworkService.queryAllNetworks().stream()
        .collect(Collectors.toMap(FpcNetworkBO::getFpcNetworkId, FpcNetworkBO::getFpcSerialNumber));
    Map<String,
        String> subnetMap = sensorLogicalSubnetService.querySensorLogicalSubnets().stream()
            .collect(Collectors.toMap(SensorLogicalSubnetBO::getId,
                SensorLogicalSubnetBO::getNetworkInSensorIds));
    if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP)) {
      networkIds.addAll(CsvUtils.convertCSVToList(sensorNetworkGroupService
          .querySensorNetworkGroup(queryVO.getNetworkGroupId()).getNetworkInSensorIds()));
      networkIds.forEach(networkId -> {
        if (physicalNetworks.containsKey(networkId)) {
          configForSensor.add(Tuples.of(networkId, physicalNetworks.get(networkId)));
        }
      });
    } else if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
      networkIds.add(queryVO.getNetworkId());
      if (physicalNetworks.containsKey(queryVO.getNetworkId())) {
        configForSensor
            .add(Tuples.of(queryVO.getNetworkId(), physicalNetworks.get(queryVO.getNetworkId())));
      } else {
        CsvUtils.convertCSVToList(subnetMap.get(queryVO.getNetworkId()))
            .forEach(physicsNetworkId -> {
              configForSensor
                  .add(Tuples.of(queryVO.getNetworkId(), physicalNetworks.get(physicsNetworkId)));
            });
      }
    } else if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_SERVICE)) {
      if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
        CsvUtils
            .convertCSVToList(sensorNetworkGroupService
                .querySensorNetworkGroup(queryVO.getNetworkGroupId()).getNetworkInSensorIds())
            .forEach(networkId -> {
              serviceNetworkIds.add(Tuples.of(networkId, queryVO.getServiceId()));
              configForSensor
                  .add(Tuples.of(StringUtils.joinWith("^", networkId, queryVO.getServiceId()),
                      physicalNetworks.get(networkId)));
            });
      } else {
        serviceNetworkIds.add(Tuples.of(queryVO.getNetworkId(), queryVO.getServiceId()));
        if (physicalNetworks.containsKey(queryVO.getNetworkId())) {
          configForSensor.add(
              Tuples.of(StringUtils.joinWith("^", queryVO.getNetworkId(), queryVO.getServiceId()),
                  physicalNetworks.get(queryVO.getNetworkId())));
        } else {
          CsvUtils.convertCSVToList(subnetMap.get(queryVO.getNetworkId()))
              .forEach(physicsNetworkId -> {
                configForSensor.add(Tuples.of(
                    StringUtils.joinWith("^", queryVO.getNetworkId(), queryVO.getServiceId()),
                    physicalNetworks.get(physicsNetworkId)));
              });
        }
      }
    }

    return tuple3;
  }

}
