package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.knowledge.bo.*;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.vo.GeoCustomCountryCreationVO;
import com.machloop.fpc.manager.knowledge.vo.GeoCustomCountryModificationVO;
import com.machloop.fpc.manager.knowledge.vo.GeoIpSettingModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2021年9月8日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class GeoRestAPIController {

  private static final Range<Double> RANGE_LONGITUDE = Range.closed(-180.00, 180.00);
  private static final Range<Double> RANGE_LATITUDE = Range.closed(-90.00, 90.00);

  @Autowired
  private GeoService geoService;

  @Autowired
  private UserService userService;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * GEOIP规则库导入
   */
  @PostMapping("/geolocation/knowledges")
  @RestApiSecured
  public RestAPIResultVO importKnowledges(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    GeoKnowledgeInfoBO knowledgeBO = null;
    try {
      knowledgeBO = geoService.importGeoKnowledges(file);

      // 获取用户信息
      UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO, userBO.getFullname(),
          userBO.getName());

    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess(knowledgeBO);
  }

  /**
   * 自定义地区
   */
  @GetMapping("/geolocation/custom-countrys")
  @RestApiSecured
  public RestAPIResultVO querySaCustomRule() {
    List<GeoCustomCountryBO> customCountrys = geoService.queryCustomCountrys();
    List<Map<String, Object>> resultList = customCountrys.stream()
        .map(customCountry -> customCountryBO2Map(customCountry)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @PostMapping("/geolocation/custom-countrys")
  @RestApiSecured
  public RestAPIResultVO saveGeoCustomCountry(
      @RequestBody @Validated GeoCustomCountryCreationVO customCountryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    RestAPIResultVO restAPIResultVO = checkParamter(bindingResult, customCountryVO.getLongitude(),
        customCountryVO.getLatitude());
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    try {
      BeanUtils.copyProperties(customCountryVO, customCountryBO);
      customCountryBO
          .setDescription(StringUtils.defaultIfBlank(customCountryBO.getDescription(), ""));
      customCountryBO = geoService.saveCustomCountry(customCountryBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customCountryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customCountryBO.getId());
  }

  @PutMapping("/geolocation/custom-countrys/{id}")
  @RestApiSecured
  public RestAPIResultVO updateGeoCustomCountry(
      @PathVariable @NotEmpty(message = "修改自定义地区时传入的id不能为空") String id,
      @RequestBody @Validated GeoCustomCountryModificationVO customCountryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    RestAPIResultVO restAPIResultVO = checkParamter(bindingResult, customCountryVO.getLongitude(),
        customCountryVO.getLatitude());
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    try {
      BeanUtils.copyProperties(customCountryVO, customCountryBO);
      customCountryBO
          .setDescription(StringUtils.defaultIfBlank(customCountryBO.getDescription(), ""));
      customCountryBO = geoService.updateCustomCountry(id, customCountryBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customCountryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customCountryBO.getId());
  }

  @PutMapping("/geolocation/ip-settings")
  @RestApiSecured
  public RestAPIResultVO updateLocationIp(
      @RequestBody @Validated GeoIpSettingModificationVO modificationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    if (StringUtils.isBlank(modificationVO.getCountryId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("countryId不能为空")
          .build();
    }

    // 获取可以配置IP的叶子节点
    List<String> leafNodes = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();
    Set<String> countryIds = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Set<String> provinceIds = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    geolocations.getT3().forEach(city -> {
      countryIds.add(city.getCountryId());
      provinceIds.add(city.getProvinceId());
      leafNodes.add(
          StringUtils.joinWith("_", city.getCountryId(), city.getProvinceId(), city.getCityId()));
    });
    geolocations.getT2().forEach(province -> {
      countryIds.add(province.getCountryId());
      if (!provinceIds.contains(province.getProvinceId())) {
        leafNodes.add(StringUtils.joinWith("_", province.getCountryId(), province.getProvinceId()));
      }
    });
    geolocations.getT1().forEach(country -> {
      if (!countryIds.contains(country.getCountryId())) {
        leafNodes.add(country.getCountryId());
      }
    });
    StringBuilder node = new StringBuilder();
    node.append(modificationVO.getCountryId());
    if (StringUtils.isNotBlank(modificationVO.getProvinceId())) {
      node.append("_");
      node.append(modificationVO.getProvinceId());
      if (StringUtils.isNotBlank(modificationVO.getCityId())) {
        node.append("_");
        node.append(modificationVO.getCityId());
      }
    }
    if (!leafNodes.contains(node.toString())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("地区ID不存在或非叶子节点")
          .build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    GeoIpSettingBO geoIpSetting = new GeoIpSettingBO();
    try {
      BeanUtils.copyProperties(modificationVO, geoIpSetting);
      geoService.updateGeoIpSetting(geoIpSetting, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, geoIpSetting, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(geoIpSetting);
  }

  @DeleteMapping("/geolocation/custom-countrys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteGeoCustomCountry(
      @PathVariable @NotEmpty(message = "删除自定义地区时传入的id不能为空") String id,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    GeoCustomCountryBO customCountry = new GeoCustomCountryBO();
    try {
      customCountry = geoService.deleteCustomCountry(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customCountry, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParamter(BindingResult bindingResult, String longitude,
      String latitude) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    try {
      if (!RANGE_LONGITUDE.contains(Double.valueOf(longitude))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("地区经度不在有效范围内： %s", longitude)).build();
      }
      if (!RANGE_LATITUDE.contains(Double.valueOf(latitude))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("地区纬度不在有效范围内： %s", latitude)).build();
      }
    } catch (NumberFormatException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("地区经纬度数值格式不合法")
          .build();
    }

    return null;
  }

  private static Map<String, Object> customCountryBO2Map(GeoCustomCountryBO customCountryBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customCountryBO.getId());
    map.put("name", customCountryBO.getName());
    map.put("ipAddress", customCountryBO.getIpAddress());
    map.put("longtitude", customCountryBO.getLongitude());
    map.put("latitude", customCountryBO.getLatitude());
    map.put("countryId", customCountryBO.getCountryId());
    map.put("description", customCountryBO.getDescription());
    map.put("createTime", customCountryBO.getCreateTime());
    map.put("updateTime", customCountryBO.getUpdateTime());
    return map;
  }
}
