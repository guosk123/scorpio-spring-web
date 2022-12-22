package com.machloop.fpc.npm.analysis.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO;
import com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao;
import com.machloop.fpc.npm.analysis.data.AbnormalEventRuleDO;
import com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
@Service
public class AbnormalEventRuleServiceImpl implements AbnormalEventRuleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbnormalEventRuleServiceImpl.class);

  private static final String CSV_TITLE = "事件类型,内容,更新时间\n";
  private static final int CSV_ITEM_COUNT = StringUtils.countMatches(CSV_TITLE, ",") + 1;

  private static final Range<Integer> RANGE_CUSTOM = Range.closedOpen(1, 200);
  private static final Range<Integer> RANGE_DOMAIN = Range.closed(1, 10);
  private static final Range<Integer> RANGE_IP = Range.closed(11, 20);
  private static final Range<Integer> RANGE_JA3 = Range.closed(41, 50);
  private static final int DHCP_IMITATION_SERVER = 205;

  private static final Pattern DOMAIN_PATTERN = Pattern.compile(
      "^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$",
      Pattern.MULTILINE);
  private static final int JA3_LENGTH = 32;
  private static final int MAX_DHCP_IMITATION_SERVER_IPV4_COUNTS = 10;

  @Autowired
  private AbnormalEventRuleDao abnormalEventRuleDao;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#queryAbnormalEventRules(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO)
   */
  @Override
  public Page<AbnormalEventRuleBO> queryAbnormalEventRules(Pageable page,
      AbnormalEventRuleQueryVO queryVO) {
    Page<AbnormalEventRuleDO> abnormalEventRuleDOs = abnormalEventRuleDao
        .queryAbnormalEventRules(page, queryVO);

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type");

    List<AbnormalEventRuleBO> abnormalEventRuleBOs = abnormalEventRuleDOs.getContent().stream()
        .map(abnormalEventRuleDO -> {
          AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
          BeanUtils.copyProperties(abnormalEventRuleDO, abnormalEventRuleBO);
          abnormalEventRuleBO.setTypeText(
              MapUtils.getString(typeDict, String.valueOf(abnormalEventRuleDO.getType()), ""));
          abnormalEventRuleBO
              .setTimestamp(DateUtils.toStringISO8601(abnormalEventRuleDO.getTimestamp()));

          return abnormalEventRuleBO;
        }).collect(Collectors.toList());

    return new PageImpl<>(abnormalEventRuleBOs, page, abnormalEventRuleDOs.getTotalElements());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#queryAbnormalEventRules()
   */
  @Override
  public List<AbnormalEventRuleBO> queryAbnormalEventRules() {
    List<AbnormalEventRuleDO> abnormalEventRules = abnormalEventRuleDao
        .queryAbnormalEventRules(null);

    List<AbnormalEventRuleBO> result = abnormalEventRules.stream().map(abnormalEventRuleDO -> {
      AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
      BeanUtils.copyProperties(abnormalEventRuleDO, abnormalEventRuleBO);
      abnormalEventRuleBO
          .setTimestamp(DateUtils.toStringISO8601(abnormalEventRuleDO.getTimestamp()));

      return abnormalEventRuleBO;
    }).collect(Collectors.toList());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#queryAbnormalEventRule(java.lang.String)
   */
  @Override
  public AbnormalEventRuleBO queryAbnormalEventRule(String id) {
    AbnormalEventRuleDO abnormalEventRuleDO = abnormalEventRuleDao.queryAbnormalEventRule(id);
    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    BeanUtils.copyProperties(abnormalEventRuleDO, abnormalEventRuleBO);

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type");
    abnormalEventRuleBO.setTypeText(
        MapUtils.getString(typeDict, String.valueOf(abnormalEventRuleDO.getType()), ""));
    abnormalEventRuleBO.setTimestamp(DateUtils.toStringISO8601(abnormalEventRuleDO.getTimestamp()));

    return abnormalEventRuleBO;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#saveAbnormalEventRule(com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO, java.lang.String)
   */
  @Override
  public AbnormalEventRuleBO saveAbnormalEventRule(AbnormalEventRuleBO abnormalEventRuleBO,
      String operatorId) {
    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type");
    if (!typeDict.containsKey(String.valueOf(abnormalEventRuleBO.getType()))
        || !RANGE_CUSTOM.contains(abnormalEventRuleBO.getType())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的事件类型");
    }
    checkContent(abnormalEventRuleBO.getType(), abnormalEventRuleBO.getContent());

    AbnormalEventRuleDO abnormalEventRuleDO = new AbnormalEventRuleDO();
    BeanUtils.copyProperties(abnormalEventRuleBO, abnormalEventRuleDO);
    abnormalEventRuleDO.setSource(FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM);
    abnormalEventRuleDO
        .setDescription(StringUtils.defaultIfBlank(abnormalEventRuleBO.getDescription(), ""));
    abnormalEventRuleDO.setOperatorId(operatorId);

    AbnormalEventRuleDO result = abnormalEventRuleDao.saveAbnormalEventRule(abnormalEventRuleDO);
    return queryAbnormalEventRule(result.getId());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#importAbnormalEventRules(org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Override
  public int importAbnormalEventRules(MultipartFile file, String operatorId) {
    LOGGER.info("start to import abnormal event rules.");

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type")
            .entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    List<AbnormalEventRuleDO> abnormalEventRuleDOList = Lists
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

        String typeText = StringUtils.trim(items[0]);
        String content = StringUtils.trim(items[1]);
        String timestamp = StringUtils.trim(items[2]);
        if (StringUtils.isBlank(typeText) || StringUtils.isBlank(content)
            || StringUtils.isBlank(timestamp)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.debug("pass title, line: [{}]", line);
          continue;
        }

        // 校验事件类型
        String type = typeDict.get(typeText);
        if (StringUtils.isBlank(type) || !RANGE_CUSTOM.contains(Integer.parseInt(type))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败，分类错误，行号：" + lineNumber);
        }

        // 校验内容
        checkContent(Integer.parseInt(type), content);

        AbnormalEventRuleDO abnormalEventRuleDO = new AbnormalEventRuleDO();
        abnormalEventRuleDO.setType(Integer.parseInt(type));
        abnormalEventRuleDO.setContent(content);
        abnormalEventRuleDO.setSource(FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM);
        abnormalEventRuleDO.setStatus(Constants.BOOL_YES);
        abnormalEventRuleDO.setDescription("");
        abnormalEventRuleDO.setOperatorId(operatorId);
        abnormalEventRuleDO.setTimestamp(DateUtils.parseYYYYMMDDDate(timestamp, "yyyy/M/d"));
        abnormalEventRuleDOList.add(abnormalEventRuleDO);
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 校验是否没有情报导入成功， 防止数据被清除
    if (abnormalEventRuleDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，无规则可导入");
    }

    // 覆盖更新
    abnormalEventRuleDao
        .deleteAbnormalEventRules(FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM);
    int count = abnormalEventRuleDao.saveAbnormalEventRules(abnormalEventRuleDOList);

    LOGGER.info("success to import abnormal event rules.total: [{}]", count);
    return count;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#exportAbnormalEventRules(java.lang.String)
   */
  @Override
  public List<String> exportAbnormalEventRules(String source) {
    List<AbnormalEventRuleDO> abnormalEventRules = abnormalEventRuleDao
        .queryAbnormalEventRules(source);

    List<String> lines = Lists.newArrayListWithCapacity(abnormalEventRules.size() + 1);

    // 填充表头
    lines.add(CSV_TITLE);

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type");
    for (AbnormalEventRuleDO rule : abnormalEventRules) {
      StringBuilder line = new StringBuilder();
      line.append(StringUtils.strip(typeDict.getOrDefault(String.valueOf(rule.getType()), "")));
      line.append(",");
      line.append(StringUtils.strip(rule.getContent()));
      line.append(",");
      line.append(StringUtils.strip(DateUtils.toStringFormat(rule.getTimestamp(), "yyyy/MM/dd")));
      line.append("\n");
      lines.add(line.toString());
    }

    return lines;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#updateAbnormalEventRule(java.lang.String, com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO, java.lang.String)
   */
  @Override
  public AbnormalEventRuleBO updateAbnormalEventRule(String id,
      AbnormalEventRuleBO abnormalEventRuleBO, String operatorId) {
    AbnormalEventRuleDO exist = abnormalEventRuleDao.queryAbnormalEventRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "异常事件规则不存在");
    }
    if (StringUtils.equals(exist.getSource(), FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_DEFAULT)
        && DHCP_IMITATION_SERVER != exist.getType()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "内置异常事件不支持编辑");
    }
    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_abnormal_event_type");
    if (!typeDict.containsKey(String.valueOf(abnormalEventRuleBO.getType()))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的事件类型");
    }

    checkContent(abnormalEventRuleBO.getType(), abnormalEventRuleBO.getContent());

    AbnormalEventRuleDO abnormalEventRuleDO = new AbnormalEventRuleDO();
    BeanUtils.copyProperties(abnormalEventRuleBO, abnormalEventRuleDO);
    abnormalEventRuleDO.setId(id);
    abnormalEventRuleDO.setOperatorId(operatorId);

    abnormalEventRuleDao.updateAbnormalEventRule(abnormalEventRuleDO);
    return queryAbnormalEventRule(id);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#updateStatus(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public AbnormalEventRuleBO updateStatus(String id, String status, String operatorId) {
    AbnormalEventRuleDO exist = abnormalEventRuleDao.queryAbnormalEventRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "异常事件规则不存在");
    }

    abnormalEventRuleDao.updateStatus(id, status, operatorId);
    return queryAbnormalEventRule(id);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService#deleteAbnormalEventRule(java.lang.String)
   */
  @Override
  public AbnormalEventRuleBO deleteAbnormalEventRule(String id) {
    AbnormalEventRuleDO exist = abnormalEventRuleDao.queryAbnormalEventRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "异常事件规则不存在");
    }
    if (StringUtils.equals(exist.getSource(),
        FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_DEFAULT)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "内置异常事件不可删除");
    }

    abnormalEventRuleDao.deleteAbnormalEventRule(id);

    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    BeanUtils.copyProperties(exist, abnormalEventRuleBO);
    return abnormalEventRuleBO;
  }

  private static void checkContent(int type, String content) {
    if (RANGE_DOMAIN.contains(type) && !DOMAIN_PATTERN.matcher(content).matches()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "无效域名:" + content);
    }
    if (RANGE_IP.contains(type) && !NetworkUtils.isInetAddress(content)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "无效IP:" + content);
    }
    if (RANGE_JA3.contains(type) && content.length() != JA3_LENGTH) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "无效JA3:" + content);
    }
    if (type == DHCP_IMITATION_SERVER) {
      List<String> ipList = CsvUtils.convertCSVToList(content);
      if (ipList.size() > MAX_DHCP_IMITATION_SERVER_IPV4_COUNTS) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "DHCP-假冒服务器最多支持10个IPv4地址");
      }
      for (String ip : ipList) {
        if (!NetworkUtils.isInetAddress(ip, IpVersion.V4)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "DHCP-假冒服务器内存在无效IPv4地址:" + ip);
        }
      }
    }
  }

}
