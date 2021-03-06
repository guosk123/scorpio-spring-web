package com.scorpio.conf;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.scorpio.security.AjaxAuthEntryPoint;
import com.scorpio.security.AjaxAuthFailHandler;
import com.scorpio.security.AjaxAuthSuccessHandler;
import com.scorpio.security.AjaxLogoutHandler;
import com.scorpio.security.filter.IpFilter;
import com.scorpio.security.filter.RefererFilter;
import com.scorpio.security.filter.VerifyCodeFilter;

/**
 * ?????????security????????????????????????security(WebSecurity)
 */
// @Configuration
public class SecurityConfiguration_old extends WebSecurityConfigurerAdapter {

  @Value("${loggeduser.concurrent.max.session}")
  private String loggedUserMaxSession;

  @Value("${server.servlet.context-path}")
  private String servletContextPath;

  @Autowired
  private SessionRegistry sessionRegistry;

  @Autowired
  private DefaultKaptcha defaultKaptcha;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // @formatter:off
    // ???????????????????????????
    http.httpBasic().disable();

    // ????????????
    http.authorizeRequests()
    .antMatchers("/actuator/**", "/web-static/**", "/error", "/api/**", "/restapi/**")
    .permitAll()
    .anyRequest()
    .fullyAuthenticated();

    AuthenticationSuccessHandler successHandler = new AjaxAuthSuccessHandler();
    AuthenticationFailureHandler failureHandler = new AjaxAuthFailHandler();
    LogoutHandler logoutHandler = new AjaxLogoutHandler();
    
    // Ajax????????????
    http.exceptionHandling()
        .authenticationEntryPoint(new AjaxAuthEntryPoint("/login"))
        .and()
        .formLogin()
        .successHandler(successHandler)
        .failureHandler(failureHandler)
        .permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/")
        .addLogoutHandler(logoutHandler)
        .permitAll()
        .and()
        .sessionManagement()
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy());
    
    // iframe
    http.headers().frameOptions().disable();
    
    // ???csp?????????header
    http.headers()
      .contentTypeOptions()
      .and()
      .xssProtection()
      .and()
      .cacheControl()
      .and()
      .httpStrictTransportSecurity()
      .and()
      .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "default-src 'self'"));
      
    // ?????????????????????filter
    http.addFilterBefore(new VerifyCodeFilter(defaultKaptcha, failureHandler),
            UsernamePasswordAuthenticationFilter.class);

    // ??????IP???????????????filter???????????????IP????????????
    http.addFilterAfter(new IpFilter(), VerifyCodeFilter.class);
    
    // ??????referrer?????????????????????????????????referer???????????????????????????????????????????????????
    http.addFilterAfter(new RefererFilter(servletContextPath), CsrfFilter.class);

    http.addFilter(new ConcurrentSessionFilter(sessionRegistry, new SessionInformationExpiredStrategy() {
      // session????????????
         @Override
         public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
             throws IOException, ServletException {
           event.getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED, "session expired");
         }
       }));
    
    http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    http.csrf().ignoringAntMatchers("/actuator/**","/restapi/**", "/api/**");
    
    // @formatter:on
  }

  @Bean
  public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
    // ?????????????????????
    ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(
        sessionRegistry) {
      @Override
      public void onAuthentication(Authentication authentication, HttpServletRequest request,
          HttpServletResponse response) {
        // ????????????????????????-1????????????
        super.setMaximumSessions(Integer.parseInt(loggedUserMaxSession));

        super.onAuthentication(authentication, request, response);
      }

      @Override
      protected void allowableSessionsExceeded(List<SessionInformation> sessions,
          int allowableSessions, SessionRegistry registry) throws SessionAuthenticationException {
        super.allowableSessionsExceeded(sessions, allowableSessions, registry);

        // ????????????????????????????????????????????????
        for (SessionInformation session : sessions) {
          session.expireNow();
        }
      }
    };

    SessionFixationProtectionStrategy sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();

    RegisterSessionAuthenticationStrategy registerSessionStrategy = new RegisterSessionAuthenticationStrategy(
        sessionRegistry);
    CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy = new CompositeSessionAuthenticationStrategy(
        Arrays.asList(concurrentSessionControlAuthenticationStrategy,
            sessionFixationProtectionStrategy, registerSessionStrategy));
    return sessionAuthenticationStrategy;
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // ??????????????????Security
    web.ignoring().antMatchers("/web-static/**", "/error");
  }

}
