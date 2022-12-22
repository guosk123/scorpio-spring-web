package com.machloop.fpc.cms.center.knowledge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.fpc.cms.center.knowledge.service.SaProtocolService;

/**
 * @author guosk
 *
 * create at 2020年12月5日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metadata")
public class SaProtocolController {

  @Autowired
  private SaProtocolService saProtocolService;

  @GetMapping("/protocols")
  @Secured("PERM_USER")
  public List<Map<String, Object>> queryProtocols(
      @RequestParam(required = false) String protocolName,
      @RequestParam(required = false) String standard,
      @RequestParam(required = false) String label) {
    return saProtocolService.queryProtocols(protocolName, standard, label);
  }

  @GetMapping("/protocols/labels")
  @Secured("PERM_USER")
  public List<Map<String, String>> queryProtocolLabels() {
    return saProtocolService.queryLabels();
  }

}
