package com.machloop.fpc.cms.center.appliance.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService;
import com.machloop.fpc.cms.center.appliance.vo.PacketAnalysisQueryVO;
import com.machloop.fpc.cms.center.broker.invoker.FpcManagerInvoker;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.FpcDO;

import reactor.util.function.Tuple2;
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
  @Value("${fpc.engine.rest.server.port}")
  private String fileServerPort;

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#queryFlowPackets(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryFlowPackets(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取探针IP
    String fpcIp = fpcDao.queryFpcBySerialNumber(queryVO.getFpcSerialNumber()).getIp();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER.warn("the packet query failed, the fpc where the packet is located was not found.");
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "数据包查询失败,未找到数据包所在探针");
    }

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = CenterConstants.REST_ENGINE_PACKETS_QUERY;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
        url.append("[").append(fpcIp).append("]");
      } else {
        url.append(fpcIp);
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

      result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
      }, false);
    } catch (Exception e) {
      LOGGER.warn("failed to query flow packets [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包列表异常");
    }

    return result;
  }

  /**  
   * @see com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService#queryFlowPacketRefines(com.machloop.fpc.cms.center.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryFlowPacketRefines(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取探针IP
    String fpcIp = fpcDao.queryFpcBySerialNumber(queryVO.getFpcSerialNumber()).getIp();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER.warn("the packet query failed, the fpc where the packet is located was not found.");
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "数据包查询失败,未找到数据包所在探针");
    }

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = CenterConstants.REST_ENGINE_PACKETS_REFINE;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
        url.append("[").append(fpcIp).append("]");
      } else {
        url.append(fpcIp);
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

      result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
      }, false);
    } catch (Exception e) {
      LOGGER.warn("failed to query flow packets statistics [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包统计异常");
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisService#queryFlowPacketDownloadUrl(com.machloop.fpc.npm.appliance.vo.PacketAnalysisQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public String queryFlowPacketDownloadUrl(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    String url = "";

    // 获取探针IP
    String fpcIp = fpcDao.queryFpcBySerialNumber(queryVO.getFpcSerialNumber()).getIp();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER.warn("the packet query failed, the fpc where the packet is located was not found.");
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "数据包查询失败,未找到数据包所在探针");
    }

    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = CenterConstants.REST_ENGINE_PACKETS_DOWNLOAD;

      // 拼接地址
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append(fileServerProtocol);
      urlBuilder.append("://");
      if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
        urlBuilder.append("[").append(fpcIp).append("]");
      } else {
        urlBuilder.append(fpcIp);
      }
      urlBuilder.append(":");
      urlBuilder.append(fileServerPort);
      urlBuilder.append(path);
      urlBuilder.append(queryVO.toParamUrl());
      urlBuilder.append("&X-Machloop-Date=");
      urlBuilder.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      urlBuilder.append("&X-Machloop-Credential=");
      urlBuilder.append(credential);
      urlBuilder.append("&X-Machloop-Signature=");
      urlBuilder.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      LOGGER.info("invoke download rest api:{}", urlBuilder.toString());

      url = urlBuilder.toString();
    } catch (Exception e) {
      LOGGER.warn("failed to encapsulation flow packets download url.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取PCAP文件下载地址异常");
    }

    return url;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService#analyzeFlowPacket(com.machloop.fpc.cms.center.appliance.vo.PacketAnalysisQueryVO, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void analyzeFlowPacket(PacketAnalysisQueryVO queryVO, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response) {
    // 获取探针IP
    FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(queryVO.getFpcSerialNumber());
    String fpcIp = fpcDO.getIp();
    String appKey = fpcDO.getAppKey();
    String appToken = fpcDO.getAppToken();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER.warn("the packet query failed, the fpc where the packet is located was not found.");
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "数据包查询失败,未找到数据包所在探针");
    }

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
        String path = CenterConstants.REST_ENGINE_PACKETS_PATH;

        // 拼接地址
        StringBuilder url = new StringBuilder();
        url.append(fileServerProtocol);
        url.append("://");
        if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
          url.append("[").append(fpcIp).append("]");
        } else {
          url.append(fpcIp);
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

      fpcManagerInvoker.analyzeNetworkPacketFile(id, fpcIp, appKey, appToken, filePath, type,
          parameter, response);
    } catch (Exception e) {
      LOGGER.warn("failed to analysis flow packets [{}].", requestUrl, e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "分析数据包文件失败");
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService#stopSearchFlowPackets(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> stopSearchFlowPackets(String fpcSerialNumber, String queryId,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取探针IP
    String fpcIp = fpcDao.queryFpcBySerialNumber(fpcSerialNumber).getIp();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER
          .warn("the packet stop query failed, the fpc where the packet is located was not found.");
      return result;
    }

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = CenterConstants.REST_ENGINE_PACKETS_QUERY_STOP;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
        url.append("[").append(fpcIp).append("]");
      } else {
        url.append(fpcIp);
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
   * @see com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService#stopFlowPacketRefines(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> stopFlowPacketRefines(String fpcSerialNumber, String queryId,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 获取探针IP
    String fpcIp = fpcDao.queryFpcBySerialNumber(fpcSerialNumber).getIp();
    if (StringUtils.isBlank(fpcIp)) {
      LOGGER
          .warn("the packet stop query failed, the fpc where the packet is located was not found.");
      return result;
    }

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = CenterConstants.REST_ENGINE_PACKETS_REFINE_STOP;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(fpcIp, IpVersion.V6)) {
        url.append("[").append(fpcIp).append("]");
      } else {
        url.append(fpcIp);
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

}
