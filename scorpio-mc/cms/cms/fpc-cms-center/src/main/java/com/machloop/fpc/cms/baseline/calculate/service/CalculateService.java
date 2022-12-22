package com.machloop.fpc.cms.baseline.calculate.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.cms.baseline.calculate.CalculateResult;
import com.machloop.fpc.cms.baseline.calculate.dao.MetricDao;
import com.machloop.fpc.cms.baseline.mission.Mission;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年9月16日, fpc-manager
 */
@Service
public class CalculateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalculateService.class);

  // TODO 线程池大小可配置
  private ExecutorService executor = new ThreadPoolExecutor(0, 20, 1, TimeUnit.MINUTES,
      new LinkedBlockingQueue<Runnable>(), new BasicThreadFactory.Builder()
          .namingPattern("calculate-task").uncaughtExceptionHandler((t, e) -> {
            LOGGER.warn("failed to execute mission, thread is: {}", t.getId(), e);
          }).build(),
      new ThreadPoolExecutor.AbortPolicy());

  @Autowired
  private MetricDao metricDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  /**
   * 执行计算任务
   * @param missions
   * @return 
   */
  public List<CalculateResult> executeCalculate(Collection<Mission> missions) {
    List<Tuple2<String, Future<CalculateResult>>> futures = missions.stream().map(mission -> {
      Future<CalculateResult> future = executor.submit(new Callable<CalculateResult>() {
        @Override
        public CalculateResult call() throws Exception {
          return calculate(mission);
        }
      });
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("success submit calculate mission, mission detail: [{}]", mission);
      }
      return Tuples.of(mission.getId(), future);
    }).collect(Collectors.toList());

    List<CalculateResult> calculateResults = futures.stream().map(future -> {
      String missionId = future.getT1();
      CalculateResult result;
      try {
        result = future.getT2().get();
      } catch (InterruptedException | ExecutionException e) {
        result = new CalculateResult();
        result.setMissionId(missionId);
        result.setSuccess(false);
        LOGGER.warn("failed to execute calculate.mission id: {}", missionId, e);
      }
      return result;
    }).collect(Collectors.toList());

    return calculateResults;
  }

  private CalculateResult calculate(Mission mission) throws IOException {
    CalculateResult result = new CalculateResult();
    result.setMissionId(mission.getId());
    result.setSourceType(mission.getMissionSourceType());
    result.setSourceId(mission.getMissionSourceId());
    if (StringUtils.equals(mission.getMissionSourceType(),
        FpcCmsConstants.BASELINE_SETTING_SOURCE_ALERT)) {
      result.setAlertNetworkId(mission.getNetworkId());
      result.setAlertNetworkGroupId(mission.getNetworkGroupId());
      result.setAlertServiceId(mission.getServiceId());
    }
    result.setCalculateTime(mission.getWindow().getConrrentWindowTime());

    // 分子
    String numeratorMetricField = mission.getMetric().getNumeratorMetric();
    String numeratorSourceType = mission.getMetric().getNumeratorSourceType();
    String numeratorSourceValue = mission.getMetric().getNumeratorSourceValue();

    String numeratorTableName = getTableName(mission.getMissionSourceType(), numeratorSourceType,
        mission.getWindow().getBucketSeconds());

    long[] numeratorValues = null;
    if (StringUtils.equalsAny(numeratorSourceType, FpcCmsConstants.SOURCE_TYPE_SERVICE,
        FpcCmsConstants.SOURCE_TYPE_NETWORK, FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP)) {
      numeratorValues = queryNetworkOrServiceMetrics(mission.getWindow().getRanges(),
          numeratorTableName, mission.getNetworkId(), mission.getNetworkGroupId(),
          mission.getServiceId(), numeratorMetricField);
    } else {
      numeratorValues = queryOtherDimensionMetrics(mission.getWindow().getRanges(),
          numeratorTableName, mission.getNetworkId(), mission.getNetworkGroupId(),
          mission.getServiceId(), numeratorSourceType, numeratorSourceValue, numeratorMetricField);
    }

    if (numeratorValues == null || numeratorValues.length == 0) {
      result.setSuccess(false);
      LOGGER.debug("failed to execute calculate cause no numerator metric.mission id: {}",
          mission.getId());
      return result;
    }

    // 计算分子基线值
    double value = mission.getWeighting().operate(numeratorValues);

    // 分母
    if (mission.getMetric().isRatio()) {
      String denominatorMetricField = mission.getMetric().getDenominatorMetric();
      String denominatorSourceType = mission.getMetric().getDenominatorSourceType();
      String denominatorSourceValue = mission.getMetric().getDenominatorSourceValue();
      Integer denominatorConstants = mission.getMetric().getDenominatorConstants();

      if (denominatorConstants != null) {
        value = value / (double) denominatorConstants;
      } else {
        String denominatorTableName = getTableName(mission.getMissionSourceType(),
            denominatorSourceType, mission.getWindow().getBucketSeconds());

        long[] denominatorValues = null;
        if (StringUtils.equalsAny(denominatorSourceType, FpcCmsConstants.SOURCE_TYPE_SERVICE,
            FpcCmsConstants.SOURCE_TYPE_NETWORK, FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP)) {
          denominatorValues = queryNetworkOrServiceMetrics(mission.getWindow().getRanges(),
              denominatorTableName, mission.getNetworkId(), mission.getNetworkGroupId(),
              mission.getServiceId(), denominatorMetricField);
        } else {
          denominatorValues = queryOtherDimensionMetrics(mission.getWindow().getRanges(),
              denominatorTableName, mission.getNetworkId(), mission.getNetworkGroupId(),
              mission.getServiceId(), denominatorSourceType, denominatorSourceValue,
              denominatorMetricField);
        }

        if (denominatorValues == null || denominatorValues.length == 0) {
          result.setSuccess(false);
          LOGGER.debug("failed to execute calculate cause no denominator metric.mission id: {}",
              mission.getId());
          return result;
        }

        // 计算最终结果
        double denominatorValue = mission.getWeighting().operate(denominatorValues);
        value = denominatorValue == 0 ? 0 : value / denominatorValue;
      }
    }

    result.setSuccess(true);
    result.setValue(value);
    return result;
  };

  /**
   * @param timeRanges 时间段
   * @param tableName 表名称
   * @param networkId 查询网络ID
   * @param networkGroupId 查询网络组ID
   * @param serviceId 查询业务ID
   * @param metricField 指标
   * @return
   * @throws IOException
   */
  private long[] queryNetworkOrServiceMetrics(List<Tuple2<Date, Date>> timeRanges, String tableName,
      String networkId, String networkGroupId, String serviceId, String metricField)
      throws IOException {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(networkId)) {
      params.put("network_id", networkId);
    } else if (StringUtils.isNotBlank(networkGroupId)) {
      SensorNetworkGroupDO sensorNetworkGroup = sensorNetworkGroupDao
          .querySensorNetworkGroup(networkGroupId);
      List<String> networkIds = CsvUtils
          .convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds());
      params.put("network_id", networkIds);
    }

    if (StringUtils.isNotBlank(serviceId)) {
      params.put("service_id", serviceId);
    }

    return metricDao.queryMetrics(tableName, metricField, params, timeRanges);
  }

  /**
   * @param timeRanges 时间段
   * @param tableName 表名称
   * @param networkId 查询网络ID
   * @param networkGroupId 查询网络组ID
   * @param serviceId 查询业务ID
   * @param termType 查询对象类型
   * @param termValue 查询对象值
   * @param metricField 指标
   * @return
   * @throws IOException
   */
  private long[] queryOtherDimensionMetrics(List<Tuple2<Date, Date>> timeRanges, String tableName,
      String networkId, String networkGroupId, String serviceId, String termType, String termValue,
      String metricField) throws IOException {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(networkId)) {
      params.put("network_id", networkId);
    } else if (StringUtils.isNotBlank(networkGroupId)) {
      SensorNetworkGroupDO sensorNetworkGroup = sensorNetworkGroupDao
          .querySensorNetworkGroup(networkGroupId);
      List<String> networkIds = CsvUtils
          .convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds());
      params.put("network_id", networkIds);
    }
    params.put("service_id", StringUtils.defaultIfBlank(serviceId, ""));

    switch (termType) {
      case "ipAddress":
        params.put("ip_address", termValue);
        break;
      case "hostGroup":
        params.put("hostgroup_id", termValue);
        break;
      case "application":
        params.put("type", FpcCmsConstants.METRIC_TYPE_APPLICATION_APP);
        params.put("application_id", termValue);
        break;
      case "location":
        String[] termValues = StringUtils.split(termValue, "_");
        params.put("country_id", termValues[0]);
        if (termValues.length > 1) {
          params.put("province_id", termValues[1]);
        }
        if (termValues.length > 2) {
          params.put("city_id", termValues[2]);
        }
        break;
      default:
        throw new IllegalArgumentException("unsupport source type.");
    }

    return metricDao.queryMetrics(tableName, metricField, params, timeRanges);
  }

  /**
   * 获取表名
   * @param missionSourceType 基线定义来源（alert/npm）
   * @param metricSourceType 统计维度（network/service/ipAddress/hostGroup/application/location）
   * @param interval 统计时间间隔
   * @return
   */
  private String getTableName(String missionSourceType, String metricSourceType, int interval) {
    String tableName;
    switch (metricSourceType) {
      case "network":
        tableName = CenterConstants.TABLE_METRIC_NETWORK_DATA_RECORD;
        break;
      case "service":
        tableName = CenterConstants.TABLE_METRIC_SERVICE_DATA_RECORD;
        break;
      case "ipAddress":
        tableName = CenterConstants.TABLE_METRIC_L3DEVICE_DATA_RECORD;
        break;
      case "hostGroup":
        tableName = CenterConstants.TABLE_METRIC_HOSTGROUP_DATA_RECORD;
        break;
      case "application":
        tableName = CenterConstants.TABLE_METRIC_APP_DATA_RECORD;
        break;
      case "location":
        tableName = CenterConstants.TABLE_METRIC_LOCATION_DATA_RECORD;
        break;
      default:
        LOGGER.warn("unsupport source type.");
        throw new IllegalArgumentException("unsupport source type.");
    }

    if (StringUtils.equals(missionSourceType, FpcCmsConstants.BASELINE_SETTING_SOURCE_NPM)) {
      if (interval == Constants.FIVE_MINUTE_SECONDS) {
        tableName += "_5m";
      } else if (interval == Constants.ONE_HOUR_SECONDS) {
        tableName += "_1h";
      }
    }

    return tableName;
  }

}
