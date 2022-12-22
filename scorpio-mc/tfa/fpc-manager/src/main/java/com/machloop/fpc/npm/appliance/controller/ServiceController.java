package com.machloop.fpc.npm.appliance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.bo.ServiceFollowBO;
import com.machloop.fpc.npm.appliance.bo.ServiceLinkBO;
import com.machloop.fpc.npm.appliance.service.ServiceService;
import com.machloop.fpc.npm.appliance.vo.ServiceCreationVO;
import com.machloop.fpc.npm.appliance.vo.ServiceModificationVO;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class ServiceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

  @Value("${file.service.template.path}")
  private String serviceTemplatePath;

  @Autowired
  private ServiceService serviceService;

  @GetMapping("/services")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryServices(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name) {
    Sort sort = new Sort(new Order(Sort.Direction.DESC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<ServiceBO> services = serviceService.queryServices(page, name);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(services.getSize());
    for (ServiceBO service : services) {
      resultList.add(serviceBO2Map(service, false));
    }

    return new PageImpl<>(resultList, page, services.getTotalElements());
  }

  @GetMapping("/services/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAllServices() {
    List<ServiceBO> services = serviceService.queryServices();

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(services.size());
    for (ServiceBO service : services) {
      resultList.add(serviceBO2Map(service, false));
    }

    return resultList;
  }

  @GetMapping("/services/follow")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryServiceFollows() {
    List<ServiceFollowBO> userFollowServices = serviceService
        .queryUserFollowService(LoggedUserContext.getCurrentUser().getId());

    List<Map<String, Object>> result = userFollowServices.stream().map(userFollowService -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("userId", userFollowService.getUserId());
      map.put("serviceId", userFollowService.getServiceId());
      map.put("networkId", userFollowService.getNetworkId());
      map.put("followTime", userFollowService.getFollowTime());

      return map;
    }).collect(Collectors.toList());

    return result;
  }

  @GetMapping("/services/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryService(@NotEmpty(message = "业务ID不能为空") @PathVariable String id) {
    ServiceBO serviceBO = serviceService.queryService(id);

    return serviceBO2Map(serviceBO, true);
  }

  @GetMapping("/services/{id}/link")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryServiceLink(
      @NotEmpty(message = "业务ID不能为空") @PathVariable String id) {
    ServiceLinkBO serviceLinkBO = serviceService.queryServiceLink(id);

    Map<String,
        Object> serviceLinkMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceLinkMap.put("serviceId", serviceLinkBO.getServiceId());
    serviceLinkMap.put("link", serviceLinkBO.getLink());
    serviceLinkMap.put("metric", serviceLinkBO.getMetric());

    return serviceLinkMap;
  }

  @GetMapping("/services/as-export")
  @Secured({"PERM_USER"})
  public void exportServices(HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "services.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = serviceService.exportServices();

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export services error ", e);
    }
  }

  @GetMapping("/services/as-template")
  @Secured({"PERM_USER"})
  public void downloadServiceTemplate(HttpServletRequest request, HttpServletResponse response) {
    File templateFile = new File(serviceTemplatePath);

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "业务导入模板.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(templateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export services template error ", e);
    }
  }

  @PostMapping("/services/as-import")
  @Secured({"PERM_USER"})
  public void importServices(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    serviceService.importServices(file, LoggedUserContext.getCurrentUser().getId());
  }

  @PostMapping("/services")
  @Secured({"PERM_USER"})
  public void saveService(@Validated ServiceCreationVO serviceCreationVO) {
    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceCreationVO, serviceBO);

    serviceBO = serviceService.saveService(serviceBO, null,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, serviceBO);
  }

  @PutMapping("/services/{id}")
  @Secured({"PERM_USER"})
  public void updateService(@PathVariable @NotEmpty(message = "修改业务时传入的id不能为空") String id,
      @Validated ServiceModificationVO serviceModificationVO) {
    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceModificationVO, serviceBO);

    serviceBO = serviceService.updateService(id, serviceBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, serviceBO);
  }

  @PutMapping("/services/{id}/link")
  @Secured({"PERM_USER"})
  public void updateServiceLink(@PathVariable @NotEmpty(message = "修改业务路径时传入的id不能为空") String id,
      @RequestParam String link, @RequestParam String metric) {
    ServiceLinkBO serviceLinkBO = new ServiceLinkBO();
    serviceLinkBO.setServiceId(id);
    serviceLinkBO.setLink(link);
    serviceLinkBO.setMetric(metric);

    serviceLinkBO = serviceService.updateServiceLink(serviceLinkBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("编辑业务路径：业务ID=" + id + ";网元链路配置=" + serviceLinkBO.getLink() + ";指标配置="
        + serviceLinkBO.getMetric());
  }

  @PutMapping("/services/follow")
  @Secured({"PERM_USER"})
  public void updateServiceFollow(@RequestParam String serviceId, @RequestParam String networkId,
      @RequestParam String state) {
    ServiceFollowBO serviceFollowBO = new ServiceFollowBO();
    serviceFollowBO.setUserId(LoggedUserContext.getCurrentUser().getId());
    serviceFollowBO.setServiceId(serviceId);
    serviceFollowBO.setNetworkId(networkId);
    serviceFollowBO.setState(state);

    serviceService.changeUserFollowState(serviceFollowBO);

    LogHelper.auditOperate("用户更改对业务的关注状态：用户=" + serviceFollowBO.getUserId() + ";业务="
        + serviceFollowBO.getServiceId() + ";网络=" + serviceFollowBO.getNetworkId() + ";关注状态="
        + (StringUtils.equals(state, Constants.BOOL_YES) ? "关注" : "取消关注"));
  }

  @DeleteMapping("/services/{id}")
  @Secured({"PERM_USER"})
  public void deleteService(@PathVariable @NotEmpty(message = "删除业务时传入的id不能为空") String id) {
    ServiceBO serviceBO = serviceService.deleteService(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, serviceBO);
  }

  private static Map<String, Object> serviceBO2Map(ServiceBO serviceBO, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", serviceBO.getId());
    map.put("name", serviceBO.getName());
    map.put("networkIds", serviceBO.getNetworkIds());
    map.put("networkNames", serviceBO.getNetworkNames());
    map.put("description", serviceBO.getDescription());
    map.put("createTime", serviceBO.getCreateTime());
    if (isDetail) {
      map.put("application", serviceBO.getApplication());
    }

    return map;
  }

}
