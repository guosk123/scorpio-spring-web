package com.machloop.fpc.manager.appliance.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.appliance.service.WebSharkService;
import com.machloop.fpc.manager.helper.TcpClientHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月20日, fpc-manager
 */
@Service
public class WebSharkServiceImpl implements WebSharkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSharkServiceImpl.class);

  static final String COMMAND_TYPE_ANALYZE = "analyze";
  static final String COMMAND_TYPE_BYE = "bye";
  static final String COMMAND_TYPE_DOWNLOAD = "download";
  static final String COMMAND_TYPE_LOAD = "load";

  private static final int FILE_NOT_EXIST_CODE = 2;
  private static final int EXCEEDED_THE_NUMBER_OF_TASKS = 45001;

  private static final int ANALYZE_MAX_RETRY_COUNTS = 3;

  // 20M
  private static final int ANALYZE_TAP_MAX_BYTE_SIZE = 20 * Constants.BLOCK_DEFAULT_SIZE
      * Constants.BLOCK_DEFAULT_SIZE;

  @Autowired
  private TcpClientHelper tcpClientHelper;

  private Map<String, Socket> connections = Maps.newConcurrentMap();

  @PreDestroy
  public void closeAllConnections() {
    connections.forEach((taskId, connection) -> tcpClientHelper.close(connection));
    LOGGER.info("close websocket connection: [{}]",
        CsvUtils.convertCollectionToCSV(connections.keySet()));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.WebSharkService#checkNetworkPacketFileLoaded(java.lang.String)
   */
  @Override
  public boolean checkNetworkPacketFileLoaded(String id) {
    Socket connection = connections.get(id);

    boolean result = connection != null && !connection.isClosed() && !connection.isInputShutdown()
        && !connection.isOutputShutdown();

    if (result) {
      try {
        if (connection != null) {
          connection.sendUrgentData(0xFF);
        }
      } catch (IOException e) {
        LOGGER.info("id：{} socket connection closed", id);
        connections.remove(id);
        tcpClientHelper.close(connection);
        return false;
      }
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskFileService#analyzeTransmitTaskFile(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String,
   *      javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void analyzeNetworkPacketFile(String id, String filePath, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response) {

    LOGGER.debug("id is {}, type is {}, parameter is {}.", id, type, parameter);

    // 加载文件，如果没有抛出异常说明加载文件成功
    Socket connection = load(id, filePath);

    // 对每个连接加锁
    synchronized (connection) {
      switch (type) {
        case COMMAND_TYPE_ANALYZE:
          analyze(id, filePath, connection, parameter, response);
          break;
        case COMMAND_TYPE_DOWNLOAD:
          download(id, parameter, connection, request, response);
          break;
        default:
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "类型错误");
      }
    }

  }

  /**
   * 调用加载命令
   * 
   * @param id
   * @param filePath
   * @param operatorId
   * @return
   */
  private synchronized Socket load(String id, String filePath) {

    // 已经加载过，并且连接可用
    if (checkNetworkPacketFileLoaded(id)) {
      LOGGER.debug("file already loaded,id:[{}]", id);
      return connections.get(id);
    }

    LOGGER.debug("start load, id:[{}], file path:[{}]", id, filePath);
    // 创建新的TCP连接
    Socket connection = tcpClientHelper.connect();
    if (connection == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "创建连接失败");
    }

    // 没有正在分析的任务，拼接load命令
    Map<String, Object> requestMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    requestMap.put("req", COMMAND_TYPE_LOAD);
    requestMap.put("file", filePath);

    // 发送load消息，并接受返回结果
    String loadResult = sendOperationCommand(id, JsonHelper.serialize(requestMap), connection);
    Map<String, Object> loadMap = JsonHelper.deserialize(loadResult,
        new TypeReference<Map<String, Object>>() {
        });

    // 如果载入文件失败，执行bye命令
    Integer err = MapUtils.getInteger(loadMap, "err");
    if (err == null) {
      bye(id, connection);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "加载文件失败，请重新分析");
    }

    if (FILE_NOT_EXIST_CODE == err.intValue()) {
      bye(id, connection);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "文件路径不存在");
    }

    // 检查是否超出任务数量
    if (EXCEEDED_THE_NUMBER_OF_TASKS == err.intValue()) {
      bye(id, connection);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "超出任务数限制，请检查正在分析的任务数量");
    }

    // 加载任务失败，返回异常
    if (!StringUtils.equals(Constants.RES_OK, String.valueOf(err))) {
      bye(id, connection);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "加载文件失败，请重新分析");
    }

    // 加载成功，更新任务Id、操作人Id、socket实例
    // analysingTaskTuple = Tuples.of(id, operatorId, socket);
    connections.put(id, connection);
    LOGGER.debug("success to load, id:[{}], file path:[{}]", id, filePath);
    return connection;
  }

  /**
   * 调用download命令下载文件
   * 
   * @param parameter
   * @param request
   * @param response
   */
  private void download(String id, String parameter, Socket connection, HttpServletRequest request,
      HttpServletResponse response) {

    // 发送下载消息并接受响应
    String downloadResult = sendOperationCommand(id, parameter, connection);

    if (StringUtils.isBlank(downloadResult)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "结果返回为空");
    }

    // 获取下载文件信息
    Map<String, Object> resultMap = JsonHelper.deserialize(downloadResult,
        new TypeReference<Map<String, Object>>() {
        });
    String fileName = String.valueOf(resultMap.get("file"));
    String mime = String.valueOf(resultMap.get("mime"));
    String data = String.valueOf(resultMap.get("data"));
    data = Base64Utils.decode(data);

    LOGGER.debug("fileName is {}, mime is {}, data is {}.", fileName, mime, data);

    // 校验获取的文件内容
    if (StringUtils.isBlank(fileName) || StringUtils.isBlank(mime) || StringUtils.isBlank(data)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "结果返回格式不正确");
    }

    // 设置文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType(mime + ";charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      out.write(data.getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("download file error ", e);
    } finally {
      connections.remove(id);
      tcpClientHelper.close(connection);
    }
  }

  /**
   * 调用bye命令并关闭连接
   * 
   * @return
   */
  private String bye(String id, Socket connection) {

    // 拼接bye命令
    Map<String, Object> requestMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    requestMap.put("req", "bye");

    // 发送bye命令并获取结果
    String byeResult = sendOperationCommand(id, JsonHelper.serialize(requestMap), connection);
    Map<String, Object> byeMap = JsonHelper.deserialize(byeResult,
        new TypeReference<Map<String, Object>>() {
        });

    // 退出后，关闭TCP连接
    connections.remove(id);
    tcpClientHelper.close(connection);

    // 返回结果为空或者退出成功，将重置当前分析任务相关信息并断开连接
    Integer err = MapUtils.getInteger(byeMap, "err");
    if (!(err == null || err.intValue() == Integer.valueOf(Constants.RES_OK))) {
      LOGGER.warn("failed to say bye, bye result: [{}]", byeResult);
    }

    return byeResult;
  }

  private synchronized void analyze(String id, String filePath, Socket connection, String parameter,
      HttpServletResponse response) {

    // 加载成功，将分析结果传回前端
    sendAnalysisMessage(id, filePath, parameter, 1, connection, response);
  }

  /**
   * 发送load、download、bye命令用该函数
   * 
   * @param message
   * @param connection
   * @return
   */
  private String sendOperationCommand(String id, String message, Socket connection) {

    if (connection == null || !connection.isConnected()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "连接已关闭，发送消息失败");
    }

    // 校验message
    if (StringUtils.isNotBlank(message)) {
      LOGGER.debug("message is {}.", message);
    }

    // 获取Socket的输出流，并发送消息到服务端
    try {
      PrintStream out = new PrintStream(connection.getOutputStream());

      // 发送数据到服务端
      out.println(message);

      // 获取Socket的输入流，用来接收从服务端发送过来的消息
      BufferedReader buf = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
      String result = buf.readLine();

      LOGGER.debug("result is {}.", result);

      return result;

    } catch (IOException e) {
      connections.remove(id);
      tcpClientHelper.close(connection);
      LOGGER.warn("failed to send message. ", e);
      // throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "发送消息失败");
      return null;
    }
  }

  /**
   * 发送analyze命令用该函数
   * 
   * @param message
   * @param connection
   * @param response
   */
  private void sendAnalysisMessage(String id, String filePath, String message, int retryCounts,
      Socket connection, HttpServletResponse response) {
    if (connection == null || !connection.isConnected()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "连接已关闭，发送消息失败");
    }

    if (retryCounts > ANALYZE_MAX_RETRY_COUNTS) {
      LOGGER.warn("retry connect socket failed, connect state: {}",
          JsonHelper.serialize(connection));
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "连接出现异常，尝试重连失败");
    }

    // 校验message
    if (StringUtils.isNotBlank(message)) {
      LOGGER.debug("message is {}.", message);
    }

    // 判断请求类型
    boolean tap = false;
    try {
      Map<String, Object> deserialize = JsonHelper.deserialize(message,
          new TypeReference<Map<String, Object>>() {
          }, false);
      // {"tap0":"conv:Ethernet","req":"tap","type":"analyze"}
      if (deserialize.containsKey("tap0")
          && StringUtils.equals("tap", MapUtils.getString(deserialize, "req"))) {
        tap = true;
      }
    } catch (Exception e) {
      LOGGER.warn("deserialize message failed.", e);
    }

    int cycles = 0;
    if (tap) {
      cycles = sendTapMessage(id, message, connection, response);
    } else {
      // 设置响应类型
      response.setContentType("text/plain;charset=utf-8");
      response.resetBuffer();

      // 获取Socket的输出流，并发送消息到服务端
      try (OutputStream outputStream = response.getOutputStream()) {
        // 发送数据到服务端
        PrintStream out = new PrintStream(connection.getOutputStream());
        out.println(message);

        // 获取Socket的输入流，用来接收从服务端发送过来的消息
        int size = 0;
        char[] cbuf = new char[4096];
        BufferedReader buf = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        while ((size = buf.read(cbuf, 0, 4096)) != -1) {
          String result = new String(cbuf, 0, size);
          outputStream.write(result.getBytes(StandardCharsets.UTF_8));
          response.flushBuffer();
          cycles++;
          if (cbuf[size - 1] == '\n' || cbuf[size - 1] == '\r') {
            LOGGER.debug("find line break.");
            break;
          }
        }
      } catch (IOException e) {
        LOGGER.warn("failed to send message. ", e);
        connections.remove(id);
        tcpClientHelper.close(connection);
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "连接出现异常，发送消息失败");
      }
    }

    if (cycles == 0) {
      // 对端连接已断开
      LOGGER.info("old connection closed, begin to reconnect.");
      connections.remove(id);
      tcpClientHelper.close(connection);
      Socket newConnection = load(id, filePath);
      sendAnalysisMessage(id, filePath, message, ++retryCounts, newConnection, response);
    }
  }

  public int sendTapMessage(String id, String message, Socket connection,
      HttpServletResponse response) {
    int cycles = 0;

    try {
      // 发送数据到服务端
      PrintStream out = new PrintStream(connection.getOutputStream());
      out.println(message);

      // 获取Socket的输入流，用来接收从服务端发送过来的消息
      List<String> results = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      int totalSize = 0;
      int size = 0;
      char[] cbuf = new char[4096];
      BufferedReader buf = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
      while ((size = buf.read(cbuf, 0, 4096)) != -1) {
        String result = new String(cbuf, 0, size);
        results.add(result);

        cycles++;
        if (cbuf[size - 1] == '\n' || cbuf[size - 1] == '\r') {
          LOGGER.debug("find line break.");
          break;
        }

        totalSize += result.getBytes().length;
        if (totalSize > ANALYZE_TAP_MAX_BYTE_SIZE) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
              "分析数据过大，无法通过浏览器在线分析，建议下载数据包进行本地分析！");
        }
      }

      // 设置响应类型
      response.setContentType("text/plain;charset=utf-8");
      response.resetBuffer();
      try (OutputStream outputStream = response.getOutputStream()) {
        for (String result : results) {
          outputStream.write(result.getBytes(StandardCharsets.UTF_8));
          response.flushBuffer();
        }
      }
    } catch (IOException e) {
      LOGGER.warn("failed to send message. ", e);
      connections.remove(id);
      tcpClientHelper.close(connection);
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "连接出现异常，发送消息失败");
    }

    return cycles;
  }

}
