package com.machloop.fpc.cms.npm.analysis.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.*;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.*;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataAlertMessageBO;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleDateBO;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleRelationBO;
import com.machloop.fpc.cms.npm.analysis.dao.MitreAttackDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataAlertMessageDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleClasstypeDao;
import com.machloop.fpc.cms.npm.analysis.data.MitreAttackDO;
import com.machloop.fpc.cms.npm.analysis.data.SuricataAlertMessageDO;
import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleClasstypeDO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataAlertMessageService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple4;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
@Service
public class SuricataAlertMessageServiceImpl implements SuricataAlertMessageService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataAlertMessageServiceImpl.class);

  private Map<String, Tuple4<String, String, Integer, Date>> packetMap = Maps.newConcurrentMap();

  private static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("timestamp", "记录时间");
    fields.put("sid", "规则ID");
    fields.put("msg", "描述信息");
    fields.put("source", "来源");
    fields.put("tag", "标签");
    fields.put("basicTag", "基础标签");
    fields.put("classtype", "规则分类");
    fields.put("mitreTactic", "战术分类");
    fields.put("mitreTechnique", "技术分类");
    fields.put("domain", "域名");
    fields.put("cve", "CVE编号");
    fields.put("cnnvd", "CNNVD编号");
    fields.put("signatureSeverity", "严重级别");
    fields.put("target", "受害者");
    fields.put("protocol", "传输协议");
    fields.put("l7Protocol", "应用协议");
    fields.put("srcIp", "源IP");
    fields.put("srcPort", "源端口");
    fields.put("destIp", "目的IP");
    fields.put("destPort", "目的端口");
  }

  @Autowired
  private SuricataAlertMessageDao suricataAlertMessageDao;

  @Autowired
  private MitreAttackDao mitreAttackDao;

  @Autowired
  private SuricataRuleClasstypeDao suricataRuleClasstypeDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private ServletContext servletContext;

  /**
   * @see com.machloop.fpc.npm.analysis.service.SuricataAlertMessageService#querySuricataAlerts(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO)
   */
  @Override
  public Page<SuricataAlertMessageBO> querySuricataAlerts(Pageable page,
      SuricataRuleQueryVO queryVO) {
    Page<SuricataAlertMessageDO> suricataAlerts = suricataAlertMessageDao.querySuricataAlerts(page,
        queryVO);

    List<
        SuricataAlertMessageBO> list = suricataAlerts.getContent().stream().map(suricataAlertDO -> {
          SuricataAlertMessageBO suricataAlertBO = new SuricataAlertMessageBO();
          BeanUtils.copyProperties(suricataAlertDO, suricataAlertBO);
          String srcIpv4 = suricataAlertDO.getSrcIpv4();
          String srcIpv6 = suricataAlertDO.getSrcIpv6();
          suricataAlertBO.setSrcIp(StringUtils.isNotBlank(srcIpv4) ? srcIpv4 : srcIpv6);
          String destIpv4 = suricataAlertDO.getDestIpv4();
          String destIpv6 = suricataAlertDO.getDestIpv6();
          suricataAlertBO.setDestIp(StringUtils.isNotBlank(destIpv4) ? destIpv4 : destIpv6);

          return suricataAlertBO;
        }).collect(Collectors.toList());

    return new PageImpl<>(list, page, suricataAlerts.getTotalElements());
  }


  /**
   * @see com.machloop.fpc.npm.analysis.service.SuricataAlertMessageService#exportSuricataAlerts(com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO, java.lang.String, java.io.OutputStream)
   */
  @Override
  public void exportSuricataAlerts(SuricataRuleQueryVO queryVO, String fileType, Sort sort,
      OutputStream out, int count) throws IOException {
    LOGGER.info("export suricata alert, fileType: {}, query: {}.", fileType, queryVO.toString());

    // 字典
    Map<String, String> mitreAttacks = mitreAttackDao.queryMitreAttacks().stream()
        .collect(Collectors.toMap(MitreAttackDO::getId, MitreAttackDO::getName));
    Map<String,
        String> classtypes = suricataRuleClasstypeDao.querySuricataRuleClasstypes().stream()
            .collect(
                Collectors.toMap(SuricataRuleClasstypeDO::getId, SuricataRuleClasstypeDO::getName));
    Map<String, String> signatureSeveritys = dictManager.getBaseDict()
        .getItemMap("analysis_suricata_signature_severity");
    Map<String,
        String> sourceDict = dictManager.getBaseDict().getItemMap("analysis_suricata_rule_source");

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // 标题
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    titles.addAll(fields.values());

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
    FileUtils.touch(tempFile);

    // 规定导出的最大数量
    int exportMaxCount = Integer
        .parseInt(HotPropertiesHelper.getProperty("export.suricata.alert.max.count"));
    count = (count <= 0 || count > exportMaxCount) ? exportMaxCount : count;

    // 单次查询数量
    int batchSize = Integer
        .parseInt(HotPropertiesHelper.getProperty("export.suricata.alert.batch.size"));

    // 创建数据迭代器
    final int fcount = count;
    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0 && offset < fcount;
      }

      @Override
      public List<List<String>> next() {
        int pageNum = offset / batchSize;
        int pageSize = fcount > offset && fcount < (offset + batchSize) ? (fcount - offset)
            : batchSize;
        PageRequest page = new PageRequest(pageNum, pageSize, sort);

        List<Map<String, Object>> temps = suricataAlertMessageDao
            .querySuricataAlertsWithoutTotal(page, queryVO);

        // 避免死循环
        if (temps.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataset = temps
            .stream().map(item -> transSuricataAlertMessagesToStr(item, titles, columnNameMap,
                mitreAttacks, classtypes, signatureSeveritys, sourceDict))
            .collect(Collectors.toList());

        offset += dataset.size();

        return dataset;
      }

    };

    // 导出数据
    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  @Override
  public Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
      PageRequest page) {
    return suricataAlertMessageDao.querySuricataAlertMessagesAsGraph(queryVO, page);
  }

  @Override
  public Map<String, Object> querySuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    long total = suricataAlertMessageDao.SuricataAlertMessagesStatistics(queryVO);
    result.put("total", total);
    return result;
  }

  @Override
  public List<SuricataRuleRelationBO> queryAlterMessagesRelation(String destIp, String srcIp,
      int sid, Date startTimeDate, Date endTimeDate) {
    // 源IP相关事件
    List<Map<String, Object>> sourceIpDOList = suricataAlertMessageDao
        .queryAlterMessagesRelation(srcIp, startTimeDate, endTimeDate);

    // 目的IP相关事件
    List<Map<String, Object>> destIpDOList = suricataAlertMessageDao
        .queryAlterMessagesRelation(destIp, startTimeDate, endTimeDate);

    // 源和目的IP集合
    List<SuricataRuleDateBO> sourceRelation = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SuricataRuleDateBO> destRelation = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    sourceIpDOList.forEach(item -> {
      SuricataRuleDateBO suricataRuleDateBO = new SuricataRuleDateBO();
      suricataRuleDateBO.setSid(MapUtils.getIntValue(item, "sid"));
      suricataRuleDateBO.setMsg(MapUtils.getString(item, "msg"));
      suricataRuleDateBO.setSrcRole(
          StringUtils.equals(srcIp, MapUtils.getString(item, "target")) ? "victim" : "offender");
      OffsetDateTime timestamp = (OffsetDateTime) item.get("minTimestamp");
      suricataRuleDateBO.setTimestamp(Date.from(timestamp.toInstant()));
      sourceRelation.add(suricataRuleDateBO);
    });

    destIpDOList.forEach(item -> {
      SuricataRuleDateBO suricataRuleDateBO = new SuricataRuleDateBO();
      suricataRuleDateBO.setSid(MapUtils.getIntValue(item, "sid"));
      suricataRuleDateBO.setMsg(MapUtils.getString(item, "msg"));
      suricataRuleDateBO.setDestRole(
          StringUtils.equals(destIp, MapUtils.getString(item, "target")) ? "victim" : "offender");
      OffsetDateTime timestamp = (OffsetDateTime) item.get("minTimestamp");
      suricataRuleDateBO.setTimestamp(Date.from(timestamp.toInstant()));
      destRelation.add(suricataRuleDateBO);
    });

    Map<String, SuricataRuleDateBO> sidAndTimestampMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, Integer> sidAndTimestampMapCount = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 通过sid + 时间戳
    for (SuricataRuleDateBO item : sourceRelation) {
      sidAndTimestampMap.put(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp()),
          item);
      sidAndTimestampMapCount
          .put(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp()), 1);
    }
    for (SuricataRuleDateBO item : destRelation) {
      if (sidAndTimestampMapCount
          .containsKey(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp()))
          && sidAndTimestampMapCount
              .get(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp())) > 0) {
        sidAndTimestampMap.get(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp()))
            .setDestRole(item.getDestRole());
      } else {
        sidAndTimestampMap.put(item.getSid() + "-" + DateUtils.toStringISO8601(item.getTimestamp()),
            item);
      }
    }

    List<SuricataRuleDateBO> listSuricata = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    int count = 0;
    for (SuricataRuleDateBO suricataRuleDateBO : sidAndTimestampMap.values()) {
      if (suricataRuleDateBO.getSid() == sid && count == 0) {
        SuricataRuleDateBO suricataRuleDateBO1 = new SuricataRuleDateBO();
        suricataRuleDateBO1.setSid(sid);
        suricataRuleDateBO1.setMsg(suricataRuleDateBO.getMsg());
        suricataRuleDateBO1.setTimestamp(DateUtils.afterDayDate(startTimeDate, 1));
        listSuricata.add(suricataRuleDateBO1);
        count++;
      } else if (suricataRuleDateBO.getSid() != sid) {
        listSuricata.add(suricataRuleDateBO);
      }
    }

    Collections.sort(listSuricata, new Comparator<SuricataRuleDateBO>() {
      @Override
      public int compare(SuricataRuleDateBO o1, SuricataRuleDateBO o2) {
        if (o1.getTimestamp().getTime() == o2.getTimestamp().getTime()) {
          return 0;
        } else if (o1.getTimestamp().getTime() > o2.getTimestamp().getTime()) {
          return 1;
        } else {
          return -1;
        }
      }
    });
    List<SuricataRuleDateBO> suricataAlterMessage = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    int i = 0;
    int currentSid = 0;
    for (SuricataRuleDateBO suricataRuleDateBO : listSuricata) {
      if (suricataRuleDateBO.getSid() == sid) {
        currentSid = i;
      }
      i++;
    }

    // 前7个
    for (int j = 7; j > 0; j--) {
      if (currentSid - j >= 0) {
        suricataAlterMessage.add(listSuricata.get(currentSid - j));
      }
    }

    // 后7个
    for (int j = 1; j < 8; j++) {
      if (currentSid + j < listSuricata.size()) {
        suricataAlterMessage.add(listSuricata.get(currentSid + j));
      }
    }

    List<SuricataRuleRelationBO> suricataRuleRelationBOLists = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    suricataAlterMessage.forEach(item -> {
      SuricataRuleRelationBO suricataRuleRelationBO = new SuricataRuleRelationBO();
      BeanUtils.copyProperties(item, suricataRuleRelationBO);
      suricataRuleRelationBO.setTimestamp(DateUtils.toStringISO8601(item.getTimestamp()));
      suricataRuleRelationBOLists.add(suricataRuleRelationBO);
    });

    return suricataRuleRelationBOLists;

  }

  @Override
  public List<Object> queryTopHundredSuricataFlowId(String sid, Date startTimeDate,
      Date endTimeDate) {
    return suricataAlertMessageDao.querySuricataTopHundredFlowIds(sid, startTimeDate, endTimeDate);
  }

  private List<String> transSuricataAlertMessagesToStr(Map<String, Object> map, List<String> titles,
      Map<String, String> columnNameMap, Map<String, String> mitreAttacks,
      Map<String, String> classtypes, Map<String, String> signatureSeveritys,
      Map<String, String> sourceDict) {

    List<String> values = titles.stream().map(title -> {
      String field = columnNameMap.get(title);
      String value = "";
      switch (field) {
        case "timestamp":
          value = DateUtils.toStringNanoISO8601(
              OffsetDateTime.parse(MapUtils.getString(map, "timestamp")), ZoneId.systemDefault());
          break;
        case "sid":
          value = String.valueOf(MapUtils.getIntValue(map, "sid"));
          break;
        case "msg":
          value = MapUtils.getString(map, "msg");
          break;
        case "source":
          String source = MapUtils.getString(map, "source");
          value = sourceDict.getOrDefault(source, source);
          break;
        case "tag":
          value = CsvUtils.convertCollectionToCSV(JsonHelper
              .deserialize(JsonHelper.serialize(map.get("tag")), new TypeReference<List<String>>() {
              }));
          break;
        case "basicTag":
          value = MapUtils.getString(map, "basic_tag");
          break;
        case "classtype":
          value = classtypes.getOrDefault(MapUtils.getString(map, "classtype_id"), "");
          break;
        case "mitreTactic":
          value = mitreAttacks.getOrDefault(MapUtils.getString(map, "mitre_tactic_id"), "");
          break;
        case "mitreTechnique":
          value = mitreAttacks.getOrDefault(MapUtils.getString(map, "mitre_technique_id"), "");
          break;
        case "domain":
          value = MapUtils.getString(map, "domain");
          break;
        case "cve":
          value = MapUtils.getString(map, "cve");
          break;
        case "cnnvd":
          value = MapUtils.getString(map, "cnnvd");
          break;
        case "signatureSeverity":
          value = signatureSeveritys
              .getOrDefault(String.valueOf(MapUtils.getInteger(map, "signature_severity")), "");
          break;
        case "target":
          value = MapUtils.getString(map, "target");
          break;
        case "protocol":
          value = MapUtils.getString(map, "protocol");
          break;
        case "l7Protocol":
          value = MapUtils.getString(map, "l7_protocol");
          break;
        case "srcIp":
          Inet4Address srcIpv4 = (Inet4Address) map.get("src_ipv4");
          Inet6Address srcIpv6 = (Inet6Address) map.get("src_ipv6");
          String srcIpv4Address = srcIpv4 != null ? srcIpv4.getHostAddress() : null;
          String srcIpv6Address = srcIpv6 != null ? srcIpv6.getHostAddress() : null;
          value = StringUtils.isNotBlank(srcIpv4Address) ? srcIpv4Address : srcIpv6Address;
          break;
        case "srcPort":
          value = String.valueOf(MapUtils.getIntValue(map, "src_port"));
          break;
        case "destIp":
          Inet4Address destIpv4 = (Inet4Address) map.get("dest_ipv4");
          Inet6Address destIpv6 = (Inet6Address) map.get("dest_ipv6");
          String destIpv4Address = destIpv4 != null ? destIpv4.getHostAddress() : null;
          String destIpv6Address = destIpv6 != null ? destIpv6.getHostAddress() : null;
          value = StringUtils.isNotBlank(destIpv4Address) ? destIpv4Address : destIpv6Address;
          break;
        case "destPort":
          value = String.valueOf(MapUtils.getIntValue(map, "dest_port"));
          break;
      }
      return value;
    }).collect(Collectors.toList());

    return values;
  }
}
