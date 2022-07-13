package com.scorpio.security.service.impl;

import com.scorpio.Constants;
import com.scorpio.util.TokenUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public abstract class ApiSecurityInterceptor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiSecurityInterceptor.class);

  /**
   * 判断ticket是否可用
   * @param token
   * @param machKey
   * @param timestamp
   * @param signature
   * @return
   */
  protected boolean validateTicket(String token, String machKey, String timestamp,
      String signature) {

    if (StringUtils.isBlank(token) || StringUtils.isBlank(machKey) || StringUtils.isBlank(timestamp)
        || StringUtils.isBlank(signature)) {
      return false;
    }

    String computeSignature = TokenUtils.makeSignature(token, machKey, timestamp);
    return StringUtils.equalsIgnoreCase(computeSignature, signature);
  }

  /**
   * @param timestamp
   * @return
   */
  protected boolean hasExpire(String timestamp) {
    long current = System.currentTimeMillis();
    boolean hasExpire = false;
    try {
      long timestampLong = Long.parseLong(timestamp);
      hasExpire = Math.abs(timestampLong - current) > Constants.TIMESTAMP_MAX_GAP_MILLSEC;
    } catch (NumberFormatException e) {
      LOGGER.warn("parse timestamp from header failed.", e);
      hasExpire = true;
    }
    return hasExpire;
  }
}
