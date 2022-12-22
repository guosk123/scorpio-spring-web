package com.machloop.fpc.npm.appliance.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.service.WebSharkService;
import com.machloop.fpc.manager.knowledge.bo.GeoCityBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoProvinceBO;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.npm.appliance.service.PacketAnalysisService;
import com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年6月7日, fpc-manager
 */
@Service
public class PacketAnalysisServiceImpl implements PacketAnalysisService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PacketAnalysisServiceImpl.class);

  private Map<String, Tuple2<Date, String>> fileMap = Maps.newConcurrentMap();

  @Value("${fpc.engine.rest.server.protocol}")
  private String fileServerProtocol;
  @Value("${fpc.engine.rest.server.host}")
  private String fileServerHost;
  @Value("${fpc.engine.rest.server.port}")
  private String fileServerPort;

  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private WebSharkService webSharkService;

  @Autowired
  private SaService saService;
  @Autowired
  private SaProtocolService saProtocolService;
  @Autowired
  private GeoService geoService;

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#queryFlowPackets(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryFlowPackets(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_PACKETS_QUERY;
      String serverIp = fileServerHost;
      String[] ipList = StringUtils.split(fileServerHost, ",");

      if (ipList.length > 1) {
        if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
          serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
        } else {
          serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
        }
      }

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append(queryVO.toParamUrlWithPlaceholder().getT1());
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      requestUrl = url.toString();
      LOGGER.info("invoke packet search rest api:{}, params: {}.", requestUrl,
          JsonHelper.serialize(queryVO.toParamUrlWithPlaceholder().getT2()));

      String resultStr = restTemplate.getForObject(url.toString(), String.class,
          queryVO.toParamUrlWithPlaceholder().getT2());
      if (StringUtils.isBlank(resultStr)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，未获取到数据包列表");
      }

      result = parseListAndPaddingDict(resultStr);
    } catch (Exception e) {
      LOGGER.warn("failed to query flow packets [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包列表异常");
    }

    return result;
  }

  private Map<String, Object> parseListAndPaddingDict(String resultStr) {
    Map<String, Object> object = JsonHelper.deserialize(resultStr,
        new TypeReference<Map<String, Object>>() {
        }, false);
    Map<String,
        Object> result = JsonHelper.deserialize(JsonHelper.serialize(object.get("result"), false),
            new TypeReference<Map<String, Object>>() {
            }, false);
    List<Map<String, Object>> list = JsonHelper.deserialize(
        JsonHelper.serialize(result.get("list"), false),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);

    // 应用字典
    Map<Integer, String> appsIdNameMapping = saService.queryAllAppsIdNameMapping();
    // 协议字典
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    list.forEach(item -> {
      item.put("applicationText",
          appsIdNameMapping.get(MapUtils.getInteger(item, "applicationId")));
      item.put("l7ProtocolText", protocolDict.get(item.get("l7ProtocolId")));
      item.put("countryIdInitiatorText", locationDict.getT1().get(item.get("countryIdInitiator")));
      item.put("provinceIdInitiatorText",
          locationDict.getT2().get(item.get("provinceIdInitiator")));
      item.put("cityIdInitiatorText", locationDict.getT3().get(item.get("cityIdInitiator")));
      item.put("countryIdResponderText", locationDict.getT1().get(item.get("countryIdResponder")));
      item.put("provinceIdResponderText",
          locationDict.getT2().get(item.get("provinceIdResponder")));
      item.put("cityIdResponderText", locationDict.getT3().get(item.get("cityIdResponder")));
    });

    result.put("list", list);
    object.put("result", result);
    return object;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#queryFlowPacketRefines(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryFlowPacketRefines(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_PACKETS_REFINE;
      String serverIp = fileServerHost;
      String[] ipList = StringUtils.split(fileServerHost, ",");

      if (ipList.length > 1) {
        if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
          serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
        } else {
          serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
        }
      }

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append(queryVO.toParamUrlWithPlaceholder().getT1());
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      requestUrl = url.toString();
      LOGGER.info("invoke packet refine rest api:{}, params: {}.", requestUrl,
          JsonHelper.serialize(queryVO.toParamUrlWithPlaceholder().getT2()));

      String resultStr = restTemplate.getForObject(url.toString(), String.class,
          queryVO.toParamUrlWithPlaceholder().getT2());
      if (StringUtils.isBlank(resultStr)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，未获取到数据包统计");
      }

      result = parseRefineAndPaddingDict(resultStr);
    } catch (Exception e) {
      LOGGER.warn("failed to query flow packets statistics [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包统计异常");
    }

    return result;
  }

  private Map<String, Object> parseRefineAndPaddingDict(String resultStr) {
    Map<String, Object> object = JsonHelper.deserialize(resultStr,
        new TypeReference<Map<String, Object>>() {
        }, false);
    Map<String,
        Object> result = JsonHelper.deserialize(JsonHelper.serialize(object.get("result"), false),
            new TypeReference<Map<String, Object>>() {
            }, false);
    List<Map<String, Object>> aggregations = JsonHelper.deserialize(
        JsonHelper.serialize(result.get("aggregations"), false),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);

    // 应用字典
    Map<Integer, String> appsIdNameMapping = saService.queryAllAppsIdNameMapping();
    // 协议字典
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    aggregations.forEach(item -> {
      String type = MapUtils.getString(item, "type");
      List<Map<String, Object>> items = JsonHelper.deserialize(
          JsonHelper.serialize(item.get("items"), false),
          new TypeReference<List<Map<String, Object>>>() {
          }, false);

      switch (type) {
        case "countryId":
          items.forEach(e -> {
            Map<String,
                Object> keys = JsonHelper.deserialize(JsonHelper.serialize(e.get("keys"), false),
                    new TypeReference<Map<String, Object>>() {
                    }, false);
            keys.put("countryText", locationDict.getT1().get(keys.get("countryId")));
            e.put("keys", keys);
          });
          break;
        case "provinceId":
          items.forEach(e -> {
            Map<String,
                Object> keys = JsonHelper.deserialize(JsonHelper.serialize(e.get("keys"), false),
                    new TypeReference<Map<String, Object>>() {
                    }, false);
            keys.put("provinceText", locationDict.getT2().get(keys.get("provinceId")));
            e.put("keys", keys);
          });
          break;
        case "cityId":
          items.forEach(e -> {
            Map<String,
                Object> keys = JsonHelper.deserialize(JsonHelper.serialize(e.get("keys"), false),
                    new TypeReference<Map<String, Object>>() {
                    }, false);
            keys.put("cityText", locationDict.getT3().get(keys.get("cityId")));
            e.put("keys", keys);
          });
          break;
        case "applicationId":
          items.forEach(e -> {
            Map<String,
                Object> keys = JsonHelper.deserialize(JsonHelper.serialize(e.get("keys"), false),
                    new TypeReference<Map<String, Object>>() {
                    }, false);
            keys.put("applicationText",
                appsIdNameMapping.get(MapUtils.getInteger(keys, "applicationId")));
            e.put("keys", keys);
          });
          break;
        case "l7ProtocolId":
          items.forEach(e -> {
            Map<String,
                Object> keys = JsonHelper.deserialize(JsonHelper.serialize(e.get("keys"), false),
                    new TypeReference<Map<String, Object>>() {
                    }, false);
            keys.put("l7ProtocolText", protocolDict.get(keys.get("l7ProtocolId")));
            e.put("keys", keys);
          });
          break;
        default:
          break;
      }

      item.put("items", items);
    });

    result.put("aggregations", aggregations);
    object.put("result", result);
    return object;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#queryFlowPacketDownloadUrl(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public String queryFlowPacketDownloadUrl(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    String url = "";

    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_PACKETS_DOWNLOAD;

      // 拼接地址
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append(path);
      String paramUrl = queryVO.toParamUrl();
      if (StringUtils.isNotBlank(queryVO.getServiceId())
          && StringUtils.isBlank(queryVO.getNetworkId())) {
        paramUrl = StringUtils.replace(paramUrl, "networkId=&", "networkId=ALL&");
      }
      urlBuilder.append(paramUrl);
      urlBuilder.append("&X-Machloop-Date=");
      urlBuilder.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      urlBuilder.append("&X-Machloop-Credential=");
      urlBuilder.append(credential);
      urlBuilder.append("&X-Machloop-Signature=");
      urlBuilder.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      LOGGER.info("invoke download packet rest api:{}.", urlBuilder.toString());

      url = urlBuilder.toString();
    } catch (Exception e) {
      LOGGER.warn("failed to encapsulation flow packets download url.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取PCAP文件下载地址异常");
    }

    return url;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#analyzeFlowPacket(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void analyzeFlowPacket(PacketAnalysisQueryVO queryVO, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response) {
    // 根据容量清空缓存文件
    if (fileMap.size() >= Constants.BLOCK_DEFAULT_SIZE) {
      fileMap = Maps.newConcurrentMap();
    }

    String requestUrl = "";
    try {
      // 根据过滤条件生成id
      Map<String, Object> queryMap = queryVO.toParamUrlWithPlaceholder().getT2();
      queryMap.remove("queryId");
      String id = DigestUtils.md5Hex(JsonHelper.serialize(queryMap));

      String filePath = null;
      if (fileMap.get(id) == null || fileMap.get(id).getT1()
          .before(DateUtils.beforeSecondDate(DateUtils.now(), Constants.ONE_MINUTE_SECONDS))) {
        // 使用UUID作为凭证，并取token进行签名
        String credential = IdGenerator.generateUUID();
        String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
        String date = DateUtils.toStringISO8601(DateUtils.now());
        String path = ManagerConstants.REST_ENGINE_PACKETS_PATH;
        String serverIp = fileServerHost;
        String[] ipList = StringUtils.split(fileServerHost, ",");

        if (ipList.length > 1) {
          if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
            serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
          } else {
            serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
          }
        }

        // 拼接地址
        StringBuilder url = new StringBuilder();
        url.append(fileServerProtocol);
        url.append("://");
        if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
          url.append("[").append(serverIp).append("]");
        } else {
          url.append(serverIp);
        }
        url.append(":");
        url.append(fileServerPort);
        url.append(path);
        url.append(queryVO.toParamUrlWithPlaceholder().getT1());
        url.append("&X-Machloop-Date=");
        url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
        url.append("&X-Machloop-Credential=");
        url.append(credential);
        url.append("&X-Machloop-Signature=");
        url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
        requestUrl = url.toString();
        LOGGER.info("invoke packet analysis rest api:{}, params:{}.", requestUrl,
            JsonHelper.serialize(queryVO.toParamUrlWithPlaceholder().getT2()));

        filePath = restTemplate.getForObject(url.toString(), String.class,
            queryVO.toParamUrlWithPlaceholder().getT2());
        if (StringUtils.isBlank(filePath)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，加载数据包文件失败");
        }

        fileMap.put(id, Tuples.of(DateUtils.now(), filePath));
      } else {
        filePath = fileMap.get(id).getT2();
      }
      LOGGER.info("invoke packet analysis rest api success, get file path:[{}]", filePath);

      webSharkService.analyzeNetworkPacketFile(id, filePath, type, parameter, request, response);
    } catch (Exception e) {
      LOGGER.warn("failed to analysis flow packets [{}].", requestUrl, e);
      String message = e instanceof BusinessException ? ((BusinessException) e).getMessage()
          : "分析数据包文件失败";
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, message);
    }
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#stopSearchFlowPackets(java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> stopSearchFlowPackets(String queryId, HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_PACKETS_QUERY_STOP;
      String serverIp = fileServerHost;
      String[] ipList = StringUtils.split(fileServerHost, ",");

      if (ipList.length > 1) {
        if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
          serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
        } else {
          serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
        }
      }

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append("?queryId=").append(queryId);
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "POST", date, path));
      requestUrl = url.toString();
      LOGGER.info("invoke stop packet search rest api:{}", requestUrl);

      String resultStr = restTemplate.postForObject(url.toString(), HttpEntity.EMPTY, String.class);
      if (StringUtils.isNotBlank(resultStr)) {
        result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
        }, false);
      }
    } catch (Exception e) {
      LOGGER.warn("failed to stop query flow packets [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "停止查询数据包列表异常");
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#stopFlowPacketRefines(java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> stopFlowPacketRefines(String queryId, HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_PACKETS_REFINE_STOP;
      String serverIp = fileServerHost;
      String[] ipList = StringUtils.split(fileServerHost, ",");

      if (ipList.length > 1) {
        if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
          serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
        } else {
          serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
        }
      }

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append("?queryId=").append(queryId);
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "POST", date, path));
      requestUrl = url.toString();
      LOGGER.info("invoke stop packet refine rest api:{}", requestUrl);

      String resultStr = restTemplate.postForObject(url.toString(), HttpEntity.EMPTY, String.class);
      if (StringUtils.isNotBlank(resultStr)) {
        result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
        }, false);
      }
    } catch (Exception e) {
      LOGGER.warn("failed to stop query flow packets [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "停止数据包统计异常");
    }

    return result;
  }

  private Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> queryGeoIpDict() {
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    Map<String, String> countryDict = geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryBO::getCountryId, GeoCountryBO::getNameText));
    countryDict.putAll(geoService.queryCustomCountrys().stream()
        .collect(Collectors.toMap(GeoCustomCountryBO::getCountryId, GeoCustomCountryBO::getName)));

    Map<String, String> provinceDict = geolocations.getT2().stream()
        .collect(Collectors.toMap(GeoProvinceBO::getProvinceId, GeoProvinceBO::getNameText));

    Map<String, String> cityDict = geolocations.getT3().stream()
        .collect(Collectors.toMap(GeoCityBO::getCityId, GeoCityBO::getNameText));

    return Tuples.of(countryDict, provinceDict, cityDict);
  }

}
