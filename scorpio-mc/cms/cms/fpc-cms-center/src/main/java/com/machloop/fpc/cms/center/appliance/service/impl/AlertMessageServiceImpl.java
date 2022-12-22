package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.fpc.cms.center.appliance.bo.AlertMessageBO;
import com.machloop.fpc.cms.center.appliance.dao.AlertMessageDao;
import com.machloop.fpc.cms.center.appliance.dao.AlertScopeDao;
import com.machloop.fpc.cms.center.appliance.dao.AnalysisAlertMessageDao;
import com.machloop.fpc.cms.center.appliance.data.AlertMessageDO;
import com.machloop.fpc.cms.center.appliance.data.AlertScopeDO;
import com.machloop.fpc.cms.center.appliance.service.AlertMessageService;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.invoker.FpcManagerInvoker;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
@Service
public class AlertMessageServiceImpl implements AlertMessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertMessageServiceImpl.class);

  @Autowired
  private AlertMessageDao alertMessageDao;

  @Autowired
  private AlertScopeDao alertScopeDao;

  @Autowired
  private AnalysisAlertMessageDao analysisAlertMessageDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorLogicalSubnetService sensorLogicalSubnetService;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  /**
   * 
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#queryAlertMessages(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO)
   */
  @Override
  public Page<AlertMessageBO> queryAlertMessages(Pageable page, AlertMessageQueryVO queryVO) {
    List<String> solverIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getSolver())) {
      solverIds.addAll(userDao.queryUserByFullname(queryVO.getSolver()).stream().map(UserDO::getId)
          .collect(Collectors.toList()));
    }

    Page<AlertMessageDO> messages = alertMessageDao.queryAlertMessages(page, queryVO, solverIds);

    Map<String, String> userDict = userDao.queryAllUsers().stream()
        .collect(Collectors.toMap(UserDO::getId, UserDO::getFullname));

    List<AlertMessageBO> list = Lists.newArrayListWithCapacity(messages.getSize());
    messages.forEach(alertMessageDO -> {
      AlertMessageBO alertMessageBO = new AlertMessageBO();
      BeanUtils.copyProperties(alertMessageDO, alertMessageBO);
      alertMessageBO.setSolver(MapUtils.getString(userDict, alertMessageDO.getSolverId(), ""));

      list.add(alertMessageBO);
    });

    return new PageImpl<>(list, page, messages.getTotalElements());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#queryAlertMessages(com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO)
   */
  @Override
  public List<AlertMessageBO> queryAlertMessages(AlertMessageQueryVO queryVO) {
    List<AlertMessageDO> alertMessages = alertMessageDao.queryAlertMessages(queryVO);

    List<AlertMessageBO> list = alertMessages.stream().map(alertMessageDO -> {
      AlertMessageBO alertMessageBO = new AlertMessageBO();
      BeanUtils.copyProperties(alertMessageDO, alertMessageBO);

      return alertMessageBO;
    }).collect(Collectors.toList());

    return list;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#analysisAlertMessage(java.util.Date, java.util.Date, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> analysisAlertMessage(Date startTime, Date endTime, int interval,
      String metrics, String sourceType, String sourceValue, String networkId, String serviceId) {
    if (StringUtils.isAllBlank(sourceType, sourceValue)) {
      sourceType = FpcCmsConstants.SOURCE_TYPE_NETWORK;
      if (StringUtils.isNotBlank(serviceId)) {
        sourceType = FpcCmsConstants.SOURCE_TYPE_SERVICE;
      }
    }

    String tableName = getTableName(sourceType, interval);
    Map<String, Object> params = generateFilter(sourceType, sourceValue, networkId, serviceId);
    return analysisAlertMessageDao.analysisAlertMessage(startTime, endTime, interval, params,
        tableName, CsvUtils.convertCSVToList(metrics));
  }

  private String getTableName(String sourceType, int interval) {
    String tableName = "";
    switch (sourceType) {
      case FpcCmsConstants.SOURCE_TYPE_NETWORK:
        tableName = "d_fpc_metric_network_data_record";
        break;
      case FpcCmsConstants.SOURCE_TYPE_SERVICE:
        tableName = "d_fpc_metric_service_data_record";
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_IP:
        tableName = "d_fpc_metric_l3device_data_record";
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_HOSTGROUP:
        tableName = "d_fpc_metric_hostgroup_data_record";
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_APPLICATION:
        tableName = "d_fpc_metric_application_data_record";
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_LOCATION:
        tableName = "d_fpc_metric_location_data_record";
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "unsupport source type.");
    }

    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName += "_5m";
    } else if (interval == Constants.ONE_HOUR_SECONDS) {
      tableName += "_1h";
    }

    return tableName;
  }

  private Map<String, Object> generateFilter(String sourceType, String sourceValue,
      String networkId, String serviceId) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(networkId)) {
      params.put("network_id", networkId);
    }
    if (StringUtils.isNotBlank(serviceId)) {
      params.put("service_id", serviceId);
    } else if (StringUtils.equalsAny(sourceType, FpcCmsConstants.ALERT_SOURCE_TYPE_IP,
        FpcCmsConstants.ALERT_SOURCE_TYPE_HOSTGROUP, FpcCmsConstants.ALERT_SOURCE_TYPE_APPLICATION,
        FpcCmsConstants.ALERT_SOURCE_TYPE_LOCATION)) {
      params.put("service_id", "");
    }

    switch (sourceType) {
      case FpcCmsConstants.ALERT_SOURCE_TYPE_IP:
        params.put("ip_address", sourceValue);
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_HOSTGROUP:
        params.put("hostgroup_id", sourceValue);
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_APPLICATION:
        params.put("type", FpcCmsConstants.METRIC_TYPE_APPLICATION_APP);
        params.put("application_id", sourceValue);
        break;
      case FpcCmsConstants.ALERT_SOURCE_TYPE_LOCATION:
        String[] termValues = StringUtils.split(sourceValue, "_");
        params.put("country_id", termValues[0]);
        if (termValues.length > 1) {
          params.put("province_id", termValues[1]);
        }
        if (termValues.length > 2) {
          params.put("city_id", termValues[2]);
        }
        break;
      default:
        break;
    }

    return params;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#countAlertMessages(com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO)
   */
  @Override
  public long countAlertMessages(AlertMessageQueryVO queryVO) {
    return alertMessageDao.countAlertMessages(queryVO);
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#queryAlertMessage(java.lang.String)
   */
  @Override
  public AlertMessageBO queryAlertMessage(String id) {
    AlertMessageDO alertMessageDO = alertMessageDao.queryAlertMessage(id);

    AlertMessageBO alertMessageBO = new AlertMessageBO();
    BeanUtils.copyProperties(alertMessageDO, alertMessageBO);

    return alertMessageBO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.appliance.service.AlertMessageService#solveAlertMessage(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public AlertMessageBO solveAlertMessage(String id, String reason, String operatorId) {
    AlertMessageDO alertMessage = alertMessageDao.queryAlertMessage(id);
    if (StringUtils.isBlank(alertMessage.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "告警消息不存在");
    }

    List<AlertScopeDO> alertScopes = alertScopeDao
        .queryAlertScopeByAlertId(alertMessage.getAlertId());

    Set<String> fpcSerialNumbers = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (alertScopes.size() == 1
        && StringUtils.equals(alertScopes.get(0).getSourceType(),
            FpcCmsConstants.SOURCE_TYPE_NETWORK)
        && StringUtils.equals(alertScopes.get(0).getNetworkId(), AlertRuleService.ALL_NETWORK)) {
      fpcSerialNumbers.addAll(fpcService.queryAllFpc().stream().map(FpcBO::getSerialNumber)
          .collect(Collectors.toSet()));
    } else {
      Map<String, String> physicsNetworkMap = fpcNetworkService.queryAllNetworks().stream().collect(
          Collectors.toMap(FpcNetworkBO::getFpcNetworkId, FpcNetworkBO::getFpcSerialNumber));
      Map<String,
          String> subnetMap = sensorLogicalSubnetService.querySensorLogicalSubnets().stream()
              .collect(Collectors.toMap(SensorLogicalSubnetBO::getId,
                  SensorLogicalSubnetBO::getNetworkInSensorIds));

      alertScopes.forEach(alertScope -> {
        String networkId = alertScope.getNetworkId();
        if (physicsNetworkMap.containsKey(networkId)) {
          fpcSerialNumbers.add(physicsNetworkMap.get(networkId));
        } else {
          fpcSerialNumbers.addAll(CsvUtils.convertCSVToList(subnetMap.get(networkId)).stream()
              .map(item -> physicsNetworkMap.get(item)).collect(Collectors.toSet()));
        }
      });
    }

    fpcService.queryFpcBySerialNumbers(Lists.newArrayList(fpcSerialNumbers), false).stream().filter(
        fpc -> StringUtils.equals(fpc.getConnectStatus(), FpcCmsConstants.CONNECT_STATUS_NORMAL))
        .forEach(fpc -> {
          try {
            fpcManagerInvoker.solveAlertMessage(fpc.getSerialNumber(), id, reason);
          } catch (RestClientException e) {
            LOGGER.warn("rest solve alert failed. ", e);
            throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "处理失败，系统异常");
          }
        });

    return queryAlertMessage(id);
  }

  @Override
  public List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO) {
    return alertMessageDao.queryAlertMessageAsHistogram(queryVO);
  }

}
