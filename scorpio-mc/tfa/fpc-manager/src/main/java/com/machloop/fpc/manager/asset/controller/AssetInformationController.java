package com.machloop.fpc.manager.asset.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.asset.bo.AssetAlarmBO;
import com.machloop.fpc.manager.asset.bo.AssetBaselineBO;
import com.machloop.fpc.manager.asset.bo.OSNameBO;
import com.machloop.fpc.manager.asset.service.AssetConfigurationService;
import com.machloop.fpc.manager.asset.service.AssetInformationService;
import com.machloop.fpc.manager.asset.vo.AssetBaselineModificationVO;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;

/**
 * @author minjiajun
 * 
 * create at 2022年9月2日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/metric")
public class AssetInformationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetInformationController.class);

  @Autowired
  private AssetInformationService assetInformationService;

  @Autowired
  private AssetConfigurationService assetConfigurationService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @GetMapping("/asset-information")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Page<Map<String, Object>> queryAssetInformation(AssetInformationQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "ip") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(sortProperty, "ipAddress")) {
      sortProperty = "ip";
    }
    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "ip"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    queryVO.setSortProperty(sortProperty);
    return assetInformationService.queryAssetInformation(queryVO, sortProperty, sortDirection,
        page);
  }

  @GetMapping("/asset-information/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAssetInformationStatistics(AssetInformationQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
    map.put("total", assetInformationService.countAssetInformation(queryVO));
    return map;
  }

  @GetMapping("/asset-information/as-export")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public void exportAssetInformation(AssetInformationQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "ipAddress") String sortProperty,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "asset_information.csv"
        : "asset_information.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();
    try (ServletOutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(sortProperty, "ipAddress")) {
        sortProperty = "ip";
      }
      assetInformationService.exportAssetInformations(out, queryVO, fileType, sortProperty,
          sortDirection);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export asset error", e);
    }
  }

  @PostMapping("/asset-information/useful-life")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public void ModifyAssetInformationUsefulLife(
      @RequestParam(required = false, defaultValue = "7") String time) {

    globalSettingService.setValue(ManagerConstants.ASSET_USEFUL_LIFE, time);
  }

  @GetMapping("/asset-information/useful-life")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Map<String, Integer> queryAssetInformationUsefulLife() {

    Map<String, Integer> map = Maps.newHashMapWithExpectedSize(1);
    map.put("usefulLife",
        Integer.parseInt(globalSettingService.getValue(ManagerConstants.ASSET_USEFUL_LIFE)));
    return map;
  }

  @GetMapping("/asset-baseline")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Page<Map<String, Object>> queryAssetBaselines(
      @RequestParam(required = false) String ipAddress,
      @RequestParam(required = false, defaultValue = "ipAddress") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    PageRequest page = new PageRequest(pageNumber, pageSize);
    return assetConfigurationService.qureyAssetBaselines(ipAddress, sortProperty, sortDirection,
        page);
  }

  @PostMapping("/asset-baseline")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public void saveOrUpdateAssetBaselines(@Validated AssetBaselineModificationVO modificationVO) {

    AssetBaselineBO assetBaselineBO = new AssetBaselineBO();
    BeanUtils.copyProperties(modificationVO, assetBaselineBO);
    AssetBaselineBO result = assetConfigurationService.saveOrUpdateAssetBaselines(assetBaselineBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @DeleteMapping("/asset-baseline")
  @Secured({"PERM_USER"})
  public void deleteAssetBaseline(
      @RequestParam @NotEmpty(message = "删除资产基线时ip地址不能为空") String ipAddress) {

    AssetBaselineBO assetBaselineBO = assetConfigurationService.deleteAssetBaseline(ipAddress,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, assetBaselineBO);
  }

  @GetMapping("/asset-alarm")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Page<Map<String, Object>> queryAssetAlarms(
      @RequestParam(name = "ipAddress", required = false) String ipAddress,
      @RequestParam(name = "type", required = false, defaultValue = "") String type,
      @RequestParam(required = false, defaultValue = "alarm_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    if (StringUtils.equals(sortProperty, "alarmTime")) {
      sortProperty = "alarm_time";
    }

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "ip_address"), new Order("alarm_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<AssetAlarmBO> assetAlarmBOPage = assetConfigurationService.queryAssetAlarms(page,
        ipAddress, type);

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (AssetAlarmBO assetAlarmBO : assetAlarmBOPage) {
      result.add(assetAlarm2Map(assetAlarmBO));
    }
    return new PageImpl<>(result, page, result.size());
  }

  @GetMapping("/asset-device")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryAssetDevices() {
    return assetConfigurationService.queryAssetDevices();
  }

  @GetMapping("/asset-os")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryAssetOS() {

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    List<OSNameBO> osNameBOList = assetConfigurationService.queryAssetOS("");
    osNameBOList.forEach(item -> {
      result.add(osNameBO2Map(item));
    });
    return result;
  }

  private Map<String, Object> assetAlarm2Map(AssetAlarmBO assetAlarmBO) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("ipAddress", assetAlarmBO.getIpAddress());
    map.put("type", assetAlarmBO.getType());
    map.put("baseline", assetAlarmBO.getBaseline());
    map.put("current", assetAlarmBO.getCurrent());
    map.put("alarmTime", assetAlarmBO.getAlarmTime());
    return map;
  }

  private Map<String, Object> osNameBO2Map(OSNameBO osNameBO) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", osNameBO.getId());
    map.put("os", osNameBO.getOs());
    map.put("updateTime", osNameBO.getUpdateTime());
    map.put("operatorId", osNameBO.getOperatorId());
    return map;
  }
}
