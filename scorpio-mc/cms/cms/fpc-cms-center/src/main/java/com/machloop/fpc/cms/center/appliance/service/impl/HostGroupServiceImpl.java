package com.machloop.fpc.cms.center.appliance.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.cms.center.appliance.bo.HostGroupBO;
import com.machloop.fpc.cms.center.appliance.dao.HostGroupDao;
import com.machloop.fpc.cms.center.appliance.data.HostGroupDO;
import com.machloop.fpc.cms.center.appliance.service.HostGroupService;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
@Order(15)
@Service
public class HostGroupServiceImpl
    implements HostGroupService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_HOSTGROUP);

  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);

  private static final String CSV_TITLE = "`名称`,`IP地址`,`描述`\n";

  private static final int IMPORT_LINE_LIMIT = 1000;

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;

  @Autowired
  private HostGroupDao hostGroupDao;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#queryHostGroups(Pageable, String, String)
   */
  @Override
  public Page<HostGroupBO> queryHostGroups(Pageable page, String name, String description) {

    Page<HostGroupDO> hostGroupDOPage = hostGroupDao.queryHostGroups(page, name, description);
    long totalElem = hostGroupDOPage.getTotalElements();

    List<HostGroupBO> hostGroupBOList = Lists.newArrayListWithCapacity(hostGroupDOPage.getSize());
    for (HostGroupDO hostGroupDO : hostGroupDOPage) {
      HostGroupBO hostGroupBO = new HostGroupBO();
      BeanUtils.copyProperties(hostGroupDO, hostGroupBO);

      hostGroupBO.setCreateTime(DateUtils.toStringISO8601(hostGroupDO.getCreateTime()));
      hostGroupBO.setUpdateTime(DateUtils.toStringISO8601(hostGroupDO.getUpdateTime()));

      hostGroupBOList.add(hostGroupBO);
    }

    return new PageImpl<>(hostGroupBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#queryHostGroups()
   */
  @Override
  public List<HostGroupBO> queryHostGroups() {

    List<HostGroupDO> hostGroupDOList = hostGroupDao.queryHostGroups();
    List<HostGroupBO> hostGroupBOList = Lists.newArrayListWithCapacity(hostGroupDOList.size());
    for (HostGroupDO hostGroupDO : hostGroupDOList) {
      HostGroupBO hostGroupBO = new HostGroupBO();
      BeanUtils.copyProperties(hostGroupDO, hostGroupBO);
      hostGroupBO.setCreateTime(DateUtils.toStringISO8601(hostGroupDO.getCreateTime()));
      hostGroupBOList.add(hostGroupBO);
    }

    return hostGroupBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#queryHostGroup(String)
   */
  @Override
  public HostGroupBO queryHostGroup(String id) {
    HostGroupBO hostGroupBO = new HostGroupBO();

    HostGroupDO hostGroupDO = hostGroupDao.queryHostGroup(id);
    BeanUtils.copyProperties(hostGroupDO, hostGroupBO);

    hostGroupBO.setCreateTime(DateUtils.toStringISO8601(hostGroupDO.getCreateTime()));
    hostGroupBO.setUpdateTime(DateUtils.toStringISO8601(hostGroupDO.getUpdateTime()));

    return hostGroupBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#exportHostGroups()
   */
  @Override
  public List<String> exportHostGroups() {

    List<HostGroupDO> hostGroupList = hostGroupDao.queryHostGroups();

    List<String> result = Lists.newArrayListWithCapacity(hostGroupList.size() + 1);
    result.add(CSV_TITLE);
    hostGroupList.forEach(hostGroup -> {
      String oneItem = CsvUtils.spliceRowData(hostGroup.getName(), hostGroup.getIpAddress(),
          hostGroup.getDescription());
      result.add(oneItem);
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#importHostGroups(org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Override
  public int importHostGroups(MultipartFile file, String operatorId) {
    LOGGER.info("begin to import hostGroup, file name :{}", file.getOriginalFilename());

    List<HostGroupDO> existHostGroupList = hostGroupDao.queryHostGroups();
    List<String> existHostGroupNameList = existHostGroupList.stream().map(e -> e.getName())
        .collect(Collectors.toList());

    List<HostGroupDO> hostGroupDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (lineNumber > (IMPORT_LINE_LIMIT + 1)) {
          LOGGER.warn("import file error, limit exceeded");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容超出" + IMPORT_LINE_LIMIT + "条");
        }

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.info("pass title, line: [{}]", line);
          continue;
        }

        // 解析每一列数据
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
        } else if (contents.size() != CsvUtils.convertCSVToList(CSV_TITLE).size()) {
          LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        String name = StringUtils.trim(contents.get(0));
        String ipAddress = StringUtils.trim(contents.get(1));
        String description = StringUtils.trim(contents.get(2));
        if (StringUtils.isBlank(name) || StringUtils.isBlank(ipAddress)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 校验名称是否重复
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地址组名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (existHostGroupNameList.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地址组名称已存在, 行号: " + lineNumber);
        }
        existHostGroupNameList.add(name);

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        // 校验IP地址
        checkIpAddressThrowExcption(ipAddress);

        // 检测IP是否和已有IP地址组重复
        checkIpAddresssDuplicate(ipAddress, hostGroupDao.queryHostGroups());

        // 地址组基本属性
        HostGroupDO hostGroupDO = new HostGroupDO();
        hostGroupDO.setName(name);
        hostGroupDO.setIpAddress(ipAddress);
        hostGroupDO.setDescription(description);
        hostGroupDO.setOperatorId(operatorId);
        hostGroupDOList.add(hostGroupDO);
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (hostGroupDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，未找到IP地址组数据");
    }

    // 保存IP地址组信息
    int importCount = hostGroupDao.batchSaveHostGroups(hostGroupDOList);

    LOGGER.info("success to import hostGroup.total: [{}]", importCount);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = hostGroupDOList.stream().map(hostGroupDO -> {
      return hostGroup2MessageBody(hostGroupDO, FpcCmsConstants.SYNC_ACTION_ADD);
    }).collect(Collectors.toList());
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_HOSTGROUP, null);

    return importCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#saveHostGroup(com.machloop.fpc.cms.center.appliance.bo.HostGroupBO, String)
   */
  @Override
  @Transactional
  public HostGroupBO saveHostGroup(HostGroupBO hostGroupBO, String operatorId) {
    HostGroupDO exist = hostGroupDao.queryHostGroupByName(hostGroupBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已经存在");
    }

    // 检测IP有效性
    checkIpAddressThrowExcption(hostGroupBO.getIpAddress());
    // 检测IP是否和已有IP地址组重复
    checkIpAddresssDuplicate(hostGroupBO.getIpAddress(), hostGroupDao.queryHostGroups());

    // 写入数据库
    HostGroupDO hostGroupDO = new HostGroupDO();
    BeanUtils.copyProperties(hostGroupBO, hostGroupDO);
    hostGroupDO.setOperatorId(operatorId);
    hostGroupDO = hostGroupDao.saveHostGroup(hostGroupDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(hostGroup2MessageBody(hostGroupDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_HOSTGROUP, null);

    return queryHostGroup(hostGroupDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#updateHostGroup(String, com.machloop.fpc.cms.center.appliance.bo.HostGroupBO, String)
   */
  @Override
  @Transactional
  public HostGroupBO updateHostGroup(String id, HostGroupBO hostGroupBO, String operatorId) {
    HostGroupDO existName = hostGroupDao.queryHostGroupByName(hostGroupBO.getName());
    if (StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(id, existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已经存在");
    }

    HostGroupDO existHostGroup = hostGroupDao.queryHostGroup(id);
    if (StringUtils.isBlank(existHostGroup.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "地址组不存在");
    }

    // 检测IP有效性
    checkIpAddressThrowExcption(hostGroupBO.getIpAddress());
    // 检测IP是否和已有IP地址组重复
    List<HostGroupDO> hostGroupList = hostGroupDao.queryHostGroups().stream()
        .filter(hostGroup -> !StringUtils.equals(hostGroup.getId(), id))
        .collect(Collectors.toList());
    checkIpAddresssDuplicate(hostGroupBO.getIpAddress(), hostGroupList);

    // 写入数据库
    HostGroupDO hostGroupDO = new HostGroupDO();
    BeanUtils.copyProperties(existHostGroup, hostGroupDO);
    hostGroupDO.setName(hostGroupBO.getName());
    hostGroupDO.setIpAddress(hostGroupBO.getIpAddress());
    hostGroupDO.setDescription(hostGroupBO.getDescription());
    hostGroupDO.setOperatorId(operatorId);
    hostGroupDao.updateHostGroup(hostGroupDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(hostGroup2MessageBody(hostGroupDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_HOSTGROUP, null);

    return queryHostGroup(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.HostGroupService#deleteHostGroup(String, String)
   */
  @Override
  @Transactional
  public HostGroupBO deleteHostGroup(String id, String operatorId, boolean forceDelete) {
    HostGroupDO exist = hostGroupDao.queryHostGroup(id);

    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "地址组不存在");
    }

    hostGroupDao.deleteHostGroup(id, operatorId);

    HostGroupBO hostGroupBO = new HostGroupBO();
    BeanUtils.copyProperties(exist, hostGroupBO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(hostGroup2MessageBody(exist, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_HOSTGROUP, null);

    return hostGroupBO;
  }

  private boolean checkIpAddressThrowExcption(String ipAddress) {
    if (StringUtils.isBlank(ipAddress)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP地址不能为空");
    }

    List<String> ips = CsvUtils.convertCSVToList(ipAddress);
    if (ips.size() > FpcCmsConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP地址过多");
    }

    if (Sets.newHashSet(ips).size() < ips.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "存在重复的IP地址");
    }

    for (String ip : ips) {
      if (StringUtils.contains(ip, "-")) {
        String[] ipRange = StringUtils.split(ip, "-");
        // 起止都是正确的ip
        if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
        if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
            && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4)
            || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V6)
                && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V6)))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
      } else if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, ip + "格式非法, 请输入正确的IP地址");
      }
    }
    return true;
  }

  private void checkIpAddresssDuplicate(String pendingIpAddress, List<HostGroupDO> hostGroupList) {
    if (CollectionUtils.isEmpty(hostGroupList)) {
      return;
    }

    // 获取已存在IP地址的范围
    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    hostGroupList.forEach(hostGroup -> {
      CsvUtils.convertCSVToList(hostGroup.getIpAddress()).forEach(ip -> {
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
            || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList.add(
              Tuples.of(hostGroup.getName(), Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {// ipv6
          existIpv6RangeList.add(Tuples.of(hostGroup.getName(), ipv6ToRange(ip)));
        }
      });
    });

    // 获取本次受检IP地址的范围
    List<Tuple2<String, Range<Long>>> pendingIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> pendingIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(pendingIpAddress).forEach(ip -> {
      if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
          || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V4)) {
        Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
        pendingIpv4RangeList.add(Tuples.of(ip, Range.closed(ip2Range.getT1(), ip2Range.getT2())));
      } else {// ipv6
        pendingIpv6RangeList.add(Tuples.of(ip, ipv6ToRange(ip)));
      }
    });

    // 校验IPV4是否重复
    for (Tuple2<String, Range<Long>> pendingIpRange : pendingIpv4RangeList) {
      for (Tuple2<String, Range<Long>> existIpRange : existIpv4RangeList) {
        if (pendingIpRange.getT2().isConnected(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, String
              .format("IP地址/段 [%s] 与已有的IP地址组[%s]重复", pendingIpRange.getT1(), existIpRange.getT1()));
        }
      }
    }

    // 校验IPV6是否重复
    for (Tuple2<String, IPv6AddressRange> pendingIpRange : pendingIpv6RangeList) {
      for (Tuple2<String, IPv6AddressRange> existIpRange : existIpv6RangeList) {
        if (pendingIpRange.getT2().contains(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, String
              .format("IP地址/段 [%s] 与已有的IP地址组[%s]重复", pendingIpRange.getT1(), existIpRange.getT1()));
        }
      }
    }
  }

  private static IPv6AddressRange ipv6ToRange(String ipv6) {
    if (StringUtils.contains(ipv6, "-")) {
      String[] ipRange = StringUtils.split(ipv6, "-");
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipRange[0]),
          IPv6Address.fromString(ipRange[1]));
    } else if (StringUtils.contains(ipv6, "/")) {
      return IPv6Network.fromString(ipv6);
    } else {
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipv6),
          IPv6Address.fromString(ipv6));
    }
  }

  /********************************************************************************************************
   * 下发模块
   *******************************************************************************************************/

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getProducer()
   */
  @Override
  public DefaultMQProducer getProducer() {
    return context.getBean("getRocketMQProducer", DefaultMQProducer.class);
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getTags()
   */
  @Override
  public List<String> getTags() {
    return TAGS;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurationIds(java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNo,
      Date beforeTime) {

    // 所有下级设备均生效，无需判断serialNo
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_HOSTGROUP, hostGroupDao.queryHostGroupIds(false));

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNo, String tag) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_HOSTGROUP)) {
      List<HostGroupDO> hostGroupList = hostGroupDao.queryHostGroups();

      // 当前地址组列表
      List<Map<String, Object>> list = hostGroupList.stream()
          .map(hostGroupDO -> hostGroup2MessageBody(hostGroupDO, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> hostGroup2MessageBody(HostGroupDO hostGroupDO, String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", hostGroupDO.getId());
    map.put("name", hostGroupDO.getName());
    map.put("ipAddress", hostGroupDO.getIpAddress());
    map.put("action", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_HOSTGROUP));
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
   */
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncHostGroup(item)).sum();
    LOGGER.info("current sync hostGroup total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncHostGroup(Map<String, Object> messageBody) {

    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    HostGroupBO hostGroupBO = new HostGroupBO();
    hostGroupBO.setAssignId(assignId);
    hostGroupBO.setId(assignId);
    hostGroupBO.setName(MapUtils.getString(messageBody, "name"));
    hostGroupBO.setIpAddress(MapUtils.getString(messageBody, "ipAddress"));
    hostGroupBO.setDescription(CMS_ASSIGNMENT);

    HostGroupDO exist = hostGroupDao.queryHostGroupByAssignId(hostGroupBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateHostGroup(exist.getId(), hostGroupBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveHostGroup(hostGroupBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteHostGroup(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync hostGroup status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 导出所有IP地址组
    try {
      StringBuilder content = new StringBuilder();
      exportHostGroups().forEach(item -> content.append(item));
      File tempFile = Paths
          .get(HotPropertiesHelper.getProperty("file.runtime.path"), "hostGroups.csv").toFile();
      FileUtils.writeByteArrayToFile(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      LOGGER.warn("backup hostGroup msg failed.", e);
    }

    // 删除
    int clearCount = 0;
    List<String> hostGroupIds = hostGroupDao.queryHostGroupIds(onlyLocal);
    for (String hostGroupId : hostGroupIds) {
      try {
        deleteHostGroup(hostGroupId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete hostGroup failed. error msg: {}", e.getMessage());
        continue;
      }
    }
    return clearCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return hostGroupDao.queryAssignHostGroupIds(beforeTime).stream().map(e -> e.getAssignId())
        .collect(Collectors.toList());
  }

}
