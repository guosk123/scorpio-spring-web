package com.scorpio.handler;

import com.scorpio.util.JsonHelper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 在响应结果写入响应前，可对结果进行处理
 * httpServletResponse 只能获取一次输出流，如果在过滤器获取输出流对数据解析，就会导致返回结果为空
 */
@SuppressWarnings("rawtypes")
@ControllerAdvice
public class ResponseBodyHandler implements ResponseBodyAdvice {

  /**
   * @see ResponseBodyAdvice#supports(MethodParameter, Class)
   */
  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {
    return true;
  }

  /**
   * @see ResponseBodyAdvice#beforeBodyWrite(Object, MethodParameter, MediaType, Class, ServerHttpRequest, ServerHttpResponse)
   */
  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType,
      MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request,
      ServerHttpResponse response) {

    HttpServletRequest httpServletRequest = ((ServletRequestAttributes) (RequestContextHolder
        .getRequestAttributes())).getRequest();

    String servletPath = httpServletRequest.getServletPath();
    if (servletPath.startsWith("/restapi")) {
      HttpSession httpSession = httpServletRequest.getSession(true);
      httpSession.setAttribute("bodySize", JsonHelper.serialize(body).getBytes().length);
    }

    return body;
  }

}
