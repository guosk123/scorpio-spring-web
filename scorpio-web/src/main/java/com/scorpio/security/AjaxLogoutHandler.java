package com.scorpio.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.scorpio.security.bo.LoggedUser;

public class AjaxLogoutHandler implements LogoutHandler {

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    LoggedUser user = LoggedUserContext.getCurrentUser();
    String username = user.getUsername();
    String ipAddress = user.getRemoteAddress();

    // 记录登出日志
  }

}
