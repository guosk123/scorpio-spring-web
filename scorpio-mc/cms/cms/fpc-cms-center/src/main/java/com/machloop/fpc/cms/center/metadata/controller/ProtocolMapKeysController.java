package com.machloop.fpc.cms.center.metadata.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.fpc.cms.center.metadata.service.ProtocolMapKeysService;

@RestController
@RequestMapping("/webapi/fpc-cms-v1/metadata")
public class ProtocolMapKeysController {

  @Autowired
  private ProtocolMapKeysService protocolMapKeysService;

  @GetMapping("/protocol-map-keys/{protocol}")
  @Secured({"PERM_USER"})
  public Map<String, Set<String>> queryProtocolMapKeys(@PathVariable String protocol) {
    return protocolMapKeysService.queryProtocolMapKeys(protocol);
  }

}
