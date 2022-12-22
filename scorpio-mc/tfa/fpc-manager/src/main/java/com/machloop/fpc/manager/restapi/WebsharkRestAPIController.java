package com.machloop.fpc.manager.restapi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.fpc.manager.appliance.service.WebSharkService;

/**
 * @author guosk
 * 
 * @apiNote 该rest接口不对外暴露，仅用于cms调用
 *
 * create at 2022年2月23日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/webshark")
public class WebsharkRestAPIController {

  @Autowired
  private WebSharkService webSharkService;

  @GetMapping("/analysis")
  @RestApiSecured
  public void analysisFlowPacket(@RequestParam String queryId, @RequestParam String filePath,
      @RequestParam String type, @RequestParam String parameter, HttpServletRequest request,
      HttpServletResponse response) {

    webSharkService.analyzeNetworkPacketFile(queryId, filePath, type, parameter, request, response);
  }

}
