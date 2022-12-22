package com.machloop.fpc.cms.center.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.machloop.alpha.common.helper.GracefulShutdownHelper;

@SpringBootApplication(
    exclude = {MongoAutoConfiguration.class, SessionAutoConfiguration.class,
        RedisAutoConfiguration.class})
@EnableScheduling
@ComponentScan(
    basePackages = {"com.machloop.alpha.webapp.boot", "com.machloop.fpc.cms.center.boot",
        "com.machloop.alpha.common", "com.machloop.alpha.webapp", "com.machloop.fpc.cms.common",
        "com.machloop.fpc.cms.center", "com.machloop.fpc.cms.baseline", "com.machloop.fpc.cms.npm"})
public class CenterApplication {

  public static void main(String[] args) {
    try {
      GracefulShutdownHelper.registShutdownHook();

      System.setProperty("jasypt.encryptor.password", "*DF226EC45AE584C0EBA60DB6DF2F94F7F5C08375");
      
      SpringApplication.run(CenterApplication.class, args);
    } catch (Throwable t) {
      System.out.println(t);
      System.exit(0);
    }
  }
}