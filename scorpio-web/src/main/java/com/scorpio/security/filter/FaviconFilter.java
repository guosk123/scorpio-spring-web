package com.scorpio.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.machloop.alpha.common.util.ImageUtils;
import com.machloop.alpha.webapp.system.bo.ProductInfoBO;
import com.machloop.alpha.webapp.system.service.ProductInfoService;

/**
 * @author guosk
 *
 * create at 2023年07月06日, machloop
 */
@Component
public class FaviconFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FaviconFilter.class);

  public static final RequestMatcher DEFAULT_STATIC_RESOURCE_MATCHER = new DefaultRequiresFaviconMatcher();

  private RequestMatcher requireFaviconMatcher = DEFAULT_STATIC_RESOURCE_MATCHER;

  @Autowired
  private ProductInfoService productInfoService;

  @Value("${server.servlet.context-path}")
  private String servletContextPath;

  private static String REQUEST_CONTEXT_PATH;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    REQUEST_CONTEXT_PATH = servletContextPath;

    if (!requireFaviconMatcher.matches(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    ProductInfoBO productInfoBO = productInfoService.queryProductInfo();
    if (StringUtils.isBlank(productInfoBO.getLogoBase64())) {
      filterChain.doFilter(request, response);
      return;
    }

    String faviconBase64 = productInfoBO.getFaviconBase64();
    if (StringUtils.isBlank(faviconBase64)) {
      ProductInfoBO result = productInfoService.updateProductInfo(productInfoBO, "3");
      faviconBase64 = result.getFaviconBase64();
    }

    ImageUtils.base64ToImageResponse(faviconBase64, "image/x-icon", response);
  }

  private static final class DefaultRequiresFaviconMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {

      String requestURI = request.getRequestURI().replace(REQUEST_CONTEXT_PATH, "");
      return requestURI.startsWith("/web-static/custom-static/icon/favicon.ico");
    }
  }

}
