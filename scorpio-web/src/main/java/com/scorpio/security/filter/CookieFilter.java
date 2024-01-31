package com.scorpio.security.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.GenericFilterBean;

import com.machloop.alpha.common.util.JsonHelper;

/**
 * @author guosk
 *
 * create at 2020年12月30日, alpha-webapp
 */
public class CookieFilter extends GenericFilterBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(CookieFilter.class);

  /**
   * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse resp = (HttpServletResponse) response;
    Collection<String> headers = resp.getHeaders(HttpHeaders.SET_COOKIE);

    if (CollectionUtils.isNotEmpty(headers)) {
      LOGGER.debug("add SameSite to cookie attribute: cookie:[{}]", JsonHelper.serialize(headers));
      boolean firstHeader = true;
      for (String header : headers) {
        if (firstHeader) {
          resp.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
          firstHeader = false;
          continue;
        }
        resp.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
      }
    }

    chain.doFilter(request, response);
  }

}
