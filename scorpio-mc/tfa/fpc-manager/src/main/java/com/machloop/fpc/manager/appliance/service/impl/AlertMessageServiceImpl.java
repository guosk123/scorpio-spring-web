package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.AlertMessageBO;
import com.machloop.fpc.manager.appliance.dao.AlertMessageDao;
import com.machloop.fpc.manager.appliance.dao.AnalysisAlertMessageDao;
import com.machloop.fpc.manager.appliance.data.AlertMessageDO;
import com.machloop.fpc.manager.appliance.service.AlertMessageService;
import com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
@Service
public class AlertMessageServiceImpl implements AlertMessageService {

  @Autowired
  private AlertMessageDao alertMessageDao;

  @Autowired
  private AnalysisAlertMessageDao analysisAlertMessageDao;

  @Autowired
  private UserDao userDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#queryAlertMessages(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO)
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
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#queryAlertMessages(com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO)
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
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#analysisAlertMessage(java.util.Date, java.util.Date, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> analysisAlertMessage(Date startTime, Date endTime, int interval,
      String metrics, String sourceType, String sourceValue, String networkId, String serviceId) {
    if (StringUtils.isAllBlank(sourceType, sourceValue)) {
      sourceType = FpcConstants.SOURCE_TYPE_NETWORK;
      if (StringUtils.isNotBlank(serviceId)) {
        sourceType = FpcConstants.SOURCE_TYPE_SERVICE;
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
      case FpcConstants.SOURCE_TYPE_NETWORK:
        tableName = "t_fpc_metric_network_data_record";
        break;
      case FpcConstants.SOURCE_TYPE_SERVICE:
        tableName = "t_fpc_metric_service_data_record";
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_IP:
        tableName = "t_fpc_metric_l3device_data_record";
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_HOSTGROUP:
        tableName = "t_fpc_metric_hostgroup_data_record";
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_APPLICATION:
        tableName = "t_fpc_metric_application_data_record";
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_LOCATION:
        tableName = "t_fpc_metric_location_data_record";
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
    } else if (StringUtils.equalsAny(sourceType, FpcConstants.ALERT_SOURCE_TYPE_IP,
        FpcConstants.ALERT_SOURCE_TYPE_HOSTGROUP, FpcConstants.ALERT_SOURCE_TYPE_APPLICATION,
        FpcConstants.ALERT_SOURCE_TYPE_LOCATION)) {
      params.put("service_id", "");
    }

    switch (sourceType) {
      case FpcConstants.ALERT_SOURCE_TYPE_IP:
        params.put("ip_address", sourceValue);
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_HOSTGROUP:
        params.put("hostgroup_id", sourceValue);
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_APPLICATION:
        params.put("type", FpcConstants.METRIC_TYPE_APPLICATION_APP);
        params.put("application_id", sourceValue);
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_LOCATION:
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
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#countAlertMessages(java.util.Date, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public long countAlertMessages(Date startTime, Date endTime, String networkId, String serviceId) {
    return alertMessageDao.countAlertMessages(startTime, endTime, networkId, serviceId);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#queryAlertMessage(java.lang.String)
   */
  @Override
  public AlertMessageBO queryAlertMessage(String id) {
    AlertMessageDO alertMessageDO = alertMessageDao.queryAlertMessage(id);

    AlertMessageBO alertMessageBO = new AlertMessageBO();
    BeanUtils.copyProperties(alertMessageDO, alertMessageBO);

    return alertMessageBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertMessageService#solveAlertMessage(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public AlertMessageBO solveAlertMessage(String id, String reason, String operatorId) {
    AlertMessageDO alertMessage = alertMessageDao.queryAlertMessage(id);
    if (StringUtils.isBlank(alertMessage.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "告警消息不存在");
    }

    alertMessageDao.updateAlertMessageStatus(alertMessage, Constants.BOOL_YES, reason, operatorId);

    return queryAlertMessage(id);
  }

  @Override
  public List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO) {
    return alertMessageDao.queryAlertMessageAsHistogram(queryVO);
  }

}
