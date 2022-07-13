package com.scorpio.security.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * springSecurity只对动态资源进行保护（涉及到数据的，增删改查），所以默认Security忽略对静态资源的校验，如果想校验静态资源可编写该过滤器，并且将该过滤器放在springSecurity初始化前
 */
@Component
public class StaticResourceFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceFilter.class);

  public static final RequestMatcher DEFAULT_STATIC_RESOURCE_MATCHER = new DefaultRequiresStaticResourceMatcher();

  private RequestMatcher requireStaticResourceMatcher = DEFAULT_STATIC_RESOURCE_MATCHER;

  @Value("${server.servlet.context-path}")
  private String servletContextPath;

  private static String REQUEST_CONTEXT_PATH;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    REQUEST_CONTEXT_PATH = servletContextPath;

    if (!requireStaticResourceMatcher.matches(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 为拦截到的静态资源加上header
    LOGGER.debug("set header to static resource.");
    response.addHeader("Content-Security-Policy", "default-src 'self'");
    response.addHeader("X-Content-Type-Options", "nosniff");
    response.addHeader("X-XSS-Protection", "1; mode=block");
    response.addHeader("Strict-Transport-Security",
        "max-age=" + Integer.MAX_VALUE + " ; includeSubDomains");

    filterChain.doFilter(request, response);
  }

  private static final class DefaultRequiresStaticResourceMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {

      // 拦截静态资源
      String requestURI = request.getRequestURI().replace(REQUEST_CONTEXT_PATH, "");
      return requestURI.startsWith("/web-static") || requestURI.startsWith("/error");
    }
  }

}

