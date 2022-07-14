package com.scorpio.security.service.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.collect.Sets;
import com.scorpio.Constants;
import com.scorpio.helper.HotPropertiesHelper;
import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;

@Service
public class SessionExpireInterceptor implements HandlerInterceptor {

  private final Set<
      String> exceptUriSet = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);

  @Autowired
  private LoggedUserManager loggedUserManager;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    int sessionExpiredSecond = Integer
        .parseInt(HotPropertiesHelper.getProperty("loggeduser.session.expired.second"));
    if (sessionExpiredSecond <= 0) {
      return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    LoggedUser currentUser = LoggedUserContext.getCurrentUser();

    // 用户未登录, 不作处理
    if (currentUser != LoggedUser.EMPTY && !exceptUriSet.contains(request.getRequestURI())) {

      String sessionId = request.getRequestedSessionId();

      // 未超时刷新用户会话,如果超时抛出异常
      loggedUserManager.refreshOrExpire(sessionId, sessionExpiredSecond);
    }
    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  public void addExceptUri(String exceptUri) {
    this.exceptUriSet.add(StringUtils.removeEnd(contextPath, "/") + exceptUri);
  }

}
