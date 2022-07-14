package com.scorpio.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.scorpio")
public class WebApplication {

  public static void main(String[] args) {
    try {
      System.setProperty("jasypt.encryptor.password", "scorpio@123");

      SpringApplication.run(WebApplication.class, args);
    } catch (Throwable t) {
      System.out.println(t);
      System.exit(0);
    }
  }

}
