package com.scorpio.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.scorpio.Constants;
import com.scorpio.exception.BusinessException;
import com.scorpio.exception.ErrorCode;
import com.scorpio.util.JsonHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public abstract class AbstractRestInvokerHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestInvokerHelper.class);

  private static final Map<String, String> EMPTYMAP = Maps.newHashMapWithExpectedSize(0);

  private static final Map<String,
      Object> EMPTYVALUEMAP = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    EMPTYVALUEMAP.put("java.lang.Boolean", Boolean.FALSE);
    EMPTYVALUEMAP.put("java.lang.Byte", Byte.valueOf((byte) 0));
    EMPTYVALUEMAP.put("java.lang.Character", Character.valueOf('\0'));
    EMPTYVALUEMAP.put("java.lang.Short", Short.valueOf((short) 0));
    EMPTYVALUEMAP.put("java.lang.Integer", Integer.valueOf(0));
    EMPTYVALUEMAP.put("java.lang.Long", Long.valueOf((long) 0));
    EMPTYVALUEMAP.put("java.lang.Float", Float.valueOf((float) 0));
    EMPTYVALUEMAP.put("java.lang.Double", Double.valueOf((double) 0));
    EMPTYVALUEMAP.put("java.lang.String", "");
    EMPTYVALUEMAP.put("java.util.List", Lists.newArrayListWithCapacity(0));
    EMPTYVALUEMAP.put("java.util.Map", Maps.newHashMapWithExpectedSize(0));
    EMPTYVALUEMAP.put("java.util.Set", Sets.newHashSetWithExpectedSize(0));
  }

  protected abstract RestTemplate getRestTemplate();

  protected abstract String getApiBaseUrl();

  public <T> T queryQuietly(String uri, TypeReference<T> responseType) {
    return doQueryQuietly(uri, null, responseType, null);
  }

  public <T> T queryQuietly(String uri, TypeReference<T> responseType, Object... uriVariables) {
    return doQueryQuietly(uri, null, responseType, null, uriVariables);
  }

  public <T> T queryQuietly(String uri, TypeReference<T> responseType, Map<String, ?> uriVarMap) {
    return doQueryQuietly(uri, null, responseType, uriVarMap);
  }

  public <T> T queryQuietly(String uri, HttpEntity<?> entity, TypeReference<T> responseType) {
    return doQueryQuietly(uri, entity, responseType, null);
  }

  /**
   * 查询，如果查询出错（包括连接错误和对端返回错误），根据responseType构造默认空对象返回
   * 
   * @param uri
   * @param entity
   * @param responseType
   * @param uriVarMap
   * @param uriVariables
   * @return
   */
  private <T> T doQueryQuietly(String uri, HttpEntity<?> entity, TypeReference<T> responseType,
      Map<String, ?> uriVarMap, Object... uriVariables) {
    try {

      String serviceUrl = getApiBaseUrl() + uri;

      if (entity == null) {
        entity = buildAuthHttpEntity();
      }
      ResponseEntity<JsonNode> response = null;
      if (MapUtils.isNotEmpty(uriVarMap)) {
        response = getRestTemplate().exchange(serviceUrl, HttpMethod.GET, entity, JsonNode.class,
            uriVarMap);
      } else if (uriVariables != null) {
        response = getRestTemplate().exchange(serviceUrl, HttpMethod.GET, entity, JsonNode.class,
            uriVariables);
      } else {
        response = getRestTemplate().exchange(serviceUrl, HttpMethod.GET, entity, JsonNode.class);
      }
      return handleQueryResponse(uri, response, responseType);
    } catch (RestClientException | UnsupportedOperationException e) {
      LOGGER.warn("failed to invoke url {}.", uri, e);
    }
    return consturctEmptyResult(responseType);
  }

  /**
   * 执行POST/PUT/DELETE
   * 
   * @param uri
   * @param method
   * @param params
   * @param uriVariables
   */
  public Map<String, String> exchange(String uri, HttpMethod method,
      MultiValueMap<String, String> params, Object... uriVariables) {

    try {
      String serviceUrl = getApiBaseUrl() + uri;

      HttpEntity<MultiValueMap<String, String>> entity = buildPostAuthHttpEntity(params);
      ResponseEntity<JsonNode> response = getRestTemplate().exchange(serviceUrl, method, entity,
          JsonNode.class, uriVariables);
      if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
        JsonNode node = response.getBody();
        if (StringUtils.equals(node.get("code").asText(), Constants.RES_OK)
            && node.hasNonNull("value")) {
          return JsonHelper.deserialize(node.get("value"),
              new TypeReference<Map<String, String>>() {
              });
        }
      }
    } catch (RestClientException | UnsupportedOperationException e) {
      LOGGER.warn("failed to invoke rest api, uri is {}.", uri, e);
    }
    return EMPTYMAP;
  }

  /**
   * 下载文件
   * 
   * @param uri
   * @return
   * @throws IOException
   * @throws RestClientException
   */
  public File downloadFile(String uri, Object... uriVariables)
      throws IOException, RestClientException {
    String serviceUrl = getApiBaseUrl() + uri;
    ResponseEntity<byte[]> response = getRestTemplate().exchange(serviceUrl, HttpMethod.GET,
        buildAuthHttpEntity(), byte[].class, uriVariables);
    // 从响应头中获取文件名称
    List<String> filenameList = response.getHeaders().get("filename");
    if (CollectionUtils.isEmpty(filenameList) || StringUtils.isBlank(filenameList.get(0))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR,
          "no file name in headers");
    }
    String filename = filenameList.get(0);

    // 临时文件夹不暴露给外部，避免安全隐患
    String tempDirectory = "";
    Path tempFilePath = Paths.get(tempDirectory, FilenameUtils.getName(filename));
    File tempFile = tempFilePath.toFile();
    if (tempFile.exists()) {
      FileUtils.deleteQuietly(tempFile);
    }
    // 从响应体中获取文件的二进制流
    byte[] fileStream = response.getBody();
    FileUtils.writeByteArrayToFile(tempFile, fileStream);
    return tempFile;
  }

  @SuppressWarnings("rawtypes")
  protected HttpEntity buildAuthHttpEntity() {
    return new HttpEntity<>(builderAuthHttpHeader());
  }

  protected HttpEntity<MultiValueMap<String, String>> buildPostAuthHttpEntity(
      MultiValueMap<String, String> params) {
    HttpHeaders headers = builderAuthHttpHeader();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return new HttpEntity<>(params, headers);
  }

  protected <T> T handleQueryResponse(String uri, ResponseEntity<JsonNode> response,
      TypeReference<T> responseType) {
    if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
      JsonNode node = response.getBody();
      if (!node.has("code")) {
        return JsonHelper.deserialize(node.toString(), responseType);
      }
      if (StringUtils.equals(node.get("code").asText(), Constants.RES_OK)) {
        return JsonHelper.deserialize(node.get("data").toString(), responseType);
      } else {
        LOGGER.warn("failed to invoke url {}, error {}.", uri, node.findValue("message").asText());
      }
    } else {
      LOGGER.warn("failed to invoke url {}, system error.", uri);
    }
    return consturctEmptyResult(responseType);
  }


  @SuppressWarnings("unchecked")
  private <T> T consturctEmptyResult(TypeReference<T> responseType) {

    Type type = responseType.getType();
    String typeName = type.getTypeName();
    if (typeName.contains("<")) {
      typeName = typeName.substring(0, typeName.indexOf('<'));
    }

    T result = (T) EMPTYVALUEMAP.get(typeName);
    if (result != null) {
      return result;
    }

    if (typeName.contains("[")) {
      result = JsonHelper.deserialize("[]", responseType);
    } else {
      result = JsonHelper.deserialize("{}", responseType);
    }
    return result;
  }

  protected HttpHeaders builderAuthHttpHeader() {
    HttpHeaders headers = new HttpHeaders();
    return headers;
  }

}
