package com.machloop.fpc.cms.npm.analysis.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleClasstypeBO;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleClasstypeDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleDao;
import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleClasstypeDO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataAlertStatisticsService;
import com.machloop.fpc.cms.npm.analysis.service.SuricataRuleClasstypeService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author chenshimiao
 *
 * create at 2022/10/13 10:31 AM,cms
 * @version 1.0
 */
@Order(16)
@Service
public class SuricataRuleClasstypeServiceImpl
    implements SuricataRuleClasstypeService, MQAssignmentService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataRuleClasstypeServiceImpl.class);

  private static final List<
      String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE);

  private static final String CSV_TITLE = "`分类ID`,`分类名称`\n";

  @Autowired
  private SuricataRuleClasstypeDao suricataRuleClasstypeDao;

  @Autowired
  private SuricataRuleDao suricataRuleDao;

  @Autowired
  private SuricataAlertStatisticsService suricataAlertStatisticsService;

  @Autowired
  private ApplicationContext context;

  @Override
  public List<SuricataRuleClasstypeBO> querySuricataRuleClasstypes(Date startTimeDate,
      Date endTimeDate) {
    List<SuricataRuleClasstypeDO> suricataRuleClasstypes = suricataRuleClasstypeDao
        .querySuricataRuleClasstypes();

    // 规则分类使用情况
    Map<String, Integer> classtypeUsedCount = suricataRuleDao.statisticsByClasstype();

    // 规则分类包含的告警分布
    Map<String,
        Long> classtypeAlertCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (startTimeDate != null && endTimeDate != null) {
      List<Map<String, Object>> alertStatistics = suricataAlertStatisticsService
          .queryAlterStatistics(
              FpcCmsConstants.SURICATA_ALERT_STATISTICS_TYPE_CLASSIFICATION_PROPORTION,
              startTimeDate, endTimeDate);
      classtypeAlertCount.putAll(alertStatistics.stream().collect(Collectors.toMap(
          item -> MapUtils.getString(item, "key"), item -> MapUtils.getLong(item, "count"))));
    }
    return suricataRuleClasstypes.stream().map(suricataRuleClasstypeDO -> {
      SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
      BeanUtils.copyProperties(suricataRuleClasstypeDO, suricataRuleClasstypeBO);
      suricataRuleClasstypeBO
          .setRuleSize(classtypeUsedCount.getOrDefault(suricataRuleClasstypeBO.getId(), 0));
      suricataRuleClasstypeBO
          .setAlertSize(classtypeAlertCount.getOrDefault(suricataRuleClasstypeBO.getId(), 0L));
      suricataRuleClasstypeBO
          .setCreateTime(DateUtils.toStringISO8601(suricataRuleClasstypeDO.getCreateTime()));
      suricataRuleClasstypeBO
          .setUpdateTime(DateUtils.toStringISO8601(suricataRuleClasstypeDO.getUpdateTime()));

      return suricataRuleClasstypeBO;
    }).collect(Collectors.toList());
  }

  @Override
  public SuricataRuleClasstypeBO querySuricataRuleClasstype(String id) {
    SuricataRuleClasstypeDO suricataRuleClasstypeDO = suricataRuleClasstypeDao
        .querySuricataRuleClasstype(id);

    SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
    BeanUtils.copyProperties(suricataRuleClasstypeDO, suricataRuleClasstypeBO);
    suricataRuleClasstypeBO
        .setCreateTime(DateUtils.toStringISO8601(suricataRuleClasstypeDO.getCreateTime()));
    suricataRuleClasstypeBO
        .setUpdateTime(DateUtils.toStringISO8601(suricataRuleClasstypeDO.getUpdateTime()));

    return suricataRuleClasstypeBO;
  }

  @Override
  public List<String> exportSuricataRuleClasstypes() {
    List<String> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    list.add(CSV_TITLE);
    suricataRuleClasstypeDao.querySuricataRuleClasstypes().forEach(classtype -> {
      list.add(CsvUtils.spliceRowData(classtype.getId(), classtype.getName()));
    });

    return list;
  }

  @Override
  public int importClasstypes(MultipartFile file, String operatorId) {
    Map<String,
        String> existClasstypes = suricataRuleClasstypeDao.querySuricataRuleClasstypes().stream()
            .collect(
                Collectors.toMap(SuricataRuleClasstypeDO::getId, SuricataRuleClasstypeDO::getName));
    Set<String> existNames = Sets.newHashSet(existClasstypes.values());

    List<SuricataRuleClasstypeDO> newClasstypeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleClasstypeDO> existClasstypeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> importIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    String line = "";
    int lineNumber = 0;
    int itemCount = CsvUtils.splitRowData(CSV_TITLE).size();
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (lineNumber == 1) {
          // 跳过首行
          continue;
        }

        List<String> items = CsvUtils.splitRowData(line);
        if (items.size() == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (items.size() != itemCount) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        SuricataRuleClasstypeDO classtype = new SuricataRuleClasstypeDO();
        classtype.setId(items.get(0));
        classtype.setName(items.get(1));
        classtype.setOperatorId(operatorId);
        importIds.add(classtype.getId());

        if (existClasstypes.containsKey(classtype.getId())) {
          String currentName = existClasstypes.get(classtype.getId());
          if (!StringUtils.equals(classtype.getName(), currentName)
              && existNames.contains(classtype.getName())) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "导入失败, 分类名称已存在, 行号: " + lineNumber + ", 内容: " + line);
          }

          existClasstypeList.add(classtype);
        } else {
          if (existNames.contains(classtype.getName())) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "导入失败, 分类名称已存在, 行号: " + lineNumber + ", 内容: " + line);
          }

          newClasstypeList.add(classtype);
        }

        existNames.add(classtype.getName());
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 校验是否没有内容
    if (newClasstypeList.isEmpty() && existClasstypeList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，文件内容为空或没有符合要求的数据");
    }

    // 文件内容中存在重复的ID
    if (Sets.newHashSet(importIds).size() < importIds.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，文件内容存在重复的分类ID");
    }

    int newCount = 0;
    if (CollectionUtils.isNotEmpty(newClasstypeList)) {
      newCount = suricataRuleClasstypeDao.saveSuricataRuleClasstypes(newClasstypeList);
      // 下发新增规则
      List<Map<String, Object>> messageBodys = newClasstypeList.stream()
          .map(item -> suricataRuleClass2MessageBody(item, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, null);
    }

    int modifyCount = 0;
    if (CollectionUtils.isNotEmpty(existClasstypeList)) {
      modifyCount = existClasstypeList.stream()
          .mapToInt(classtype -> suricataRuleClasstypeDao.updateSuricataRuleClasstype(classtype))
          .sum();
      // 下发修改规则
      List<Map<String, Object>> messageBodys = existClasstypeList.stream()
          .map(item -> suricataRuleClass2MessageBody(item, FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, null);
    }

    LOGGER.info("success to import suricata classtype. save: [{}], update: [{}]", newCount,
        modifyCount);
    return newCount + modifyCount;

  }

  @Override
  public SuricataRuleClasstypeBO saveSuricataRuleClasstype(
      SuricataRuleClasstypeBO suricataRuleClasstypeBO, String operatorId) {
    SuricataRuleClasstypeDO existName = suricataRuleClasstypeDao
        .querySuricataRuleClasstypeByName(suricataRuleClasstypeBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "分类名称已存在");
    }

    SuricataRuleClasstypeDO suricataRuleClasstypeDO = new SuricataRuleClasstypeDO();
    BeanUtils.copyProperties(suricataRuleClasstypeBO, suricataRuleClasstypeDO);
    suricataRuleClasstypeDO.setOperatorId(operatorId);
    suricataRuleClasstypeDO = suricataRuleClasstypeDao
        .saveSuricataRuleClasstype(suricataRuleClasstypeDO);

    // 新增规则下发到直属的CMS和探针中
    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        suricataRuleClass2MessageBody(suricataRuleClasstypeDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, null);

    return querySuricataRuleClasstype(suricataRuleClasstypeDO.getId());
  }

  @Override
  public SuricataRuleClasstypeBO updateSuricataRuleClasstype(String id,
      SuricataRuleClasstypeBO suricataRuleClasstypeBO, String operatorId) {
    SuricataRuleClasstypeDO existName = suricataRuleClasstypeDao
        .querySuricataRuleClasstypeByName(suricataRuleClasstypeBO.getName());
    if (StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(id, existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "分类名称已存在");
    }

    SuricataRuleClasstypeDO suricataRuleClasstypeDO = new SuricataRuleClasstypeDO();
    BeanUtils.copyProperties(suricataRuleClasstypeBO, suricataRuleClasstypeDO);
    suricataRuleClasstypeDO.setId(id);
    suricataRuleClasstypeDO.setOperatorId(operatorId);
    suricataRuleClasstypeDao.updateSuricataRuleClasstype(suricataRuleClasstypeDO);

    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        suricataRuleClass2MessageBody(suricataRuleClasstypeDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, null);

    return querySuricataRuleClasstype(id);
  }

  @Override
  public SuricataRuleClasstypeBO deleteSuricataRuleClasstype(String id, String operatorId) {
    SuricataRuleClasstypeDO exist = suricataRuleClasstypeDao.querySuricataRuleClasstype(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "分类不存在");
    }

    SuricataRuleQueryVO suricataRuleQueryVO = new SuricataRuleQueryVO();
    suricataRuleQueryVO.setClasstypeIds(id);
    List<Integer> count = suricataRuleDao.querySuricataRuleIds(suricataRuleQueryVO);
    if (count.size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "分类已被规则使用，无法删除");
    }

    suricataRuleClasstypeDao.deleteSuricataRuleClasstype(id, operatorId);

    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(suricataRuleClass2MessageBody(exist, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, null);

    SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
    BeanUtils.copyProperties(exist, suricataRuleClasstypeBO);
    return suricataRuleClasstypeBO;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE));
  }

  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    List<Map<String, Object>> messages = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          }));
    } else {
      messages.add(messageBody);
    }

    int syncTotalCount = messages.stream().mapToInt(item -> syncClasstype(item)).sum();
    LOGGER.info("current sync SurictaRuleClasstype total : {}", syncTotalCount);

    return syncTotalCount;
  }

  private int syncClasstype(Map<String, Object> messageBody) {

    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
    suricataRuleClasstypeBO.setId(assignId);
    suricataRuleClasstypeBO.setAssignId(assignId);
    suricataRuleClasstypeBO.setName(MapUtils.getString(messageBody, "name"));

    // 判断本次下发的classtype是否存在
    SuricataRuleClasstypeDO exist = suricataRuleClasstypeDao
        .querySuricataRuleClasstype(suricataRuleClasstypeBO.getId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSuricataRuleClasstype(exist.getId(), suricataRuleClasstypeBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveSuricataRuleClasstype(suricataRuleClasstypeBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSuricataRuleClasstype(exist.getId(), CMS_ASSIGNMENT);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步量
      syncCount = addCount + modifyCount + deleteCount;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync geoCustom status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 删除
    int clearCount = 0;
    try {
      clearCount = suricataRuleClasstypeDao.deleteSuricataRuleClasstype(onlyLocal);
    } catch (BusinessException e) {
      LOGGER.warn("delete suricataRuleClasstype failed. error msg: {}", e.getMessage());
    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return suricataRuleClasstypeDao.querySuricataRuleClasstypes(beforeTime).stream()
        .map(item -> item.getAssignId()).collect(Collectors.toList());
  }


  /********************************************************************************************************
   * 下发模块
   *******************************************************************************************************/
  @Override
  public DefaultMQProducer getProducer() {
    return context.getBean("getRocketMQProducer", DefaultMQProducer.class);
  }

  @Override
  public List<String> getTags() {
    return TAGS;
  }

  @Override
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNumber,
      Date beforeTime) {
    // 所有下级设备均生效，无需判断serialNO
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE,
        suricataRuleClasstypeDao.querySuricataRuleClasstypeIds(false));

    return map;
  }

  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE)) {

      List<SuricataRuleClasstypeDO> classtypesList = suricataRuleClasstypeDao
          .querySuricataRuleClasstypes();

      // 当前规则条件
      List<Map<String, Object>> list = classtypesList.stream()
          .map(item -> suricataRuleClass2MessageBody(item, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> suricataRuleClass2MessageBody(
      SuricataRuleClasstypeDO suricataRuleClasstypeDO, String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", suricataRuleClasstypeDO.getId());
    map.put("name", suricataRuleClasstypeDO.getName());
    map.put("action", action);

    return map;
  }
}
