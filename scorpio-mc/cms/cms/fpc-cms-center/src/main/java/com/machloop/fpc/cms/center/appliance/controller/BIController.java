package com.machloop.fpc.cms.center.appliance.controller;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.security.bo.LoggedUser;
import com.machloop.alpha.webapp.system.service.SecuritySettingService;

/**
 * @author guosk
 *
 * create at 2022年3月17日, fpc-cms-center
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class BIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BIController.class);

  private static final int DEFAULT_SESSION_EXPIRED_SECOND = 600;

  @Value("${bi.entry}")
  private String biEntry;

  @Value("${bi.jwt.secret}")
  private String secret;

  @Autowired
  private SecuritySettingService securitySettingService;

  @GetMapping("/bi-infos")
  @Secured({"PERM_USER"})
  public Map<String, String> queryBIInfos() {
    int sessionExpiredSecond = securitySettingService.querySecuritySetting(false)
        .getSessionExpiredSecond();
    if (sessionExpiredSecond == 0) {
      sessionExpiredSecond = DEFAULT_SESSION_EXPIRED_SECOND;
    }

    String token = "";
    try {
      Date now = new Date();
      Date expire = DateUtils.afterSecondDate(now, sessionExpiredSecond);
      LoggedUser currentUser = LoggedUserContext.getCurrentUser();

      Algorithm algorithm = Algorithm.HMAC256(secret);
      token = JWT.create().withClaim("userId", currentUser.getId())
          .withClaim("username", currentUser.getFullname()).withExpiresAt(expire).withIssuedAt(now)
          .withNotBefore(now).sign(algorithm);
    } catch (IllegalArgumentException | JWTCreationException e) {
      LOGGER.warn("build bi token failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统出现异常");
    }

    Map<String, String> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("entry", biEntry);
    result.put("token", token);
    return result;
  }

}
