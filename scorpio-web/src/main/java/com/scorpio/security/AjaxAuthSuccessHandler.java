package com.scorpio.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.scorpio.security.bo.LoggedUser;

public class AjaxAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    LoggedUser user = LoggedUserContext.getCurrentUser();
    String username = user.getUsername();
    String ipAddress = user.getRemoteAddress();

    response.setStatus(HttpServletResponse.SC_OK);

    // 记录登录日志。。。
  }

}
