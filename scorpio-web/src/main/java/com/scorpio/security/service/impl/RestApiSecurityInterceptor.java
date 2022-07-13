package com.scorpio.security.service.impl;

import com.scorpio.security.service.RestApiSecured;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class RestApiSecurityInterceptor extends ApiSecurityInterceptor
        implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiSecurityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        LOGGER.debug("Start to execute restapi security interceptor...");

        HandlerMethod method = (HandlerMethod) handler;
        RestApiSecured methodAnnotation = method.getMethodAnnotation(RestApiSecured.class);
        if (methodAnnotation == null) {
            return true;
        }

        // 校验添加了restapi权限校验注解的请求信息
        // 如header信息
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
            return false;
        }

        LOGGER.debug("Successful execution of restapi security interceptor.");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 请求没有异常时，进入该方法
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 请求无论成功还是失败最终都会进入该方法
    }

}
