package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.restapi.vo.PacketAnalysisTaskRestAPIVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisSubTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskBO;
import com.machloop.fpc.npm.appliance.service.PacketAnalysisService;
import com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService;
import com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO;

/**
 * @author chenxiao
 * create at 2022/7/7
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class PacketAnalysisTaskRestAPIController {


  @Autowired
  private PacketAnalysisTaskService packetAnalysisTaskService;

  private static final List<String> MODE = Lists.newArrayList("MULTIPLE_FILES_TO_SINGLE_TASK",
      "SINGLE_DIRECTORY_TO_SINGLE_TASK", "SINGLE_DIRECTORY_TO_MULTIPLE_TASK");

  @Autowired
  private UserService userService;

  @Autowired
  private PacketAnalysisService packetAnalysisService;


  @GetMapping("/packet-analysis-tasks")
  @RestApiSecured
  public RestAPIResultVO queryPacketAnalysisTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "status", required = false) String status) {

    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Sort.Order(Sort.Direction.DESC, "create_time")));

    Page<PacketAnalysisTaskBO> packetAnalysisTaskPage = packetAnalysisTaskService
        .queryPacketAnalysisTasks(page, name, status);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(packetAnalysisTaskPage.getSize());
    packetAnalysisTaskPage
        .forEach(packetAnalysisTask -> resultList.add(packetAnalysisTask2Map(packetAnalysisTask)));

    return RestAPIResultVO
        .resultSuccess(new PageImpl<>(resultList, page, packetAnalysisTaskPage.getTotalElements()));

  }

  @GetMapping("/packet-analysis-subtasks")
  @RestApiSecured
  public RestAPIResultVO queryPacketAnalysisSubTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "taskId", required = false) String taskId,
      @RequestParam(name = "source", required = false) String source) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Sort.Order(Sort.Direction.DESC, "create_time")));

    Page<PacketAnalysisSubTaskBO> packetAnalysisSubTaskPage = packetAnalysisTaskService
        .queryPacketAnalysisSubTasks(page, name, taskId, source);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(packetAnalysisSubTaskPage.getSize());
    packetAnalysisSubTaskPage.forEach(
        packetAnalysisSubTask -> resultList.add(packetAnalysisSubTask2Map(packetAnalysisSubTask)));

    return RestAPIResultVO.resultSuccess(
        new PageImpl<>(resultList, page, packetAnalysisSubTaskPage.getTotalElements()));
  }

  @GetMapping("/packet-analysis-tasks/{id}")
  @RestApiSecured
  public RestAPIResultVO queryPacketAnalysisTask(
      @NotEmpty(message = "离线分析任务ID不能为空") @PathVariable String id) {
    PacketAnalysisTaskBO packetAnalysisTaskBO = packetAnalysisTaskService
        .queryPacketAnalysisTask(id);

    if (StringUtils.isBlank(packetAnalysisTaskBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("业务不存在").build();
    }

    return RestAPIResultVO.resultSuccess(packetAnalysisTask2Map(packetAnalysisTaskBO));
  }

  @GetMapping("/packet-file-directory")
  @RestApiSecured
  public RestAPIResultVO queryPacketFileDirectory(
      @RequestParam(name = "type", defaultValue = "FILE") String type,
      @RequestParam(name = "filename", required = false) String filename,
      @RequestParam(name = "count", required = false, defaultValue = "100") int count) {
    return RestAPIResultVO
        .resultSuccess(packetAnalysisTaskService.queryPacketFileDirectory(type, filename, count));
  }

  @PostMapping("/packet-analysis-tasks")
  @RestApiSecured
  public RestAPIResultVO savePacketAnalysisTask(
      @RequestBody @Validated PacketAnalysisTaskRestAPIVO packetAnalysisTaskVO,
      HttpServletRequest request) {
    String mode = packetAnalysisTaskVO.getMode();
    if (!MODE.contains(mode)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("参数不合法").build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    PacketAnalysisTaskBO packetAnalysisTaskBO = new PacketAnalysisTaskBO();

    try {
      packetAnalysisTaskBO
          .setSendPolicyIds(CollectionUtils.isEmpty(packetAnalysisTaskVO.getSendPolicyIds()) ? ""
              : CsvUtils.convertCollectionToCSV(packetAnalysisTaskVO.getSendPolicyIds()));
      packetAnalysisTaskBO.setName(packetAnalysisTaskVO.getName());
      packetAnalysisTaskBO.setMode(packetAnalysisTaskVO.getMode());
      List<String> filePath = packetAnalysisTaskVO.getFilePath();
      packetAnalysisTaskBO.setFilePath(
          CollectionUtils.isEmpty(filePath) ? "" : JsonHelper.serialize(filePath, false));
      Map<String, Object> configuration = packetAnalysisTaskVO.getConfiguration();
      packetAnalysisTaskBO.setConfiguration(
          MapUtils.isEmpty(configuration) ? "" : JsonHelper.serialize(configuration, false));
      packetAnalysisTaskService.savePacketAnalysisTask(packetAnalysisTaskBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, packetAnalysisTaskBO, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(packetAnalysisTaskBO.getId());
  }

  @DeleteMapping("/packet-analysis-tasks/{id}")
  @RestApiSecured
  public RestAPIResultVO deletePacketAnalysisTasks(
      @PathVariable @NotEmpty(message = "删除离线分析任务时传入的id不能为空") String id,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    PacketAnalysisTaskBO packetAnalysisTask = null;
    try {
      packetAnalysisTask = packetAnalysisTaskService.deletePacketAnalysisTask(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, packetAnalysisTask, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }

  @GetMapping("/packet-analysis-tasks/upload-urls")
  @RestApiSecured
  public RestAPIResultVO queryPacketFileUploadUrl(@RequestParam String name,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    PacketAnalysisSubTaskBO packetAnalysisSubTask = new PacketAnalysisSubTaskBO();
    packetAnalysisSubTask.setName(name);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, packetAnalysisSubTask, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO
        .resultSuccess(packetAnalysisTaskService.queryFileUploadUrl(request, userBO.getId()));
  }

  @GetMapping("/packets/file-urls")
  @RestApiSecured
  public RestAPIResultVO queryFlowPacketDownloadUrl(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DOWNLOAD, queryVO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO
        .resultSuccess(packetAnalysisService.queryFlowPacketDownloadUrl(queryVO, request));
  }

  @GetMapping("/packet-analysis-tasks/logs")
  @RestApiSecured
  public RestAPIResultVO queryPacketAnalysisLogs(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "taskId", required = false) String taskId,
      @RequestParam(name = "subTaskId", required = false) String subTaskId) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Sort.Order(Sort.Direction.DESC, "arise_time")));

    return RestAPIResultVO
        .resultSuccess(packetAnalysisTaskService.queryPacketAnalysisLog(page, taskId, subTaskId));
  }

  private Map<String, Object> packetAnalysisTask2Map(PacketAnalysisTaskBO packetAnalysisTask) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", packetAnalysisTask.getId());
    map.put("name", packetAnalysisTask.getName());
    map.put("mode", packetAnalysisTask.getMode());
    map.put("modeText", packetAnalysisTask.getModeText());
    map.put("source", packetAnalysisTask.getSource());
    map.put("sourceText", packetAnalysisTask.getSourceText());
    map.put("filePath", packetAnalysisTask.getFilePath());
    map.put("configuration", packetAnalysisTask.getConfiguration());
    map.put("status", packetAnalysisTask.getStatus());
    map.put("statusText", packetAnalysisTask.getStatusText());
    map.put("createTime", packetAnalysisTask.getCreateTime());
    map.put("operatorId", packetAnalysisTask.getOperatorId());
    return map;
  }

  private Map<String, Object> packetAnalysisSubTask2Map(
      PacketAnalysisSubTaskBO packetAnalysisSubTask) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", packetAnalysisSubTask.getId());
    map.put("name", packetAnalysisSubTask.getName());
    map.put("taskId", packetAnalysisSubTask.getTaskId());
    map.put("packetStartTime", packetAnalysisSubTask.getPacketStartTime());
    map.put("packetEndTime", packetAnalysisSubTask.getPacketEndTime());
    map.put("size", packetAnalysisSubTask.getSize());
    map.put("status", packetAnalysisSubTask.getStatus());
    map.put("executionProgress", packetAnalysisSubTask.getExecutionProgress());
    return map;
  }

}
