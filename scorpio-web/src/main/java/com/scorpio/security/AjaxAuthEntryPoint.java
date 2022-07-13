package com.scorpio.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.util.HtmlUtils;

public class AjaxAuthEntryPoint implements AuthenticationEntryPoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(AjaxAuthEntryPoint.class);

  private final AuthenticationEntryPoint authenticationEntryPoint;

  public AjaxAuthEntryPoint(String loginPage) {
    this.authenticationEntryPoint = new LoginUrlAuthenticationEntryPoint(loginPage);
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    String uri = request.getRequestURI();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("auth commence for uri {}", StringUtils.replace(uri, "\r\n", ""));
    }

    if (isAjaxRequest(request)) {

      Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
      String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

      if (statusCode == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
            HtmlUtils.htmlEscape(authException.getMessage(), StandardCharsets.UTF_8.name()));
      } else {
        response.sendError(statusCode,
            HtmlUtils.htmlEscape(message, StandardCharsets.UTF_8.name()));
      }
    } else {
      authenticationEntryPoint.commence(request, response, authException);
    }
  }

  private boolean isAjaxRequest(HttpServletRequest request) {
    String ajaxFlag = request.getHeader("X-Requested-With");
    return ajaxFlag != null && "XMLHttpRequest".equals(ajaxFlag);
  }
}
