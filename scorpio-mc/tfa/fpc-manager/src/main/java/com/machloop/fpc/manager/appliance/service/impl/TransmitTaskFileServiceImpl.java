package com.machloop.fpc.manager.appliance.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.TransmitTaskBO;
import com.machloop.fpc.manager.appliance.service.TransmitTaskFileService;
import com.machloop.fpc.manager.appliance.service.TransmitTaskService;
import com.machloop.fpc.manager.appliance.service.WebSharkService;

/**
 * @author liyongjun
 *
 * create at 2020年1月14日, fpc-manager
 */
@Service
public class TransmitTaskFileServiceImpl implements TransmitTaskFileService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransmitTaskFileServiceImpl.class);

  @Autowired
  private TransmitTaskService transmitTaskService;

  @Autowired
  private WebSharkService webSharkService;

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskFileService#analyzeTransmitTaskFile(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void analyzeTransmitTaskFile(String taskId, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response) {

    // 分析新的任务，根据taskId获取文件地址
    TransmitTaskBO transmitTaskBO = transmitTaskService.queryTransmitTask(taskId);
    String filePath = transmitTaskBO.getExecutionCachePath();

    // 校验任务是否完成和文件地址是否为空
    if (!StringUtils.equals(transmitTaskBO.getState(),
        FpcConstants.APPLIANCE_TRANSMITTASK_STATE_FINISH) || StringUtils.isBlank(filePath)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "该任务未完成，请完成后分析");
    }

    // 校验文件是否存在，如果不存在重新执行改文件
    File file = new File(filePath);
    if (!file.exists() || file.isDirectory()) {
      transmitTaskService.redoTransmitTask(taskId);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件失效，正在重新执行该任务");
    }
    LOGGER.debug("taskId is {}, type is {}, parameter is {}.", taskId, type, parameter);

    String analyzeId = "transmit_" + taskId;
    webSharkService.analyzeNetworkPacketFile(analyzeId, filePath, type, parameter, request,
        response);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskFileService#downloadTransmitTaskFile(java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, String> downloadTransmitTaskFile(String id, String remoteAddr) {

    // 根据ID查询任务
    TransmitTaskBO transmitTask = transmitTaskService.queryTransmitTask(id);
    if (StringUtils.isBlank(transmitTask.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "查询任务不存在。");
    }

    // 判断任务是否完成
    if (!StringUtils.equals(transmitTask.getState(),
        FpcConstants.APPLIANCE_TRANSMITTASK_STATE_FINISH)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "任务未完成或已失败，请检查任务状态。");
    }

    // 提取缓存文件
    Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    String fileCache = transmitTask.getExecutionCachePath();
    if (StringUtils.isBlank(fileCache)) {
      // 任务正在执行中
      resultMap.put("result-code", "TASK_EXECUTING");
      return resultMap;
    }

    // 如果文件不存在硬盘中，则重启查询任务
    File file = new File(fileCache);
    if (!file.exists()) {
      // 文件被老化，需要重新执行查询任务
      transmitTask = transmitTaskService.redoTransmitTask(id);
      resultMap.put("result-code", "TASK_REDO_EXECUTING");
      return resultMap;
    }

    // 下载前touch被下载文件，使其免于老化
    boolean touch = file.setLastModified(System.currentTimeMillis());
    if (!touch) {
      LOGGER.warn("Fail to touch download file {}.", file.getName());
    }

    // 如果文件在硬盘中，重定向请求到下载服务
    String date = DateUtils.toStringISO8601(DateUtils.now());
    String path = String.format(ManagerConstants.REST_ENGINE_TASK_PACKET_DOWNLOAD, id);

    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);

    StringBuilder fileUrl = new StringBuilder();
    try {
      // 拼接文件下载地址
      fileUrl.append(path);
      fileUrl.append("?X-Machloop-Date=");
      fileUrl.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      fileUrl.append("&X-Machloop-Credential=");
      fileUrl.append(credential);
      fileUrl.append("&X-Machloop-Signature=");
      fileUrl.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("failed to download transmit task file.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "下载失败");
    }

    resultMap.put("result-code", "GO_DOWNLOAD");
    resultMap.put("download-path", fileUrl.toString());

    return resultMap;
  }
}
