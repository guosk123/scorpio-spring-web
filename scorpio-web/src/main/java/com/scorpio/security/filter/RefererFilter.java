package com.scorpio.security.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

public final class RefererFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefererFilter.class);

  public static final RequestMatcher DEFAULT_REFERER_MATCHER = new DefaultRequiresRefererMatcher();

  private RequestMatcher requireRefererProtectionMatcher = DEFAULT_REFERER_MATCHER;

  private static String contextPath;

  public RefererFilter(String contextPath) {
    RefererFilter.contextPath = contextPath;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    LOGGER.debug("requestUrl:{}", request.getRequestURI());

    if (!requireRefererProtectionMatcher.matches(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String referer = request.getHeader("referer");
    String serverName = request.getServerName();

    String host = "";
    try {
      URL url = new URL(referer);
      host = url.getHost();
    } catch (MalformedURLException e) {
      LOGGER.warn("the request referer analysis failed: {} ", referer);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "referer analysis failed.");
      return;
    }
    LOGGER.debug("the request serverName: {}, referer.host:{}.", serverName, host);

    try {
      // 判断请求域名和referer域名是否一致
      if (!StringUtils.equals(host, serverName)) {
        // 不一致则判断该referer域名是否在白名单内
        if (StringUtils.equals(host, "localhost")) {
          LOGGER.warn("the request referer not in whitelist domain: {} ", referer);
          response.sendError(HttpServletResponse.SC_FORBIDDEN,
              "the request referer not in whitelist domain");
          return;
        }
      }
    } catch (IOException e) {
      // 系统出现异常默认拒绝请求
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static final class DefaultRequiresRefererMatcher implements RequestMatcher {
    private final HashSet<String> allowedMethods = new HashSet<>(Arrays.asList("GET", "HEAD"));

    @Override
    public boolean matches(HttpServletRequest request) {

      return this.allowedMethods.contains(request.getMethod())
          && StringUtils.startsWith(request.getRequestURI(), contextPath + "/api");
    }
  }

}
