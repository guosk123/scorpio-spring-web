package com.machloop.fpc.manager.boot.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.security.bo.LoggedUser;
import com.machloop.alpha.webapp.security.service.impl.UserSecurityService;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;

/**
 * @author guosk
 *
 * create at 2022年1月10日, fpc-manager
 */
@RestController
@RequestMapping("/api/v1/sso")
public class SsoQianxinController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SsoQianxinController.class);

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Autowired
  private UserService userService;

  @Autowired
  private SessionRegistry sessionRegistry;

  @Autowired
  private UserSecurityService userSecurityService;

  @GetMapping("/smc")
  public void ssoLoginForQianxin(String username, String password, String sip, String dip,
      String sport, String dport, String startTime, String endTime, HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    try {
      username = Base64Utils.decode(username);
      password = Base64Utils.decode(password);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("user login msg decode error.", e);

      try {
        httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
            : contextPath + "/#/sso/error?error=" + URLEncoder.encode("登录用户信息解密失败", "utf-8"));
      } catch (IOException e1) {
        LOGGER.warn("redirect page failed.", e1);
      }
      return;
    }

    if ((StringUtils.isNotBlank(startTime) && StringUtils.isBlank(endTime))
        || (StringUtils.isBlank(startTime) && StringUtils.isNotBlank(endTime))) {
      try {
        httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
            : contextPath + "/#/sso/error?error=" + URLEncoder.encode("开始和结束时间必须成对出现", "utf-8"));
      } catch (IOException e) {
        LOGGER.warn("redirect page failed.", e);
      }
      return;
    }

    UserBO userSecurity = userService.queryUserByName(username);
    if (StringUtils.isBlank(userSecurity.getId())) {
      LOGGER.warn("user does not exist.");

      try {
        httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
            : contextPath + "/#/sso/error?error=" + URLEncoder.encode("用户名或密码错误", "utf-8"));
      } catch (IOException e) {
        LOGGER.warn("redirect page failed.", e);
      }
      return;
    }

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    if (!encoder.matches(password, userSecurity.getPassword())) {
      LOGGER.warn("user password verification failed.");

      try {
        httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
            : contextPath + "/#/sso/error?error=" + URLEncoder.encode("用户名或密码错误", "utf-8"));
      } catch (IOException e) {
        LOGGER.warn("redirect page failed.", e);
      }
      return;
    }

    // 检测用户是否被锁定
    if (StringUtils.equals(userSecurity.getLocked(), Constants.LOCKED)) {
      LOGGER.warn("system user are locked.");

      try {
        httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
            : contextPath + "/#/sso/error?error=" + URLEncoder.encode("登录失败，用户已被锁定", "utf-8"));
      } catch (IOException e) {
        LOGGER.warn("redirect page failed.", e);
      }
      return;
    }

    // 自动登录
    innerLogin(userSecurity.getName(), httpRequest);

    // SameSite=None
    String header = httpResponse.getHeader(HttpHeaders.SET_COOKIE);
    if (StringUtils.isNotBlank(header)) {
      httpResponse.setHeader(HttpHeaders.SET_COOKIE,
          String.format("%s; %s", header, "SameSite=None"));
    }

    // 响应头添加配置，解决跨域问题
    String allowOrigin = HotPropertiesHelper.getProperty("sso.qianxin.allow.origin");
    httpResponse.setHeader("Access-Control-Allow-Origin",
        StringUtils.isBlank(allowOrigin) ? "*" : allowOrigin);
    httpResponse.setHeader("Access-Control-Allow-Methods", "*");
    httpResponse.setHeader("Access-Control-Max-Age", "3600");
    httpResponse.setHeader("Access-Control-Allow-Headers", "*");
    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

    // 重定向页面
    try {
      StringBuilder urlWithConditionBuilder = new StringBuilder(
          HotPropertiesHelper.getProperty("sso.qianxin.redirect.url"));
      if (StringUtils.isNotBlank(startTime)) {
        urlWithConditionBuilder.append("?startTime=")
            .append(URLEncoder.encode(startTime, StandardCharsets.UTF_8.name()));
      }
      if (StringUtils.isNotBlank(endTime)) {
        urlWithConditionBuilder.append("&endTime=")
            .append(URLEncoder.encode(endTime, StandardCharsets.UTF_8.name()));
      }
      if (StringUtils.isNotBlank(sip)) {
        urlWithConditionBuilder.append("&ipInitiator=").append(sip);
      }
      if (StringUtils.isNotBlank(sport)) {
        urlWithConditionBuilder.append("&portInitiator=").append(sport);
      }
      if (StringUtils.isNotBlank(dip)) {
        urlWithConditionBuilder.append("&ipResponder=").append(dip);
      }
      if (StringUtils.isNotBlank(dport)) {
        urlWithConditionBuilder.append("&portResponder=").append(dport);
      }

      httpResponse.sendRedirect(StringUtils.equals(contextPath, "/") ? ""
          : contextPath + "/#" + urlWithConditionBuilder.toString());
    } catch (IOException e) {
      LOGGER.warn("redirect page failed.", e);
    }
  }

  /**
   * 内部用户登录
   * @param username
   * @param httpRequest
   */
  private void innerLogin(String username, HttpServletRequest httpRequest) {
    // 将用户信息放入SecurityContext,并注册session
    UserDetails userDetails = userSecurityService.loadUserByUsername(username);
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        userDetails, "", userDetails.getAuthorities());
    authenticationToken.setDetails(new WebAuthenticationDetails(httpRequest));
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    httpRequest.getSession().setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext());

    sessionRegistry.registerNewSession(httpRequest.getSession().getId(), userDetails);

    // 记录登录日志
    LoggedUser user = LoggedUserContext.getCurrentUser();
    LogHelper.auditLogin(username + "登录成功（sso_qianxin）。",
        user.getFullname() + "/" + user.getUsername() + "（" + user.getRemoteAddress() + "）");
  }

}
