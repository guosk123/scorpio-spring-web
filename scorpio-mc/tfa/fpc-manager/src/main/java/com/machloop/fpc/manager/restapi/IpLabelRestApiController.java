package com.machloop.fpc.manager.restapi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.appliance.bo.IpLabelBO;
import com.machloop.fpc.manager.appliance.service.IpLabelService;
import com.machloop.fpc.manager.appliance.vo.IpLabelCreationVO;
import com.machloop.fpc.manager.appliance.vo.IpLabelModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/22 11:40 AM,cms
 * @version 1.0
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class IpLabelRestApiController {

  @Autowired
  private IpLabelService ipLabelService;

  @Autowired
  private UserService userService;

  @GetMapping("/ip-label")
  @RestApiSecured
  public RestAPIResultVO queryIplabel(
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

    return RestAPIResultVO
        .resultSuccess(new PageImpl<>(resultList, page, labelBOPage.getTotalElements()));
  }

  @GetMapping("/ip-label/as-list")
  @RestApiSecured
  public RestAPIResultVO queryIpLabels() {

    List<IpLabelBO> ipLabelBOS = ipLabelService.queryIpLabels();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(ipLabelBOS.size());
    ipLabelBOS.forEach(ipLabelBO -> resultList.add(ipLabelBO2Map(ipLabelBO, false)));
    return RestAPIResultVO.resultSuccess(resultList);
  }

  @GetMapping("/ip-label/{id}")
  @RestApiSecured
  public RestAPIResultVO queryIpLabel(@PathVariable String id) {
    return RestAPIResultVO.resultSuccess(ipLabelBO2Map(ipLabelService.queryIpLabel(id), true));
  }

  @GetMapping("/ip-label/queryIp")
  @RestApiSecured
  public RestAPIResultVO queryIpLabelIp(@RequestParam String ip) {

    return RestAPIResultVO.resultSuccess(ipLabelBO2Map(ipLabelService.queryIpLabelByIp(ip), false));
  }

  @GetMapping("/ip-label/statistics")
  @RestApiSecured
  public RestAPIResultVO queryIpLabelCategory() {

    return RestAPIResultVO.resultSuccess(ipLabelService.queryIpLabelCategory());
  }

  @PostMapping("/ip-label")
  @RestApiSecured
  public RestAPIResultVO saveIpLabel(@Validated IpLabelCreationVO ipLabelCreationVO,
      HttpServletRequest request) {

    IpLabelBO ipLabelBO = new IpLabelBO();
    BeanUtils.copyProperties(ipLabelCreationVO, ipLabelBO);
    ipLabelBO.setIpAddress(ipLabelBO.getIpAddress().trim());

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    IpLabelBO labelBO = null;
    try {
      labelBO = ipLabelService.saveIpLabel(ipLabelBO, userBO.getId());
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, labelBO);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess("IP标签保存成功:" + labelBO.getName());
  }

  @PutMapping("/ip-label/{id}")
  @RestApiSecured
  public RestAPIResultVO updateIpLabel(
      @PathVariable @NotEmpty(message = "修改标签时传入的id不能为空") String id,
      @Validated IpLabelModificationVO ipLabelModificationVO, HttpServletRequest request) {
    IpLabelBO ipLabelBO = new IpLabelBO();
    BeanUtils.copyProperties(ipLabelModificationVO, ipLabelBO);

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    IpLabelBO ipLabel = null;
    try {
      ipLabel = ipLabelService.updateIpLabel(id, ipLabelBO, userBO.getId());
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, ipLabel);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess("修改标签成功：" + ipLabel.getName());
  }

  @DeleteMapping("/ip-label/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteIpLabel(
      @PathVariable @NotEmpty(message = "删除标签时传入的id不能为空") String id, HttpServletRequest request) {

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    IpLabelBO ipLabelBO = null;
    try {
      ipLabelBO = ipLabelService.deleteIpLabel(id, userBO.getId(), false);
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, ipLabelBO);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess("删除成功: " + ipLabelBO.getName());
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
