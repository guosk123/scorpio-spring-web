package com.machloop.fpc.cms.center.central.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.system.bo.AlarmCountBO;
import com.machloop.alpha.webapp.system.bo.LogBO;
import com.machloop.alpha.webapp.system.bo.AlarmBO;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.AlarmCountDO;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.alpha.webapp.system.data.RoleDO;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;
import com.machloop.fpc.cms.center.central.service.CentralMessageService;

@Service
public class CentralMessageServiceImpl implements CentralMessageService {

  @Autowired
  private LogDao logDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralMessageService#queryLogs(com.machloop.alpha.common.base.page.Pageable, java.util.List, com.machloop.alpha.webapp.system.vo.LogQueryVO, java.lang.String)
   */
  @Override
  public Page<LogBO> queryLogs(Pageable page, List<RoleDO> roleList, LogQueryVO query,
      String nodeType) {
    Map<String, String> componentDict = dictManager.getBaseDict()
        .getItemMap(Constants.DICT_SYSTEM_COMPONENT);
    Map<String,
        String> logLevelDict = dictManager.getBaseDict().getItemMap(LogHelper.DICT_LOG_LEVEL);
    Map<String,
        String> logCategoryDict = dictManager.getBaseDict().getItemMap(LogHelper.DICT_LOG_CATEGORY);

    if (StringUtils.equals(nodeType, LogHelper.LOCAL_NODE_LOG)) {
      List<String> roleNameEnList = Lists.newArrayListWithExpectedSize(roleList.size());
      for (RoleDO roleDO : roleList) {
        roleNameEnList.add(roleDO.getNameEn());
      }
      // 权限中包含审计权限，不包含系统管理员权限，返回日志类型为审计的日志
      if (roleNameEnList.contains(WebappConstants.ROLE_AUDIT_USER)
          && !roleNameEnList.contains(WebappConstants.ROLE_SYS_USER)) {
        query.setCategory(LogHelper.CATEGORY_AUDIT);
      } else if (roleNameEnList.contains(WebappConstants.ROLE_SYS_USER)
          && !roleNameEnList.contains(WebappConstants.ROLE_AUDIT_USER)) {
        // 权限中包含系统管理员权限并且不包含审计，返回除了日志类型为审计的所有日志
        Set<String> categorySet = Sets.newHashSet(logCategoryDict.keySet());
        categorySet.remove(LogHelper.CATEGORY_AUDIT);

        // 与筛选条件中的日志类型取交集
        if (StringUtils.isNotBlank(query.getCategory())) {
          categorySet.retainAll(CsvUtils.convertCSVToList(query.getCategory()));
        }

        query.setCategory(CsvUtils.convertCollectionToCSV(categorySet));
      }
    }

    Page<LogDO> logDoPage = logDao.queryNodeLogs(page, query, nodeType);
    long totalElem = logDoPage.getTotalElements();
    List<LogBO> logVoList = Lists.newArrayListWithCapacity(logDoPage.getSize());
    for (LogDO logDO : logDoPage) {
      LogBO logBO = new LogBO();
      BeanUtils.copyProperties(logDO, logBO);
      logBO.setLevel(MapUtils.getString(logLevelDict, logDO.getLevel(), ""));
      logBO.setCategory(MapUtils.getString(logCategoryDict, logDO.getCategory(), ""));
      logBO.setComponent(MapUtils.getString(componentDict, logDO.getComponent(), ""));
      logBO.setAriseTime(DateUtils.toStringISO8601(logDO.getAriseTime()));
      logVoList.add(logBO);
    }

    return new PageImpl<>(logVoList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralMessageService#queryAlarms(com.machloop.alpha.common.base.page.Pageable, com.machloop.alpha.webapp.system.vo.AlarmQueryVO, java.lang.String)
   */
  @Override
  public Page<AlarmBO> queryAlarms(Pageable page, AlarmQueryVO query, String nodeType) {
    Map<String, String> componentDict = dictManager.getBaseDict()
        .getItemMap(Constants.DICT_SYSTEM_COMPONENT);
    Map<String,
        String> alarmLevelDict = dictManager.getBaseDict().getItemMap(AlarmHelper.DICT_ALARM_LEVEL);
    Map<String, Map<String, String>> alarmCategoryDict = Maps
        .newHashMapWithExpectedSize(componentDict.size());
    for (String component : componentDict.keySet()) {
      String alarmCategoryKey = AlarmHelper.DICT_ALARM_CATEGORY_PREFIX + component;
      if (CollectionUtils.isNotEmpty(dictManager.getBaseDict().getItem(alarmCategoryKey))) {
        alarmCategoryDict.put(alarmCategoryKey,
            dictManager.getBaseDict().getItemMap(alarmCategoryKey));
      }
    }

    Page<AlarmDO> alarmDOPage = alarmDao.queryNodeAlarms(page, query, nodeType);
    long totalElem = alarmDOPage.getTotalElements();

    List<String> userIdList = Lists.newArrayListWithCapacity(alarmDOPage.getSize());
    for (AlarmDO alarmDO : alarmDOPage.getContent()) {
      userIdList.add(alarmDO.getSolverId());
    }

    List<UserDO> userDoList = userDao.queryUsersByIds(userIdList);
    Map<String, String> userMap = Maps.newHashMapWithExpectedSize(userDoList.size());
    for (UserDO userDo : userDoList) {
      userMap.put(userDo.getId(), userDo.getFullname());
    }

    List<AlarmBO> alarmVoList = Lists.newArrayListWithCapacity(alarmDOPage.getSize());
    for (AlarmDO alarmDO : alarmDOPage) {
      AlarmBO alarmBO = new AlarmBO();
      BeanUtils.copyProperties(alarmDO, alarmBO);
      alarmBO.setSolver(MapUtils.getString(userMap, alarmDO.getSolverId(), ""));
      alarmBO.setLevel(MapUtils.getString(alarmLevelDict, alarmDO.getLevel(), ""));
      alarmBO.setComponent(MapUtils.getString(componentDict, alarmDO.getComponent(), ""));
      String alarmCategoryKey = AlarmHelper.DICT_ALARM_CATEGORY_PREFIX + alarmDO.getComponent();
      Map<String, String> alarmCategoryMap = alarmCategoryDict.get(alarmCategoryKey);
      if (MapUtils.isNotEmpty(alarmCategoryMap)) {
        alarmBO.setCategory(alarmCategoryMap.getOrDefault(alarmDO.getCategory(), ""));
      } else {
        alarmBO.setCategory("");
      }
      alarmBO.setAriseTime(DateUtils.toStringISO8601(alarmDO.getAriseTime()));
      alarmBO.setSolveTime(DateUtils.toStringISO8601(alarmDO.getSolveTime()));
      alarmVoList.add(alarmBO);
    }

    return new PageImpl<>(alarmVoList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralMessageService#countAlarmsGroupByLevelWithoutCms()
   */
  @Override
  public List<AlarmCountBO> countAlarmsGroupByLevelWithoutCms() {
    List<AlarmCountDO> list = alarmDao
        .countUnsolvedAlarmsGroupByLevel(HotPropertiesHelper.getProperty("fpc.cms.node.id"));
    List<AlarmCountBO> result = Lists.newArrayListWithCapacity(list.size());
    for (AlarmCountDO alarmCountDO : list) {
      AlarmCountBO alarmCountBO = new AlarmCountBO();
      BeanUtils.copyProperties(alarmCountDO, alarmCountBO);
      result.add(alarmCountBO);
    }
    return result;
  }

}
