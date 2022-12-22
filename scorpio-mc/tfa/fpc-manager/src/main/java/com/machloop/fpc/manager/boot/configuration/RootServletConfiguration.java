package com.machloop.fpc.manager.boot.configuration;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1、WEB页面访问根路径（https://ip:port）时自动重定向到servlet.context-path
 * 2、tomcat graceful shutdown
 * 
 * @author guosk
 *
 * create at 2020年10月26日, fpc-manager
 */
@Configuration
public class RootServletConfiguration {

  public static final Logger LOGGER = LoggerFactory.getLogger(RootServletConfiguration.class);

  @Autowired
  private GracefulShutdownTomcat gracefulShutdownTomcat;

  @Bean
  public TomcatServletWebServerFactory servletWebServerFactory() {
    CustomTomcatServletWebServerFactory tomcatServletWebServerFactory = new CustomTomcatServletWebServerFactory();
    tomcatServletWebServerFactory.addConnectorCustomizers(gracefulShutdownTomcat);

    return tomcatServletWebServerFactory;
  }

  static final class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

    @Override
    protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
      super.prepareContext(host, initializers);

      String contextPath = getContextPath();
      if (StringUtils.isBlank(contextPath) || StringUtils.equals("/", contextPath)) {
        return;
      }

      StandardContext child = new StandardContext();
      child.setName(System.currentTimeMillis() + "");
      child.addLifecycleListener(new Tomcat.FixContextListener());
      child.setPath("");
      child.setSessionCookiePath("/manager");
      ServletContainerInitializer initializer = getServletContextInitializer(contextPath);
      child.addServletContainerInitializer(initializer, Collections.emptySet());
      child.setCrossContext(true);
      host.addChild(child);
    }

  }

  private static final ServletContainerInitializer getServletContextInitializer(
      String contextPath) {
    return (c, context) -> {
      Servlet servlet = new HttpServlet() {

        private static final long serialVersionUID = -4654151668459966241L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
          resp.sendRedirect(contextPath);
        }

      };
      context.addServlet("root", servlet).addMapping("");
    };
  }

}
