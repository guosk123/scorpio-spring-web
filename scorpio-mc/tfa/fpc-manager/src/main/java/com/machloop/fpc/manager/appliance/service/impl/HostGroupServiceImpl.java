package com.machloop.fpc.manager.appliance.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
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
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.dao.HostGroupDao;
import com.machloop.fpc.manager.appliance.data.HostGroupDO;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
@Order(10)
@Service
public class HostGroupServiceImpl implements HostGroupService, SyncConfigurationService {

  @Autowired
  private HostGroupDao hostGroupDao;

  @Autowired
  private GlobalSettingService globalSettingService;
  private static final String CSV_TITLE = "`名称`,`IP地址组`,`描述`\n";
  private static final int IMPORT_LINE_LIMIT = 1000;
  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);
  private static final Logger LOGGER = LoggerFactory.getLogger(HostGroupServiceImpl.class);

  /**
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#queryHostGroups(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
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
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#queryHostGroups(java.lang.String, java.lang.String)
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

  @Override
  public List<String> exportHostGroups(String name) {
    List<HostGroupDO> hostGroupDOS = hostGroupDao.queryHostGroupByNameList(name);
    List<String> result = Lists.newArrayListWithCapacity(hostGroupDOS.size() + 1);
    result.add(CSV_TITLE);
    hostGroupDOS.forEach(hostGroupDO -> {
      String outItem = CsvUtils.spliceRowData(hostGroupDO.getName(), hostGroupDO.getIpAddress(),
          hostGroupDO.getDescription());
      result.add(outItem);
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#queryHostGroup(java.lang.String)
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
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#queryHostGroupByCmsHostGroupId(java.lang.String)
   */
  @Override
  public HostGroupBO queryHostGroupByCmsHostGroupId(String cmsHostGroupId) {

    HostGroupDO hostGroupDO = hostGroupDao.queryHostGroupByCmsHostGroupId(cmsHostGroupId);

    HostGroupBO hostGroupBO = new HostGroupBO();
    BeanUtils.copyProperties(hostGroupDO, hostGroupBO);

    return hostGroupBO;
  }

  @Transactional
  @Override
  public synchronized void importHostGroups(MultipartFile file, String id) {
    LOGGER.info("begin to import hostGroups, file name :{}", file.getOriginalFilename());

    List<HostGroupDO> existHostGroups = hostGroupDao.queryHostGroups();
    List<String> existHostGroupsName = existHostGroups.stream()
        .map(hostGroup -> hostGroup.getName()).collect(Collectors.toList());

    List<HostGroupDO> hostGroupDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (lineNumber > (IMPORT_LINE_LIMIT + 1)) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容超出" + IMPORT_LINE_LIMIT + "条");
        }

        if (lineNumber == 1) {
          LOGGER.info("pass title, line: [{}]", line);
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
        } else if (contents.size() != CsvUtils.convertCSVToList(CSV_TITLE).size()) {
          LOGGER.info("import file error, lineNumber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容解析错误，行号：" + lineNumber + ", 内容：" + line);
        }

        String name = StringUtils.trim(contents.get(0));
        String ipAddress = StringUtils.trim(contents.get(1));
        String description = StringUtils.trim(contents.get(2));
        if (StringUtils.isAnyBlank(name, ipAddress)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容解析失败，行号：" + lineNumber);
        }

        HostGroupDO hostGroupDO = new HostGroupDO();

        if (existHostGroupsName.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber:{}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，IP地址组名称已存在，行号：" + lineNumber);
        }
        existHostGroupsName.add(name);

        // 检验地址组是否合规
        List<String> ips = CsvUtils.convertCSVToList(ipAddress);
        if (ips.size() > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
          LOGGER.warn("import file error, limit exceeded");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，IP地址超过" + FpcConstants.HOSTGROUP_MAX_IP_COUNT + "条");
        }
        if (Sets.newHashSet(ips).size() < ips.size()) {
          LOGGER.warn("import file error, ip exist");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，存在重复的IP地址，行号：" + lineNumber + "行");
        }
        for (String ip : ips) {
          if (StringUtils.contains(ip, "-")) {
            String[] ipRange = StringUtils.split(ip, "-");
            if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
                || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
              LOGGER.warn("import file error, ip error, lineNumber:{}, content: {}", lineNumber,
                  line);
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "导入失败，IP地址非法，行号：" + lineNumber + "行");
            }
            if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
                && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4)
                || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V6)
                    && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V6)))) {
              LOGGER.warn("import file error, ip error, lineNumber:{}, content: {}", lineNumber,
                  line);
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "导入失败，IP地址不合法，行号：" + lineNumber + "行");
            }
          } else if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
            LOGGER.warn("import file error, ip error, lineNumber:{}, content: {}", lineNumber,
                line);
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                ip + "格式非法，请输入正确的IP地址，行号：" + lineNumber + "行");
          }
        }
        try {
          checkIpAddressDuplicate(ipAddress, hostGroupDao.queryHostGroups());
        } catch (Exception e) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入文件在" + lineNumber + "行与已有地址段存在冲突。");
        }
        hostGroupDO.setName(name);
        hostGroupDO.setIpAddress(ipAddress);
        hostGroupDO.setDescription(description);
        hostGroupDOList.add(hostGroupDO);
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (CollectionUtils.isEmpty(hostGroupDOList)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件为空，为检索到地址组信息");
    }

    int count = hostGroupDao.saveHostGroups(hostGroupDOList, id);
    LOGGER.info("import file succeess,lineNumber: {}", count);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#saveHostGroup(com.machloop.fpc.manager.appliance.bo.HostGroupBO, java.lang.String)
   */
  @Override
  @Transactional
  public HostGroupBO saveHostGroup(HostGroupBO hostGroupBO, String operatorId) {
    HostGroupDO existName = hostGroupDao.queryHostGroupByName(hostGroupBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已经存在");
    }

    // 检测IP有效性
    checkIpAddressThrowExcption(hostGroupBO.getIpAddress());
    // 检测IP是否和已有IP地址组重复
    checkIpAddressDuplicate(hostGroupBO.getIpAddress(), hostGroupDao.queryHostGroups());

    // 写入数据库
    HostGroupDO hostGroupDO = new HostGroupDO();
    BeanUtils.copyProperties(hostGroupBO, hostGroupDO);
    hostGroupDO.setOperatorId(operatorId);
    hostGroupDO = hostGroupDao.saveOrRecoverHostGroup(hostGroupDO);

    return queryHostGroup(hostGroupDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#updateHostGroup(java.lang.String, com.machloop.fpc.manager.appliance.bo.HostGroupBO, java.lang.String)
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
    checkIpAddressDuplicate(hostGroupBO.getIpAddress(), hostGroupList);

    // 写入数据库
    HostGroupDO hostGroupDO = new HostGroupDO();
    BeanUtils.copyProperties(existHostGroup, hostGroupDO);
    hostGroupDO.setName(hostGroupBO.getName());
    hostGroupDO.setIpAddress(hostGroupBO.getIpAddress());
    hostGroupDO.setDescription(hostGroupBO.getDescription());
    hostGroupDO.setOperatorId(operatorId);
    hostGroupDao.updateHostGroup(hostGroupDO);

    return queryHostGroup(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.HostGroupService#deleteHostGroup(java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public HostGroupBO deleteHostGroup(String id, String operatorId, boolean forceDelete) {
    HostGroupBO hostGroupBO = queryHostGroup(id);

    if (!forceDelete && StringUtils.isBlank(hostGroupBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "地址组不存在");
    }

    hostGroupDao.deleteHostGroup(id, operatorId);

    return hostGroupBO;
  }

  private boolean checkIpAddressThrowExcption(String ipAddress) {
    if (StringUtils.isBlank(ipAddress)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "IP地址不能为空");
    }

    List<String> ips = CsvUtils.convertCSVToList(ipAddress);
    if (ips.size() > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
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

  private void checkIpAddressDuplicate(String pendingIpAddress, List<HostGroupDO> hostGroupList) {
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
        if (pendingIpRange.getT2().contains(existIpRange.getT2())
            || existIpRange.getT2().contains(pendingIpRange.getT2())) {
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

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_HOSTGROUP));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(java.util.Map)
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

    int syncTotalCount = messages.stream().mapToInt(item -> synHostGroup(item)).sum();
    LOGGER.info("current sync hostGroup total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int synHostGroup(Map<String, Object> messageBody) {

    int syncCount = 0;

    String hostGroupInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(hostGroupInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    HostGroupBO hostGroupBO = new HostGroupBO();
    hostGroupBO.setId(hostGroupInCmsId);
    hostGroupBO.setHostGroupInCmsId(hostGroupInCmsId);
    hostGroupBO.setName(MapUtils.getString(messageBody, "name"));
    hostGroupBO.setIpAddress(MapUtils.getString(messageBody, "ipAddress"));
    hostGroupBO.setDescription(CMS_ASSIGNMENT);

    HostGroupBO exist = queryHostGroupByCmsHostGroupId(hostGroupBO.getHostGroupInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcConstants.SYNC_ACTION_ADD:
        case FpcConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateHostGroup(exist.getId(), hostGroupBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                hostGroupBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveHostGroup(hostGroupBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                hostGroupBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcConstants.SYNC_ACTION_DELETE:
          deleteHostGroup(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              hostGroupBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tags, Date beforeTime) {
    return hostGroupDao.queryAssignHostGroups(beforeTime);
  }
}
