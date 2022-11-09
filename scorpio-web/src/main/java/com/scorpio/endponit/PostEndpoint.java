package com.scorpio.endponit;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;


@Component
@WebEndpoint(id = "testEndpoint")
public class PostEndpoint {

  // curl -XPOST http://localhost:<管理端口>/context-path/actuator/testEndpoint

  @WriteOperation
  public String write() {
    // 业务逻辑

    return "success";
  }

  @ReadOperation
  public String get(){

    return  "ok";
  }
}
