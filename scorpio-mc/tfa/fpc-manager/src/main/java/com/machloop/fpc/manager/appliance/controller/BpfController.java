package com.machloop.fpc.manager.appliance.controller;

import java.nio.charset.StandardCharsets;

import javax.validation.constraints.NotEmpty;

import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Base64Utils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.algorithm.bpf.BpfCheck;

/**
 * 
 * @author mazhiyuan
 *
 * create at 2019年1月8日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class BpfController {

  @GetMapping("/bpf-rule-verifications")
  @Secured({"PERM_USER"})
  public boolean checkBpfRuleValid(@NotEmpty(message = "bpf不能为空") @RequestParam String bpf) {
    bpf = new String(Base64Utils.decodeFromString(bpf), StandardCharsets.UTF_8);
    return BpfCheck.isBpfValid(bpf);
  }

}
