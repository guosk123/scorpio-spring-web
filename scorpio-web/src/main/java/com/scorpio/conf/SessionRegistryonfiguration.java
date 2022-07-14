package com.scorpio.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

/**
 * 配置
 */
@Configuration
public class SessionRegistryonfiguration {

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }
}
