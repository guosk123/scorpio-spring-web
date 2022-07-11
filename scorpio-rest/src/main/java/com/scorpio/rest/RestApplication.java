package com.scorpio.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestApplication {

  public static void main(String[] args) {

    System.setProperty("jasypt.encryptor.password", "*DF226EC45AE584C0EBA60DB6DF2F94F7F5C08375");

    SpringApplication.run(RestApplication.class, args);
  }

}
