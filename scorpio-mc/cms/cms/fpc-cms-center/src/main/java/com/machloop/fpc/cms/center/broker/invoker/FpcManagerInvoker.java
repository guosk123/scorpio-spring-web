package com.machloop.fpc.cms.center.broker.invoker;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.invoker.WebappInvoker;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.data.FpcDO;

/**
 * @author liyongjun
 *
 * create at 2019年11月18日, fpc-cms-center
 */
@Service
public class FpcManagerInvoker extends WebappInvoker {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcManagerInvoker.class);

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  /**
   * 下载全包任务文件
   * @param fpcSerialNumber
   * @param id
   * @param serverName
   * @return
   */
  public Map<String, Object> downloadTransmitTaskFile(String fpcSerialNumber, String id,
      String serverName) {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 校验设备Ip
    FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(fpcSerialNumber);
    String fpcIp = fpcDO.getIp();
    if (StringUtils.isBlank(fpcIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该设备 ip为空");
    }

    // 校验设备任务id
    AssignmentTaskRecordDO assignmentTaskRecord = assignmentTaskRecordDao
        .queryAssignmentTaskRecord(fpcSerialNumber, id);
    String fpcTaskId = assignmentTaskRecord.getFpcTaskId();
    if (StringUtils.isBlank(fpcTaskId)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该设备任务id为空");
    }

    // 拼接rest请求地址
    String downloadFileUri = HotPropertiesHelper
        .getProperty("fpc.manager.download.task.rest.address").replace("{fpcIp}", fpcIp)
        .replace("{fpcPort}", HotPropertiesHelper.getProperty("fpc.manager.server.port"))
        .replace("{fpcTaskId}", fpcTaskId);

    // rest请求并获取响应结果
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    Map<String,
        Object> restResultMap = queryQuietly(downloadFileUri,
            new HttpEntity<>(builderAuthHttpHeader(fpcDO.getAppKey(), appToken)),
            new TypeReference<Map<String, Object>>() {
            });

    LOGGER.debug("request url is {}, response result is {}.", downloadFileUri, restResultMap);

    if (restResultMap.get("code") == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "连接异常");
    }

    // 请求成功获取下载地址
    if ((int) restResultMap.get("code") == WebappConstants.REST_RESULT_SUCCESS_CODE) {
      String path = String.valueOf(restResultMap.get("result"));
      LOGGER.debug("download path before encryption is {}.", path);

      // 对path加密并进行url编码
      try {
        path = KeyEncUtils
            .encrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), path);
        path = URLEncoder.encode(path, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException | UnsupportedOperationException e) {
        LOGGER.warn("failed to download path url encoding, path is {}", path, e);
        throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "下载地址格式错误");
      }

      // 拼接url
      String filePath = HotPropertiesHelper.getProperty("download.server.protocol") + "://"
          + serverName + ":" + HotPropertiesHelper.getProperty("download.server.port")
          + "/download?downloadUrl=" + path;

      resultMap.put("filePath", filePath);
    }

    // 拼接返回结果
    resultMap.put("code", restResultMap.get("code"));
    if (restResultMap.get("msg") != null) {
      resultMap.put("msg", restResultMap.get("msg"));
    }

    return resultMap;
  }

  /**
   * 导入探针license
   * @param fpcId
   * @param file
   * @return
   */
  public Map<String, Object> importFpcLicense(String fpcId, File file) {

    // 校验设备Ip
    FpcDO fpcDO = fpcDao.queryFpcById(fpcId);
    String fpcIp = fpcDO.getIp();
    if (StringUtils.isBlank(fpcIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备IP为空");
    }

    // 校验设备appKey、appToken
    if (StringUtils.isAnyBlank(fpcDO.getAppKey(), fpcDO.getAppToken())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备appKey、appToken为空");
    }

    // 拼接rest请求地址
    String importLicenseUrl = getApiBaseUrl() + HotPropertiesHelper
        .getProperty("fpc.manager.import.license.rest.address").replace("{fpcIp}", fpcIp)
        .replace("{fpcPort}", HotPropertiesHelper.getProperty("fpc.manager.server.port"));

    // rest请求并获取响应结果
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    Map<String, Object> resultMap = exchangeFile(importLicenseUrl, HttpMethod.POST, "file", file,
        fpcDO.getAppKey(), appToken);

    LOGGER.info("request url is {}, response result is {}.", importLicenseUrl, resultMap);

    if (resultMap.get("code") == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "连接异常");
    }

    return resultMap;
  }

  public Map<String, Object> importSuricataRules(FpcDO fpcDO, File file) {
    return importApplication(fpcDO, file, "fpc.manager.suricata.rest.address");
  }

  // "fpc.cms.domain.rest.address"
  public Map<String, Object> importApplication(FpcDO fpcDO, File file, String importUrl) {

    // 检验设备Ip
    String fpcIp = fpcDO.getIp();
    if (StringUtils.isBlank(fpcIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备IP为空");
    }

    // 校验设备appKey、appToken
    if (StringUtils.isAnyBlank(fpcDO.getAppKey(), fpcDO.getAppToken())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备appKey、appToken为空");
    }

    // 拼接rest请求接口
    String importApplicationUrl = getApiBaseUrl()
        + HotPropertiesHelper.getProperty(importUrl).replace("{fpcIp}", fpcIp).replace("{fpcPort}",
            HotPropertiesHelper.getProperty("fpc.manager.server.port"));
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    Map<String, Object> resultMap = exchangeFile(importApplicationUrl, HttpMethod.POST, "file",
        file, fpcDO.getAppKey(), appToken);
    LOGGER.info("request url is {}, response result is {}.", importApplicationUrl, resultMap);
    if (resultMap.get("code") == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "连接异常");
    }

    return resultMap;
  }

  public Map<String, Object> importSuricataRules(CmsDO cmsDO, File file) {
    return importApplication(cmsDO, file, "fpc.cms.suricata.rest.address");
  }

  public Map<String, Object> importApplication(CmsDO cmsDO, File file, String importUrl) {

    // 检验设备Ip
    String fpcIp = cmsDO.getIp();
    if (StringUtils.isBlank(fpcIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备IP为空");
    }

    // 校验设备appKey、appToken
    if (StringUtils.isAnyBlank(cmsDO.getAppKey(), cmsDO.getAppToken())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备appKey、appToken为空");
    }

    // 拼接rest请求接口
    String importApplicationUrl = getApiBaseUrl()
        + HotPropertiesHelper.getProperty(importUrl).replace("{cmsIp}", fpcIp).replace("{cmsPort}",
            HotPropertiesHelper.getProperty("fpc.manager.server.port"));
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
    Map<String, Object> resultMap = exchangeFile(importApplicationUrl, HttpMethod.POST, "file",
        file, cmsDO.getAppKey(), appToken);
    LOGGER.info("request url is {}, response result is {}.", importApplicationUrl, resultMap);
    if (resultMap.get("code") == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "连接异常");
    }

    return resultMap;
  }

  /**
   * 调用探针上的webshark分析数据包
   * @param id
   * @param ip
   * @param appKey
   * @param appToken
   * @param filePath
   * @param type
   * @param parameter
   * @return
   * @throws UnsupportedEncodingException
   */
  public void analyzeNetworkPacketFile(String id, String ip, String appKey, String appToken,
      String filePath, String type, String parameter, HttpServletResponse response)
      throws UnsupportedEncodingException {
    // 校验设备appKey、appToken
    if (StringUtils.isAnyBlank(appKey, appToken)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备appKey、appToken为空");
    }

    // 拼接rest请求地址
    String websharkUrl = getApiBaseUrl() + HotPropertiesHelper
        .getProperty("fpc.manager.webshark.rest.address").replace("{fpcIp}", ip)
        .replace("{fpcPort}", HotPropertiesHelper.getProperty("fpc.manager.server.port"))
        .replace("{queryId}", id).replace("{filePath}", filePath).replace("{type}", type)
        .replace("{parameter}", URLEncoder.encode(parameter, StandardCharsets.UTF_8.name()));

    LOGGER.info("request url is {}.", websharkUrl);

    // rest请求并获取响应结果
    HttpGet request = new HttpGet(websharkUrl);
    try {
      String timestamp = String.valueOf(System.currentTimeMillis());
      appToken = KeyEncUtils
          .decrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), appToken);
      request.setHeader("Content-Type", "application/json");
      request.setHeader("appKey", appKey);
      request.setHeader("timestamp", timestamp);
      request.setHeader("signature", TokenUtils.makeSignature(appKey, appToken, timestamp));

      HttpResponse httpResponse = getHttpClient().execute(request);

      if (httpResponse.getEntity() != null) {
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          Map<String, Object> exceptionMsg = JsonHelper.deserialize(
              httpResponse.getEntity().getContent(), new TypeReference<Map<String, Object>>() {
              });
          throw new BusinessException(MapUtils.getString(exceptionMsg, "code"),
              MapUtils.getString(exceptionMsg, "message"));
        }

        // result = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        response.setContentType("text/plain;charset=utf-8");
        response.resetBuffer();

        OutputStream outputStream = response.getOutputStream();
        // 获取Socket的输入流，用来接收从服务端发送过来的消息
        int size = 0;
        char[] cbuf = new char[4096];
        BufferedReader buf = new BufferedReader(
            new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
        while ((size = buf.read(cbuf, 0, 4096)) != -1) {
          String result = new String(cbuf, 0, size);
          outputStream.write(result.getBytes(StandardCharsets.UTF_8));
          response.flushBuffer();
          if (cbuf[size - 1] == '\n' || cbuf[size - 1] == '\r') {
            LOGGER.debug("find line break.");
            break;
          }
        }
      }
    } catch (ClientProtocolException e) {
      LOGGER.warn("connect webshark to analyze packet failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "连接出现异常，获取数据失败");
    } catch (IOException e) {
      LOGGER.warn("connect webshark to analyze packet failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "连接出现异常，获取数据失败");
    } finally {
      request.releaseConnection();
    }
  }

  /**
   * 解决业务告警
   * @param alertMessageId
   * @param reason
   */
  public void solveAlertMessage(String fpcSerialNumber, String alertMessageId, String reason) {
    // 校验设备Ip
    FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(fpcSerialNumber);
    String fpcIp = fpcDO.getIp();
    if (StringUtils.isBlank(fpcIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备IP为空");
    }

    // 校验设备appKey、appToken
    if (StringUtils.isAnyBlank(fpcDO.getAppKey(), fpcDO.getAppToken())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备appKey、appToken为空");
    }

    // 拼接rest请求地址
    String solveMsgUrl = getApiBaseUrl() + HotPropertiesHelper
        .getProperty("fpc.manager.solve.alert.address").replace("{fpcIp}", fpcIp)
        .replace("{fpcPort}", HotPropertiesHelper.getProperty("fpc.manager.server.port"))
        .replace("{id}", alertMessageId)
        .replace("{reason}", StringUtils.defaultIfBlank(reason, ""));

    LOGGER.debug("request url is {}.", solveMsgUrl);

    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    getRestTemplate().put(solveMsgUrl,
        new HttpEntity<>(builderAuthHttpHeader(fpcDO.getAppKey(), appToken)));
  }

  /**
   * 上传文件 POST/PUT
   * @param uri
   * @param method
   * @param fileParamName
   * @param file
   * @param uriVariables
   * @return
   */
  public Map<String, Object> exchangeFile(String uri, HttpMethod method, String fileParamName,
      File file, String appKey, String appToken) {

    try {
      MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
      Resource resource = new FileSystemResource(file);
      params.add(fileParamName, resource);

      HttpHeaders headers = builderAuthHttpHeader(appKey, appToken);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

      ResponseEntity<
          JsonNode> response = getRestTemplate().exchange(uri, method, entity, JsonNode.class);
      if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
        return handleQueryResponse(uri, response, new TypeReference<Map<String, Object>>() {
        });
      }
    } catch (RestClientException | UnsupportedOperationException e) {
      LOGGER.warn("failed to invoke rest api, uri is {}.", uri, e);
    }

    return Maps.newHashMapWithExpectedSize(0);
  }

  @Override
  protected <T> T handleQueryResponse(String uri, ResponseEntity<JsonNode> response,
      TypeReference<T> responseType) {
    if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
      JsonNode node = response.getBody();
      if (!node.has("code")) {
        return JsonHelper.deserialize(node.toString(), responseType);
      }

      // 如果接受到错误信息，输出错误信息
      if (!StringUtils.equals(node.get("code").asText(), Constants.RES_OK)) {
        LOGGER.warn("failed to fpc manager invoke url {}, error {}.", uri,
            node.findValue("msg").asText());
      }

      return JsonHelper.deserialize(node.toString(), responseType);
    } else {
      LOGGER.warn("failed to invoke url {}, system error.", uri);
    }

    return super.handleQueryResponse(uri, response, responseType);
  }

  @Override
  protected String getApiBaseUrl() {
    return HotPropertiesHelper.getProperty("fpc.manager.server.protocol") + "://";
  }

  private HttpHeaders builderAuthHttpHeader(String key, String token) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String signature = TokenUtils.makeSignature(key, token, timestamp);

    HttpHeaders headers = new HttpHeaders();
    headers.add("appKey", key);
    headers.add("timestamp", timestamp);
    headers.add("signature", signature);

    return headers;
  }

  private HttpClient getHttpClient() {
    SSLContext sslContext = null;
    try {
      sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
          return true;
        }
      }).build();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }

    return HttpClients.custom().setSSLContext(sslContext)
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
  }
}
