package com.scorpio.rest.dos;

/**
 * @author guosk
 *
 * create at 2022年5月20日, alpha-zurich-rest
 */
public abstract class Abstract {

  public String say() {
    return build();
  }

  protected String build() {
    return "Abstract";
  }

}
