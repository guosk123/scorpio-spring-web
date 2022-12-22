package com.machloop.fpc.npm.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisSubTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskLogBO;
import com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService;
import com.machloop.fpc.npm.appliance.vo.PacketAnalysisTaskCreationVO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class PacketAnalysisTaskController {

  @Autowired
  private PacketAnalysisTaskService packetAnalysisTaskService;

  @GetMapping("/packet-analysis-tasks")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryPacketAnalysisTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "status", required = false) String status) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Order(Sort.Direction.DESC, "create_time")));

    Page<PacketAnalysisTaskBO> packetAnalysisTaskPage = packetAnalysisTaskService
        .queryPacketAnalysisTasks(page, name, status);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(packetAnalysisTaskPage.getSize());
    packetAnalysisTaskPage
        .forEach(packetAnalysisTask -> resultList.add(packetAnalysisTask2Map(packetAnalysisTask)));

    return new PageImpl<>(resultList, page, packetAnalysisTaskPage.getTotalElements());
  }

  @GetMapping("/packet-analysis-tasks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryPacketAnalysisTask(
      @NotEmpty(message = "离线分析任务ID不能为空") @PathVariable String id) {
    PacketAnalysisTaskBO packetAnalysisTaskBO = packetAnalysisTaskService
        .queryPacketAnalysisTask(id);

    return packetAnalysisTask2Map(packetAnalysisTaskBO);
  }

  @GetMapping("/packet-analysis-tasks/upload-urls")
  @Secured({"PERM_USER"})
  public String queryPacketFileUploadUrl(@RequestParam String name, HttpServletRequest request) {

    PacketAnalysisSubTaskBO packetAnalysisSubTask = new PacketAnalysisSubTaskBO();
    packetAnalysisSubTask.setName(name);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, packetAnalysisSubTask);

    return packetAnalysisTaskService.queryFileUploadUrl(request,
        LoggedUserContext.getCurrentUser().getId());
  }

  @GetMapping("/packet-file-directory")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryPacketFileDirectory(
      @RequestParam(name = "type", defaultValue = "FILE") String type,
      @RequestParam(name = "filename", required = false) String filename,
      @RequestParam(name = "count", required = false, defaultValue = "100") int count) {
    return packetAnalysisTaskService.queryPacketFileDirectory(type, filename, count);
  }

  @GetMapping("/packet-analysis-tasks/logs")
  @Secured({"PERM_USER"})
  public Page<PacketAnalysisTaskLogBO> queryPacketAnalysisLogs(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "taskId", required = false) String taskId,
      @RequestParam(name = "subTaskId", required = false) String subTaskId) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Order(Sort.Direction.DESC, "arise_time")));

    return packetAnalysisTaskService.queryPacketAnalysisLog(page, taskId, subTaskId);
  }

  @PostMapping("/packet-analysis-tasks")
  @Secured({"PERM_USER"})
  public void savePacketAnalysisTask(@Validated PacketAnalysisTaskCreationVO packetAnalysisTaskVO) {
    PacketAnalysisTaskBO packetAnalysisTaskBO = new PacketAnalysisTaskBO();
    BeanUtils.copyProperties(packetAnalysisTaskVO, packetAnalysisTaskBO);
    packetAnalysisTaskService.savePacketAnalysisTask(packetAnalysisTaskBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, packetAnalysisTaskBO);
  }

  @DeleteMapping("/packet-analysis-tasks/{id}")
  @Secured({"PERM_USER"})
  public void deletePacketAnalysisTasks(
      @PathVariable @NotEmpty(message = "删除离线分析任务时传入的id不能为空") String id) {
    PacketAnalysisTaskBO packetAnalysisTask = packetAnalysisTaskService.deletePacketAnalysisTask(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, packetAnalysisTask);
  }

  @GetMapping("/packet-analysis-subtasks")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryPacketAnalysisSubTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "taskId", required = false) String taskId,
      @RequestParam(name = "source", required = false) String source) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Order(Sort.Direction.DESC, "create_time")));

    Page<PacketAnalysisSubTaskBO> packetAnalysisSubTaskPage = packetAnalysisTaskService
        .queryPacketAnalysisSubTasks(page, name, taskId, source);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(packetAnalysisSubTaskPage.getSize());
    packetAnalysisSubTaskPage.forEach(
        packetAnalysisSubTask -> resultList.add(packetAnalysisSubTask2Map(packetAnalysisSubTask)));

    return new PageImpl<>(resultList, page, packetAnalysisSubTaskPage.getTotalElements());
  }

  @GetMapping("/packet-analysis-subtasks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryPacketAnalysisSubTask(
      @NotEmpty(message = "离线分析子任务ID不能为空") @PathVariable String id) {
    PacketAnalysisSubTaskBO packetAnalysisSubTaskBO = packetAnalysisTaskService
        .queryPacketAnalysisSubTask(id);

    return packetAnalysisSubTask2Map(packetAnalysisSubTaskBO);
  }

  @DeleteMapping("/packet-analysis-subtasks/{id}")
  @Secured({"PERM_USER"})
  public void deletePacketAnalysisSubTasks(
      @PathVariable @NotEmpty(message = "删除离线分析子任务时传入的id不能为空") String id) {
    PacketAnalysisSubTaskBO packetAnalysisSubTask = packetAnalysisTaskService
        .deletePacketAnalysisSubTask(id, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, packetAnalysisSubTask);
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
    map.put("executionTrace", packetAnalysisTask.getExecutionTrace());
    map.put("status", packetAnalysisTask.getStatus());
    map.put("statusText", packetAnalysisTask.getStatusText());
    map.put("createTime", packetAnalysisTask.getCreateTime());
    map.put("operatorId", packetAnalysisTask.getOperatorId());
    map.put("sendPolicyIds", packetAnalysisTask.getSendPolicyIds());
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
    map.put("filePath", packetAnalysisSubTask.getFilePath());
    map.put("status", packetAnalysisSubTask.getStatus());
    map.put("statusText", packetAnalysisSubTask.getStatusText());
    map.put("executionProgress", packetAnalysisSubTask.getExecutionProgress());
    map.put("executionResult", packetAnalysisSubTask.getExecutionResult());
    map.put("createTime", packetAnalysisSubTask.getCreateTime());
    map.put("operatorId", packetAnalysisSubTask.getOperatorId());
    return map;
  }

}
