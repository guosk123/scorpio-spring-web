package com.machloop.fpc.cms.center.restapi;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCityBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoIpSettingBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoKnowledgeInfoBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoProvinceBO;
import com.machloop.fpc.cms.center.knowledge.service.GeoService;
import com.machloop.fpc.cms.center.knowledge.vo.GeoCustomCountryCreationVO;
import com.machloop.fpc.cms.center.knowledge.vo.GeoCustomCountryModificationVO;
import com.machloop.fpc.cms.center.knowledge.vo.GeoIpSettingModificationVO;
import com.machloop.fpc.cms.center.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月21日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-cms-v1/appliance")
public class GeoRestAPIController {

  private static final Range<Double> RANGE_LONGITUDE = Range.closed(-180.00, 180.00);
  private static final Range<Double> RANGE_LATITUDE = Range.closed(-90.00, 90.00);

  @Autowired
  private GeoService geoService;

  @Autowired
  private UserService userService;

  @GetMapping("/geolocations")
  @RestApiSecured
  public RestAPIResultVO queryGeolocations() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    List<Map<String, Object>> countryList = geolocations.getT1().stream().map(country -> {
      Map<String, Object> countryMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      countryMap.put("countryId", country.getCountryId());
      countryMap.put("countryName", country.getNameText());

      return countryMap;
    }).collect(Collectors.toList());
    countryList.addAll(geoService.queryCustomCountrys().stream().map(country -> {
      Map<String, Object> countryMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      countryMap.put("countryId", country.getCountryId());
      countryMap.put("countryName", country.getName());

      return countryMap;
    }).collect(Collectors.toList()));
    result.put("countrys", countryList);

    List<Map<String, Object>> provinceList = geolocations.getT2().stream().map(province -> {
      Map<String, Object> provinceMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      provinceMap.put("countryId", province.getCountryId());
      provinceMap.put("provinceId", province.getProvinceId());
      provinceMap.put("provinceName", province.getNameText());

      return provinceMap;
    }).collect(Collectors.toList());
    result.put("provinces", provinceList);

    List<Map<String, Object>> cityList = geolocations.getT3().stream().map(city -> {
      Map<String, Object> cityMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      cityMap.put("countryId", city.getCountryId());
      cityMap.put("provinceId", city.getProvinceId());
      cityMap.put("cityId", city.getCityId());
      cityMap.put("cityName", city.getNameText());

      return cityMap;
    }).collect(Collectors.toList());
    result.put("citys", cityList);

    return RestAPIResultVO.resultSuccess(result);
  }

  /**
   * GEOIP规则库导入
   */
  @PostMapping("/geolocation/knowledges")
  @RestApiSecured
  public RestAPIResultVO importKnowledges(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    GeoKnowledgeInfoBO knowledgeBO = geoService.importGeoKnowledges(file);

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO, userBO.getFullname(),
        userBO.getName());

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
    if (StringUtils.isBlank(modificationVO.getCountryId())) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg("countryId不能为空").build();
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
      return new RestAPIResultVO.Builder(FpcCmsConstants.OBJECT_NOT_FOUND_CODE).msg("地区ID不存在或非叶子节点")
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
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    GeoCustomCountryBO customCountry = new GeoCustomCountryBO();
    try {
      customCountry = geoService.deleteCustomCountry(id, userBO.getId(), true);
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
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    try {
      if (!RANGE_LONGITUDE.contains(Double.valueOf(longitude))) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("地区经度不在有效范围内： %s", longitude)).build();
      }
      if (!RANGE_LATITUDE.contains(Double.valueOf(latitude))) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("地区纬度不在有效范围内： %s", latitude)).build();
      }
    } catch (NumberFormatException e) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("地区经纬度数值格式不合法")
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
