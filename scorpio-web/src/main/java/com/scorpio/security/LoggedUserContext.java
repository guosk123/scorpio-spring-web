package com.scorpio.security;

import com.scorpio.security.bo.LoggedUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class LoggedUserContext {

  private LoggedUserContext() {
    throw new IllegalStateException("Utility class");
  }

  public static LoggedUser getCurrentUser() {
    SecurityContext ctx = SecurityContextHolder.getContext();
    Authentication auth = ctx.getAuthentication();
    if (auth == null) {
      return LoggedUser.EMPTY;
    }

    if (StringUtils.equalsIgnoreCase(auth.getName(), "anonymousUser")) {
      return LoggedUser.EMPTY;
    }

    LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
    if (StringUtils.isBlank(loggedUser.getRemoteAddress())) {
      loggedUser.setRemoteAddress(acquireRemoteAddrFromRequest());
    }
    return loggedUser;
  }

  private static String acquireRemoteAddrFromRequest() {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getRequest();
    if (StringUtils.isNotBlank(request.getHeader("x-forwarded-for"))
        && !StringUtils.equalsIgnoreCase("unknown", request.getHeader("x-forwarded-for"))) {
      return StringUtils.substringBefore(request.getHeader("x-forwarded-for"), ",");
    }
    return StringUtils.defaultIfBlank(request.getRemoteAddr(), "");
  }

}
