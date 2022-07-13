package com.scorpio.conf;

import com.scorpio.security.service.impl.SessionExpireInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  @Autowired
  private SessionExpireInterceptor sessionExpireInterceptor;

  public void addViewControllers(ViewControllerRegistry registry) {

    registry.addViewController("/").setViewName("index");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 拦截请求，进行业务处理
    registry.addInterceptor(sessionExpireInterceptor).addPathPatterns("/api/**");

  }

}
