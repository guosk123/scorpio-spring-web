package com.machloop.fpc.manager.appliance.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月20日, fpc-manager
 */
public interface WebSharkService {

  boolean checkNetworkPacketFileLoaded(String id);

  void analyzeNetworkPacketFile(String id, String filePath, String type, String parameter,
      HttpServletRequest request, HttpServletResponse response);

}
