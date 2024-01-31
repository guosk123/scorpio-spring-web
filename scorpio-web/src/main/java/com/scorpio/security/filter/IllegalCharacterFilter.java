package com.scorpio.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;

/**
 * @author guosk
 *
 * create at 2022年1月19日, alpha-webapp
 */
public final class IllegalCharacterFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IllegalCharacterFilter.class);

  public static final RequestMatcher DEFAULT_REFERER_MATCHER = new DefaultRequiresMatcher();

  private RequestMatcher requireProtectionMatcher = DEFAULT_REFERER_MATCHER;

  private static final Pattern ILLEGAL_CHARACTER_PATTERN = Pattern.compile("[*?;!\\\'\\`|]{1}");

  /**
   * 忽略校验字段
   */
  private static final List<String> EXCEPT_FIELD = Lists.newArrayList("filterBpf", "exceptBpf",
      "filterSpl", "password", "oldPassword");

  private static String contextPath;
  private static Set<
      String> exceptUriSet = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);

  public IllegalCharacterFilter(String contextPath, Set<String> exceptUri) {
    IllegalCharacterFilter.contextPath = contextPath;
    IllegalCharacterFilter.exceptUriSet.addAll(exceptUri);
  }

  /**
   * @see OncePerRequestFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    LOGGER.debug("requestUrl:{}, method: {}.", request.getRequestURI(), request.getMethod());

    if (!requireProtectionMatcher.matches(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    Map<String, String[]> parameterMap = request.getParameterMap();
    if (MapUtils.isEmpty(parameterMap)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 只校验form表单数据
    Set<String> illegalCharacters = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    parameterMap.forEach((paramName, paramValues) -> {
      if (!EXCEPT_FIELD.contains(paramName)) {
        for (String value : paramValues) {
          Matcher matcher = ILLEGAL_CHARACTER_PATTERN.matcher(value);
          while (matcher.find()) {
            illegalCharacters.add(matcher.group());
          }
        }
      }
    });

    if (!illegalCharacters.isEmpty()) {
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
          String.format("内容存在非法字符： [%s]", CsvUtils.convertCollectionToCSV(illegalCharacters)));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static final class DefaultRequiresMatcher implements RequestMatcher {
    private final HashSet<
        String> allowedMethods = new HashSet<>(Arrays.asList("POST", "PUT", "DELETE", "OPTIONS"));

    @Override
    public boolean matches(HttpServletRequest request) {
      String truncateURI = "";
      if (StringUtils.equals(request.getMethod(), "PUT")) {
        // PUT请求，URI后可能包含ID， xxx/{id}
        truncateURI = StringUtils.substringBeforeLast(request.getRequestURI(), "/");
      }

      return this.allowedMethods.contains(request.getMethod())
          && StringUtils.startsWith(request.getRequestURI(), contextPath + "/webapi")
          && !(exceptUriSet.contains(request.getRequestURI())
              || exceptUriSet.contains(truncateURI));
    }
  }

}
