package com.machloop.fpc.npm.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.npm.appliance.bo.BaselineValueBO;
import com.machloop.fpc.npm.appliance.dao.BaselineSettingDao;
import com.machloop.fpc.npm.appliance.dao.BaselineValueDao;
import com.machloop.fpc.npm.appliance.data.BaselineSettingDO;
import com.machloop.fpc.npm.appliance.data.BaselineValueDO;
import com.machloop.fpc.npm.appliance.service.BaselineService;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
@Service
public class BaselineServiceImpl implements BaselineService {

  private static final String WINDOWING_MODEL_DAY_ON_DAY_BASIS = String.join(",",
      FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY,
      FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY,
      FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY);
  private static final String WINDOWING_MODEL_WEEK_ON_WEEK_BASIS = String.join(",",
      FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK,
      FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK,
      FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK);
  private static final String WINDOWING_MODEL_TIME_ON_TIME_RATIO = String.join(",",
      FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES,
      FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES,
      FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS);

  @Autowired
  private BaselineSettingDao baselineSettingDao;

  @Autowired
  private BaselineValueDao baselineValueDao;

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#queryBaselineSettings(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<BaselineSettingBO> queryBaselineSettings(String sourceType, String networkId,
      String serviceId) {
    List<BaselineSettingDO> baselineSettings = baselineSettingDao.queryBaselineSettings(sourceType,
        networkId, serviceId, null);
    Map<String,
        BaselineSettingBO> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    baselineSettings.forEach(baselineSettingDO -> {
      StringBuilder key = new StringBuilder();
      key.append(baselineSettingDO.getSourceType()).append("_")
          .append(baselineSettingDO.getNetworkId()).append("_")
          .append(baselineSettingDO.getServiceId()).append("_")
          .append(baselineSettingDO.getCategory());

      if (map.get(key.toString()) == null) {
        BaselineSettingBO baselineSettingBO = new BaselineSettingBO();
        BeanUtils.copyProperties(baselineSettingDO, baselineSettingBO);
        baselineSettingBO
            .setWindowingModel(getParentWindowingModel(baselineSettingDO.getWindowingModel()));

        baselineSettingBO
            .setUpdateTime(DateUtils.toStringISO8601(baselineSettingDO.getUpdateTime()));
        map.put(key.toString(), baselineSettingBO);
      }
    });

    return Lists.newArrayList(map.values());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#querySubdivisionBaselineSettings(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<BaselineSettingBO> querySubdivisionBaselineSettings(String sourceType,
      String networkId, String serviceId, String category) {
    List<BaselineSettingDO> baselineSettings = baselineSettingDao.queryBaselineSettings(sourceType,
        networkId, serviceId, category);

    return baselineSettings.stream().map(baselineSettingDO -> {
      BaselineSettingBO baselineSettingBO = new BaselineSettingBO();
      BeanUtils.copyProperties(baselineSettingDO, baselineSettingBO);

      return baselineSettingBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#queryWindowingModelByInterval(int)
   */
  @Override
  public List<String> queryWindowingModelByInterval(int interval) {
    List<String> result = Lists.newArrayList();

    switch (interval) {
      case Constants.ONE_MINUTE_SECONDS:
        result = Lists.newArrayList(FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY,
            FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK,
            FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES);
        break;
      case Constants.FIVE_MINUTE_SECONDS:
        result = Lists.newArrayList(FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY,
            FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK,
            FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES);
        break;
      case Constants.ONE_HOUR_SECONDS:
        result = Lists.newArrayList(FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY,
            FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK,
            FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS);
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的查询时间间隔");
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#queryPreviousPeriodTimePeriod(java.util.Date, java.util.Date, com.machloop.fpc.npm.appliance.bo.BaselineSettingBO, int)
   */
  @Override
  public Tuple3<Date, Date, Integer> queryPreviousPeriodTimePeriod(Date currentStartTime,
      Date currentEndTime, BaselineSettingBO baselineSetting, int n) {
    String windowingModel = baselineSetting.getWindowingModel();
    int windowingCount = baselineSetting.getWindowingCount();
    if (n > windowingCount) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的回顾周期");
    }

    Tuple3<Date, Date, Integer> result = null;
    switch (windowingModel) {
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY:
        result = Tuples.of(DateUtils.beforeDayDate(currentStartTime, Constants.ONE_DAYS * n),
            DateUtils.beforeDayDate(currentEndTime, Constants.ONE_DAYS * n),
            Constants.ONE_DAY_SECONDS * n);
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK:
        result = Tuples.of(DateUtils.beforeDayDate(currentStartTime, Constants.ONE_WEEK_DAYS * n),
            DateUtils.beforeDayDate(currentEndTime, Constants.ONE_WEEK_DAYS * n),
            Constants.ONE_WEEK_DAYS * n * Constants.ONE_DAY_SECONDS);
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES:
        result = Tuples.of(
            DateUtils.beforeSecondDate(currentStartTime, Constants.ONE_MINUTE_SECONDS * n),
            DateUtils.beforeSecondDate(currentEndTime, Constants.ONE_MINUTE_SECONDS * n),
            Constants.ONE_MINUTE_SECONDS * n);
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES:
        result = Tuples.of(
            DateUtils.beforeSecondDate(currentStartTime, Constants.FIVE_MINUTE_SECONDS * n),
            DateUtils.beforeSecondDate(currentEndTime, Constants.FIVE_MINUTE_SECONDS * n),
            Constants.FIVE_MINUTE_SECONDS * n);
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS:
        result = Tuples.of(
            DateUtils.beforeSecondDate(currentStartTime, Constants.ONE_HOUR_SECONDS * n),
            DateUtils.beforeSecondDate(currentEndTime, Constants.ONE_HOUR_SECONDS * n),
            Constants.ONE_HOUR_SECONDS * n);
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的窗口模型");
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#updateBaselineSettings(java.util.List, java.lang.String)
   */
  @Override
  public int updateBaselineSettings(List<BaselineSettingBO> baselineSettings, String operatorId) {
    int suceess = 0;
    if (baselineSettings.isEmpty()) {
      return suceess;
    }

    // 每种基线类型的配置映射关系
    BaselineSettingBO item = baselineSettings.get(0);
    List<BaselineSettingBO> settings = queryBaselineSettings(item.getSourceType(),
        item.getNetworkId(), item.getServiceId());
    Map<String,
        String> categoryMap = settings.stream()
            .collect(Collectors.toMap(BaselineSettingBO::getCategory,
                setting -> StringUtils.joinWith("_", setting.getWeightingModel(),
                    setting.getWindowingModel(), setting.getWindowingCount())));

    for (BaselineSettingBO baselineSettingBO : baselineSettings) {
      if (!StringUtils.equalsAny(baselineSettingBO.getWindowingModel(),
          WINDOWING_MODEL_DAY_ON_DAY_BASIS, WINDOWING_MODEL_WEEK_ON_WEEK_BASIS,
          WINDOWING_MODEL_TIME_ON_TIME_RATIO)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的基线窗口模型");
      }

      // 配置未更改则不更新数据库
      if (StringUtils.equals(categoryMap.get(baselineSettingBO.getCategory()),
          StringUtils.joinWith("_", baselineSettingBO.getWeightingModel(),
              baselineSettingBO.getWindowingModel(), baselineSettingBO.getWindowingCount()))) {
        continue;
      }

      List<BaselineSettingDO> list = CsvUtils
          .convertCSVToList(baselineSettingBO.getWindowingModel()).stream().map(windowingModel -> {
            BaselineSettingDO baselineSettingDO = new BaselineSettingDO();
            BeanUtils.copyProperties(baselineSettingBO, baselineSettingDO);
            baselineSettingDO
                .setServiceId(StringUtils.defaultIfBlank(baselineSettingBO.getServiceId(), ""));
            baselineSettingDO.setWindowingModel(windowingModel);
            baselineSettingDO.setOperatorId(operatorId);

            return baselineSettingDO;
          }).collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(list)) {
        suceess += baselineSettingDao.batchUpdateBaselineSetting(list);
      }
    }

    return suceess;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#deleteBaselineSettings(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteBaselineSettings(String sourceType, String networkId, String serviceId) {
    return baselineSettingDao.deleteBaselineSetting(sourceType, networkId, serviceId);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.BaselineService#queryBaselineValue(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<BaselineValueBO> queryBaselineValue(String sourceType, String sourceId,
      Date startTime, Date endTime) {
    List<BaselineValueDO> baselineValues = baselineValueDao.queryBaselineValues(sourceType,
        sourceId, startTime, endTime);

    return baselineValues.stream().map(baselineValueDO -> {
      BaselineValueBO baselineValueBO = new BaselineValueBO();
      BeanUtils.copyProperties(baselineValueDO, baselineValueBO);

      return baselineValueBO;
    }).collect(Collectors.toList());
  }

  private String getParentWindowingModel(String childWindowingModel) {
    String parentWindowingModel = "";

    switch (childWindowingModel) {
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY:
        parentWindowingModel = WINDOWING_MODEL_DAY_ON_DAY_BASIS;
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK:
        parentWindowingModel = WINDOWING_MODEL_WEEK_ON_WEEK_BASIS;
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES:
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES:
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS:
        parentWindowingModel = WINDOWING_MODEL_TIME_ON_TIME_RATIO;
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的窗口模型");
    }

    return parentWindowingModel;
  }

}
