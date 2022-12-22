package com.machloop.fpc.manager.appliance.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liyongjun
 *
 * create at 2020年1月15日, fpc-manager
 */
public interface TransmitTaskFileService {

  void analyzeTransmitTaskFile(String taskId, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response);

  Map<String, String> downloadTransmitTaskFile(String id, String remoteAddr);
}
