package com.machloop.fpc.manager.analysis.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.analysis.bo.StandardProtocolBO;
import com.machloop.fpc.manager.analysis.service.StandardProtocolService;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolCreationVO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolModificationVO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/analysis")
public class StandardProtocolController {

  @Autowired
  private StandardProtocolService standardProtocolService;

  @GetMapping("/standard-protocols")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryStandardProtocols(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      StandardProtocolQueryVO queryVO) {

    Sort sort = new Sort(Lists.newArrayList(new Order(Sort.Direction.DESC, "create_time"),
        new Order(Sort.Direction.ASC, "l7_protocol_id")));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<StandardProtocolBO> standardProtocolsPage = standardProtocolService
        .queryStandardProtocols(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(standardProtocolsPage.getSize());
    for (StandardProtocolBO standardProtocol : standardProtocolsPage) {
      resultList.add(standardProtocolBO2Map(standardProtocol, false));
    }

    return new PageImpl<>(resultList, page, standardProtocolsPage.getTotalElements());
  }

  @GetMapping("/standard-protocols/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryStandardProtocols(StandardProtocolQueryVO queryVO) {
    List<StandardProtocolBO> standardProtocolsList = standardProtocolService
        .queryStandardProtocols(queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(standardProtocolsList.size());
    for (StandardProtocolBO standardProtocol : standardProtocolsList) {
      resultList.add(standardProtocolBO2Map(standardProtocol, false));
    }
    return resultList;
  }

  @GetMapping("/standard-protocols/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryStandardProtocol(@PathVariable String id) {
    return standardProtocolBO2Map(standardProtocolService.queryStandardProtocol(id), true);
  }

  @PostMapping("/standard-protocols")
  @Secured({"PERM_USER"})
  public void saveStandardProtocol(@Validated StandardProtocolCreationVO standardProtocolVO) {
    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    BeanUtils.copyProperties(standardProtocolVO, standardProtocolBO);

    StandardProtocolBO standardProtocol = standardProtocolService
        .saveStandardProtocol(standardProtocolBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, standardProtocol);
  }

  @PutMapping("/standard-protocols/{id}")
  @Secured({"PERM_USER"})
  public void updateStandardProtocol(
      @PathVariable @NotEmpty(message = "修改协议配置时传入的id不能为空") String id,
      @Validated StandardProtocolModificationVO standardProtocolVO) {
    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    BeanUtils.copyProperties(standardProtocolVO, standardProtocolBO);

    StandardProtocolBO standardProtocol = standardProtocolService.updateStandardProtocol(id,
        standardProtocolBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, standardProtocol);
  }

  @DeleteMapping("/standard-protocols/{id}")
  @Secured({"PERM_USER"})
  public void deleteStandardProtocol(
      @PathVariable @NotEmpty(message = "删除协议配置时传入的id不能为空") String id) {
    StandardProtocolBO standardProtocolBO = standardProtocolService.deleteStandardProtocol(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, standardProtocolBO);
  }

  private Map<String, Object> standardProtocolBO2Map(StandardProtocolBO standardProtocol,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", standardProtocol.getId());
    map.put("l7ProtocolId", standardProtocol.getL7ProtocolId());
    map.put("ipProtocol", standardProtocol.getIpProtocol());
    map.put("port", standardProtocol.getPort());
    map.put("source", standardProtocol.getSource());
    map.put("sourceText", standardProtocol.getSourceText());

    if (isDetail) {
      map.put("createTime", standardProtocol.getCreateTime());
      map.put("updateTime", standardProtocol.getUpdateTime());
    }

    return map;
  }
}
