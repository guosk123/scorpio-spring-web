package com.machloop.fpc.manager.appliance.service.impl;

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
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.FilterRuleBO;
import com.machloop.fpc.manager.appliance.dao.FilterRuleDao;
import com.machloop.fpc.manager.appliance.data.FilterRuleDO;
import com.machloop.fpc.manager.appliance.service.FilterRuleService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.npm.appliance.service.NetworkService;

/**
 * @author chenshimiao
 *
 * create at 2022/8/8 6:48 PM,cms
 * @version 1.0
 */
@Order(11)
@Service
public class FilterRuleServiceImpl implements FilterRuleService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilterRuleServiceImpl.class);

  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MIN_FILTER_TUPLE_TRUNCLEN = 64;
  private static final int MAX_FILTER_TUPLE_TRUNCLEN = 1500;
  private static final String DEFAULT_FILTER_RULE_ID = "1";
  private static final String FILTER_RULE_CSV_TITLE = "`名称`,`规则`,`状态`,`备注`\n";
  private static final String[] PROTOCOLS = {"TCP", "UDP"};
  private static final String[] ACTIONS = {"store", "truncate", "drop"};

  private static final String OPERATE_SWAP_TOP = "move_top";
  private static final String OPERATE_SWAP_BOTTOM = "move_bottom";
  private static final String OPERATE_SWAP_UP = "move_up";
  private static final String OPERATE_SWAP_DOWN = "move_down";
  private static final String OPERATE_SWAP_PAGE = "move_page";

  @SuppressWarnings("unused")
  private static final int IMPORT_LINE_LIMIT = 1000;
  @SuppressWarnings("unused")
  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);
  private static final int MAX_DESCRIPTION_LENGTH = 255;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private FilterRuleDao filterRuleDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Override
  public Page<FilterRuleBO> queryFilterRules(PageRequest page) {
    Page<FilterRuleDO> filterRules = filterRuleDao.queryFilterRules(page);
    long totalElem = filterRules.getTotalElements();
    List<FilterRuleBO> result = Lists.newArrayListWithCapacity(filterRules.getSize());
    filterRules.forEach(filterRuleDO -> {
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
      List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao.queryNetworkPolicyByPolicyId(
          filterRuleBO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
      List<String> networkIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getNetworkId)
          .collect(Collectors.toList());
      filterRuleBO.setNetworkId(networkIds);
      filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
      filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));

      result.add(filterRuleBO);
    });
    return new PageImpl<>(result, page, totalElem);
  }

  @Override
  public FilterRuleBO queryFilterRule(String id) {
    FilterRuleDO filterRuleDO = filterRuleDao.queryFilterRule(id);
    FilterRuleBO filterRuleBO = new FilterRuleBO();
    BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
    filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
    filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));

    List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao.queryNetworkPolicyByPolicyId(
        filterRuleBO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
    List<String> networkIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getNetworkId)
        .collect(Collectors.toList());
    filterRuleBO.setNetworkId(networkIds);
    return filterRuleBO;
  }

  @Override
  public List<FilterRuleBO> queryFilterRule() {
    List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRule();
    List<FilterRuleBO> result = Lists.newArrayListWithCapacity(filterRuleDOList.size());
    filterRuleDOList.forEach(filterRuleDO -> {
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
      List<String> networkIds = networkPolicyDao
          .queryNetworkPolicyByPolicyId(filterRuleBO.getId(),
              FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE)
          .stream().map(NetworkPolicyDO::getNetworkId).collect(Collectors.toList());
      filterRuleBO.setNetworkId(networkIds);
      filterRuleBO.setCreateTime(DateUtils.toStringISO8601(filterRuleDO.getCreateTime()));
      filterRuleBO.setUpdateTime(DateUtils.toStringISO8601(filterRuleDO.getUpdateTime()));
      filterRuleBO.setDeleteTime(DateUtils.toStringISO8601(filterRuleDO.getDeleteTime()));
      result.add(filterRuleBO);
    });
    return result;
  }

  @Override
  public FilterRuleBO updateFilterRule(String id, FilterRuleBO filterRuleBO, String operatorId,
      boolean issued) {
    FilterRuleDO exist = filterRuleDao.queryFilterRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "存储过滤规则不存在");
    }

    // 如果网络为空，则跳过判断
    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkId())) {
      for (String networkId : filterRuleBO.getNetworkId()) {
        List<NetworkPolicyDO> storage = networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicyType(
            networkId, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
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

    filterRuleBO.setTuple(StringUtils.defaultIfBlank(filterRuleBO.getTuple(), exist.getTuple()));
    String tuples = StringUtils.strip(filterRuleBO.getTuple(), "[]");
    for (String tuple : Collections.singletonList(tuples)) {
      checkTuple(tuple);
    }

    FilterRuleDO filterRuleDO = new FilterRuleDO();
    filterRuleDO.setId(id);
    filterRuleDO.setName(StringUtils.defaultIfBlank(filterRuleBO.getName(), exist.getName()));
    filterRuleDO.setTuple(StringUtils.defaultIfBlank(filterRuleBO.getTuple(), exist.getTuple()));
    filterRuleDO.setDescription(
        StringUtils.defaultIfBlank(filterRuleBO.getDescription(), exist.getDescription()));
    filterRuleDO.setPriority(
        filterRuleBO.getPriority() == null ? exist.getPriority() : filterRuleBO.getPriority());
    filterRuleDO.setState(StringUtils.defaultIfBlank(filterRuleBO.getState(), exist.getState()));
    filterRuleDO.setOperatorId(operatorId);
    filterRuleDao.updateFilterRule(filterRuleDO);

    // 网络不为空时，进行网络操作 如果是本地执行更新操作，则默认先删除
    if (!issued) {
      networkService.deleteNetworkPolicy(id, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
    }
    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkId())) {
      List<String> networkIds = filterRuleBO.getNetworkId();
      if (!networkIds.isEmpty()) {
        List<NetworkPolicyBO> networkPolicyList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        for (String networkId : networkIds) {
          NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
          networkPolicyBO.setPolicyId(id);
          networkPolicyBO.setNetworkId(networkId);
          networkPolicyBO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
          networkPolicyList.add(networkPolicyBO);
        }
        networkService.saveNetworkPolicy(networkPolicyList, operatorId);
      }
    }

    return queryFilterRule(id);
  }

  @Override
  public FilterRuleBO saveFilterRule(FilterRuleBO filterRuleBO, String before, String operatorId) {
    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkId())) {
      for (String networkId : filterRuleBO.getNetworkId()) {
        List<NetworkPolicyDO> storage = networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicyType(
            networkId, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        if (storage.size() >= 50) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "每个网络最多有50个规则生效");
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
    BeanUtils.copyProperties(filterRuleBO, filterRuleDO);
    if (filterRuleBO.getPriority() == null) {
      int maxPriority = filterRuleDao.queryFilterMaxPriority();
      filterRuleDO.setPriority(maxPriority + 1);
    }
    filterRuleDO.setOperatorId(operatorId);
    FilterRuleDO filterRule = filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);

    if (CollectionUtils.isNotEmpty(filterRuleBO.getNetworkId())) {
      List<NetworkPolicyBO> networkPolicyBOList = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      // 网络操作
      for (String networkId : filterRuleBO.getNetworkId()) {
        NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
        networkPolicyBO.setNetworkId(networkId);
        networkPolicyBO.setPolicyId(filterRule.getId());
        networkPolicyBO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        networkPolicyBOList.add(networkPolicyBO);
      }
      if (CollectionUtils.isNotEmpty(networkPolicyBOList)) {
        networkService.saveNetworkPolicy(networkPolicyBOList, operatorId);
      }
    }

    // 如果是插入和复制，则重新排序
    if (StringUtils.isNotBlank(before)) {
      List<FilterRuleDO> filterRuleDOList = filterRuleDao.queryFilterRule();
      List<String> filterRuleIds = filterRuleDOList.stream().map(FilterRuleDO::getId)
          .collect(Collectors.toList());
      List<Integer> filterRuleDoPriority = filterRuleDOList.stream().map(FilterRuleDO::getPriority)
          .collect(Collectors.toList());
      filterRuleIds.add(filterRuleIds.indexOf(before) - 1, filterRuleIds.remove(0));

      for (String allId : filterRuleIds) {
        filterRuleDao.updateFilterRule(allId,
            filterRuleDoPriority.get(filterRuleIds.indexOf(allId)), operatorId);
      }
    }
    return queryFilterRule(filterRuleDO.getId());
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
  public List<FilterRuleBO> updateFilterRulePriority(List<String> idList, Integer page,
      Integer pageSize, String operator, String operatorId) {
    PageRequest pageRequest = new PageRequest(page, pageSize,
        new Sort(new Sort.Order(Direction.DESC, "priority")));
    List<
        FilterRuleBO> filterRuleBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    switch (operator) {
      case OPERATE_SWAP_TOP:
        List<FilterRuleDO> filterRuleDOListTop = filterRuleDao.queryFilterRule(page, pageSize);
        List<String> idListTop = filterRuleDOListTop.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priority = filterRuleDOListTop.stream().map(FilterRuleDO::getPriority)
            .collect(Collectors.toList());
        Collections.reverse(idList);
        for (String id : idList) {
          idListTop.add(0, idListTop.remove(idListTop.indexOf(id)));
        }
        for (String allId : idListTop) {
          FilterRuleDO filterRuleDO = filterRuleDOListTop.get(idListTop.indexOf(allId));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBO.setPriority(priority.get(idListTop.indexOf(allId)));
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(allId, priority.get(idListTop.indexOf(allId)), operatorId);
        }
        return filterRuleBOList;
      case OPERATE_SWAP_BOTTOM:
        List<FilterRuleDO> filterRuleDOListBottom = filterRuleDao.queryFilterRule();
        List<String> idListBottom = filterRuleDOListBottom.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priorityBottom = filterRuleDOListBottom.stream()
            .map(FilterRuleDO::getPriority).collect(Collectors.toList());
        for (String id : idList) {
          idListBottom.add(filterRuleDOListBottom.size() - 2,
              idListBottom.remove(idListBottom.indexOf(id)));
        }
        for (String idBottom : idListBottom) {
          FilterRuleDO filterRuleDO = filterRuleDOListBottom.get(idListBottom.indexOf(idBottom));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBO.setPriority(priorityBottom.get(idListBottom.indexOf(idBottom)));
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(idBottom,
              priorityBottom.get(idListBottom.indexOf(idBottom)), operatorId);
        }
        return filterRuleBOList;
      case OPERATE_SWAP_UP:
        List<FilterRuleDO> filterRuleDOListUp = filterRuleDao.queryFilterRule(page, pageSize);
        List<String> idUp = filterRuleDOListUp.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<FilterRuleDO> filterRuleDOList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        swapFilterRule(filterRuleDOListUp, idUp, idList, OPERATE_SWAP_UP, filterRuleDOList);
        for (FilterRuleDO filterRuleDO : filterRuleDOList) {
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);
          filterRuleBOList.add(filterRuleBO);
        }
        return filterRuleBOList;
      case OPERATE_SWAP_DOWN:
        List<FilterRuleDO> filterRuleDOListDown = filterRuleDao.queryFilterRule(page + 1, pageSize);
        List<String> idDown = filterRuleDOListDown.stream()
            .filter(filterRuleDO -> filterRuleDO.getPriority() != 0).map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<FilterRuleDO> filterRuleDOListByDown = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        swapFilterRule(filterRuleDOListDown, idDown, idList, OPERATE_SWAP_DOWN,
            filterRuleDOListByDown);
        for (FilterRuleDO filterRuleDO : filterRuleDOListByDown) {
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleDao.saveOrRecoverFilterRule(filterRuleDO);
          filterRuleBOList.add(filterRuleBO);
        }
        return filterRuleBOList;
      case OPERATE_SWAP_PAGE:
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
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBO.setPriority(allPriority.get(allIds.indexOf(id)));
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(id, allPriority.get(allIds.indexOf(id)), operatorId);
        }
        return filterRuleBOList;
      default:
        List<FilterRuleDO> filterRuleDefault = filterRuleDao.queryFilterRules(pageRequest)
            .getContent();
        List<String> idListDefault = filterRuleDefault.stream().map(FilterRuleDO::getId)
            .collect(Collectors.toList());
        List<Integer> priorityDefault = filterRuleDefault.stream().map(FilterRuleDO::getPriority)
            .collect(Collectors.toList());
        for (String idDefault : idList) {
          FilterRuleDO filterRuleDO = filterRuleDefault.get(idListDefault.indexOf(idDefault));
          FilterRuleBO filterRuleBO = new FilterRuleBO();
          BeanUtils.copyProperties(filterRuleDO, filterRuleBO);
          filterRuleBO.setPriority(priorityDefault.get(idList.indexOf(idDefault)));
          filterRuleBOList.add(filterRuleBO);
          filterRuleDao.updateFilterRule(idDefault, priorityDefault.get(idList.indexOf(idDefault)),
              operatorId);
        }
        return filterRuleBOList;
    }
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
      BeanUtils.copyProperties(exist, filterRuleBO);
      filterRuleBO.setState(state);
      filterRuleBOList.add(filterRuleBO);
    }

    filterRuleDao.updateFilterRuleState(idList, state, operatorId);
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

  @Override
  public List<FilterRuleBO> deleteFilterRule(List<String> idList, String operatorId,
      boolean forceDelete) {
    List<
        FilterRuleBO> filterRuleBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      FilterRuleDO exist = filterRuleDao.queryFilterRule(id);
      if (StringUtils.isBlank(exist.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "应用过滤条件不存在");
      }
      FilterRuleBO filterRuleBO = new FilterRuleBO();
      BeanUtils.copyProperties(exist, filterRuleBO);
      networkService.deleteNetworkPolicy(id, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
      filterRuleBOList.add(filterRuleBO);
    }
    filterRuleDao.deleteNetworkRule(idList, operatorId);
    return filterRuleBOList;
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
    if (StringUtils.equals(operate, OPERATE_SWAP_DOWN)) {
      secondFilterRule = filterRuleDOListDown.get(idDown.indexOf(idList.get(0)) + 1);
    } else if (StringUtils.equals(operate, OPERATE_SWAP_UP)) {
      secondFilterRule = filterRuleDOListDown.get(idDown.indexOf(idList.get(0)) - 1);
    } else {
      return;
    }

    int firstPriorityByUp = firstFilterRule.getPriority();
    firstFilterRule.setPriority(secondFilterRule.getPriority());
    secondFilterRule.setPriority(firstPriorityByUp);
    filterRuleDOListByDown.add(firstFilterRule);
    filterRuleDOListByDown.add(secondFilterRule);
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_FILTERRULE));
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
    if (StringUtils.equals(action, FpcConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())
        && !StringUtils.equals(existName.getId(), DEFAULT_FILTER_RULE_ID)) {
      name = name + "_" + "CMS";
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;

    FilterRuleBO filterRuleBO = new FilterRuleBO();
    filterRuleBO.setId(filterRUleInCmsId);
    filterRuleBO.setStorageRuleInCmsId(filterRUleInCmsId);
    filterRuleBO.setName(name);
    filterRuleBO.setPriority(MapUtils.getIntValue(messageBody, "priority"));
    filterRuleBO.setState(MapUtils.getString(messageBody, "state"));
    filterRuleBO.setTuple(MapUtils.getString(messageBody, "tuple"));
    filterRuleBO.setDescription(MapUtils.getString(messageBody, "description"));
    filterRuleBO
        .setNetworkId(CsvUtils.convertCSVToList(MapUtils.getString(messageBody, "networkIds")));

    List<String> vaildNetworkIds = networkDao.queryNetworks().stream().map(NetworkDO::getId)
        .collect(Collectors.toList());
    Map<String, String> validSubnetIdMap = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getSubnetInCmsId, LogicalSubnetDO::getId));

    FilterRuleBO exist = new FilterRuleBO();
    if (!StringUtils.equals(filterRUleInCmsId, DEFAULT_FILTER_RULE_ID)) {
      exist = queryFilterRuleByCmsFilterRuleId(filterRuleBO.getStorageRuleInCmsId());
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
        case FpcConstants.SYNC_ACTION_ADD:
        case FpcConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateFilterRule(exist.getId(), filterRuleBO, CMS_ASSIGNMENT, true);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                filterRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveFilterRule(filterRuleBO, null, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                filterRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcConstants.SYNC_ACTION_DELETE:
          deleteFilterRule(CsvUtils.convertCSVToList(exist.getId()), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
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
  public FilterRuleBO queryFilterRuleByCmsFilterRuleId(String storageRuleInCmsId) {

    FilterRuleDO filterRuleDO = filterRuleDao.queryFilterRuleByCmsFilterRuleId(storageRuleInCmsId);

    FilterRuleBO filterRuleBO = new FilterRuleBO();
    BeanUtils.copyProperties(filterRuleDO, filterRuleBO);

    return filterRuleBO;
  }

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    List<String> filterRuleIds = filterRuleDao.queryFilterRule(onlyLocal);
    // 删除规则时，与规则相关的network_policy表中相关信息也将被删除
    for (String filterRuleId : filterRuleIds) {
      // 默认规则不能被删除
      if (StringUtils.equals(filterRuleId, DEFAULT_FILTER_RULE_ID)) {
        LOGGER.warn("默认规则不能被删除");
        continue;
      }

      try {
        deleteFilterRule(CsvUtils.convertCSVToList(filterRuleId), CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete filterRule failed, error msg: {}", e.getMessage());
        continue;
      }
    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return filterRuleDao.queryFilterRule(beforeTime);
  }
}
