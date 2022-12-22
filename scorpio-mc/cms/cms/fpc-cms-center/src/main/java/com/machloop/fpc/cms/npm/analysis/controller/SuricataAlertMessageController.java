package com.machloop.fpc.cms.npm.analysis.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataAlertMessageBO;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleRelationBO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataAlertMessageService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/suricata")
public class SuricataAlertMessageController {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataAlertMessageController.class);

  @Autowired
  private SuricataAlertMessageService suricataAlertMessageService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @GetMapping("/alert-messages")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> querySuricataAlertMessages(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      SuricataRuleQueryVO queryVO) {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "timestamp"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    Page<SuricataAlertMessageBO> suricataAlertPage = suricataAlertMessageService
        .querySuricataAlerts(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(suricataAlertPage.getSize());
    for (SuricataAlertMessageBO suricataAlertMessageBO : suricataAlertPage) {
      resultList.add(suricataAlert2Map(suricataAlertMessageBO));
    }

    return new PageImpl<>(resultList, page, suricataAlertPage.getTotalElements());
  }


  @GetMapping("/alert-messages/relation")
  @Secured({"PERM_USER"})
  public List<SuricataRuleRelationBO> alterMessagesRelation(@RequestParam String destIp,
      @RequestParam String srcIp, @RequestParam int sid, @RequestParam String startTime,
      @RequestParam String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    return suricataAlertMessageService.queryAlterMessagesRelation(destIp, srcIp, sid, startTimeDate,
        endTimeDate);
  }

  @GetMapping("/alert-messages/as-export")
  @Secured({"PERM_USER"})
  public void exportSuricataAlertMessages(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      @RequestParam(required = false, defaultValue = "0") int count, SuricataRuleQueryVO queryVO) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "SuricataAlertMessages.csv"
        : "SuricataAlertMessages.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }

      Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "timestamp"));
      suricataAlertMessageService.exportSuricataAlerts(queryVO, fileType, sort, out, count);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export suricata alert error ", e);
    }
  }

  @GetMapping("/alert-messages/as-graph")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "counts") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      queryVO.setNetworkIds(CsvUtils.convertCSVToList(sensorNetworkGroupService
          .querySensorNetworkGroup(queryVO.getNetworkGroupId()).getNetworkInSensorIds()));

    }
    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    return suricataAlertMessageService.querySuricataAlertMessagesAsGraph(queryVO, page);
  }

  @GetMapping("/alert-messages/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      queryVO.setNetworkIds(CsvUtils.convertCSVToList(sensorNetworkGroupService
          .querySensorNetworkGroup(queryVO.getNetworkGroupId()).getNetworkInSensorIds()));

    }
    return suricataAlertMessageService.querySuricataAlertMessagesStatistics(queryVO);
  }

  private Map<String, Object> suricataAlert2Map(SuricataAlertMessageBO suricataAlertMessageBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("timestamp", suricataAlertMessageBO.getTimestamp());
    map.put("sid", suricataAlertMessageBO.getSid());
    map.put("msg", suricataAlertMessageBO.getMsg());
    map.put("networkId", suricataAlertMessageBO.getNetworkId());
    map.put("classtypeId", suricataAlertMessageBO.getClasstypeId());
    map.put("mitreTacticId", suricataAlertMessageBO.getMitreTacticId());
    map.put("mitreTechniqueId", suricataAlertMessageBO.getMitreTechniqueId());
    map.put("cve", suricataAlertMessageBO.getCve());
    map.put("cnnvd", suricataAlertMessageBO.getCnnvd());
    map.put("signatureSeverity", suricataAlertMessageBO.getSignatureSeverity());
    map.put("target", suricataAlertMessageBO.getTarget());
    map.put("srcIp", suricataAlertMessageBO.getSrcIp());
    map.put("srcPort", suricataAlertMessageBO.getSrcPort());
    map.put("destIp", suricataAlertMessageBO.getDestIp());
    map.put("destPort", suricataAlertMessageBO.getDestPort());
    map.put("protocol", suricataAlertMessageBO.getProtocol());
    map.put("l7Protocol", suricataAlertMessageBO.getL7Protocol());
    map.put("flowId", suricataAlertMessageBO.getFlowId());
    map.put("domain", suricataAlertMessageBO.getDomain());
    map.put("url", suricataAlertMessageBO.getUrl());
    map.put("countryIdInitiator", suricataAlertMessageBO.getCountryIdInitiator());
    map.put("provinceIdInitiator", suricataAlertMessageBO.getProvinceIdInitiator());
    map.put("cityIdInitiator", suricataAlertMessageBO.getCityIdInitiator());
    map.put("countryIdResponder", suricataAlertMessageBO.getCountryIdResponder());
    map.put("provinceIdResponder", suricataAlertMessageBO.getProvinceIdResponder());
    map.put("cityIdResponder", suricataAlertMessageBO.getCityIdResponder());
    map.put("source", suricataAlertMessageBO.getSource());
    String tag = suricataAlertMessageBO.getTag();
    List<String> tags = CsvUtils.convertCSVToList(tag);
    map.put("tag", tags);
    map.put("basicTag", suricataAlertMessageBO.getBasicTag());

    return map;
  }


}
