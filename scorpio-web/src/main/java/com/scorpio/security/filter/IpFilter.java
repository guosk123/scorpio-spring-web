package com.scorpio.security.filter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 可通过过滤器限制可登录的IP
 */
public class IpFilter extends AbstractAuthenticationProcessingFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpFilter.class);

  public static final RequestMatcher DEFAULT_REFERER_MATCHER = new DefaultRequiresRefererMatcher();

  private RequestMatcher requestMatcher = DEFAULT_REFERER_MATCHER;

  private LoadingCache<String, Boolean> cache = CacheBuilder.newBuilder().maximumSize(128)
      .expireAfterWrite(3, TimeUnit.SECONDS).build(new CacheLoader<String, Boolean>() {
        @Override
        public Boolean load(String key) throws Exception {
          try {
            // 判断当前IP在白名单内
            return true;
          } catch (Exception e) {
            return false;
          }
        }
      });

  public IpFilter() {
    super("/api/**");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (!requiresAuthentication(httpRequest, httpResponse)
        && !requestMatcher.matches(httpRequest)) {
      chain.doFilter(httpRequest, httpResponse);
      return;
    }

    // 获取IP
    String remoteAddr = request.getRemoteAddr();
    if (StringUtils.isNotBlank(httpRequest.getHeader("x-forwarded-for"))
        && !StringUtils.equalsIgnoreCase("unknown", httpRequest.getHeader("x-forwarded-for"))) {
      remoteAddr = StringUtils.substringBefore(httpRequest.getHeader("x-forwarded-for"), ",");
    }

    boolean isPass = false;
    try {
      isPass = cache.get(remoteAddr);
    } catch (ExecutionException e) {
      LOGGER.warn("failed to check login ip range." + e);
    }

    if (!isPass) {
      LOGGER.warn("the current ip is not within the allowed ip range, current ip is {}.",
          remoteAddr);
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
          "无权登录：" + remoteAddr + "不在允许登录IP范围内");
      return;
    }

    chain.doFilter(httpRequest, httpResponse);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
    return null;
  }

  private static final class DefaultRequiresRefererMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
      return StringUtils.equalsAny(request.getRequestURI(), "/login",
          request.getContextPath() + "/login");
    }
  }
}
