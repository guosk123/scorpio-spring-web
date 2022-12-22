package com.machloop.fpc.cms.center.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.IpConversationsHistoryBO;
import com.machloop.fpc.cms.center.appliance.service.IpConversationsHistoryService;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryModificationVO;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/11
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class IpConversationsHistoryController {

  @Autowired
  private IpConversationsHistoryService ipConversationsHistoryService;


  @GetMapping("/ip-conversations/history")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryIpConversationsHistories(
      IpConversationsHistoryQueryVO queryVO) {
    List<IpConversationsHistoryBO> ipConversationsHistoryBOS = ipConversationsHistoryService
        .queryIpConversationsHistories(queryVO);
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(ipConversationsHistoryBOS.size());
    for (IpConversationsHistoryBO ipConversationsHistoryBO : ipConversationsHistoryBOS) {
      resultList.add(ipConversationsHistoryBO2Map(ipConversationsHistoryBO));
    }
    return resultList;

  }

  @GetMapping("/ip-conversations/history/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIpConversationsHistory(
      @PathVariable @NotEmpty(message = "历史画布id不能为空") String id) {
    IpConversationsHistoryBO ipConversationsHistoryBO = ipConversationsHistoryService
        .queryIpConversationsHistory(id);

    return ipConversationsHistoryBO2Map(ipConversationsHistoryBO);

  }

  @PostMapping("/ip-conversations/history")
  @Secured({"PERM_USER"})
  public void saveIpConversationsHistory(
      @Validated IpConversationsHistoryCreationVO ipConversationsHistoryVO) {
    IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
    BeanUtils.copyProperties(ipConversationsHistoryVO, ipConversationsHistoryBO);

    ipConversationsHistoryBO = ipConversationsHistoryService.saveIpConversationsHistory(
        ipConversationsHistoryBO, LoggedUserContext.getCurrentUser().getId());


    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, ipConversationsHistoryBO);
  }

  @PutMapping("/ip-conversations/history/{id}")
  @Secured({"PERM_USER"})
  public void updateIpConversationsHistory(
      @PathVariable @NotEmpty(message = "修改历史画布时传入的id不能为空") String id,
      @Validated IpConversationsHistoryModificationVO ipConversationsHistoryVO) {
    IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
    BeanUtils.copyProperties(ipConversationsHistoryVO, ipConversationsHistoryBO);
    ipConversationsHistoryBO.setId(id);
    IpConversationsHistoryBO ipConversationsHistory = ipConversationsHistoryService
        .updateIpConversationsHistory(ipConversationsHistoryBO, id,
            LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, ipConversationsHistory);
  }

  @DeleteMapping("/ip-conversations/history/{id}")
  @Secured({"PERM_USER"})
  public void deleteIpConversationsHistory(
      @PathVariable @NotEmpty(message = "删除历史画布时传入的id不能为空") String id) {

    IpConversationsHistoryBO ipConversationsHistoryBO = ipConversationsHistoryService
        .deleteIpConversationsHistory(id, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, ipConversationsHistoryBO);
  }

  @DeleteMapping("/ip-conversation/history/{id}")
  @Secured({"PERM_USER"})
  public void deleteIpConversationHistory(
      @PathVariable @NotEmpty(message = "删除历史画布时传入的id不能为空") String id,
      @Validated IpConversationsHistoryModificationVO ipConversationsHistoryVO) {
    IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
    BeanUtils.copyProperties(ipConversationsHistoryVO, ipConversationsHistoryBO);
    ipConversationsHistoryBO.setId(id);
    IpConversationsHistoryBO ipConversationsHistory = ipConversationsHistoryService
        .deleteIpConversationHistory(ipConversationsHistoryBO, id,
            LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, ipConversationsHistory);

  }

  private Map<String, Object> ipConversationsHistoryBO2Map(
      IpConversationsHistoryBO ipConversationsHistoryBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", ipConversationsHistoryBO.getId());
    map.put("name", ipConversationsHistoryBO.getName());
    map.put("data", ipConversationsHistoryBO.getData());

    return map;

  }
}
