package com.machloop.fpc.manager.appliance.controller;

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
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.IpLabelBO;
import com.machloop.fpc.manager.appliance.service.IpLabelService;
import com.machloop.fpc.manager.appliance.vo.IpLabelCreationVO;
import com.machloop.fpc.manager.appliance.vo.IpLabelModificationVO;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 10:43 AM,cms
 * @version 1.0
 */
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class IpLabelController {

  @Autowired
  private IpLabelService ipLabelService;

  @GetMapping("/ip-label")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryIpLabel(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "") String name,
      @RequestParam(required = false, defaultValue = "") String category) {
    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<IpLabelBO> labelBOPage = ipLabelService.queryIpLabels(page, name, category);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(labelBOPage.getSize());
    for (IpLabelBO ipLabelBO : labelBOPage) {
      resultList.add(ipLabelBO2Map(ipLabelBO, true));
    }

    return new PageImpl<>(resultList, page, labelBOPage.getTotalElements());
  }

  @GetMapping("/ip-label/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryIpLabels() {

    List<IpLabelBO> ipLabelBOS = ipLabelService.queryIpLabels();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(ipLabelBOS.size());
    ipLabelBOS.forEach(ipLabelBO -> resultList.add(ipLabelBO2Map(ipLabelBO, false)));
    return resultList;
  }

  @GetMapping("/ip-label/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIpLabel(@PathVariable String id) {
    return ipLabelBO2Map(ipLabelService.queryIpLabel(id), true);
  }

  @GetMapping("/ip-label/statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIpLabelCategory() {

    return ipLabelService.queryIpLabelCategory();
  }

  @PostMapping("/ip-label")
  @Secured({"PERM_USER"})
  public void saveIpLabel(@Validated IpLabelCreationVO ipLabelCreationVO) {

    IpLabelBO iplabelBO = new IpLabelBO();
    BeanUtils.copyProperties(ipLabelCreationVO, iplabelBO);

    IpLabelBO ipLabelBO = ipLabelService.saveIpLabel(iplabelBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, ipLabelBO);
  }

  @PutMapping("/ip-label/{id}")
  @Secured({"PERM_USER"})
  public void updateIpLabel(@PathVariable @NotEmpty(message = "修改标签时传入的id不能为空") String id,
      @Validated IpLabelModificationVO ipLabelModificationVO) {
    IpLabelBO ipLabelBO = new IpLabelBO();
    BeanUtils.copyProperties(ipLabelModificationVO, ipLabelBO);

    IpLabelBO ipLabel = ipLabelService.updateIpLabel(id, ipLabelBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, ipLabel);
  }

  @DeleteMapping("/ip-label/{id}")
  @Secured({"PERM_USER"})
  public void deleteIpLabel(@PathVariable @NotEmpty(message = "删除标签时传入的id不能为空") String id) {

    IpLabelBO ipLabelBO = ipLabelService.deleteIpLabel(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, ipLabelBO);
  }

  private static Map<String, Object> ipLabelBO2Map(IpLabelBO ipLabelBO, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", ipLabelBO.getId());
    map.put("name", ipLabelBO.getName());
    map.put("ipAddress", ipLabelBO.getIpAddress());
    map.put("category", ipLabelBO.getCategory());
    map.put("description", ipLabelBO.getDescription());

    if (isDetail) {
      map.put("createTime", ipLabelBO.getCreateTime());
      map.put("updateTime", ipLabelBO.getUpdateTime());
    }

    return map;
  }
}
