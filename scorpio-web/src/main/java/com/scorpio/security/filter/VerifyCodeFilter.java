package com.scorpio.security.filter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;

/**
 * 生成登录验证码，校验登录验证码
 */
public class VerifyCodeFilter extends AbstractAuthenticationProcessingFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCodeFilter.class);

  private final DefaultKaptcha defaultKaptcha;
  private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/verify-code", "GET");

  static {
    System.setProperty("java.awt.headless", "true");
  }

  public VerifyCodeFilter(DefaultKaptcha defaultKaptcha,
      AuthenticationFailureHandler failureHandler) {
    super(new AntPathRequestMatcher("/login", "POST"));
    super.setAuthenticationFailureHandler(failureHandler);

    this.defaultKaptcha = defaultKaptcha;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 是否是校验码生成URL
    if (requestMatcher.matches(httpRequest)) {
      responseVerifyCode(httpRequest, httpResponse);
    } else if (requiresAuthentication(httpRequest, httpResponse)) { // 是否是登录URL

      // 取输入校验码和生成校验码
      String verification = httpRequest.getParameter("code");
      String captcha = (String) httpRequest.getSession()
          .getAttribute(Constants.KAPTCHA_SESSION_KEY);
      httpRequest.getSession().removeAttribute(Constants.KAPTCHA_SESSION_KEY);// 删除避免重放攻击

      // 比较是否一致
      if (StringUtils.isNotBlank(verification)
          && StringUtils.equalsIgnoreCase(verification, captcha)) {
        chain.doFilter(httpRequest, httpResponse);
      } else {
        unsuccessfulAuthentication(httpRequest, httpResponse,
            new BadCredentialsException("Verification code failded"));
      }
    } else {
      chain.doFilter(httpRequest, httpResponse);
    }
  }

  private void responseVerifyCode(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
    try {
      // 生产验证码字符串并保存到session中
      String createText = defaultKaptcha.createText();

      request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, createText);

      // 使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
      BufferedImage challenge = defaultKaptcha.createImage(createText);
      ImageIO.write(challenge, "jpg", jpegOutputStream);

    } catch (IllegalArgumentException e) {
      LOGGER.warn("create kaptcha image failed.", e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // 定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
    byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setContentType("image/jpeg");

    ServletOutputStream responseOutputStream = response.getOutputStream();
    responseOutputStream.write(captchaChallengeAsJpeg);
    responseOutputStream.flush();
    responseOutputStream.close();
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    return null;
  }
}
