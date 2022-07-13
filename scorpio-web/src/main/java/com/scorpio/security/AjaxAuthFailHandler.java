package com.scorpio.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.util.HtmlUtils;

public class AjaxAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    // 内部服务错误返回500
    if (exception instanceof InternalAuthenticationServiceException) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Authentication internal failed");
    } else if (exception instanceof LockedException
        || exception instanceof BadCredentialsException) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
          HtmlUtils.htmlEscape(exception.getMessage(), StandardCharsets.UTF_8.name()));
    } else if (exception instanceof SessionAuthenticationException) {
      response.sendError(HttpServletResponse.SC_CONFLICT, "User already login");
    } else {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
    }
  }
}
