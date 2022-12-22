package com.machloop.fpc.cms.center.appliance.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.FilterRuleBO;
import com.machloop.fpc.cms.center.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.cms.center.appliance.dao.FilterRuleDao;
import com.machloop.fpc.cms.center.appliance.dao.FilterRuleIpDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.data.FilterRuleDO;
import com.machloop.fpc.cms.center.appliance.data.FilterRuleNetworkDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.service.FilterRuleService;
import com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author chenshimiao
 * 
 * create at 2022/8/18 10:54 AM,cms
 * @version 1.0
 */
@Order(11)
@Service
public class FilterRuleServiceImpl
    implements FilterRuleService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_FILTERRULE);
  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MIN_FILTER_TUPLE_TRUNCLEN = 64;
  private static final int MAX_FILTER_TUPLE_TRUNCLEN = 1500;
  private static final String DEFAULT_FILTER_RULE_ID = "1";
  private static final String FILTER_RULE_CSV_TITLE = "`名称`,`规则`,`状态`,`备注`\n";
  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);
  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final String[] PROTOCOLS = {"TCP", "UDP"};
  private static final String[] ACTIONS = {"store", "truncate", "drop"};

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private FilterRuleDao filterRuleDao;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private FilterRuleIpDao filterRuleIpDao;

  @Autowired
  private SensorNetworkGroupService networkGroupService;

  @Autowired
  private NetworkPolicyService networkPolicyService;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Override
  public Page<FilterRuleBO> queryFilterRules(PageRequest page) {
    Page<FilterRuleDO> filterRules = filterRuleDao.queryFilterRules(page);
    long totalElem = filterRules.getTotalElements();
    List<FilterRuleBO> result = Lists.newArrayListWithCapacity(filterRules.getSize());

    // 获取网络和网路组相关信息
    Map<String, List<String>> filterRuleNetworkIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> filterRuleNetworkGroupIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<FilterRuleNetworkDO> filterRuleNetworkDOS = filterRuleIpDao.queryFilterRuleNetworks();
    filterRuleNetworkDOS.forEach(filterRuleNetworkDO -> {
      List<String> list = filterRuleNetworkIds.get(filterRuleNetworkDO.getFilterRuleId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkId())) {
        list.add(filterRuleNetworkDO.getNetworkId());
        filterRuleNetworkIds.put(filterRuleNetworkDO.getFilterRuleId(), list);
      }
      List<String> groupList = filterRuleNetworkGroupIds.get(filterRuleNetworkDO.getFilterRuleId());
      if (CollectionUtils.isEmpty(groupList)) {
        groupList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkGroupId())) {
        groupList.add(filterRuleNetworkDO.getNetworkGroupId());
        filterRuleNetworkGroupIds.put(filterRuleNetworkDO.getFilterRuleId(), groupList);
      }
    });
    filterRules.forEach(filterRuleDO -> {
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
      filterRuleBO.setNetworkId(filterRuleNetworkIds.get(filterRuleDO.getId()));
      filterRuleBO.setNetworkGroupId(filterRuleNetworkGroupIds.get(filterRuleDO.getId()));
      filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
      filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));

      result.add(filterRuleBO);
    });
    return new PageImpl<>(result, page, totalElem);
  }

  @Override
  public List<FilterRuleBO> queryFilterRule() {
    List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRule();
    List<FilterRuleBO> result = Lists.newArrayListWithCapacity(filterRuleDOList.size());
    filterRuleDOList.forEach(filterRuleDO -> {
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
      FilterRuleNetworkDO ipOrIPHostGroupDO = filterRuleIpDao.queryIPOrIPGroupByFilterRuleId();
      BeanUtils.copyProperties(ipOrIPHostGroupDO, filterRuleBO);
      filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
      filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));
      filterRuleBO.setDeletedTime(DateUtils.toStringISO8601(filterRuleDO.getDeleteTime()));
      result.add(filterRuleBO);
    });
    return result;
  }

  @Override
  public FilterRuleBO queryFilterRule(String id) {
    FilterRuleDO filterRuleDO = filterRuleDao.queryFilterRule(id);
    FilterRuleBO filterRuleBO = new FilterRuleBO();
    BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
    filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
    filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<FilterRuleNetworkDO> filterRuleNetworkDOS = filterRuleIpDao.getIP(id);
    filterRuleNetworkDOS.forEach(filterRuleNetworkDO -> {
      if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkId())) {
        networkIds.add(filterRuleNetworkDO.getNetworkId());
      } else {
        networkGroupIds.add(filterRuleNetworkDO.getNetworkGroupId());
      }
    });

    filterRuleBO.setNetworkId(networkIds);
    filterRuleBO.setNetworkGroupId(networkGroupIds);
    return filterRuleBO;
  }

  @Override
  public List<String> exportFilterRules() {
    List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    result.add(FILTER_RULE_CSV_TITLE);
    filterRuleDao.queryFilterRule().forEach(filterRuleDO -> {
      result.add(CsvUtils.spliceRowData(filterRuleDO.getName(), filterRuleDO.getTuple(),
          filterRuleDO.getState(), filterRuleDO.getDescription()));
    });
    return result;
  }

  @Override
  public FilterRuleBO updateFilterRule(String id, FilterRuleBO filterRuleBO, String operatorId) {
    FilterRuleDO exist = filterRuleDao.queryFilterRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "存储过滤规则不存在");
    }

    // 判断网络个数是否超出限制
    if (filterRuleBO.getNetworkId().isEmpty() && !filterRuleBO.getNetworkGroupId().isEmpty()) {
      for (String networkGroupId : filterRuleBO.getNetworkGroupId()) {
        List<String> networkIdInGroup = CsvUtils.convertCSVToList(
            networkGroupService.querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds());
        filterRuleBO.getNetworkId().addAll(networkIdInGroup);
      }
    }
    List<String> vaildNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    for (String networkId : filterRuleBO.getNetworkId()) {
      if (vaildNetworkIds.contains(networkId)) {
        List<NetworkPolicyDO> storage = networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicyType(
            networkId, FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        if (storage.size() >= 50) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "每个网络最多有50个规则生效");
        }
      }
    }

    if (StringUtils.equals(id, DEFAULT_FILTER_RULE_ID)
        && !StringUtils.equals(exist.getName(), filterRuleBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许变更默认规则");
    }

    FilterRuleDO filterRuleByName = filterRuleDao.queryFilterRuleByName(filterRuleBO.getName());
    if (StringUtils.isNotBlank(filterRuleByName.getId())
        && !StringUtils.equals(id, filterRuleByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "规则名称不允许重复");
    }

    // 在存储到数据中为数组类型，为后续支持多个规则做准备
    String tuples = StringUtils.strip(filterRuleBO.getTuple(), "[]");
    for (String tuple : Collections.singletonList(tuples)) {
      checkTuple(tuple);
    }

    // 更新存储过滤规则网络存储
    List<FilterRuleNetworkDO> filterRuleNetworks = null;
    List<FilterRuleNetworkDO> filterRuleNetworkGroupDOList = null;
    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkId())) {
      filterRuleNetworks = filterRuleBO.getNetworkId().stream().map(networkId -> {
        FilterRuleNetworkDO filterRuleNetworkDO = new FilterRuleNetworkDO();
        filterRuleNetworkDO.setNetworkId(networkId);
        filterRuleNetworkDO.setFilterRuleId(id);

        return filterRuleNetworkDO;
      }).collect(Collectors.toList());
    }

    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkGroupId())) {
      filterRuleNetworkGroupDOList = filterRuleBO.getNetworkGroupId().stream()
          .map(networkGroupId -> {
            FilterRuleNetworkDO filterRuleNetworkDO = new FilterRuleNetworkDO();
            filterRuleNetworkDO.setNetworkGroupId(networkGroupId);
            filterRuleNetworkDO.setFilterRuleId(id);

            return filterRuleNetworkDO;
          }).collect(Collectors.toList());
    }
    if (CollectionUtils.isNotEmpty(filterRuleNetworkGroupDOList)) {
      filterRuleIpDao.saveFilterRuleIp(filterRuleNetworkGroupDOList);
    } else if (CollectionUtils.isNotEmpty(filterRuleNetworks)) {
      filterRuleIpDao.saveFilterRuleIp(filterRuleNetworks);
    } else {
      filterRuleIpDao.deleteFilterRuleByFilterRuleId(id);
    }

    // 修改filterRule规则表
    FilterRuleDO filterRuleDO = new FilterRuleDO();
    BeanUtils.copyProperties(filterRuleBO, filterRuleDO);
    filterRuleDO.setId(id);
    filterRuleDO.setName(StringUtils.defaultIfBlank(filterRuleBO.getName(), exist.getName()));
    filterRuleDO.setTuple(StringUtils.defaultIfBlank(filterRuleBO.getTuple(), exist.getTuple()));
    filterRuleDO.setDescription(
        StringUtils.defaultIfBlank(filterRuleBO.getDescription(), exist.getDescription()));
    filterRuleDO.setPriority(
        filterRuleBO.getPriority() == null ? exist.getPriority() : filterRuleBO.getPriority());
    filterRuleDO.setState(StringUtils.defaultIfBlank(filterRuleBO.getState(), exist.getState()));
    filterRuleDO.setOperatorId(operatorId);
    filterRuleDO.setNetworkId(StringUtils
        .defaultIfBlank(StringUtils.join(filterRuleBO.getNetworkId(), ","), exist.getNetworkId()));
    filterRuleDao.updateFilterRule(filterRuleDO);

    // 下发filterRule规则表
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(filterRule2MessageBody(filterRuleDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_FILTERRULE, null);

    List<String> networkIds = filterRuleBO.getNetworkId();

    // 先删除，再插入
    networkPolicyService.deleteNetworkPolicyByPriorId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
    if (!networkIds.isEmpty()) {

      List<NetworkPolicyBO> networkPolicyBOList = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

      for (String networkId : networkIds) {
        // 判断网络是否在当前探针存在
        if (vaildNetworkIds.contains(networkId)) {
          NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
          networkPolicyBO.setPolicyId(id);
          networkPolicyBO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
          networkPolicyBO.setNetworkId(networkId);
          networkPolicyBOList.add(networkPolicyBO);
        }
      }
      networkPolicyService.saveNetworkPolicy(networkPolicyBOList, operatorId);
    }

    return queryFilterRule(id);
  }

  @Override
  public List<FilterRuleBO> updateFilterRulePriority(List<String> idList, Integer page,
      Integer pageSize, String operator, String operatorId) {
    PageRequest pageRequest = new PageRequest(page, pageSize,
        new Sort(new Sort.Order(Sort.Direction.DESC, "priority")));
    List<
        FilterRuleBO> filterRuleBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<FilterRuleDO> filterRuleDOSendAll = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    switch (operator) {
      case "move_top":
        List<FilterRuleDO> filterRuleDOListTop = filterRuleDao.queryFilterRule(page, pageSize);
        List<String> idListTop = filterRuleDOListTop.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priority = filterRuleDOListTop.stream().map(FilterRuleDO::getPriority)
            .collect(Collectors.toList());
        Collections.reverse(idList);
        for (String id : idList) {
          filterRuleDOListTop.add(0, filterRuleDOListTop.remove(idListTop.indexOf(id)));
          idListTop.add(0, idListTop.remove(idListTop.indexOf(id)));
        }
        for (String allId : idListTop) {
          FilterRuleDO filterRuleDO = filterRuleDOListTop.get(idListTop.indexOf(allId));
          filterRuleDO.setPriority(priority.get(idListTop.indexOf(allId)));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleDOSendAll.add(filterRuleDO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(allId, priority.get(idListTop.indexOf(allId)), operatorId);
        }
        break;
      case "move_bottom":
        List<FilterRuleDO> filterRuleDOListBottom = filterRuleDao.queryFilterRule();
        List<String> idListBottom = filterRuleDOListBottom.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priorityBottom = filterRuleDOListBottom.stream()
            .map(FilterRuleDO::getPriority).collect(Collectors.toList());
        for (String id : idList) {
          filterRuleDOListBottom.add(filterRuleDOListBottom.size() - 2,
              filterRuleDOListBottom.remove(idListBottom.indexOf(id)));
          idListBottom.add(filterRuleDOListBottom.size() - 2,
              idListBottom.remove(idListBottom.indexOf(id)));
        }
        for (String idBottom : idListBottom) {
          FilterRuleDO filterRuleDO = filterRuleDOListBottom.get(idListBottom.indexOf(idBottom));
          filterRuleDO.setPriority(priorityBottom.get(idListBottom.indexOf(idBottom)));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleDOSendAll.add(filterRuleDO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(idBottom,
              priorityBottom.get(idListBottom.indexOf(idBottom)), operatorId);
        }
        break;
      case "move_up":
        List<FilterRuleDO> filterRuleDOListUp = filterRuleDao.queryFilterRule(page, pageSize);
        List<String> idUp = filterRuleDOListUp.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<FilterRuleDO> filterRuleDOList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        swapFilterRule(filterRuleDOListUp, idUp, idList, "move_up", filterRuleDOList);
        for (FilterRuleDO filterRuleDO : filterRuleDOList) {
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDOSendAll.add(filterRuleDO);
          filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);
        }
        break;
      case "move_down":
        List<FilterRuleDO> filterRuleDOListDown = filterRuleDao.queryFilterRule(page + 1, pageSize);
        List<String> idDown = filterRuleDOListDown.stream()
            .filter(filterRuleDO -> filterRuleDO.getPriority() != 0).map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<FilterRuleDO> filterRuleDOListByDown = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        swapFilterRule(filterRuleDOListDown, idDown, idList, "move_down", filterRuleDOListByDown);
        for (FilterRuleDO filterRuleDO : filterRuleDOListByDown) {
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDOSendAll.add(filterRuleDO);
          Collections.reverse(filterRuleDOSendAll);
          filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);
        }
        break;
      case "move_page":
        // 获取当页在数据库中保存的信息
        List<String> pageFilterRuleDOList = filterRuleDao.queryFilterRules(pageRequest).getContent()
            .stream().map(FilterRuleDO::getId).collect(Collectors.toList());
        List<String> onlyIdList = new ArrayList<>();
        onlyIdList.addAll(idList);
        idList.removeAll(pageFilterRuleDOList);
        // 获取全部数据
        List<FilterRuleDO> allFilterRuleDOList = filterRuleDao.queryFilterRule();
        List<String> allIds = allFilterRuleDOList.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> allPriority = allFilterRuleDOList.stream().map(FilterRuleDO::getPriority)
            .collect(Collectors.toList());
        int i = idList.size();
        for (String id : idList) {
          String remove = allIds.remove(allIds.indexOf(id));
          allIds.add(allIds.indexOf(onlyIdList.get(onlyIdList.indexOf(id) + i--)), remove);
        }
        for (String id : allIds) {
          FilterRuleDO filterRuleDO = allFilterRuleDOList.get(allIds.indexOf(id));
          filterRuleDO.setPriority(allPriority.get(allIds.indexOf(id)));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(id, allPriority.get(allIds.indexOf(id)), operatorId);
        }
        filterRuleDOSendAll.addAll(filterRuleDao.queryFilterRule());
        break;
      default:
        List<FilterRuleDO> filterRuleDefault = filterRuleDao.queryFilterRules(pageRequest)
            .getContent();
        List<String> idListDefault = filterRuleDefault.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priorityDefault = filterRuleDefault.stream().map(FilterRuleDO::getPriority)
            .collect(Collectors.toList());
        for (String idDefault : idList) {
          FilterRuleDO filterRuleDO = filterRuleDefault.get(idListDefault.indexOf(idDefault));
          filterRuleDO.setPriority(priorityDefault.get(idList.indexOf(idDefault)));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBOList.add(filterRuleBO);
          filterRuleDOSendAll.add(filterRuleDO);
          filterRuleDao.updateFilterRule(idDefault, priorityDefault.get(idList.indexOf(idDefault)),
              operatorId);
        }
        break;
    }
    List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRule();
    // 下发时加上网络相关信息，用来判断
    Map<String,
        String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
            .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                SensorNetworkGroupDO::getNetworkInSensorIds));

    filterRuleDOList.forEach(filterRuleDO -> {
      List<FilterRuleNetworkDO> ipInfo = filterRuleIpDao.getIP(filterRuleDO.getId());
      List<String> networkIds = Lists.newArrayListWithCapacity(0);
      List<String> networkGroupIds = Lists.newArrayListWithCapacity(0);
      for (FilterRuleNetworkDO filterRuleNetworkDO : ipInfo) {
        if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkId())) {
          networkIds.add(filterRuleNetworkDO.getNetworkId());
        }
        if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkGroupId())) {
          networkGroupIds.add(filterRuleNetworkDO.getNetworkGroupId());
        }
      }
      if (CollectionUtils.isNotEmpty(networkIds)) {
        filterRuleDO.setNetworkId(CsvUtils.convertCollectionToCSV(networkIds));
      } else if (CollectionUtils.isNotEmpty(networkGroupIds)) {
        // 解析除全部规则
        List<String> items = networkGroupIds.stream().map(item -> networkGroupDict.get(item))
            .collect(Collectors.toList());
        filterRuleDO.setNetworkId(CsvUtils.convertCollectionToCSV(items));
      } else {
        filterRuleDO.setNetworkId(null);
      }
    });

    // 删除网络为空的规则
    Iterator<FilterRuleDO> iterator = filterRuleDOList.iterator();
    while (iterator.hasNext()) {
      if (StringUtils.isEmpty(iterator.next().getNetworkId())) {
        iterator.remove();
        continue;
      }
    }

    // 优先级改变下发
    List<Map<String, Object>> filterRuleDOSendPriority = filterRuleDOList.stream()
        .filter(item -> !StringUtils.equals(item.getId(), DEFAULT_FILTER_RULE_ID)).map(item -> {
          return filterRule2MessageBody(item, FpcCmsConstants.SYNC_ACTION_MODIFY);
        }).collect(Collectors.toList());
    Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    messageBody.put("batch", true);
    messageBody.put("data", filterRuleDOSendPriority);
    Message message = MQMessageHelper.convertToMessage(messageBody,
        FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_FILTERRULE, null);
    assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    return filterRuleBOList;
  }

  @Override
  public List<FilterRuleBO> updateFilterRuleState(List<String> idList, String state,
      String operatorId) {
    List<
        FilterRuleBO> filterRuleBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      FilterRuleDO exist = filterRuleDao.queryFilterRule(id);
      if (StringUtils.isBlank(exist.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "过滤法则不存在");
      }
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      exist.setState(state);
      filterRuleBOList.add(filterRuleBO);
    }

    filterRuleDao.updateFilterRuleState(idList, state, operatorId);
    List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRuleByIds(idList);
    // 下发时加上网络相关信息，用来判断
    Map<String,
        String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
            .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                SensorNetworkGroupDO::getNetworkInSensorIds));

    filterRuleDOList.forEach(filterRuleDO -> {
      List<FilterRuleNetworkDO> ipInfo = filterRuleIpDao.getIP(filterRuleDO.getId());
      List<String> networkIds = Lists.newArrayListWithCapacity(1);
      List<String> networkGroupIds = Lists.newArrayListWithCapacity(1);
      for (FilterRuleNetworkDO filterRuleNetworkDO : ipInfo) {
        networkIds.add(filterRuleNetworkDO.getNetworkId());
        networkGroupIds.add(filterRuleNetworkDO.getNetworkGroupId());
      }
      if (CollectionUtils.isNotEmpty(networkIds)) {
        filterRuleDO.setNetworkId(CsvUtils.convertCollectionToCSV(networkIds));
      } else if (CollectionUtils.isNotEmpty(networkGroupIds)) {
        // 解析除全部规则
        List<String> items = networkGroupIds.stream().map(item -> networkGroupDict.get(item))
            .collect(Collectors.toList());
        filterRuleDO.setNetworkId(CsvUtils.convertCollectionToCSV(items));
      } else {
        filterRuleDO.setNetworkId(null);
      }
    });

    // 删除网络为空的规则
    Iterator<FilterRuleDO> iterator = filterRuleDOList.iterator();
    while (iterator.hasNext()) {
      if (StringUtils.isEmpty(iterator.next().getNetworkId())) {
        iterator.remove();
      }
    }

    List<Map<String, Object>> messageBodys = filterRuleDOList.stream().map(filterRuleDO -> {
      return filterRule2MessageBody(filterRuleDO, FpcCmsConstants.SYNC_ACTION_MODIFY);
    }).collect(Collectors.toList());
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_FILTERRULE, null);
    return filterRuleBOList;
  }

  @Override
  public FilterRuleBO saveFilterRule(FilterRuleBO filterRuleBO, String before, String operatorId) {

    // 如果存储的为网络组，解析网络组
    if (CollectionUtils.isEmpty(filterRuleBO.getNetworkId())) {
      for (String networkGroupId : filterRuleBO.getNetworkGroupId()) {
        List<String> networkIds = CsvUtils.convertCSVToList(
            networkGroupService.querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds());
        filterRuleBO.getNetworkId().addAll(networkIds);
      }
    }

    List<String> vaildNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    for (String networkId : filterRuleBO.getNetworkId()) {
      if (vaildNetworkIds.contains(networkId)) {
        List<NetworkPolicyDO> storage = networkPolicyDao.queryNetworkPolicyByPolicyId(networkId,
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        if (storage.size() >= 50) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "每个网络最多在50个网络中生效");
        }
      }
    }

    FilterRuleDO exist = filterRuleDao.queryFilterRuleByName(filterRuleBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "存储过滤规则名称已存在");
    }

    String tuples = StringUtils.strip(filterRuleBO.getTuple(), "[]");
    for (String tuple : Collections.singletonList(tuples)) {
      checkTuple(tuple);
    }

    FilterRuleDO filterRuleDO = new FilterRuleDO();
    int maxPriority = filterRuleDao.queryFilterMaxPriority();
    filterRuleBO.setPriority(maxPriority + 1);
    BeanUtils.copyProperties(filterRuleBO, filterRuleDO);
    filterRuleDO.setOperatorId(operatorId);
    filterRuleDO.setNetworkId(StringUtils.join(filterRuleBO.getNetworkId(), ","));
    FilterRuleDO filterRule = filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);

    // 保存规则与作用IP的关系
    List<FilterRuleNetworkDO> filterRuleNetworks = filterRuleBO.getNetworkId().stream()
        .map(networkId -> {
          FilterRuleNetworkDO filterRuleNetworkDO = new FilterRuleNetworkDO();
          filterRuleNetworkDO.setNetworkId(networkId);
          filterRuleNetworkDO.setFilterRuleId(filterRule.getId());

          return filterRuleNetworkDO;
        }).collect(Collectors.toList());

    List<FilterRuleNetworkDO> filterRuleNetworkGroupDOList = filterRuleBO.getNetworkGroupId()
        .stream().map(networkGroupId -> {
          FilterRuleNetworkDO filterRuleNetworkDO = new FilterRuleNetworkDO();
          filterRuleNetworkDO.setNetworkGroupId(networkGroupId);
          filterRuleNetworkDO.setFilterRuleId(filterRule.getId());

          return filterRuleNetworkDO;
        }).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(filterRuleNetworkGroupDOList)) {
      filterRuleIpDao.saveFilterRuleIp(filterRuleNetworkGroupDOList);
    } else if (CollectionUtils.isNotEmpty(filterRuleNetworks)) {
      filterRuleIpDao.saveFilterRuleIp(filterRuleNetworks);
    }

    List<NetworkPolicyBO> networkPolicyBOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 向规则与网络关联表中添加对应关系 解析网络组

    for (String networkId : filterRuleBO.getNetworkId()) {
      if (vaildNetworkIds.contains(networkId)) {
        NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
        networkPolicyBO.setPolicyId(filterRule.getId());
        networkPolicyBO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        networkPolicyBO.setNetworkId(networkId);
        networkPolicyBOList.add(networkPolicyBO);
      }
    }
    networkPolicyService.saveNetworkPolicy(networkPolicyBOList, operatorId);

    List<String> sortIds = filterRuleDao.queryFilterRule().stream().map(BaseDO::getId)
        .collect(Collectors.toList());
    Collections.reverse(sortIds);
    if (!StringUtils.equals(filterRule.getId(), DEFAULT_FILTER_RULE_ID)) {
      filterRule.setPriorId(sortIds.get(sortIds.indexOf(filterRule.getId()) - 1));
    }
    // 下发到直属fpc和cms
    filterRule.setNetworkId(CsvUtils.convertCollectionToCSV(filterRuleBO.getNetworkId()));
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(filterRule2MessageBody(filterRule, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_FILTERRULE, null);

    // 如果是插入和复制，则重新排序
    if (StringUtils.isNotBlank(before)) {
      List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRule();
      List<String> filterRuleIds = filterRuleDOList.stream().map(FilterRuleDO::getId)
          .collect(Collectors.toList());
      List<Integer> filterRuleDoPriority = filterRuleDOList.stream().map(FilterRuleDO::getPriority)
          .collect(Collectors.toList());
      filterRuleIds.add(filterRuleIds.indexOf(before) - 1, filterRuleIds.remove(0));

      List<FilterRuleDO> filterRuleDOS = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (String allId : filterRuleIds) {
        filterRuleDao.updateFilterRule(allId,
            filterRuleDoPriority.get(filterRuleIds.indexOf(allId)), operatorId);
        FilterRuleDO filterRuleDOFpc = new FilterRuleDO();
        filterRuleDOFpc.setId(allId);
        filterRuleDOFpc.setPriority(filterRuleDoPriority.get(filterRuleIds.indexOf(allId)));
        filterRuleDOS.add(filterRuleDOFpc);
      }

      // List<FilterRuleDO> filterUpdateRuleDOList =
      // filterRuleDao.queryFilterRuleByIds(filterRuleIds);
      // 下发时加上网络相关信息，用来判断
      Map<String,
          String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
              .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                  SensorNetworkGroupDO::getNetworkInSensorIds));

      filterRuleDOS.forEach(fitlerRuleDOItem -> {
        List<FilterRuleNetworkDO> ipInfo = filterRuleIpDao.getIP(fitlerRuleDOItem.getId());
        List<String> networkIds = Lists.newArrayListWithCapacity(1);
        List<String> networkGroupIds = Lists.newArrayListWithCapacity(1);
        for (FilterRuleNetworkDO filterRuleNetworkDO : ipInfo) {
          networkIds.add(filterRuleNetworkDO.getNetworkId());
          networkGroupIds.add(filterRuleNetworkDO.getNetworkGroupId());
        }
        if (CollectionUtils.isNotEmpty(networkIds)) {
          fitlerRuleDOItem.setNetworkId(CsvUtils.convertCollectionToCSV(networkIds));
        } else if (CollectionUtils.isNotEmpty(networkGroupIds)) {
          // 解析除全部规则
          List<String> items = networkGroupIds.stream().map(item -> networkGroupDict.get(item))
              .collect(Collectors.toList());
          fitlerRuleDOItem.setNetworkId(CsvUtils.convertCollectionToCSV(items));
        } else {
          fitlerRuleDOItem.setNetworkId(null);
        }
      });

      List<Map<String, Object>> messageBodysList = filterRuleDOS.stream()
          .filter(item -> !StringUtils.equals(item.getId(), DEFAULT_FILTER_RULE_ID))
          .map(filterRuleDOStream -> {
            return filterRule2MessageBody(filterRuleDOStream, FpcCmsConstants.SYNC_ACTION_MODIFY);
          }).collect(Collectors.toList());
      Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      messageBody.put("batch", true);
      messageBody.put("data", messageBodysList);
      Message message = MQMessageHelper.convertToMessage(messageBody,
          FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_FILTERRULE, null);
      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    }

    return queryFilterRule(filterRuleDO.getId());
  }

  @Override
  public List<FilterRuleBO> deleteFilterRule(List<String> idList, String operatorId,
      boolean forceDelete) {
    List<
        FilterRuleBO> filterRuleBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<
        FilterRuleDO> filterRuleDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      // 删除与networkPolicy相关规则
      filterRuleIpDao.deleteFilterRuleByFilterRuleId(id);
      FilterRuleDO exist = filterRuleDao.queryFilterRule(id);
      if (StringUtils.isBlank(exist.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "应用过滤条件不存在");
      }
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(exist, filterRuleBO);
      filterRuleDOList.add(exist);
      networkPolicyService.deleteNetworkPolicyByPriorId(id,
          FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
      filterRuleBOList.add(filterRuleBO);
    }

    filterRuleDao.deleteNetworkRule(idList, operatorId);

    List<Map<String, Object>> messageBodys = filterRuleDOList.stream().map(filterRuleDO -> {
      return filterRule2MessageBody(filterRuleDO, FpcCmsConstants.SYNC_ACTION_DELETE);
    }).collect(Collectors.toList());
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_FILTERRULE, null);
    return filterRuleBOList;
  }

  @Override
  public void importFilterRule(MultipartFile file, String operatorId) {
    LOGGER.info("begin to import filterRuls, file name :{}", file.getOriginalFilename());

    List<FilterRuleDO> existFilterRule = filterRuleDao.queryFilterRule();
    List<String> existFilterRuleName = existFilterRule.stream().map(FilterRuleDO::getName)
        .collect(Collectors.toList());

    // 将导入的优先级默认置为最高
    int priority = filterRuleDao.queryFilterMaxPriority();

    List<
        FilterRuleDO> filterRuleDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        priority++;

        if (lineNumber == 1) {
          LOGGER.info("pass title, line [{}]", line);
          continue;
        }

        List<String> contents = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        Matcher matcher = SPLIT_PATTERN.matcher(line);
        while (matcher.find()) {
          String fieldContext = StringUtils
              .substringBeforeLast(StringUtils.substringAfter(matcher.group(), "`"), "`");
          contents.add(
              StringUtils.replace(StringUtils.replace(fieldContext, "\\`", "`"), "\\r\\n", "\r\n"));
        }

        if (contents.size() == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (contents.size() != CsvUtils.convertCSVToList(FILTER_RULE_CSV_TITLE).size()) {
          LOGGER.info("import file error, lineNunber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容解析错误，行号: " + lineNumber + ", 内容" + line);
        }
        String name = StringUtils.trim(contents.get(0));
        String tuple = StringUtils.trim(contents.get(1));
        String state = StringUtils.trim(contents.get(2));
        String description = StringUtils.trim(contents.get(3));
        if (StringUtils.isAnyBlank(name, tuple, state)) {
          LOGGER.info("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容解析失败，行号：" + lineNumber);
        }

        FilterRuleDO filterRuleDO = new FilterRuleDO();
        if (existFilterRuleName.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber:{}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，IP地址组名称已存在，行号: " + lineNumber);
        }
        filterRuleDO.setName(name);
        existFilterRuleName.add(name);

        // 检测传入的规则是否合法
        String tuples = StringUtils.strip(tuple, "[]");
        for (String item : Collections.singletonList(tuples)) {
          try {
            checkTuple(item);
          } catch (BusinessException exception) {
            LOGGER.warn("import file error, analysis tuple error, lineNumber:{}, content: {}",
                lineNumber, line);
            throw new BusinessException(exception.getCode(), exception.getMessage());
          }
        }
        filterRuleDO.setTuple(tuple);

        // 判断状态是否合法
        if (!StringUtils.equalsAny(state, Constants.BOOL_YES, Constants.BOOL_NO)) {
          LOGGER.warn("import file error, state is not legal, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，状态不合法，行号: " + lineNumber);
        }
        filterRuleDO.setState(state);

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds the limit, lineNumber: {}, line: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，描述长度超出限制,行号：" + lineNumber);
        }
        filterRuleDO.setDescription(description);
        filterRuleDO.setPriority(priority);
        filterRuleDOList.add(filterRuleDO);
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, line: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (CollectionUtils.isEmpty(filterRuleDOList)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件为空，导入失败。");
    }

    int count = filterRuleDao.saveFilterRules(filterRuleDOList, operatorId);
    LOGGER.info("import file success, lineNumber: {}", count);
  }

  private void checkTuple(String tuple) {
    Map<String, Object> tupleByMap = JsonHelper.deserialize(tuple,
        new TypeReference<Map<String, Object>>() {
        }, false);

    String sourceIp = MapUtils.getString(tupleByMap, "sourceIp", "");
    String destIp = MapUtils.getString(tupleByMap, "destIp", "");
    if (StringUtils.isNotBlank(sourceIp) && (!NetworkUtils
        .isInetAddress(StringUtils.substringBeforeLast(sourceIp, "/"), NetworkUtils.IpVersion.V4)
        && !NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(sourceIp, "/"),
            NetworkUtils.IpVersion.V6))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "源IP格式不合法");
    }

    if (StringUtils.isNotBlank(destIp) && (!NetworkUtils
        .isInetAddress(StringUtils.substringBeforeLast(destIp, "/"), NetworkUtils.IpVersion.V4)
        && !NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(destIp, "/"),
            NetworkUtils.IpVersion.V6))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "目的IP格式不合法");
    }

    if (StringUtils.isNotBlank(destIp) && StringUtils.isNotBlank(sourceIp)) {
      if (!((NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(sourceIp, "/"),
          NetworkUtils.IpVersion.V4)
          && NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(destIp, "/"),
              NetworkUtils.IpVersion.V4))
          || (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(sourceIp, "/"),
              NetworkUtils.IpVersion.V6)
              && NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(destIp, "/"),
                  NetworkUtils.IpVersion.V6)))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "源/目的IP必须同时为IPV4/IPV6");
      }

      if (StringUtils.equals(sourceIp, destIp)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "源IP和目的IP不能相同");
      }
    }

    String sourcePort = MapUtils.getString(tupleByMap, "sourcePort");
    if (StringUtils.isNotBlank(sourcePort) && !NetworkUtils.isInetAddressPort(sourcePort)) {
      String[] range = StringUtils.split(sourcePort, "-");
      if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
          || !NetworkUtils.isInetAddressPort(range[1])
          || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的源端口：" + sourcePort);
      }
    }

    String destPort = MapUtils.getString(tupleByMap, "destPort");
    if (StringUtils.isNotBlank(destPort) && !NetworkUtils.isInetAddressPort(destPort)) {
      String[] destRange = StringUtils.split(destPort, "-");
      if (destRange.length != 2 || !NetworkUtils.isInetAddressPort(destRange[0])
          || !NetworkUtils.isInetAddressPort(destRange[1])
          || (Integer.parseInt(destRange[0]) >= Integer.parseInt(destRange[1]))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的目的端口：" + destRange);
      }
    }

    String protocol = MapUtils.getString(tupleByMap, "protocol");
    if (StringUtils.isNotBlank(protocol) && !Arrays.asList(PROTOCOLS).contains(protocol)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "不合法的协议号:" + protocol);
    }

    boolean isOk = true;
    String vlanIdStr = MapUtils.getString(tupleByMap, "vlanId");
    if (StringUtils.isNotBlank(vlanIdStr)) {
      try {
        int vlanId = Integer.parseInt(vlanIdStr);
        if (vlanId < MIN_FILTER_TUPLE_VLANID || vlanId > MAX_FILTER_TUPLE_VLANID) {
          isOk = false;
        }
      } catch (NumberFormatException e) {
        String[] range = StringUtils.split(vlanIdStr, "-");
        try {
          int vlanId1 = Integer.parseInt(range[0]);
          int vlanId2 = Integer.parseInt(range[1]);
          if (vlanId1 < MIN_FILTER_TUPLE_VLANID || vlanId1 > MAX_FILTER_TUPLE_VLANID) {
            isOk = false;
          }
          if (vlanId2 < MIN_FILTER_TUPLE_VLANID || vlanId2 > MAX_FILTER_TUPLE_VLANID) {
            isOk = false;
          }
          if (vlanId1 > vlanId2) {
            isOk = false;
          }
        } catch (NumberFormatException nfException) {
          isOk = false;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
          isOk = false;
        }
      }
    }
    if (!isOk) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE,
          "不合法的vlanId：" + vlanIdStr);
    }

    String action = MapUtils.getString(tupleByMap, "action");
    if (StringUtils.isNotBlank(action) && Arrays.asList(ACTIONS).contains(action)) {
      if (StringUtils.equals(action, "truncate")) {
        int truncLen = MapUtils.getIntValue(tupleByMap, "truncLen");
        if (truncLen < MIN_FILTER_TUPLE_TRUNCLEN && truncLen > MAX_FILTER_TUPLE_TRUNCLEN) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE,
              "不合法的truncLen：" + truncLen);
        }
      }
    } else {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "不合法的动作：" + action);
    }
  }

  private void swapFilterRule(List<FilterRuleDO> filterRuleDOListDown, List<String> idDown,
      List<String> idList, String operate, List<FilterRuleDO> filterRuleDOListByDown) {
    FilterRuleDO firstFilterRule = filterRuleDOListDown.get(idDown.indexOf(idList.get(0)));
    FilterRuleDO secondFilterRule = null;
    if (StringUtils.equals(operate, "move_down")) {
      secondFilterRule = filterRuleDOListDown.get(idDown.indexOf(idList.get(0)) + 1);
    } else if (StringUtils.equals(operate, "move_up")) {
      secondFilterRule = filterRuleDOListDown.get(idDown.indexOf(idList.get(0)) - 1);
    }
    int firstPriorityByUp = firstFilterRule.getPriority();
    firstFilterRule.setPriority(secondFilterRule.getPriority());
    secondFilterRule.setPriority(firstPriorityByUp);
    filterRuleDOListByDown.add(firstFilterRule);
    filterRuleDOListByDown.add(secondFilterRule);
  }

  private Map<String, Object> filterRule2MessageBody(FilterRuleDO filterRuleDO, String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", filterRuleDO.getId());
    map.put("name", filterRuleDO.getName());
    map.put("tuple", filterRuleDO.getTuple());
    map.put("description", filterRuleDO.getDescription());
    map.put("state", filterRuleDO.getState());
    map.put("networkIds", filterRuleDO.getNetworkId());
    map.put("priority", filterRuleDO.getPriority());
    map.put("action", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_FILTERRULE));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncFilterRule(item)).sum();
    LOGGER.info("current sync filterRule total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncFilterRule(Map<String, Object> messageBody) {
    int sysCount = 0;
    String filterRUleInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(filterRUleInCmsId)) {
      return sysCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    // 下发规则与本地冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    FilterRuleDO existName = filterRuleDao.queryFilterRuleByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())
        && !StringUtils.equals(existName.getId(), DEFAULT_FILTER_RULE_ID)) {
      name = name + "_" + "CMS";
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;

    FilterRuleBO filterRuleBO = new FilterRuleBO();
    filterRuleBO.setId(filterRUleInCmsId);
    filterRuleBO.setAssignId(filterRUleInCmsId);
    filterRuleBO.setName(name);
    filterRuleBO.setPriority(MapUtils.getIntValue(messageBody, "priority"));
    filterRuleBO.setState(MapUtils.getString(messageBody, "state"));
    filterRuleBO.setTuple(MapUtils.getString(messageBody, "tuple"));
    filterRuleBO.setDescription(MapUtils.getString(messageBody, "description"));
    filterRuleBO
        .setNetworkId(CsvUtils.convertCSVToList(MapUtils.getString(messageBody, "networkIds")));

    List<String> vaildNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    Map<String,
        String> validSubnetIdMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
            .collect(
                Collectors.toMap(SensorLogicalSubnetDO::getAssignId, SensorLogicalSubnetDO::getId));

    FilterRuleBO exist = new FilterRuleBO();
    if (!StringUtils.equals(filterRUleInCmsId, DEFAULT_FILTER_RULE_ID)) {
      exist = queryFilterRule(filterRuleBO.getAssignId());
    } else {
      exist.setId(filterRUleInCmsId);
    }

    // 缺省策略不进行此判断
    if (!StringUtils.equals(filterRuleBO.getId(), DEFAULT_FILTER_RULE_ID)) {
      List<String> serviceNetworkList = filterRuleBO.getNetworkId().stream().distinct()
          .filter(networkId -> vaildNetworkIds.contains(networkId)
              || validSubnetIdMap.containsKey(networkId))
          .map(networkId -> validSubnetIdMap.getOrDefault(networkId, networkId))
          .collect(Collectors.toList());

      outer: if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_MODIFY)) {
        if (CollectionUtils.isEmpty(serviceNetworkList) && StringUtils.isNotEmpty(exist.getId())) {
          action = FpcCmsConstants.SYNC_ACTION_DELETE;
          break outer;
        }
        if (StringUtils.isNotEmpty(exist.getId())
            && CollectionUtils.isNotEmpty(serviceNetworkList)) {
          action = FpcCmsConstants.SYNC_ACTION_ADD;
          break outer;
        }
      }

      if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_MODIFY)
          && CollectionUtils.isEmpty(serviceNetworkList) && StringUtils.isEmpty(exist.getId())) {
        return sysCount;
      }

      if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
          && CollectionUtils.isEmpty(serviceNetworkList)) {
        // 不存在该业务所包含的网络
        return sysCount;
      }
    }

    // 网络从networkPolicy进行下发
    filterRuleBO.setNetworkId(null);

    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateFilterRule(exist.getId(), filterRuleBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP),
                filterRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveFilterRule(filterRuleBO, null, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP),
                filterRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteFilterRule(CsvUtils.convertCSVToList(exist.getId()), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP),
              filterRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数量
      sysCount = addCount + deleteCount + modifyCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync filterRule status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return sysCount;
    }
    return sysCount;
  }


  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    List<String> filterRuleIds = filterRuleDao.queryFilterRules(onlyLocal);
    for (String filterRuleId : filterRuleIds) {
      try {
        deleteFilterRule(CsvUtils.convertCSVToList(filterRuleId), CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete filterPolicy failed. error msg: {}", e.getMessage());
        continue;
      }
    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return filterRuleDao.queryAssignFilterPolicyIds(beforeTime).stream().map(e -> e.getAssignId())
        .collect(Collectors.toList());
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
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNo,
      Date beforeTime) {
    // 所有下级设备均生效，无需判断serialNo
    // 网络组字典
    Map<String,
        String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
            .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                SensorNetworkGroupDO::getNetworkInSensorIds));

    // 每个规则对应的网络ID集合
    Map<String, List<String>> filterRuleNetworkIdMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取所有对应的网络信息
    filterRuleIpDao.queryFilterRuleNetworks().forEach(filterRuleNetworkDO -> {
      List<String> list = filterRuleNetworkIdMap.getOrDefault(filterRuleNetworkDO.getFilterRuleId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));

      if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkId())) {
        list.addAll(CsvUtils.convertCSVToList(filterRuleNetworkDO.getNetworkId()));
      } else {
        list.addAll(CsvUtils
            .convertCSVToList(networkGroupDict.get(filterRuleNetworkDO.getNetworkGroupId())));
      }

      filterRuleNetworkIdMap.put(filterRuleNetworkDO.getFilterRuleId(), list);
    });

    // 如果指定了下发的设备，并且存储过滤规则不包含将要下发设备的网络，则不下发该规则
    if (!StringUtils.isAllBlank(deviceType, serialNo)) {
      // 下发设备包含的主网络
      List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNo).stream()
          .map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
      // 下发设备包含的子网
      fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
          .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
          .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

      Iterator<
          Map.Entry<String, List<String>>> iterator = filterRuleNetworkIdMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, List<String>> entry = iterator.next();
        List<String> bak = Lists.newArrayList(entry.getValue());
        bak.removeAll(fpcNetworkIds);
        entry.getValue().removeAll(bak);

        if (CollectionUtils.isEmpty(entry.getValue())) {
          iterator.remove();
        }
      }
    }

    List<String> fullConfigurations = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    fullConfigurations.addAll(filterRuleNetworkIdMap.keySet());
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_FILTERRULE, fullConfigurations);

    return map;
  }

  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    // 思路：下发时，首先根据顺序进行筛选，将筛选完成后的优先级都存储入网络，然后根据是否包含此网络通过迭代器进行删除等操作，最后得到最终结果进行下发
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_FILTERRULE)) {
      // 网络组字典
      Map<String,
          String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
              .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                  SensorNetworkGroupDO::getNetworkInSensorIds));

      // 每个规则对应的网络ID集合
      Map<String, List<String>> filterRuleNetworkIdMap = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      // 获取所有对应的网络信息
      filterRuleIpDao.queryFilterRuleNetworks().forEach(filterRuleNetworkDO -> {
        List<String> list = filterRuleNetworkIdMap.getOrDefault(
            filterRuleNetworkDO.getFilterRuleId(),
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));

        if (StringUtils.isNotBlank(filterRuleNetworkDO.getNetworkId())) {
          list.addAll(CsvUtils.convertCSVToList(filterRuleNetworkDO.getNetworkId()));
        } else {
          list.addAll(CsvUtils
              .convertCSVToList(networkGroupDict.get(filterRuleNetworkDO.getNetworkGroupId())));
        }

        filterRuleNetworkIdMap.put(filterRuleNetworkDO.getFilterRuleId(), list);
      });

      // 如果指定了下发的设备，并且存储过滤规则不包含将要下发设备的网络，则不下发该规则
      if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
        // 下发设备包含的主网络
        List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
            .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
        // 下发设备包含的子网
        fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
            .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
            .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

        Iterator<Map.Entry<String, List<String>>> iterator = filterRuleNetworkIdMap.entrySet()
            .iterator();
        while (iterator.hasNext()) {
          Map.Entry<String, List<String>> entry = iterator.next();
          List<String> bak = Lists.newArrayList(entry.getValue());
          bak.removeAll(fpcNetworkIds);
          entry.getValue().removeAll(bak);

          if (CollectionUtils.isEmpty(entry.getValue())) {
            iterator.remove();
          }
        }
      }

      if (MapUtils.isEmpty(filterRuleNetworkIdMap)) {
        return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
      }

      // 缺省策略必须下发
      List<String> filterRuleIds = Lists.newArrayList(filterRuleNetworkIdMap.keySet());
      filterRuleIds.add(DEFAULT_FILTER_RULE_ID);
      // 根据筛选后的有效网络规则组合，查询将要下发的规则
      List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRuleByIds(filterRuleIds);
      filterRuleDOList.forEach(filterRuleDO -> {
        filterRuleDO
            .setNetworkId(StringUtils.join(filterRuleNetworkIdMap.get(filterRuleDO.getId()), ','));
      });
      List<Map<String, Object>> list = filterRuleDOList.stream()
          .map(
              filterRuleDO -> filterRule2MessageBody(filterRuleDO, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }
}
