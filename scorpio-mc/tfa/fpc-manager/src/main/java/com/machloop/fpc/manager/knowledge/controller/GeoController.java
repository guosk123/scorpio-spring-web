package com.machloop.fpc.manager.knowledge.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.knowledge.bo.GeoCityBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoIpSettingBO;
import com.machloop.fpc.manager.knowledge.bo.GeoKnowledgeInfoBO;
import com.machloop.fpc.manager.knowledge.bo.GeoProvinceBO;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.vo.GeoCustomCountryCreationVO;
import com.machloop.fpc.manager.knowledge.vo.GeoCustomCountryModificationVO;
import com.machloop.fpc.manager.knowledge.vo.GeoIpSettingModificationVO;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2020年12月31日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class GeoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoController.class);

  @Value("${file.geoip.custom.country.template.path}")
  private String geoipCustomCountryTemplatePath;

  @Autowired
  private GeoService geoService;

  /**
   * GEOIP规则库
   */
  @GetMapping("/geolocation/knowledge-infos")
  @Secured({"PERM_USER"})
  public Map<String, String> queryGeoKnowledgeInfo() {
    Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    GeoKnowledgeInfoBO infoBO = geoService.queryGeoKnowledgeInfos();
    resultMap.put("version", infoBO.getVersion());
    resultMap.put("releaseDate", DateUtils.toStringISO8601(infoBO.getReleaseDate()));
    resultMap.put("uploadDate", DateUtils.toStringISO8601(infoBO.getImportDate()));
    return resultMap;
  }

  @GetMapping("/geolocation/rules")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryGeolocations() {
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> locations = geoService.queryGeolocations();
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("countryList", locations.getT1());
    result.put("customCountryList", geoService.queryCustomCountrys());
    result.put("provinceList", locations.getT2());
    result.put("cityList", locations.getT3());

    return result;
  }

  @PostMapping("/geolocation/knowledges")
  @Secured({"PERM_USER"})
  public void importKnowledges(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    GeoKnowledgeInfoBO knowledgeBO = geoService.importGeoKnowledges(file);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO);
  }

  /**
   * 自定义地区导入导出
   * @param request
   * @param response
   */
  @GetMapping("/geolocation/as-export")
  @Secured({"PERM_USER"})
  public void exportCustomCountrys(HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "geoip-custom-country.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = geoService.exportCustomCountrys();

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export custom country error ", e);
    }
  }

  @GetMapping("/geolocation/as-template")
  @Secured({"PERM_USER"})
  public void downloadCustomCountryTemplate(HttpServletRequest request,
      HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "自定义地区导入模板.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(new File(geoipCustomCountryTemplatePath))) {
      int len = 0;
      byte[] buffer = new byte[1024];
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export custom sa template error ", e);
    }
  }

  @PostMapping("/geolocation/as-import")
  @Secured({"PERM_USER"})
  public void importCustomRule(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    geoService.importCustomCountrys(file, LoggedUserContext.getCurrentUser().getId());
  }

  /**
   * 自定义地区
   */
  @GetMapping("/geolocation/custom-countrys/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryGeoCustomCountry(@PathVariable String id) {
    GeoCustomCountryBO customCountry = geoService.queryCustomCountry(id);
    return geoCustomCountryBO2Map(customCountry);
  }

  @PostMapping("/geolocation/custom-countrys")
  @Secured({"PERM_USER"})
  public void saveGeoCustomCountry(@Validated GeoCustomCountryCreationVO customCountryVO) {
    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    BeanUtils.copyProperties(customCountryVO, customCountryBO);

    GeoCustomCountryBO customCountry = geoService.saveCustomCountry(customCountryBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customCountry);
  }

  @PutMapping("/geolocation/custom-countrys/{id}")
  @Secured({"PERM_USER"})
  public void updateGeoCustomCountry(
      @PathVariable @NotEmpty(message = "修改自定义地区时传入的id不能为空") String id,
      @Validated GeoCustomCountryModificationVO customCountryVO) {
    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    BeanUtils.copyProperties(customCountryVO, customCountryBO);

    GeoCustomCountryBO updateCustomCountry = geoService.updateCustomCountry(id, customCountryBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, updateCustomCountry);
  }

  @PutMapping("/geolocation/ip-settings")
  @Secured({"PERM_USER"})
  public void updateLocationIp(@Validated GeoIpSettingModificationVO modificationVO) {
    GeoIpSettingBO geoIpSetting = new GeoIpSettingBO();
    BeanUtils.copyProperties(modificationVO, geoIpSetting);

    geoService.updateGeoIpSetting(geoIpSetting, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, geoIpSetting);
  }

  @DeleteMapping("/geolocation/custom-countrys/{id}")
  @Secured({"PERM_USER"})
  public void deleteGeoCustomCountry(
      @PathVariable @NotEmpty(message = "删除自定义地区时传入的id不能为空") String id) {
    GeoCustomCountryBO customCountry = geoService.deleteCustomCountry(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customCountry);
  }

  private Map<String, Object> geoCustomCountryBO2Map(GeoCustomCountryBO customCountryBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customCountryBO.getId());
    map.put("name", customCountryBO.getName());
    map.put("countryId", customCountryBO.getCountryId());
    map.put("longitude", customCountryBO.getLongitude());
    map.put("latitude", customCountryBO.getLatitude());
    map.put("ipAddress", customCountryBO.getIpAddress());
    map.put("description", customCountryBO.getDescription());
    map.put("createTime", customCountryBO.getCreateTime());
    map.put("updateTime", customCountryBO.getUpdateTime());

    return map;
  }

}
