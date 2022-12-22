package com.machloop.fpc.cms.center.appliance.service.impl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.DomainWhiteListBO;
import com.machloop.fpc.cms.center.appliance.dao.DomainWhiteListDao;
import com.machloop.fpc.cms.center.appliance.data.DomainWhiteListDO;
import com.machloop.fpc.cms.center.appliance.service.DomainWhiteListService;
import com.machloop.fpc.cms.center.broker.invoker.FpcManagerInvoker;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.service.CmsService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/9 5:05 PM,cms
 * @version 1.0
 */
@Order(18)
@Service
public class DomainWhiteListServiceImpl
    implements DomainWhiteListService, MQAssignmentService, SyncConfigurationService {
  private static final List<
      String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST);

  @Autowired
  private DomainWhiteListDao domainWhiteListDao;

  @Autowired
  private ApplicationContext context;

  private static final String CSV_TITLE = "`名称`,`域名白名单`,`描述`\n";

  private static final int MAX_SIZE_DOMAIN_WHITE = 10000;

  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainWhiteListServiceImpl.class);

  private static final String PATH_NAME = "/usr/tmp/domain";

  private static final String DEVICE_ONLINE = "0";

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private CmsService cmsService;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

  @Override
  public Page<DomainWhiteListBO> queryDomainWhiteList(PageRequest page, String name,
      String domain) {
    Page<DomainWhiteListDO> domainWhiteListDOPage = domainWhiteListDao.queryDomainWhiteList(page,
        name, domain);
    long totalElements = domainWhiteListDOPage.getTotalElements();

    List<DomainWhiteListBO> domainWhiteListBOList = Lists
        .newArrayListWithCapacity(domainWhiteListDOPage.getSize());
    for (DomainWhiteListDO domainWhiteListDO : domainWhiteListDOPage) {
      DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
      BeanUtils.copyProperties(domainWhiteListDO, domainWhiteListBO);

      domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getCreateTime()));
      domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getUpdateTime()));

      domainWhiteListBOList.add(domainWhiteListBO);
    }

    return new PageImpl<>(domainWhiteListBOList, page, totalElements);
  }

  @Override
  public List<DomainWhiteListBO> queryDomainWhiteList() {
    List<DomainWhiteListDO> domainWhiteListDOList = domainWhiteListDao.queryDomainWhiteList();

    List<DomainWhiteListBO> domainWhiteListBOList = Lists
        .newArrayListWithCapacity(domainWhiteListDOList.size());
    for (DomainWhiteListDO domainWhiteListDO : domainWhiteListDOList) {
      DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
      BeanUtils.copyProperties(domainWhiteListDO, domainWhiteListBO);

      domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getCreateTime()));
      domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getUpdateTime()));

      domainWhiteListBOList.add(domainWhiteListBO);
    }

    return domainWhiteListBOList;
  }

  @Override
  public List<String> exportDomainWhiteList(String name, String domain) {
    List<DomainWhiteListDO> domainWhiteListDOList = domainWhiteListDao
        .queryDomainWhiteListByNameAndDomain(name, domain);
    List<String> result = Lists.newArrayListWithCapacity(domainWhiteListDOList.size() + 1);
    result.add(CSV_TITLE);
    domainWhiteListDOList.forEach(domainWhiteListDO -> {
      String outItem = CsvUtils.spliceRowData(domainWhiteListDO.getName(),
          domainWhiteListDO.getDomain(), domainWhiteListDO.getDescription());
      result.add(outItem);
    });
    return result;
  }

  @Override
  public DomainWhiteListBO queryDomainWhite(String id) {
    DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();

    DomainWhiteListDO domainWhiteListDO = domainWhiteListDao.queryDomainWhiteList(id);
    BeanUtils.copyProperties(domainWhiteListDO, domainWhiteListBO);

    domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getCreateTime()));
    domainWhiteListBO.setCreateTime(DateUtils.toStringISO8601(domainWhiteListDO.getUpdateTime()));

    return domainWhiteListBO;
  }

  @Override
  public DomainWhiteListBO saveDomainWhiteList(DomainWhiteListBO domainWhiteListBO,
      String operatorId) {
    int count = domainWhiteListDao.queryCountDomainWhiteList();
    if (count >= MAX_SIZE_DOMAIN_WHITE) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "域名白名单配置个数最多10000个.");
    }
    DomainWhiteListDO existName = domainWhiteListDao
        .queryDomainWhiteByName(domainWhiteListBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已存在");
    }

    // 检验域名有效性
    checkDomain(domainWhiteListBO.getDomain());

    DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
    BeanUtils.copyProperties(domainWhiteListBO, domainWhiteListDO);
    domainWhiteListDO.setOperatorId(operatorId);
    domainWhiteListDO = domainWhiteListDao.saveOrRecoverDomainWhite(domainWhiteListDO, operatorId);

    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        domainWhiteList2MessageBody(domainWhiteListDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST, null);
    return queryDomainWhite(domainWhiteListDO.getId());
  }

  @Override
  public void importDomainWhiteList(MultipartFile file, String operatorId) {

    // 清除所有配置下发信息
    File issued = new File(PATH_NAME + "/" + "issued");
    deleteDirectoryLegacyIO(issued);

    LOGGER.info("begin to import domainWhiteList, file name :{}", file.getOriginalFilename());
    List<String> existDomainWhiteName = domainWhiteListDao.queryDomainWhiteListName();
    int existCount = existDomainWhiteName.size();

    List<DomainWhiteListDO> domainWhiteListDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<DomainWhiteListDO> domainWhiteListDOS = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

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
        String domain = StringUtils.trim(contents.get(1));
        String description = StringUtils.trim(contents.get(2));
        if (StringUtils.isAnyBlank(name, domain)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，文件内容解析失败，行号：" + lineNumber);
        }

        DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
        if (existDomainWhiteName.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber:{}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，域名白名单名称已存在，行号：" + lineNumber);
        }
        existDomainWhiteName.add(name);

        checkDomain(domain);
        domainWhiteListDO.setName(name);
        domainWhiteListDO.setDomain(domain);
        domainWhiteListDO.setDescription(description);
        domainWhiteListDOList.add(domainWhiteListDO);

        // 减1是为了避免第一行无效信息
        if (lineNumber + existCount - 1 == MAX_SIZE_DOMAIN_WHITE) {
          LOGGER.info("domainWhiteList count up to 1w, save {} domainWhiteList", lineNumber);
          domainWhiteListDOS = domainWhiteListDao.saveDomainWhite(domainWhiteListDOList,
              operatorId);
          break;
        }
      }
    } catch (IOException exception) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (CollectionUtils.isEmpty(domainWhiteListDOList) && lineNumber == 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件为空，未检索到域名白名单信息");
    }

    // 如果上边已经保存，则此处不必执行保存
    if (CollectionUtils.isEmpty(domainWhiteListDOS)) {
      domainWhiteListDOS = domainWhiteListDao.saveDomainWhite(domainWhiteListDOList, operatorId);
    }

    File issuedFile = new File(PATH_NAME + "/" + "issued" + "/" + "domain.csv");
    writeCSVFile(domainWhiteListDOS, issuedFile);

    singleThreadExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // 执行下发命令
        issued(issued);
      }
    });
    LOGGER.info("import file succeess,lineNumber: {}", domainWhiteListDOS.size());
  }

  @Override
  public int importIssuedDomain(MultipartFile file, String id) {
    LOGGER.info("start to import DomainWhiteList.");

    List<String> domainWhiteListInCmsId = domainWhiteListDao.queryDomainWhiteListInCmsId();

    List<DomainWhiteListDO> existDomainWhiteListDO = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<DomainWhiteListDO> domainWhiteListDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        String[] contants = StringUtils.split(line);
        DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
        domainWhiteListDO.setId(contants[0]);
        domainWhiteListDO.setAssignId(contants[0]);
        domainWhiteListDO.setName(contants[1]);
        domainWhiteListDO.setDomain(contants[2]);
        if (contants.length == 3) {
          domainWhiteListDO.setDescription("");
        } else {
          domainWhiteListDO.setDescription(contants[3]);
        }

        if (domainWhiteListInCmsId.contains(domainWhiteListDO.getAssignId())) {
          existDomainWhiteListDO.add(domainWhiteListDO);
        } else {
          domainWhiteListDOList.add(domainWhiteListDO);
        }
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 修改已经存在的域名白名单
    if (!CollectionUtils.isEmpty(existDomainWhiteListDO)) {
      domainWhiteListDao.updateBatchDomainWhiteList(existDomainWhiteListDO, CMS_ASSIGNMENT);
    }

    // 新增已经存在的域名白名单
    if (!CollectionUtils.isEmpty(domainWhiteListDOList)) {
      domainWhiteListDao.saveDomainWhite(domainWhiteListDOList, CMS_ASSIGNMENT);
    }

    return lineNumber;
  }

  @Override
  public DomainWhiteListBO updateDomainWhiteList(String id, DomainWhiteListBO domainWhiteListBO,
      String operatorId) {
    DomainWhiteListDO existName = domainWhiteListDao
        .queryDomainWhiteByName(domainWhiteListBO.getName());
    if (StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(id, existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "名称已存在");
    }

    DomainWhiteListDO existDomainWhiteList = domainWhiteListDao.queryDomainWhiteList(id);
    if (StringUtils.isBlank(existDomainWhiteList.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "域名白名单不存在");
    }

    checkDomain(domainWhiteListBO.getDomain());

    DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
    BeanUtils.copyProperties(existDomainWhiteList, domainWhiteListDO);
    domainWhiteListDO.setName(domainWhiteListBO.getName());
    domainWhiteListDO.setDomain(domainWhiteListBO.getDomain());
    domainWhiteListDO.setDescription(domainWhiteListBO.getDescription());
    domainWhiteListDO.setOperatorId(operatorId);
    domainWhiteListDao.updateDomainWhiteList(domainWhiteListDO);

    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        domainWhiteList2MessageBody(domainWhiteListDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST, null);
    return queryDomainWhite(id);
  }

  @Override
  public DomainWhiteListBO deleteDomainWhite(String id, String operatorId, boolean forceDelete) {
    DomainWhiteListBO domainWhiteListBO = queryDomainWhite(id);

    if (!forceDelete && StringUtils.isBlank(domainWhiteListBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "域名白名单不存在");
    }

    domainWhiteListDao.deleteDomainWhiteList(id, operatorId);

    DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
    domainWhiteListDO.setId(id);

    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        domainWhiteList2MessageBody(domainWhiteListDO, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST, null);
    return domainWhiteListBO;
  }

  @Override
  public List<DomainWhiteListBO> deleteDomainWhiteByNameAndDomain(String name, String domain,
      String operatorId) {
    List<DomainWhiteListDO> domainWhiteListDOList = domainWhiteListDao
        .queryDomainWhiteListByNameAndDomain(name, domain);

    domainWhiteListDao.deleteDOmainWHiteLIstByNameAndDomain(name, domain, operatorId);
    DomainWhiteListDO issuedDomainWhiteListDO = new DomainWhiteListDO();
    issuedDomainWhiteListDO.setName(name);
    issuedDomainWhiteListDO.setDomain(domain);
    List<Map<String, Object>> messageBodys = Lists.newArrayList(domainWhiteList2MessageBody(
        issuedDomainWhiteListDO, FpcCmsConstants.SYNC_ACTION_BATCH_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST, null);

    List<DomainWhiteListBO> domainWhiteListBOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (DomainWhiteListDO domainWhiteListDO : domainWhiteListDOList) {
      DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
      BeanUtils.copyProperties(domainWhiteListDO, domainWhiteListBO);
      domainWhiteListBOList.add(domainWhiteListBO);
    }
    return domainWhiteListBOList;
  }

  private boolean checkDomain(String domain) {

    // 域名正则表达式
    String domainRegex = "^([*]|[a-zA-Z0-9][-a-zA-Z0-9]{0,62})(?:\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";

    if (StringUtils.isBlank(domain)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "域名不能为空");
    }

    String[] domains = StringUtils.split(domain, ",");
    if (domains.length > FpcCmsConstants.MAX_DOMAIN_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "最多支持100个域名");
    }
    if (Sets.newHashSet(domains).size() < domains.length) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "存在重复的域名");
    }

    for (String singleDomain : domains) {
      if (!singleDomain.matches(domainRegex)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "域名不合法，请输入正确的域名");
      }
    }

    return true;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST));
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

    messages.stream().mapToInt(item -> domainWhiteList(item)).sum();
    return 0;
  }

  private int domainWhiteList(Map<String, Object> messageBody) {

    int syncCount = 0;
    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }
    String action = MapUtils.getString(messageBody, "action");
    // 下发的规则与本地规则名称冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    DomainWhiteListDO existName = domainWhiteListDao.queryDomainWhiteByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + "CMS";
    }
    DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
    domainWhiteListBO.setId(assignId);
    domainWhiteListBO.setAssignId(assignId);
    domainWhiteListBO.setName(name);
    domainWhiteListBO.setDomain(MapUtils.getString(messageBody, "domain"));
    domainWhiteListBO.setDescription(MapUtils.getString(messageBody, "description"));

    DomainWhiteListDO exist = domainWhiteListDao
        .queryDomainWhiteListByAssignId(domainWhiteListBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateDomainWhiteList(exist.getId(), domainWhiteListBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveDomainWhiteList(domainWhiteListBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteDomainWhite(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync domainWhiteList status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync failed. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 全部删除
    int clearCount = 0;
    try {
      // 删除所有包含系统内置
      clearCount = domainWhiteListDao.deleteDomainWhiteListAll(onlyLocal, CMS_ASSIGNMENT);
    } catch (Exception e) {
      LOGGER.warn("delete domainWhiteList failed. error msg: {}", e.getMessage());
    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return domainWhiteListDao.queryAssignDomainWhiteListIds(beforeTime);
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

    // 所有下级设备均生效，无需判断seriaNumber
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST,
        domainWhiteListDao.queryDomainWhiteListIds(false));

    return map;
  }

  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {

    File issuedFile = new File(PATH_NAME + "/" + serialNumber);
    deleteDirectoryLegacyIO(issuedFile);

    List<DomainWhiteListDO> domainWhiteListDOS = domainWhiteListDao.queryDomainWhiteList();
    File includeFile = new File(PATH_NAME + "/" + serialNumber + "/" + "domainWhiteList.csv");
    writeCSVFile(domainWhiteListDOS, includeFile);

    singleThreadExecutor.execute(new Runnable() {
      @Override
      public void run() {
        if (StringUtils.isEmpty(deviceType) && StringUtils.isEmpty(serialNumber)) {
          issued(issuedFile);
        } else {
          for (File file : issuedFile.listFiles()) {
            if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
              FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(serialNumber);
              fpcManagerInvoker.importApplication(fpcDO, file, "fpc.manager.domain.rest.address");
              file.delete();
            } else {
              CmsDO cmsDO = cmsDao.queryCmsBySerialNumber(serialNumber);
              fpcManagerInvoker.importApplication(cmsDO, file, "fpc.cms.domain.rest.address");
              file.delete();
            }
          }
        }
      }
    });

    return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
  }

  public static File writeCSVFile(List<DomainWhiteListDO> domainWhiteListDOS, File file) {

    BufferedWriter csvWtriter = null;
    try {
      File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }

      // GB2312使正确读取分隔符","
      csvWtriter = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"), 1024);

      // 写入文件内容 下发时包括规则id和规则状态
      for (DomainWhiteListDO domainWhiteListDO : domainWhiteListDOS) {
        csvWtriter
            .write(StringUtils.join(
                domainWhiteListDO.getId() + " " + domainWhiteListDO.getName() + " "
                    + domainWhiteListDO.getDomain() + " " + domainWhiteListDO.getDescription(),
                "\n"));
      }
      csvWtriter.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        csvWtriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  private void issued(File issuedFile) {
    // 获取当前在线设备
    List<FpcDO> onlineFpcDO = fpcService.queryAllFpc().stream()
        .filter(item -> StringUtils.equals(item.getConnectStatus(), DEVICE_ONLINE)).map(fpcBO -> {
          FpcDO fpcDO = new FpcDO();
          BeanUtils.copyProperties(fpcBO, fpcDO);
          return fpcDO;
        }).collect(Collectors.toList());
    List<CmsDO> onlineCmsDO = cmsService.queryCms(new CmsQueryVO()).stream()
        .filter(item -> StringUtils.equals(item.getConnectStatus(), DEVICE_ONLINE)).map(cmsBO -> {
          CmsDO cmsDO = new CmsDO();
          BeanUtils.copyProperties(cmsBO, cmsDO);
          return cmsDO;
        }).collect(Collectors.toList());

    // 下发当前文件信息
    for (File file : issuedFile.listFiles()) {
      for (FpcDO fpcDO : onlineFpcDO) {
        // 获取所有需要下发的文件
        try {
          fpcManagerInvoker.importApplication(fpcDO, file, "fpc.manager.domain.rest.address");
        } catch (BusinessException exception) {
          LOGGER.info("File delivery failure, " + fpcDO.getName());
          continue;
        }
      }
      file.delete();
    }
    for (File file : issuedFile.listFiles()) {
      for (CmsDO cmsDO : onlineCmsDO) {
        // 获取所有需要下发的文件
        try {
          fpcManagerInvoker.importApplication(cmsDO, file, "fpc.cms.domain.rest.address");
        } catch (BusinessException exception) {
          LOGGER.info("File delivery failure, " + cmsDO.getName());
          continue;
        }
      }
      file.delete();
    }
  }

  private Map<String, Object> domainWhiteList2MessageBody(DomainWhiteListDO domainWhiteListDO,
      String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", domainWhiteListDO.getId());
    map.put("name", domainWhiteListDO.getName());
    map.put("domain", domainWhiteListDO.getDomain());
    map.put("description", domainWhiteListDO.getDescription());
    map.put("action", action);

    return map;
  }

  // 删除指定目录下所有文件
  private void deleteDirectoryLegacyIO(File file) {

    // 获取指定目录下所有文件
    File[] list = file.listFiles();
    if (list != null) {
      for (File item : list) {
        // 删除指定目录下文件
        item.delete();
      }
    }
  }
}
