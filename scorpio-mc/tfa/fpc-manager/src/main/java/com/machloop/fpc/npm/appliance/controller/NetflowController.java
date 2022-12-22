package com.machloop.fpc.npm.appliance.controller;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.appliance.bo.NetflowConfigBO;
import com.machloop.fpc.npm.appliance.bo.NetflowSourceBO;
import com.machloop.fpc.npm.appliance.service.NetflowService;
import com.machloop.fpc.npm.appliance.vo.NetflowModificationVO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月12日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class NetflowController {
  @Autowired
  private NetflowService netflowService;

  @GetMapping("/netflow-sources")
  @Secured({"PERM_USER"})
  public Page<NetflowSourceBO> queryNetflowSources(NetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.ASC, "device_name"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return netflowService.queryNetflowSources(queryVO, page);
  }
  
  @GetMapping("/netflow-sources/as-list")
  @Secured({"PERM_USER"})
  public List<NetflowConfigBO> queryNetflowsWithConfig(String keywords){
    return netflowService.queryNetflowConfigs(keywords);
  }

  @PutMapping("/netflow-sources")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public void updateNetflows(@RequestParam @NotEmpty(message = "接口信息不能为空") String netflowListJson) {

    List<NetflowModificationVO> netflowList = JsonHelper.deserialize(netflowListJson,
        new TypeReference<List<NetflowModificationVO>>() {
        }, false);
    List<NetflowConfigBO> netflowBOList = Lists.newArrayListWithCapacity(netflowList.size());
    for (NetflowModificationVO netflowVO : netflowList) {
      NetflowConfigBO netflowConfigBO = new NetflowConfigBO();
      BeanUtils.copyProperties(netflowVO, netflowConfigBO);

      netflowBOList.add(netflowConfigBO);
    }
    netflowService.batchUpdateNetflows(netflowBOList, LoggedUserContext.getCurrentUser().getId());

    StringBuilder logContent = new StringBuilder("修改接口设置：");
    netflowBOList.forEach(netflowBO -> logContent
        .append(netflowBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE)));
    LogHelper.auditOperate(logContent.toString());
  }
}
