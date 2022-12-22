package com.machloop.fpc.manager.appliance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.appliance.vo.HostGroupCreationVO;
import com.machloop.fpc.manager.appliance.vo.HostGroupModificationVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class HostGroupController {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(HostGroupController.class);
  @Value("${file.hostGroup.template.path}")
  private String hostGroupTemplatePath;
  @Autowired
  private HostGroupService hostGroupService;

  @GetMapping("/host-groups")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryHostGroups(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "") String name,
      @RequestParam(required = false, defaultValue = "") String description) {

    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<HostGroupBO> hostGroupPage = hostGroupService.queryHostGroups(page, name, description);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(hostGroupPage.getSize());
    for (HostGroupBO hostGroup : hostGroupPage) {
      resultList.add(hostGroupBO2Map(hostGroup, true));
    }

    return new PageImpl<>(resultList, page, hostGroupPage.getTotalElements());
  }

  @GetMapping("/host-groups/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryHostGroups() {
    List<HostGroupBO> hostGroupBOs = hostGroupService.queryHostGroups();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(hostGroupBOs.size());
    hostGroupBOs.forEach(hostGroupBO -> resultList.add(hostGroupBO2Map(hostGroupBO, false)));
    return resultList;
  }

  @GetMapping("/host-groups/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryHostGroup(@PathVariable String id) {
    return hostGroupBO2Map(hostGroupService.queryHostGroup(id), true);
  }

  @GetMapping("/host-groups/as-template")
  @Secured({"PERM_USER"})
  public void downloadHostGroupsTemplate(HttpServletRequest request, HttpServletResponse response) {
    File templateFile = new File(hostGroupTemplatePath);

    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "地址组导入模版.csv"));
    response.resetBuffer();

    try (ServletOutputStream outputStream = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(templateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export hostGroups template error ", e);
    }
  }

  @GetMapping("/host-groups/as-export")
  @Secured({"PERM_USER"})
  public void exportHostGroups(@RequestParam(required = false) String name,
      HttpServletRequest request, HttpServletResponse response) {
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "hostGroups.csv"));
    response.resetBuffer();
    try (ServletOutputStream outputStream = response.getOutputStream();) {
      List<String> lineList = hostGroupService.exportHostGroups(name);

      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        outputStream.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export hostGroups error", e);
    }
  }

  @PostMapping("/host-groups/as-import")
  @Secured({"PERM_USER"})
  public void importHostGroups(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符：[%s]", matchingIllegalCharacters));
    }

    hostGroupService.importHostGroups(file, LoggedUserContext.getCurrentUser().getId());
  }

  @PostMapping("/host-groups")
  @Secured({"PERM_USER"})
  public void saveHostGroup(@Validated HostGroupCreationVO hostGroupVO) {
    HostGroupBO hostGroupBO = new HostGroupBO();
    BeanUtils.copyProperties(hostGroupVO, hostGroupBO);

    HostGroupBO hostGroup = hostGroupService.saveHostGroup(hostGroupBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, hostGroup);
  }

  @PutMapping("/host-groups/{id}")
  @Secured({"PERM_USER"})
  public void updateHostGroup(@PathVariable @NotEmpty(message = "修改地址组时传入的id不能为空") String id,
      @Validated HostGroupModificationVO hostGroupVO) {
    HostGroupBO hostGroupBO = new HostGroupBO();
    BeanUtils.copyProperties(hostGroupVO, hostGroupBO);

    HostGroupBO hostGroup = hostGroupService.updateHostGroup(id, hostGroupBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, hostGroup);
  }

  @DeleteMapping("/host-groups/{id}")
  @Secured({"PERM_USER"})
  public void deleteHostGroup(@PathVariable @NotEmpty(message = "删除地址组时传入的id不能为空") String id) {

    HostGroupBO hostGroupBO = hostGroupService.deleteHostGroup(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, hostGroupBO);
  }

  private static Map<String, Object> hostGroupBO2Map(HostGroupBO hostGroup, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", hostGroup.getId());
    map.put("name", hostGroup.getName());
    map.put("ipAddress", hostGroup.getIpAddress());
    map.put("description", hostGroup.getDescription());

    if (isDetail) {
      map.put("createTime", hostGroup.getCreateTime());
      map.put("updateTime", hostGroup.getUpdateTime());
    }

    return map;
  }
}
