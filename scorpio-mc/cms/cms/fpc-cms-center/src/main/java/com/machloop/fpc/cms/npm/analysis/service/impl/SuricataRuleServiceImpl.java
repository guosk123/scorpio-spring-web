package com.machloop.fpc.cms.npm.analysis.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
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
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleBO;
import com.machloop.fpc.cms.npm.analysis.dao.MitreAttackDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleClasstypeDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleDao;
import com.machloop.fpc.cms.npm.analysis.data.MitreAttackDO;
import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleClasstypeDO;
import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleDO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataRuleService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/10 2:50 PM,cms
 * @version 1.0
 */
@Order(17)
@Service
public class SuricataRuleServiceImpl
    implements SuricataRuleService, MQAssignmentService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuricataRuleServiceImpl.class);

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SURICATA);
  private static final String INTERNAL_SOURCE = "0";
  private static final String CSV_TITLE = "`规则ID`,`动作`,`协议`,`源IP（组）`,`源端口（组）`,`方向`,`目的IP（组）`,`目的端口（组）`,`描述信息`,`规则正文`,`优先级`,`规则分类`,`战术分类`,`技术分类`,`CVE编号`,`CNNVD编号`,`严重级别`,`受害方`,`告警频率`\n";
  private static final int DEFAULT_PRIORITY = 3;
  private static final String DEFAULT_CLASSTYPE = "0";
  private static final String DEFAULT_SIGNATURE_SEVERITY = "2";
  private static final String DEFAULT_ATTACK = "0";
  private static final List<String> EN_SIGNATUE_SEVERITY = Lists.newArrayList("Critical", "Major",
      "Minor", "Informational", "Audit");
  private static final String DEFAULT_TARGET = "dest_ip";
  private static final String DEFAULT_SOURCE = "1";
  private static final int BATCH_SIZE = 1000;
  private static final int MAX_SIZE = 10000;
  private static final int ISSUED_MAX_SIZE = 100000;
  private static final List<String> DIRECTION = Lists.newArrayList("->", "<>");
  private static final Range<Integer> RANGE_PRIORITY = Range.closed(1, 255);
  private static final Pattern MSG_PATTERN = Pattern.compile("msg:\"(.*?)\";", Pattern.MULTILINE);
  private static final String PATH_NAME = "/usr/tmp";
  private static final String PATH_ISSUED_NAME = "/usr/tmp/issued";
  private static final String FILE_NAME = "Rule.rules";

  private static boolean IS_RUNNING_ISSUED = false;
  private static final String SUCCESS_ISSUED = "0";
  private static final String DEVICE_ONLINE = "0";

  private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

  private static final List<String> RULE_FIELDS = Lists.newArrayList("sid", "action", "protocol",
      "src_ip", "src_port", "direction", "dest_ip", "dest_port", "msg", "rev", "priority",
      "classtype_id", "mitre_tactic_id", "mitre_technique_id", "cve", "cnnvd", "signature_severity",
      "target", "threshold", "source");
  private static final String DEFAULT_MITRETACTICID = "0";

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  @Autowired
  private SuricataRuleDao suricataRuleDao;

  @Autowired
  private MitreAttackDao mitreAttackDao;

  @Autowired
  private SuricataRuleClasstypeDao suricataRuleClasstypeDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private CmsService cmsService;

  @Override
  public Page<SuricataRuleBO> querySuricataRules(PageRequest page, SuricataRuleQueryVO queryVO) {
    Page<SuricataRuleDO> suricataRules = suricataRuleDao.querySuricataRules(page, queryVO);

    Map<String, String> parseState = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_rule_parse_state");
    Map<String,
        String> source = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_source");
    Map<String, String> signatureSeverity = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_signature_severity");

    List<SuricataRuleBO> list = suricataRules.getContent().stream().map(suricataRuleDO -> {
      SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
      BeanUtils.copyProperties(suricataRuleDO, suricataRuleBO);
      suricataRuleBO.setSignatureSeverityText(
          MapUtils.getString(signatureSeverity, suricataRuleBO.getSignatureSeverity()));
      suricataRuleBO
          .setParseStateText(MapUtils.getString(parseState, suricataRuleBO.getParseState(), ""));
      suricataRuleBO.setSourceText(MapUtils.getString(source, suricataRuleBO.getSource(), ""));
      suricataRuleBO.setCreateTime(DateUtils.toStringISO8601(suricataRuleDO.getCreateTime()));
      suricataRuleBO.setUpdateTime(DateUtils.toStringISO8601(suricataRuleDO.getUpdateTime()));

      return suricataRuleBO;
    }).collect(Collectors.toList());

    return new PageImpl<SuricataRuleBO>(list, page, suricataRules.getTotalElements());
  }

  @Override
  public Map<String, String> queryRuleSource() {
    Map<String,
        String> sourceDict = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_source");

    // 获取所有来源
    Set<String> ruleSources = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    ruleSources.addAll(suricataRuleDao.queryRuleSource());
    ruleSources.addAll(sourceDict.keySet());

    return ruleSources.stream()
        .collect(Collectors.toMap(item -> item, item -> sourceDict.getOrDefault(item, item)));
  }

  @Override
  public SuricataRuleBO querySuricataRule(int sid) {
    Map<String, String> parseState = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_rule_parse_state");
    Map<String,
        String> source = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_source");
    Map<String, String> signatureSeverity = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_signature_severity");

    SuricataRuleDO suricataRuleDO = suricataRuleDao.querySuricataRule(sid);
    SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
    BeanUtils.copyProperties(suricataRuleDO, suricataRuleBO);
    suricataRuleBO.setSignatureSeverityText(
        MapUtils.getString(signatureSeverity, suricataRuleBO.getSignatureSeverity()));
    suricataRuleBO
        .setParseStateText(MapUtils.getString(parseState, suricataRuleBO.getParseState(), ""));
    suricataRuleBO.setSourceText(MapUtils.getString(source, suricataRuleBO.getSource(), ""));
    suricataRuleBO.setCreateTime(DateUtils.toStringISO8601(suricataRuleDO.getCreateTime()));
    suricataRuleBO.setUpdateTime(DateUtils.toStringISO8601(suricataRuleDO.getUpdateTime()));

    return suricataRuleBO;
  }

  @Override
  public void exportSuricataRules(SuricataRuleQueryVO queryVO, List<String> sids,
      ServletOutputStream out) throws IOException {
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    int offset = 0;

    List<Integer> sidList = suricataRuleDao.querySuricataRuleIds(queryVO);
    List<String> list = null;
    while (offset % batchSize == 0) {
      if (sids.isEmpty()) {
        List<Integer> skipSids = sidList.stream().skip(offset).limit(batchSize)
            .collect(Collectors.toList());
        if (skipSids.size() == 0) {
          break;
        }
        list = suricataRuleDao.querySuricataRule(skipSids);
      } else {
        List<Integer> intSids = sids.stream().map(sid -> Integer.parseInt(sid))
            .collect(Collectors.toList());
        intSids = suricataRuleDao.querySuricataRulesBySids(intSids).stream()
            .filter(item -> !StringUtils.equals(item.getSource(), INTERNAL_SOURCE))
            .map(SuricataRuleDO::getSid).collect(Collectors.toList());
        if (intSids.size() == 0) {
          break;
        }
        list = suricataRuleDao.querySuricataRule(intSids);
      }
      for (String line : list) {
        out.write(StringUtils.join(line, "\n").getBytes(StandardCharsets.UTF_8));
      }

      offset += list.size();
    }
  }

  @Transactional
  @Override
  public synchronized int importSuricataRules(MultipartFile file, String classtypeId, String source,
      String operatorId) {
    LOGGER.info("start to import suricata rules.");

    File fileImportPath = new File(PATH_ISSUED_NAME);
    // 判断是否正在下发
    if (IS_RUNNING_ISSUED) {
      LOGGER.warn("rule is running issued.");
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "规则正在下发.");
    } else {
      // 不下发则删除可能留存的文件
      deleteDirectoryLegacyIO(fileImportPath);
    }

    Map<Integer, Tuple4<Integer, String, Date, String>> suricataRuleSid = suricataRuleDao
        .querySuricataRuleTuple4();

    Map<String,
        String> classtypes = suricataRuleClasstypeDao.querySuricataRuleClasstypes().stream()
            .collect(
                Collectors.toMap(SuricataRuleClasstypeDO::getName, SuricataRuleClasstypeDO::getId));
    List<String> mitreAttackIds = mitreAttackDao.queryMitreAttacks().stream()
        .map(MitreAttackDO::getId).collect(Collectors.toList());
    Map<String,
        String> actionMap = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_action");
    Map<String, String> signatureSeverityDict = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_signature_severity");

    if (StringUtils.isNotBlank(classtypeId) && !classtypes.containsValue(classtypeId)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "导入失败, 不合法的分类: " + classtypeId);
    }

    List<SuricataRuleDO> newSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleDO> existSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 新生成的规则和规则ID 用来区分旧ID
    List<SuricataRuleDO> issuedNewSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleDO> issuedExistSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Set<Integer> newSids = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    int itemCount = StringUtils.countMatches(CSV_TITLE, ",") + 1;
    String line = "";
    boolean isSplit = true;
    int lineNumber = 0;
    int newCount = 0;
    int modifyCount = 0;
    int offset = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        offset += 1;
        File fileImport = new File(
            PATH_ISSUED_NAME + File.separator + offset / ISSUED_MAX_SIZE + FILE_NAME);
        if (existSuricataRuleList.size() % BATCH_SIZE == 0
            && CollectionUtils.isNotEmpty(existSuricataRuleList)) {
          LOGGER.info(
              "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
          removeNotUpdateByTuple4(existSuricataRuleList, suricataRuleSid);
          if (!existSuricataRuleList.isEmpty()) {
            suricataRuleDao.deleteSuricataRule(existSuricataRuleList.stream()
                .map(SuricataRuleDO::getSid).collect(Collectors.toList()));
            issuedExistSuricataRuleList = suricataRuleDao.saveSuricataRules(existSuricataRuleList);
            modifyCount += existSuricataRuleList.size();
          }

          writeCSVFile(issuedExistSuricataRuleList, fileImport);
          // 防止全部重复导致保存有上一次遗留数据问题
          issuedExistSuricataRuleList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          existSuricataRuleList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        }
        if (newSuricataRuleList.size() % BATCH_SIZE == 0
            && CollectionUtils.isNotEmpty(newSuricataRuleList)) {
          LOGGER.info(
              "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
          issuedNewSuricataRuleList = suricataRuleDao.saveSuricataRules(newSuricataRuleList);
          newCount += newSuricataRuleList.size();
          writeCSVFile(issuedNewSuricataRuleList, fileImport);
          newSuricataRuleList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        }
        lineNumber++;

        if (lineNumber == 1) {
          isSplit = CsvUtils.splitRowData(line).size() == itemCount ? true : false;

          if (isSplit) {
            // 跳过首行
            continue;
          }
        }

        SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
        if (isSplit) {
          List<String> items = CsvUtils.splitRowData(line);
          if (items.size() == 0) {
            LOGGER.info("skip blank line, line number: [{}]", lineNumber);
            continue;
          } else if (items.size() != itemCount) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
          }

          String sid = StringUtils.trim(items.get(0));
          suricataRuleDO.setSid(StringUtils.isNotBlank(sid) ? Integer.parseInt(sid) : 0);
          suricataRuleDO.setAction(StringUtils.trim(items.get(1)));
          suricataRuleDO.setProtocol(StringUtils.trim(items.get(2)));
          suricataRuleDO.setSrcIp(StringUtils.trim(items.get(3)));
          suricataRuleDO.setSrcPort(StringUtils.trim(items.get(4)));
          suricataRuleDO.setDirection(StringUtils.trim(items.get(5)));
          suricataRuleDO.setDestIp(StringUtils.trim(items.get(6)));
          suricataRuleDO.setDestPort(StringUtils.trim(items.get(7)));
          suricataRuleDO.setMsg(StringUtils.trim(items.get(8)));
          suricataRuleDO.setContent(StringUtils.trim(items.get(9)));
          String priority = StringUtils.trim(items.get(10));
          suricataRuleDO.setPriority(
              StringUtils.isNotBlank(priority) ? Integer.parseInt(priority) : DEFAULT_PRIORITY);
          if (StringUtils.isNotBlank(classtypeId)) {
            suricataRuleDO.setClasstypeId(classtypeId);
          } else {
            String classtypeName = items.get(11);
            if (StringUtils.isNotBlank(classtypeName) && !classtypes.containsKey(classtypeName)) {
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "导入失败, 规则分类不存在，行号：" + lineNumber + ", 分类名称: " + classtypeName);
            }
            suricataRuleDO.setClasstypeId(
                StringUtils.isNotBlank(classtypeName) ? classtypes.get(classtypeName)
                    : DEFAULT_CLASSTYPE);
          }
          String mitreTacticId = StringUtils.trim(items.get(12));
          suricataRuleDO
              .setMitreTacticId(StringUtils.defaultIfBlank(mitreTacticId, DEFAULT_ATTACK));
          suricataRuleDO.setMitreTechniqueId(StringUtils.trim(items.get(13)));
          suricataRuleDO.setCve(StringUtils.trim(items.get(14)));
          suricataRuleDO.setCnnvd(StringUtils.trim(items.get(15)));
          String signatureSeverity = StringUtils.trim(items.get(16));
          signatureSeverity = StringUtils.isNotBlank(signatureSeverity) ? signatureSeverity
              : DEFAULT_SIGNATURE_SEVERITY;
          if (!signatureSeverityDict.containsKey(signatureSeverity)) {
            if (EN_SIGNATUE_SEVERITY.contains(signatureSeverity)) {
              signatureSeverity = String.valueOf(EN_SIGNATUE_SEVERITY.indexOf(signatureSeverity));
            } else {
              signatureSeverity = DEFAULT_SIGNATURE_SEVERITY;
            }
          }
          suricataRuleDO.setSignatureSeverity(signatureSeverity);
          suricataRuleDO.setTarget(
              StringUtils.defaultIfBlank(StringUtils.trim(items.get(17)), DEFAULT_TARGET));
          suricataRuleDO.setThreshold(StringUtils.trim(items.get(18)));
          suricataRuleDO.setParseState(Constants.BOOL_NO);
          suricataRuleDO.setState(Constants.BOOL_YES);
          suricataRuleDO.setSource(StringUtils.defaultIfBlank(source, DEFAULT_SOURCE));
          suricataRuleDO.setOperatorId(operatorId);
        } else {
          if (StringUtils.isBlank(line) || StringUtils.startsWith(line, "#")) {
            continue;
          }

          SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
          parseSuricataRule(line, suricataRuleBO, lineNumber);
          String signatureSeverity = suricataRuleBO.getSignatureSeverity();
          if (!signatureSeverityDict.containsKey(signatureSeverity)) {
            if (EN_SIGNATUE_SEVERITY.contains(signatureSeverity)) {
              signatureSeverity = String.valueOf(EN_SIGNATUE_SEVERITY.indexOf(signatureSeverity));
            } else {
              signatureSeverity = DEFAULT_SIGNATURE_SEVERITY;
            }
          }
          suricataRuleBO.setSignatureSeverity(signatureSeverity);
          BeanUtils.copyProperties(suricataRuleBO, suricataRuleDO);
          suricataRuleDO.setParseState(Constants.BOOL_NO);
          suricataRuleDO.setState(Constants.BOOL_YES);
          suricataRuleDO.setClasstypeId(
              StringUtils.defaultIfBlank(classtypeId, suricataRuleDO.getClasstypeId()));
          suricataRuleDO.setSource(StringUtils.defaultIfBlank(source,
              StringUtils.defaultIfBlank(suricataRuleBO.getSource(), DEFAULT_SOURCE)));
          suricataRuleDO.setOperatorId(operatorId);
        }

        if (suricataRuleDO.getSid() <= 0 || StringUtils.isBlank(suricataRuleDO.getAction())
            || StringUtils.isBlank(suricataRuleDO.getProtocol())
            || StringUtils.isBlank(suricataRuleDO.getSrcIp())
            || StringUtils.isBlank(suricataRuleDO.getSrcPort())
            || StringUtils.isBlank(suricataRuleDO.getDirection())
            || StringUtils.isBlank(suricataRuleDO.getDestIp())
            || StringUtils.isBlank(suricataRuleDO.getDestPort())
            || StringUtils.isBlank(suricataRuleDO.getMsg())) {
          LOGGER.warn("missing content: {}", suricataRuleDO.toString());
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误,必选内容缺失, 行号: " + lineNumber);
        }

        // 校验内容
        if (!actionMap.containsKey(suricataRuleDO.getAction())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 规则动作不合法, 行号: " + lineNumber + ", 动作: " + suricataRuleDO.getAction());
        }
        if (!DIRECTION.contains(suricataRuleDO.getDirection())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 方向不合法, 行号: " + lineNumber + ", 方向: " + suricataRuleDO.getDirection());
        }
        if (!RANGE_PRIORITY.contains(suricataRuleDO.getPriority())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 优先级不合法, 行号: " + lineNumber + ", 优先级: " + suricataRuleDO.getPriority());
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getSignatureSeverity())
            && !signatureSeverityDict.containsKey(suricataRuleDO.getSignatureSeverity())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "导入失败, 严重级别不合法, 行号: "
              + lineNumber + ", 严重级别: " + suricataRuleDO.getSignatureSeverity());
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getMitreTacticId())
            && !mitreAttackIds.contains(suricataRuleDO.getMitreTacticId())) {
          // 战术分类不支持时，跳过
          LOGGER.warn("not support mitre tactic id: {}, skip.", suricataRuleDO.getMitreTacticId());
          continue;
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getMitreTechniqueId())
            && !mitreAttackIds.contains(suricataRuleDO.getMitreTechniqueId())) {
          // 技术分类不支持时，跳过
          LOGGER.warn("not support mitre technique id: {}, skip.",
              suricataRuleDO.getMitreTechniqueId());
          continue;
        }

        if (suricataRuleSid.containsKey(suricataRuleDO.getSid())) {
          suricataRuleDO.setRev(suricataRuleSid.get(suricataRuleDO.getSid()).getT1() + 1);
          suricataRuleDO.setState(suricataRuleSid.get(suricataRuleDO.getSid()).getT4());
          combinationSuricataRule(suricataRuleDO);
          existSuricataRuleList.add(suricataRuleDO);
        } else if (!newSids.contains(suricataRuleDO.getSid())) {
          suricataRuleDO.setRev(1);
          combinationSuricataRule(suricataRuleDO);
          newSuricataRuleList.add(suricataRuleDO);

          newSids.add(suricataRuleDO.getSid());
        }
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 校验是否没有情报导入成功， 防止数据被清除
    if (newSuricataRuleList.isEmpty() && existSuricataRuleList.isEmpty() && newCount == 0
        && modifyCount == 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，无规则可导入，或规则所属战术/技术分类不支持");
    }

    // 剩余文件
    File fileExImport = new File(
        PATH_ISSUED_NAME + File.separator + offset / ISSUED_MAX_SIZE + FILE_NAME);
    if (CollectionUtils.isNotEmpty(newSuricataRuleList)) {
      LOGGER.info(
          "the file content is parsed successfully, and writing to {} pieces of data begins.",
          newSuricataRuleList.size());
      issuedNewSuricataRuleList = suricataRuleDao.saveSuricataRules(newSuricataRuleList);
      newCount += newSuricataRuleList.size();
      writeCSVFile(issuedNewSuricataRuleList, fileExImport);
    }

    if (CollectionUtils.isNotEmpty(existSuricataRuleList)) {
      LOGGER.info(
          "the file content is parsed successfully, and writing to {} pieces of data begins.",
          existSuricataRuleList.size());
      removeNotUpdateByTuple4(existSuricataRuleList, suricataRuleSid);
      if (!existSuricataRuleList.isEmpty()) {
        suricataRuleDao.deleteSuricataRule(existSuricataRuleList.stream()
            .map(SuricataRuleDO::getSid).collect(Collectors.toList()));
        issuedExistSuricataRuleList = suricataRuleDao.saveSuricataRules(existSuricataRuleList);
        modifyCount += existSuricataRuleList.size();
      }
      writeCSVFile(issuedExistSuricataRuleList, fileExImport);
    }

    LOGGER.info("success to import suricata rules. save: [{}], update: [{}]", newCount,
        modifyCount);

    singleThreadExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // 执行下发命令
        IS_RUNNING_ISSUED = true;
        issued(fileImportPath);
        IS_RUNNING_ISSUED = false;
      }
    });

    // 下发到直属fpc和cms
    assignmentConfiguration(MQMessageHelper.EMPTY, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);

    return newCount + modifyCount;
  }

  @Override
  public int importIssuedSuricataRules(MultipartFile file, String classtypeId, String source,
      String operatorId, boolean isEngine) {
    LOGGER.info("start to import suricata rules.");

    File fileImportPath = new File(PATH_ISSUED_NAME);
    // 判断是否正在下发
    if (IS_RUNNING_ISSUED) {
      LOGGER.warn("rule is running issued.");
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "规则正在下发.");
    } else {
      // 不下发则删除可能留存的文件
      deleteDirectoryLegacyIO(fileImportPath);
    }

    // 使用tuple4来进行存储
    Map<Integer, Tuple4<Integer, String, Date, String>> suricataRuleSid = suricataRuleDao
        .querySuricataRuleTuple4();
    Map<String,
        String> classtypes = suricataRuleClasstypeDao.querySuricataRuleClasstypes().stream()
            .collect(
                Collectors.toMap(SuricataRuleClasstypeDO::getName, SuricataRuleClasstypeDO::getId));
    List<String> mitreAttackIds = mitreAttackDao.queryMitreAttacks().stream()
        .map(MitreAttackDO::getId).collect(Collectors.toList());
    Map<String,
        String> actionMap = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_action");
    Map<String, String> signatureSeverityDict = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_signature_severity");

    if (StringUtils.isNotBlank(classtypeId) && !classtypes.containsValue(classtypeId)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "导入失败, 不合法的分类: " + classtypeId);
    }

    List<SuricataRuleDO> newSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleDO> existSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 新生成的规则和规则ID 用来区分旧ID
    List<SuricataRuleDO> issuedNewSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleDO> issuedExistSuricataRuleList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Set<Integer> newSids = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    String line = "";
    int lineNumber = 0;
    int newCount = 0;
    int modifyCount = 0;
    int offset = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        offset += 1;
        File fileImport = new File(
            PATH_ISSUED_NAME + File.separator + offset / ISSUED_MAX_SIZE + FILE_NAME);
        if (existSuricataRuleList.size() % BATCH_SIZE == 0) {
          if (CollectionUtils.isNotEmpty(existSuricataRuleList)) {
            LOGGER.info(
                "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
            removeNotUpdateByTuple4(existSuricataRuleList, suricataRuleSid);
            if (!existSuricataRuleList.isEmpty()) {
              suricataRuleDao.deleteSuricataRule(existSuricataRuleList.stream()
                  .map(SuricataRuleDO::getSid).collect(Collectors.toList()));
              issuedExistSuricataRuleList = suricataRuleDao
                  .saveSuricataRules(existSuricataRuleList);
              modifyCount += existSuricataRuleList.size();
            }
            writeCSVFile(issuedExistSuricataRuleList, fileImport);
            // 防止全部都存在，不进入判断
            issuedExistSuricataRuleList = Lists
                .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
            existSuricataRuleList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          }
        }
        if (newSuricataRuleList.size() % BATCH_SIZE == 0) {
          if (CollectionUtils.isNotEmpty(newSuricataRuleList)) {
            LOGGER.info(
                "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
            issuedNewSuricataRuleList = suricataRuleDao.saveSuricataRules(newSuricataRuleList);
            newCount += newSuricataRuleList.size();
            writeCSVFile(issuedNewSuricataRuleList, fileImport);
            newSuricataRuleList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          }
        }

        SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
        if (StringUtils.isBlank(line) || StringUtils.startsWith(line, "#")) {
          continue;
        }

        SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
        parseSuricataRule(line, suricataRuleBO, lineNumber);
        String signatureSeverity = suricataRuleBO.getSignatureSeverity();
        if (!signatureSeverityDict.containsKey(signatureSeverity)) {
          if (EN_SIGNATUE_SEVERITY.contains(signatureSeverity)) {
            signatureSeverity = String.valueOf(EN_SIGNATUE_SEVERITY.indexOf(signatureSeverity));
          } else {
            signatureSeverity = DEFAULT_SIGNATURE_SEVERITY;
          }
        }
        suricataRuleBO.setSignatureSeverity(signatureSeverity);
        BeanUtils.copyProperties(suricataRuleBO, suricataRuleDO);
        suricataRuleDO.setParseState(Constants.BOOL_NO);
        // 新创建时保存的状态规则
        suricataRuleDO.setState(Constants.BOOL_YES);
        suricataRuleDO.setClasstypeId(
            StringUtils.defaultIfBlank(classtypeId, suricataRuleDO.getClasstypeId()));
        suricataRuleDO.setSource(StringUtils.defaultIfBlank(source,
            StringUtils.defaultIfBlank(suricataRuleBO.getSource(), DEFAULT_SOURCE)));
        suricataRuleDO.setOperatorId(operatorId);
        if (suricataRuleDO.getSid() <= 0 || StringUtils.isBlank(suricataRuleDO.getAction())
            || StringUtils.isBlank(suricataRuleDO.getProtocol())
            || StringUtils.isBlank(suricataRuleDO.getSrcIp())
            || StringUtils.isBlank(suricataRuleDO.getSrcPort())
            || StringUtils.isBlank(suricataRuleDO.getDirection())
            || StringUtils.isBlank(suricataRuleDO.getDestIp())
            || StringUtils.isBlank(suricataRuleDO.getDestPort())
            || StringUtils.isBlank(suricataRuleDO.getMsg())) {
          LOGGER.warn("missing content: {}", suricataRuleDO.toString());
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误,必选内容缺失, 行号: " + lineNumber);
        }

        // 校验内容
        if (!actionMap.containsKey(suricataRuleDO.getAction())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 规则动作不合法, 行号: " + lineNumber + ", 动作: " + suricataRuleDO.getAction());
        }
        if (!DIRECTION.contains(suricataRuleDO.getDirection())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 方向不合法, 行号: " + lineNumber + ", 方向: " + suricataRuleDO.getDirection());
        }
        if (!RANGE_PRIORITY.contains(suricataRuleDO.getPriority())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 优先级不合法, 行号: " + lineNumber + ", 优先级: " + suricataRuleDO.getPriority());
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getSignatureSeverity())
            && !signatureSeverityDict.containsKey(suricataRuleDO.getSignatureSeverity())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "导入失败, 严重级别不合法, 行号: "
              + lineNumber + ", 严重级别: " + suricataRuleDO.getSignatureSeverity());
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getMitreTacticId())
            && !mitreAttackIds.contains(suricataRuleDO.getMitreTacticId())) {
          // 战术分类不支持时，跳过
          LOGGER.warn("not support mitre tactic id: {}, skip.", suricataRuleDO.getMitreTacticId());
          continue;
        }
        if (StringUtils.isNotBlank(suricataRuleDO.getMitreTechniqueId())
            && !mitreAttackIds.contains(suricataRuleDO.getMitreTechniqueId())) {
          // 技术分类不支持时，跳过
          LOGGER.warn("not support mitre technique id: {}, skip.",
              suricataRuleDO.getMitreTechniqueId());
          continue;
        }

        if (suricataRuleSid.containsKey(suricataRuleDO.getSid())) {
          suricataRuleDO.setRev(suricataRuleSid.get(suricataRuleDO.getSid()).getT1() + 1);
          suricataRuleDO.setState(suricataRuleSid.get(suricataRuleDO.getSid()).getT4());
          combinationSuricataRule(suricataRuleDO);
          existSuricataRuleList.add(suricataRuleDO);
        } else if (!newSids.contains(suricataRuleDO.getSid())) {
          suricataRuleDO.setRev(1);
          combinationSuricataRule(suricataRuleDO);
          newSuricataRuleList.add(suricataRuleDO);

          newSids.add(suricataRuleDO.getSid());
        }
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    // 校验是否没有情报导入成功， 防止数据被清除 如果为引擎端写入的话，则不抛出错误
    if (!isEngine && newSuricataRuleList.isEmpty() && existSuricataRuleList.isEmpty()
        && newCount == 0 && modifyCount == 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，无规则可导入，或规则所属战术/技术分类不支持");
    }

    // 剩余文件
    File fileExImport = new File(
        PATH_ISSUED_NAME + File.separator + offset % ISSUED_MAX_SIZE + FILE_NAME);
    if (CollectionUtils.isNotEmpty(newSuricataRuleList)) {
      LOGGER.info(
          "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
      issuedNewSuricataRuleList = suricataRuleDao.saveSuricataRules(newSuricataRuleList);
      newCount += newSuricataRuleList.size();
      writeCSVFile(issuedNewSuricataRuleList, fileExImport);
    }

    if (CollectionUtils.isNotEmpty(existSuricataRuleList)) {
      LOGGER.info(
          "the file content is parsed successfully, and writing to 1000 pieces of data begins.");
      removeNotUpdateByTuple4(existSuricataRuleList, suricataRuleSid);
      if (!existSuricataRuleList.isEmpty()) {
        suricataRuleDao.deleteSuricataRule(existSuricataRuleList.stream()
            .map(SuricataRuleDO::getSid).collect(Collectors.toList()));
        issuedExistSuricataRuleList = suricataRuleDao.saveSuricataRules(existSuricataRuleList);
        modifyCount += existSuricataRuleList.size();
      }
      writeCSVFile(issuedExistSuricataRuleList, fileExImport);
    }

    // 判断是否为引擎端写入，如果为引擎端写入，则使用同步下发，如果为页面导入，则使用异步下发
    if (isEngine) {
      // 引擎端写入
      IS_RUNNING_ISSUED = true;
      issued(fileImportPath);
      IS_RUNNING_ISSUED = false;
    } else {
      // 页面导入
      singleThreadExecutor.execute(new Runnable() {
        @Override
        public void run() {
          // 执行下发命令
          IS_RUNNING_ISSUED = true;
          issued(fileImportPath);
          IS_RUNNING_ISSUED = false;
        }
      });
    }

    LOGGER.info("success to import suricata rules. save: [{}], update: [{}]", newCount,
        modifyCount);
    return newCount + modifyCount;
  }

  @Override
  public SuricataRuleBO saveSuricataRule(SuricataRuleBO suricataRuleBO, String operatorId) {
    if (StringUtils.isNotBlank(suricataRuleBO.getRule())) {
      parseSuricataRule(suricataRuleBO.getRule(), suricataRuleBO, 0);
    }

    SuricataRuleDO exist = suricataRuleDao.querySuricataRule(suricataRuleBO.getSid());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "规则ID已存在");
    }

    if (!DIRECTION.contains(suricataRuleBO.getDirection())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "方向不合法");
    }
    if (suricataRuleBO.getPriority() != null
        && !RANGE_PRIORITY.contains(suricataRuleBO.getPriority())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "优先级不合法");
    }
    if (StringUtils.isNotBlank(suricataRuleBO.getSignatureSeverity())) {
      Map<String, String> signatureSeverity = dictManager.getBaseDict()
          .getItemMap("analysis_suricata_signature_severity");
      if (!signatureSeverity.containsKey(suricataRuleBO.getSignatureSeverity())
          && !EN_SIGNATUE_SEVERITY.contains(suricataRuleBO.getSignatureSeverity())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "严重级别不合法");
      }

      if (EN_SIGNATUE_SEVERITY.contains(suricataRuleBO.getSignatureSeverity())) {
        suricataRuleBO.setSignatureSeverity(
            String.valueOf(EN_SIGNATUE_SEVERITY.indexOf(suricataRuleBO.getSignatureSeverity())));
      }
    }
    if (StringUtils.isNotBlank(suricataRuleBO.getClasstypeId())
        && StringUtils.isBlank(suricataRuleClasstypeDao
            .querySuricataRuleClasstype(suricataRuleBO.getClasstypeId()).getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "规则分类不存在");
    }

    // 填充默认值
    suricataRuleBO.setRev(1);
    suricataRuleBO.setContent(StringUtils.defaultIfBlank(suricataRuleBO.getContent(), ""));
    suricataRuleBO.setPriority(
        suricataRuleBO.getPriority() == null ? DEFAULT_PRIORITY : suricataRuleBO.getPriority());
    suricataRuleBO.setClasstypeId(
        StringUtils.defaultIfBlank(suricataRuleBO.getClasstypeId(), DEFAULT_CLASSTYPE));
    suricataRuleBO.setMitreTacticId(
        StringUtils.defaultIfBlank(suricataRuleBO.getMitreTacticId(), DEFAULT_ATTACK));
    suricataRuleBO
        .setMitreTechniqueId(StringUtils.defaultIfBlank(suricataRuleBO.getMitreTechniqueId(), ""));
    suricataRuleBO.setSignatureSeverity(StringUtils
        .defaultIfBlank(suricataRuleBO.getSignatureSeverity(), DEFAULT_SIGNATURE_SEVERITY));
    suricataRuleBO
        .setTarget(StringUtils.defaultIfBlank(suricataRuleBO.getTarget(), DEFAULT_TARGET));
    suricataRuleBO.setParseState(Constants.BOOL_NO);
    suricataRuleBO
        .setSource(StringUtils.defaultIfBlank(suricataRuleBO.getSource(), DEFAULT_SOURCE));

    // 保存规则
    SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
    BeanUtils.copyProperties(suricataRuleBO, suricataRuleDO);
    combinationSuricataRule(suricataRuleDO);
    suricataRuleDO.setOperatorId(operatorId);
    SuricataRuleDO issuedSuricataRuleDO = suricataRuleDao.saveSuricataRule(suricataRuleDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(suricata2MessageBody(issuedSuricataRuleDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA, null);

    return querySuricataRule(suricataRuleBO.getSid());
  }

  public int batchUpdateSuricataRule(SuricataRuleQueryVO suricataRuleQueryVO, List<Integer> sids,
      String state, String classtypeIds, String source, String mitreTacticIds,
      String mitreTechniqueIds, String operatorId) {

    List<MitreAttackDO> mitreAttackDOS = mitreAttackDao.queryMitreAttacks();
    Map<String,
        String> collect = mitreAttackDOS.stream()
            .filter(item -> StringUtils.isNotBlank(item.getParentId()))
            .collect(Collectors.toMap(MitreAttackDO::getId, MitreAttackDO::getParentId));
    List<String> mitreAttackIds = mitreAttackDOS.stream().map(MitreAttackDO::getId)
        .collect(Collectors.toList());
    SuricataRuleClasstypeDO existClasstypeId = suricataRuleClasstypeDao
        .querySuricataRuleClasstype(classtypeIds);
    if (sids.size() > 0 && suricataRuleDao.querySuricataRulesBySids(sids).size() != sids.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "检测规则ID不存在");
    }
    if (StringUtils.isNotBlank(classtypeIds) && StringUtils.isBlank(existClasstypeId.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "规则分类不存在");
    }
    if (StringUtils.isNotBlank(mitreTacticIds) && !mitreAttackIds.contains(mitreTacticIds)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "战术分类不存在");
    }
    if (StringUtils.isNotBlank(mitreTechniqueIds) && !mitreAttackIds.contains(mitreTechniqueIds)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "技术分类不存在");
    } else if (StringUtils.isNotBlank(mitreTechniqueIds)) {
      List<String> parentIds = CsvUtils.convertCSVToList(collect.get(mitreTechniqueIds));
      for (String parentId : parentIds) {
        if (!StringUtils.equals(parentId, mitreTacticIds)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "技术分类不属于战术分类");
        }
      }
    }

    if (StringUtils.isNotBlank(state)
        && !StringUtils.equalsAny(state, Constants.BOOL_NO, Constants.BOOL_YES)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "状态修改不合法");
    }

    if (sids.isEmpty()) {
      List<Integer> allSids = suricataRuleDao.querySuricataRuleIds(suricataRuleQueryVO);

      int maxSize = 1000;
      int offset = 0;
      while (offset % maxSize == 0) {
        List<Integer> splitSids = allSids.stream().skip(offset).limit(maxSize)
            .collect(Collectors.toList());
        List<SuricataRuleDO> suricataRulesBySids = suricataRuleDao
            .querySuricataRulesBySids(splitSids);

        suricataRuleDao.deleteSuricataRule(splitSids);
        updateSuricataRuleOffset(suricataRulesBySids, state, classtypeIds, source, mitreTacticIds,
            mitreTechniqueIds, operatorId);
        suricataRuleDao.saveSuricataRules(suricataRulesBySids);

        if (splitSids.size() == 0) {
          maxSize = -1;
        }

        offset += splitSids.size();
      }
      SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
      suricataRuleDO.setQueryVO(suricataRuleQueryVO);
      suricataRuleDO
          .setSids(sids.stream().map(sid -> Integer.toString(sid)).collect(Collectors.toList()));
      suricataRuleDO.setClasstypeId(classtypeIds);
      suricataRuleDO.setState(state);
      suricataRuleDO.setSource(source);
      suricataRuleDO.setMitreTacticId(mitreTacticIds);
      suricataRuleDO.setMitreTechniqueId(mitreTechniqueIds);

      List<Map<String, Object>> messageBodys = Lists.newArrayList(
          suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_BATCH_MODIFY));
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA, null);
      return offset;
    } else {
      // 进行批量修改，查询当前条件下所有数据
      List<SuricataRuleDO> suricataRuleDOList = suricataRuleDao.querySuricataRulesBySids(sids);
      List<SuricataRuleDO> internalSource = suricataRuleDOList.stream()
          .filter(item -> StringUtils.equals(item.getSource(), INTERNAL_SOURCE))
          .collect(Collectors.toList());
      if (internalSource.size() != 0) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许删除内置规则");
      }
      updateSuricataRuleOffset(suricataRuleDOList, state, classtypeIds, source, mitreTacticIds,
          mitreTechniqueIds, operatorId);
      suricataRuleDao.updateBatchSuricataRule(suricataRuleDOList);

      SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
      suricataRuleDO.setQueryVO(suricataRuleQueryVO);
      suricataRuleDO
          .setSids(sids.stream().map(sid -> Integer.toString(sid)).collect(Collectors.toList()));
      suricataRuleDO.setClasstypeId(classtypeIds);
      suricataRuleDO.setState(state);
      suricataRuleDO.setSource(source);
      suricataRuleDO.setMitreTacticId(mitreTacticIds);
      suricataRuleDO.setMitreTechniqueId(mitreTechniqueIds);

      List<Map<String, Object>> messageBodys = Lists.newArrayList(
          suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_BATCH_MODIFY));
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA, null);
      return suricataRuleDOList.size();
    }
  }

  private void removeNotUpdateByTuple4(List<SuricataRuleDO> existSuricataRuleList,
      Map<Integer, Tuple4<Integer, String, Date, String>> suricataRuleSid) {
    Iterator<SuricataRuleDO> iterator = existSuricataRuleList.iterator();
    while (iterator.hasNext()) {
      SuricataRuleDO suricataRuleDO = iterator.next();
      // 判断是否一致
      SuricataRuleDO suricataRuleDOUpdateTime = new SuricataRuleDO();
      BeanUtils.copyProperties(suricataRuleDO, suricataRuleDOUpdateTime);
      if (!suricataRuleDOUpdateTime.getContent().contains("metadata:")) {
        String updateTime = DateUtils
            .toStringFormat(suricataRuleSid.get(suricataRuleDO.getSid()).getT3(), "yyyy_MM_dd");
        suricataRuleDOUpdateTime.setContent(
            suricataRuleDOUpdateTime.getContent() + "metadata:updated_at " + updateTime);
      }
      suricataRuleDOUpdateTime.setRev(suricataRuleDOUpdateTime.getRev() - 1);
      combinationSuricataRule(suricataRuleDOUpdateTime);
      if (StringUtils.equals(suricataRuleSid.get(suricataRuleDOUpdateTime.getSid()).getT2(),
          DigestUtils.md5Hex(suricataRuleDOUpdateTime.getRule()))) {
        iterator.remove();
      }
    }
  }

  private void updateSuricataRuleOffset(List<SuricataRuleDO> suricataRulesBySids, String state,
      String classtypeIds, String source, String mitreTacticIds, String mitreTechniqueIds,
      String operatorId) {

    for (SuricataRuleDO suricataRuleDO : suricataRulesBySids) {
      suricataRuleDO.setId(null);
      suricataRuleDO.setState(StringUtils.defaultIfBlank(state, suricataRuleDO.getState()));
      suricataRuleDO.setClasstypeId(
          StringUtils.defaultIfBlank(classtypeIds, suricataRuleDO.getClasstypeId()));
      suricataRuleDO.setSource(StringUtils.defaultIfBlank(source, suricataRuleDO.getSource()));
      suricataRuleDO.setMitreTacticId(
          StringUtils.defaultIfBlank(mitreTacticIds, suricataRuleDO.getMitreTacticId()));
      if ((StringUtils.isNotBlank(mitreTacticIds)
          || StringUtils.equals(mitreTacticIds, DEFAULT_MITRETACTICID))
          && StringUtils.isBlank(mitreTechniqueIds)) {
        suricataRuleDO.setMitreTechniqueId("");
      }
      suricataRuleDO.setMitreTechniqueId(
          StringUtils.defaultIfBlank(mitreTechniqueIds, suricataRuleDO.getMitreTechniqueId()));
      suricataRuleDO.setOperatorId(operatorId);
      if (!StringUtils.isAllBlank(classtypeIds, source, mitreTacticIds, mitreTechniqueIds)) {
        suricataRuleDO.setRev(suricataRuleDO.getRev() + 1);
      }
      suricataRuleDO.setUpdateTime(DateUtils.now());
      combinationSuricataRule(suricataRuleDO);
    }
  }

  @Override
  public int updateState(List<String> sids, String state, String operatorId) {

    SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
    if (sids.contains("all")) {
      sids = Lists.newArrayListWithCapacity(0);
      suricataRuleDO.setSids(Lists.newArrayList("all"));
    } else {
      suricataRuleDO.setSids(sids);
    }

    // 将规则条件下发
    suricataRuleDO.setState(state);
    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_BATCH_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA, null);

    return suricataRuleDao.updateState(sids, state, operatorId);
  }

  @Override
  public synchronized SuricataRuleBO updateSuricataRule(int sid, SuricataRuleBO suricataRuleBO,
      String operatorId) {
    if (StringUtils.isNotBlank(suricataRuleBO.getRule())) {
      parseSuricataRule(suricataRuleBO.getRule(), suricataRuleBO, 0);
    }

    SuricataRuleDO exist = suricataRuleDao.querySuricataRule(sid);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "规则不存在");
    }

    if (!DIRECTION.contains(suricataRuleBO.getDirection())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "方向不合法");
    }
    if (suricataRuleBO.getPriority() != null
        && !RANGE_PRIORITY.contains(suricataRuleBO.getPriority())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "优先级不合法");
    }
    if (StringUtils.isNotBlank(suricataRuleBO.getSignatureSeverity())) {
      Map<String, String> signatureSeverity = dictManager.getBaseDict()
          .getItemMap("analysis_suricata_signature_severity");
      if (!signatureSeverity.containsKey(suricataRuleBO.getSignatureSeverity())
          && !EN_SIGNATUE_SEVERITY.contains(suricataRuleBO.getSignatureSeverity())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "严重级别不合法");
      }

      if (EN_SIGNATUE_SEVERITY.contains(suricataRuleBO.getSignatureSeverity())) {
        suricataRuleBO.setSignatureSeverity(
            String.valueOf(EN_SIGNATUE_SEVERITY.indexOf(suricataRuleBO.getSignatureSeverity())));
      }
    }

    // 填充默认值
    suricataRuleBO.setSid(sid);
    suricataRuleBO.setRev(exist.getRev() + 1);
    suricataRuleBO.setContent(StringUtils.defaultIfBlank(suricataRuleBO.getContent(), ""));
    suricataRuleBO.setPriority(
        suricataRuleBO.getPriority() == null ? exist.getPriority() : suricataRuleBO.getPriority());
    suricataRuleBO.setSignatureSeverity(
        StringUtils.isBlank(suricataRuleBO.getSignatureSeverity()) ? exist.getSignatureSeverity()
            : suricataRuleBO.getSignatureSeverity());
    suricataRuleBO.setClasstypeId(
        StringUtils.defaultIfBlank(suricataRuleBO.getClasstypeId(), DEFAULT_CLASSTYPE));
    suricataRuleBO.setMitreTacticId(
        StringUtils.defaultIfBlank(suricataRuleBO.getMitreTacticId(), DEFAULT_ATTACK));
    suricataRuleBO
        .setMitreTechniqueId(StringUtils.defaultIfBlank(suricataRuleBO.getMitreTechniqueId(), ""));
    suricataRuleBO
        .setTarget(StringUtils.defaultIfBlank(suricataRuleBO.getTarget(), DEFAULT_TARGET));
    suricataRuleBO.setParseState(Constants.BOOL_NO);
    suricataRuleBO.setSource(exist.getSource());

    // 修改规则
    SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
    BeanUtils.copyProperties(suricataRuleBO, suricataRuleDO);
    combinationSuricataRule(suricataRuleDO);
    suricataRuleDO.setOperatorId(operatorId);
    suricataRuleDao.updateSuricataRule(suricataRuleDO);

    // 下发到探针和下级CMS
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SURICATA, null);
    return querySuricataRule(sid);
  }

  @Override
  public synchronized int deleteSuricataRule(List<String> sids, SuricataRuleQueryVO queryVO,
      String operatorId) {

    if (queryVO == null) {
      List<Integer> list = Lists.newArrayListWithCapacity(sids.size());
      if (!sids.contains("all")) {
        list = sids.stream().map(sid -> Integer.parseInt(sid)).collect(Collectors.toList());
      }
      int deleteCount = suricataRuleDao.deleteSuricataRule(list);

      // 将条件下发
      SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
      suricataRuleDO.setSids(sids);
      suricataRuleDO.setQueryVO(null);
      List<Map<String, Object>> messageBodys = Lists.newArrayList(
          suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_BATCH_DELETE));
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA, null);

      return deleteCount;
    } else {
      int deleteCount = suricataRuleDao.deleteSuricataRule(queryVO, operatorId);

      // 将根据规则条件下发的删除
      SuricataRuleDO suricataRuleDO = new SuricataRuleDO();
      suricataRuleDO.setSids(null);
      suricataRuleDO.setQueryVO(queryVO);
      List<Map<String, Object>> messageBodys = Lists.newArrayList(
          suricata2MessageBody(suricataRuleDO, FpcCmsConstants.SYNC_ACTION_BATCH_DELETE));
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_SURICATA, null);

      return deleteCount;
    }
  }

  private static void parseSuricataRule(String rule, SuricataRuleBO suricataRuleBO,
      int lineNumber) {
    String tuple = StringUtils.substringBefore(rule, "(").trim();
    String[] tupleItems = tuple.split("[\\s]+(?=[^\\]]*(\\[|$))");
    if (tupleItems.length != 7) {
      LOGGER.warn("suricata rule tuple missing.");
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          (lineNumber != 0 ? "行号：" + lineNumber + "," : "") + "规则格式不合法");
    }
    suricataRuleBO.setAction(tupleItems[0]);
    suricataRuleBO.setProtocol(tupleItems[1]);
    suricataRuleBO.setSrcIp(tupleItems[2]);
    suricataRuleBO.setSrcPort(tupleItems[3]);
    suricataRuleBO.setDirection(tupleItems[4]);
    suricataRuleBO.setDestIp(tupleItems[5]);
    suricataRuleBO.setDestPort(tupleItems[6]);
    suricataRuleBO.setRule(rule);

    // 解析内容和元数据
    Map<String, String> fieldMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder contentValue = new StringBuilder();
    try {
      rule = StringUtils.substringAfter(rule, "(");
      String body = StringUtils.endsWith(rule, ";)") ? StringUtils.substringBeforeLast(rule, ";)")
          : StringUtils.substringBeforeLast(rule, ")");

      // 单独解析msg
      String msgMatch = "";
      Matcher matcher = MSG_PATTERN.matcher(body);
      if (matcher.find()) {
        msgMatch = matcher.group();
        fieldMap.put("msg", StringUtils.substringBetween(msgMatch, "msg:\"", "\";"));
      }

      // 解析括号内信息
      body = StringUtils.substringAfter(body, msgMatch);
      for (String item : StringUtils.split(body, ";")) {
        if (StringUtils.isBlank(item)) {
          continue;
        }

        String[] keyValue = StringUtils.split(StringUtils.trim(item), ':');
        String key = StringUtils.trim(keyValue[0]);
        String value = "";
        if (keyValue.length >= 2) {
          value = StringUtils.trim(StringUtils.substringAfter(StringUtils.trim(item), key + ":"));
        }
        if (RULE_FIELDS.contains(key)) {
          fieldMap.put(key,
              StringUtils.contains(value, "\"") ? StringUtils.substringBetween(value, "\"", "\"")
                  : value);
        } else {
          if (StringUtils.equals(key, "metadata")) {
            List<String> metadata = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
            CsvUtils.convertCSVToList(value).forEach(metadataItem -> {
              String[] split = StringUtils.split(metadataItem);
              String metadataKey = StringUtils.trim(split[0]);
              if (RULE_FIELDS.contains(metadataKey)) {
                fieldMap.put(metadataKey, StringUtils.trim(split[1]));
              } else {
                metadata.add(metadataItem);
              }
            });
            if (CollectionUtils.isNotEmpty(metadata)) {
              item = " metadata:" + StringUtils.join(metadata, ",");
            } else {
              item = "";
            }
          }
          contentValue.append(item).append(StringUtils.isNotBlank(item) ? ";" : "");
        }
      }
    } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
      LOGGER.warn("parse suricata rule failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          (lineNumber != 0 ? "行号：" + lineNumber + "," : "") + "规则格式不合法");
    }

    // sid校验
    try {
      if (fieldMap.containsKey("sid")) {
        suricataRuleBO.setSid(Integer.parseInt(fieldMap.get("sid")));
      } else {
        LOGGER.warn("parse suricata rule failed, can not found sid.");
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            (lineNumber != 0 ? "行号：" + lineNumber + "," : "") + "规则sid不存在");
      }
    } catch (NumberFormatException e) {
      LOGGER.warn("parse suricata rule failed, sid invalid format.");
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          (lineNumber != 0 ? "行号：" + lineNumber + "," : "") + "规则sid格式不合法");
    }

    suricataRuleBO.setMsg(fieldMap.getOrDefault("msg", ""));
    if (fieldMap.containsKey("rev")) {
      suricataRuleBO.setRev(Integer.parseInt(fieldMap.get("rev")));
    }
    suricataRuleBO.setContent(StringUtils.trim(contentValue.toString()));
    suricataRuleBO.setPriority(MapUtils.getIntValue(fieldMap, "priority", DEFAULT_PRIORITY));
    suricataRuleBO.setClasstypeId(fieldMap.getOrDefault("classtype_id", DEFAULT_CLASSTYPE));
    suricataRuleBO.setMitreTacticId(fieldMap.getOrDefault("mitre_tactic_id", DEFAULT_ATTACK));
    suricataRuleBO.setMitreTechniqueId(fieldMap.getOrDefault("mitre_technique_id", ""));
    suricataRuleBO.setCve(fieldMap.getOrDefault("cve", ""));
    suricataRuleBO.setCnnvd(fieldMap.getOrDefault("cnnvd", ""));
    suricataRuleBO.setSignatureSeverity(
        fieldMap.getOrDefault("signature_severity", DEFAULT_SIGNATURE_SEVERITY));
    suricataRuleBO.setTarget(fieldMap.getOrDefault("target", DEFAULT_TARGET));
    suricataRuleBO.setThreshold(fieldMap.getOrDefault("threshold", ""));
    suricataRuleBO.setSource(fieldMap.getOrDefault("source", DEFAULT_SOURCE));
  }

  private static void combinationSuricataRule(SuricataRuleDO suricataRuleDO) {
    StringBuilder rule = new StringBuilder();
    rule.append(suricataRuleDO.getAction()).append(" ");
    rule.append(suricataRuleDO.getProtocol()).append(" ");
    rule.append(suricataRuleDO.getSrcIp()).append(" ");
    rule.append(suricataRuleDO.getSrcPort()).append(" ");
    rule.append(suricataRuleDO.getDirection()).append(" ");
    rule.append(suricataRuleDO.getDestIp()).append(" ");
    rule.append(suricataRuleDO.getDestPort()).append(" ");
    rule.append("(msg:\"").append(suricataRuleDO.getMsg()).append("\"; ");

    // content
    String metadata = "";
    if (StringUtils.isNotBlank(suricataRuleDO.getContent())) {
      String[] items = suricataRuleDO.getContent()
          .split(";((?=([^\"]*\"[^\"]*\")*[^\"]*$)(?=([^\']*\'[^\']*\')*[^\']*$))");
      for (String item : items) {
        item = StringUtils.trim(item);
        if (item.startsWith("metadata:")) {
          metadata = item;
        } else {
          rule.append(item).append("; ");
        }
      }
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getThreshold())) {
      rule.append("threshold: ").append(suricataRuleDO.getThreshold()).append("; ");
    }
    rule.append("target: ")
        .append(StringUtils.defaultIfBlank(suricataRuleDO.getTarget(), DEFAULT_TARGET))
        .append("; ");
    rule.append("sid: ").append(suricataRuleDO.getSid()).append("; ");
    rule.append("rev: ").append(suricataRuleDO.getRev()).append("; ");
    rule.append("priority: ").append(suricataRuleDO.getPriority()).append("; ");

    // metadata
    String updateTime = DateUtils.toStringFormat(DateUtils.now(), "yyyy_MM_dd");
    if (StringUtils.isNotBlank(metadata)) {
      rule.append(StringUtils.removeEnd(metadata, ";"));
    } else {
      rule.append("metadata:updated_at ").append(updateTime);
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getClasstypeId())) {
      rule.append(", ").append("classtype_id ").append(suricataRuleDO.getClasstypeId());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getCve())) {
      rule.append(", ").append("cve ").append(suricataRuleDO.getCve());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getCnnvd())) {
      rule.append(", ").append("cnnvd ").append(suricataRuleDO.getCnnvd());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getSignatureSeverity())) {
      rule.append(", ").append("signature_severity ").append(suricataRuleDO.getSignatureSeverity());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getMitreTacticId())) {
      rule.append(", ").append("mitre_tactic_id ").append(suricataRuleDO.getMitreTacticId());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getMitreTechniqueId())) {
      rule.append(", ").append("mitre_technique_id ").append(suricataRuleDO.getMitreTechniqueId());
    }
    if (StringUtils.isNotBlank(suricataRuleDO.getSource())) {
      rule.append(", ").append("source ").append(suricataRuleDO.getSource());
    }
    rule.append(";)");

    suricataRuleDO.setRule(rule.toString());
  }

  private Map<String, Object> suricata2MessageBody(SuricataRuleDO suricataRuleDO, String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", suricataRuleDO.getId());
    map.put("sid", suricataRuleDO.getSid());
    map.put("action", suricataRuleDO.getAction());
    map.put("protocol", suricataRuleDO.getProtocol());
    map.put("srcIp", suricataRuleDO.getSrcIp());
    map.put("srcPort", suricataRuleDO.getSrcPort());
    map.put("direction", suricataRuleDO.getDirection());
    map.put("destIp", suricataRuleDO.getDestIp());
    map.put("destPort", suricataRuleDO.getDestPort());
    map.put("msg", suricataRuleDO.getMsg());
    map.put("rev", suricataRuleDO.getRev());
    map.put("rule", suricataRuleDO.getRule());
    map.put("content", suricataRuleDO.getContent());
    map.put("priority", suricataRuleDO.getPriority());
    map.put("classtypeId", suricataRuleDO.getClasstypeId());
    map.put("mitreTacticId", suricataRuleDO.getMitreTacticId());
    map.put("mitreTechniqueId", suricataRuleDO.getMitreTechniqueId());
    map.put("cve", suricataRuleDO.getCve());
    map.put("cnnvd", suricataRuleDO.getCnnvd());
    map.put("signatureSeverity", suricataRuleDO.getSignatureSeverity());
    map.put("target", suricataRuleDO.getTarget());
    map.put("threshold", suricataRuleDO.getThreshold());
    map.put("parseState", suricataRuleDO.getParseState());
    map.put("parseLog", suricataRuleDO.getParseLog());
    map.put("state", suricataRuleDO.getState());
    map.put("source", suricataRuleDO.getSource());
    map.put("createTime", suricataRuleDO.getCreateTime());
    map.put("sids", StringUtils.join(suricataRuleDO.getSids(), ","));
    map.put("queryVO", JsonHelper.serialize(suricataRuleDO.getQueryVO()));
    map.put("cmsAction", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SURICATA));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncSuricata(item)).sum();
    LOGGER.info("current sync suricataRule total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncSuricata(Map<String, Object> messageBody) {

    int syncCount = 0;

    String cmsAction = MapUtils.getString(messageBody, "cmsAction");
    Integer sid = MapUtils.getInteger(messageBody, "sid");
    if (!StringUtils.equalsAny(cmsAction, FpcCmsConstants.SYNC_ACTION_BATCH_MODIFY,
        FpcCmsConstants.SYNC_ACTION_BATCH_DELETE)) {
      if (sid == 0) {
        return syncCount;
      }
    }

    SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
    suricataRuleBO.setSid(sid);
    suricataRuleBO.setId(MapUtils.getString(messageBody, "id"));
    suricataRuleBO.setAssignId(MapUtils.getString(messageBody, "id"));
    suricataRuleBO.setAction(MapUtils.getString(messageBody, "action"));
    suricataRuleBO.setProtocol(MapUtils.getString(messageBody, "protocol"));
    suricataRuleBO.setSrcIp(MapUtils.getString(messageBody, "srcIP"));
    suricataRuleBO.setSrcPort(MapUtils.getString(messageBody, "srcPort"));
    suricataRuleBO.setDirection(MapUtils.getString(messageBody, "direction"));
    suricataRuleBO.setDestIp(MapUtils.getString(messageBody, "destIp"));
    suricataRuleBO.setDestPort(MapUtils.getString(messageBody, "destPort"));
    suricataRuleBO.setMsg(MapUtils.getString(messageBody, "msg"));
    suricataRuleBO.setRev(MapUtils.getIntValue(messageBody, "rev"));
    suricataRuleBO.setContent(MapUtils.getString(messageBody, "content"));
    suricataRuleBO.setPriority(MapUtils.getIntValue(messageBody, "priority"));
    suricataRuleBO.setClasstypeId(MapUtils.getString(messageBody, "classtypeId"));
    suricataRuleBO.setMitreTacticId(MapUtils.getString(messageBody, "mitreTacticId"));
    suricataRuleBO.setMitreTechniqueId(MapUtils.getString(messageBody, "mitreTechniqueId"));
    suricataRuleBO.setCve(MapUtils.getString(messageBody, "cve"));
    suricataRuleBO.setCnnvd(MapUtils.getString(messageBody, "cnnvd"));
    suricataRuleBO.setSignatureSeverity(MapUtils.getString(messageBody, "signatureSeverity"));
    suricataRuleBO.setTarget(MapUtils.getString(messageBody, "target"));
    suricataRuleBO.setThreshold(MapUtils.getString(messageBody, "threshold"));
    suricataRuleBO.setRule(MapUtils.getString(messageBody, "rule"));
    suricataRuleBO.setParseState(MapUtils.getString(messageBody, "parseState"));
    suricataRuleBO.setParseLog(MapUtils.getString(messageBody, "parseLog"));
    suricataRuleBO.setState(MapUtils.getString(messageBody, "state"));
    suricataRuleBO.setSource(MapUtils.getString(messageBody, "source"));
    suricataRuleBO.setCreateTime(MapUtils.getString(messageBody, "createTime"));
    suricataRuleBO.setUpdateTime(MapUtils.getString(messageBody, "updateTime"));
    suricataRuleBO.setSids(CsvUtils.convertCSVToList(MapUtils.getString(messageBody, "sids")));
    suricataRuleBO.setQueryVO(JsonHelper.deserialize(MapUtils.getString(messageBody, "queryVO"),
        SuricataRuleQueryVO.class));

    SuricataRuleBO exist = querySuricataRule(suricataRuleBO.getSid());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (cmsAction) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSuricataRule(exist.getSid(), suricataRuleBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveSuricataRule(suricataRuleBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSuricataRule(Lists.newArrayListWithCapacity(exist.getSid()),
              new SuricataRuleQueryVO(), CMS_ASSIGNMENT);
          deleteCount++;
          break;
        case FpcCmsConstants.SYNC_ACTION_BATCH_MODIFY:
          if (StringUtils.isAllBlank(suricataRuleBO.getClasstypeId(), suricataRuleBO.getSource(),
              suricataRuleBO.getMitreTacticId(), suricataRuleBO.getMitreTechniqueId())
              && !suricataRuleBO.getSids().isEmpty()
              && StringUtils.isNotBlank(suricataRuleBO.getState())) {
            updateState(suricataRuleBO.getSids(), suricataRuleBO.getState(), CMS_ASSIGNMENT);
          } else {
            batchUpdateSuricataRule(suricataRuleBO.getQueryVO(),
                suricataRuleBO.getSids().stream().map(item -> Integer.parseInt(item))
                    .collect(Collectors.toList()),
                suricataRuleBO.getState(), suricataRuleBO.getClasstypeId(),
                suricataRuleBO.getSource(), suricataRuleBO.getMitreTacticId(),
                suricataRuleBO.getMitreTechniqueId(), CMS_ASSIGNMENT);
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_BATCH_DELETE:
          deleteSuricataRule(suricataRuleBO.getSids(), suricataRuleBO.getQueryVO(), CMS_ASSIGNMENT);
          break;
        default:
          break;
      }

      // 本次同步量
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

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 全部删除
    int clearCount = 0;
    try {
      // 删除所有包含系统内置
      clearCount = suricataRuleDao.deleteSuricataRuleContainsDefault(onlyLocal, beforeTime,
          CMS_ASSIGNMENT);
    } catch (Exception e) {
      LOGGER.warn("delete filterPolicy failed. error msg: {}", e.getMessage());
    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    // 生成md5值进行判断
    return Lists.newArrayList(
        DigestUtils.md5Hex(JsonHelper.serialize(suricataRuleDao.querySuricataRule(beforeTime))));
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
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    String md5 = DigestUtils.md5Hex(JsonHelper.serialize(suricataRuleDao.querySuricataRule()));
    map.put(FpcCmsConstants.MQ_TAG_SURICATA, Lists.newArrayList(md5));

    return map;
  }

  @Override
  @Async
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {

    // 保险机制，如果此探针目录下有相关文件则删除
    File issuedFile = new File(PATH_NAME + "/" + serialNumber);
    deleteDirectoryLegacyIO(issuedFile);

    // 使用单线程来进行下发工作
    if (!IS_RUNNING_ISSUED) {
      singleThreadExecutor.execute(new Runnable() {
        @Override
        public void run() {
          List<String> ids = suricataRuleDao.querySuricataRuleIds();
          int offset = 0;
          List<SuricataRuleDO> suricataRuleDOList = null;
          while (offset % MAX_SIZE == 0) {
            File file = new File(PATH_NAME + "/" + serialNumber + File.separator
                + offset / ISSUED_MAX_SIZE + FILE_NAME);
            List<String> splitSid = ids.stream().skip(offset).limit(MAX_SIZE)
                .collect(Collectors.toList());
            if (!splitSid.isEmpty()) {
              suricataRuleDOList = suricataRuleDao.querySuricataRulesByIds(splitSid);
            } else {
              suricataRuleDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
            }

            writeCSVFile(suricataRuleDOList, file);
            if (suricataRuleDOList.isEmpty()) {
              break;
            }
            offset += MAX_SIZE;
          }
          // 向全部探针与CMS下发安全分析规则
          if (StringUtils.isEmpty(deviceType) && StringUtils.isEmpty(serialNumber)) {
            issued(issuedFile);
          } else {
            // 向指定探针或cms下发安全分析规则
            for (File file : issuedFile.listFiles()) {
              if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
                FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(serialNumber);
                fpcManagerInvoker.importSuricataRules(fpcDO, file);
                file.delete();
              } else {
                CmsDO cmsDO = cmsDao.queryCmsBySerialNumber(serialNumber);
                fpcManagerInvoker.importSuricataRules(cmsDO, file);
                file.delete();
              }
            }
          }
        }
      });
    }

    return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
  }

  // 下发文件方法
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
          Map<String, Object> issuedState = fpcManagerInvoker.importSuricataRules(fpcDO, file);
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
          Map<String, Object> issuedState = fpcManagerInvoker.importSuricataRules(cmsDO, file);
        } catch (BusinessException exception) {
          LOGGER.info("File delivery failure, " + cmsDO.getName());
          continue;
        }
      }
      file.delete();
    }
  }

  public static File writeCSVFile(List<SuricataRuleDO> suricataRuleDOS, File file) {

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
      for (SuricataRuleDO suricataRuleDO : suricataRuleDOS) {
        csvWtriter.write(StringUtils.join(suricataRuleDO.getId() + " " + suricataRuleDO.getState()
            + " " + suricataRuleDO.getRule(), "\n"));
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
