package com.scorpio.conf;

import com.scorpio.security.filter.StaticResourceFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import javax.servlet.ServletContext;

/**
 * 继承AbstractSecurityWebApplicationInitializer该抽象类，可通过覆盖方法，将security初始化前的操作写入
 */
@Configuration
public class SecurityApplicationInitializer extends AbstractSecurityWebApplicationInitializer {

  @Override
  protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
    insertFilters(servletContext, new StaticResourceFilter());
  }

}
