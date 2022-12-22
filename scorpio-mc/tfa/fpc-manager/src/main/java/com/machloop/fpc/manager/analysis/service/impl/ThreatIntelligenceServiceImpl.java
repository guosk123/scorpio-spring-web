package com.machloop.fpc.manager.analysis.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.bo.ThreatIntelligenceBO;
import com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao;
import com.machloop.fpc.manager.analysis.data.ThreatIntelligenceDO;
import com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
@Service
public class ThreatIntelligenceServiceImpl implements ThreatIntelligenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreatIntelligenceServiceImpl.class);

  private static final String CSV_TITLE = "情报类型,情报信息,威胁类型,更新时间\n";
  private static final int CSV_ITEM_COUNT = StringUtils.countMatches(CSV_TITLE, ",") + 1;

  private static final Set<String> THREAT_CATOGORY_CUSTOM = Sets.newHashSet("suspicious");
  private static final Set<String> THREAT_CATOGORY_OFFICIAL = Sets.newHashSet("dynamic domain",
      "sinkhole redirect ip", "whitelist", "suspicious");

  @Autowired
  private DictManager dictManager;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private ThreatIntelligenceDao intelligenceDao;

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#queryIntelligences(com.machloop.alpha.common.base.page.PageRequest, java.lang.String, java.lang.String)
   */
  @Override
  public Page<ThreatIntelligenceBO> queryIntelligences(Pageable page, String type, String content) {

    Map<String, String> typeDict = dictManager.getBaseDict()
        .getItemMap("analysis_threat_intelligence_type");

    // 仅查询用户自定义方式上传的情报
    Page<ThreatIntelligenceDO> intelligenceDOPage = intelligenceDao.queryIntelligences(page, type,
        content, THREAT_CATOGORY_CUSTOM);
    long totalElem = intelligenceDOPage.getTotalElements();

    List<ThreatIntelligenceBO> intelligenceBOList = Lists
        .newArrayListWithCapacity(intelligenceDOPage.getSize());
    for (ThreatIntelligenceDO intelligenceDO : intelligenceDOPage) {
      ThreatIntelligenceBO intelligenceBO = new ThreatIntelligenceBO();
      BeanUtils.copyProperties(intelligenceDO, intelligenceBO);

      intelligenceBO.setTypeText(
          MapUtils.getString(typeDict, StringUtils.lowerCase(intelligenceDO.getType()), ""));
      intelligenceBO.setTimestamp(DateUtils.toStringISO8601(intelligenceDO.getTimestamp()));

      intelligenceBOList.add(intelligenceBO);
    }

    return new PageImpl<>(intelligenceBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#queryIntelligence(java.lang.String)
   */
  @Override
  public ThreatIntelligenceBO queryIntelligence(String id) {
    Map<String, String> typeDict = dictManager.getBaseDict()
        .getItemMap("analysis_threat_intelligence_type");

    // 仅查询用户自定义方式上传的情报
    ThreatIntelligenceDO intelligenceDO = intelligenceDao.queryIntelligence(id);

    ThreatIntelligenceBO intelligenceBO = new ThreatIntelligenceBO();
    BeanUtils.copyProperties(intelligenceDO, intelligenceBO);

    intelligenceBO.setTypeText(
        MapUtils.getString(typeDict, StringUtils.lowerCase(intelligenceDO.getType()), ""));
    intelligenceBO.setTimestamp(DateUtils.toStringISO8601(intelligenceDO.getTimestamp()));

    return intelligenceBO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#importIntelligences(org.springframework.web.multipart.MultipartFile)
   */
  @Override
  @Transactional
  public synchronized int importIntelligences(MultipartFile file, boolean custom) {
    LOGGER.info("start to import thread intelligence.");

    Map<String, String> typeMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    typeMap.put("domain", "dynamic domain");
    typeMap.put("ip", "sinkhole redirect ip");
    typeMap.put("ja3", "whitelist");

    List<ThreatIntelligenceDO> threatIntelligenceDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        String[] items = StringUtils.split(line, ",");
        if (items.length == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (items.length != CSV_ITEM_COUNT) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        String type = StringUtils.trim(items[0]);
        String content = StringUtils.trim(items[1]);
        String category = StringUtils.trim(items[2]);
        String timestamp = StringUtils.trim(items[3]);
        if (StringUtils.isBlank(type) || StringUtils.isBlank(content)
            || StringUtils.isBlank(category) || StringUtils.isBlank(timestamp)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.debug("pass title, line: [{}]", line);
          continue;
        }

        if (!typeMap.containsKey(type)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，情报类型错误，行号：" + lineNumber);
        }

        if (!StringUtils.equals(category, typeMap.get(type))
            && !THREAT_CATOGORY_CUSTOM.contains(category)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，威胁类型错误，行号：" + lineNumber);
        }

        ThreatIntelligenceDO intelligenceDO = new ThreatIntelligenceDO();
        intelligenceDO.setType(type);
        intelligenceDO.setContent(content);
        intelligenceDO.setThreatCategory(category);
        intelligenceDO.setTimestamp(DateUtils.parseYYYYMMDDDate(timestamp, "yyyy/M/d"));

        intelligenceDO.setDescription("");

        threatIntelligenceDOList.add(intelligenceDO);
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 校验是否没有情报导入成功， 防止数据被清除
    if (threatIntelligenceDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，无规则可导入");
    }

    // 覆盖更新，根据上传类型执行操作
    intelligenceDao.deleteIntelligences(custom ? THREAT_CATOGORY_CUSTOM : THREAT_CATOGORY_OFFICIAL);
    int count = intelligenceDao.saveIntelligences(threatIntelligenceDOList);
    freshVersion();

    LOGGER.info("success to import thread intelligence.total: [{}]", count);
    return count;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#exportIntelligences(java.lang.String, java.lang.String)
   */
  @Override
  public List<String> exportIntelligences(String type, String content) {

    List<ThreatIntelligenceDO> intelligenceDOList = intelligenceDao.queryIntelligences(type,
        content, THREAT_CATOGORY_CUSTOM);

    List<String> lines = Lists.newArrayListWithCapacity(intelligenceDOList.size() + 1);

    // 填充表头
    lines.add(CSV_TITLE);

    for (ThreatIntelligenceDO intelligence : intelligenceDOList) {
      StringBuilder line = new StringBuilder();
      line.append(StringUtils.strip(intelligence.getType()));
      line.append(",");
      line.append(StringUtils.strip(intelligence.getContent()));
      line.append(",");
      line.append(StringUtils.strip(intelligence.getThreatCategory()));
      line.append(",");
      line.append(
          StringUtils.strip(DateUtils.toStringFormat(intelligence.getTimestamp(), "yyyy/MM/dd")));
      line.append("\n");
      lines.add(line.toString());
    }

    return lines;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#updateIntelligence(java.lang.String, com.machloop.fpc.manager.analysis.bo.ThreatIntelligenceBO)
   */
  @Override
  @Transactional
  public ThreatIntelligenceBO updateIntelligence(String id, ThreatIntelligenceBO intelligenceBO) {
    ThreatIntelligenceDO existThreatIntelligence = intelligenceDao.queryIntelligence(id);
    if (StringUtils.isBlank(existThreatIntelligence.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }
    if (!THREAT_CATOGORY_CUSTOM.contains(existThreatIntelligence.getThreatCategory())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }

    ThreatIntelligenceDO intelligenceDO = new ThreatIntelligenceDO();
    BeanUtils.copyProperties(intelligenceBO, intelligenceDO);
    intelligenceDO.setId(id);

    intelligenceDao.updateIntelligence(intelligenceDO);
    freshVersion();

    return queryIntelligence(id);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService#deleteIntelligence(java.lang.String)
   */
  @Override
  @Transactional
  public ThreatIntelligenceBO deleteIntelligence(String id) {
    ThreatIntelligenceBO intelligenceBO = queryIntelligence(id);

    if (StringUtils.isNotBlank(intelligenceBO.getId())) {

      if (!THREAT_CATOGORY_CUSTOM.contains(intelligenceBO.getThreatCategory())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持修改");
      }

      intelligenceDao.deleteIntelligence(intelligenceBO.getId());
      freshVersion();
    }

    return intelligenceBO;
  }

  private void freshVersion() {
    globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_ANALYSIS_INTELLIGENCES_VERSION,
        IdGenerator.generateUUID());
  }
}
