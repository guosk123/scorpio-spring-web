package com.scorpio.rest.dos;

import org.springframework.stereotype.Service;

/**
 * @author guosk
 *
 * create at 2022年5月20日, alpha-zurich-rest
 */
@Service("testA")
public class TestA extends Abstract implements TestI {

  public String build() {
    return "TestA";
  }

}
