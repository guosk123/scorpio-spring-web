package com.scorpio.exception;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.scorpio.Constants;

public class BusinessException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String code;
  private final String message;
  private final String originCode;
  private final List<String> transmitSystems;
  private final transient List<StringWriter> exceptions;
  private final transient List<Map<String, Object>> requestParams;

  public BusinessException(String code, String message) {
    super();
    this.code = code;
    this.message = message;
    this.originCode = code;
    this.transmitSystems = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    this.exceptions = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    this.requestParams = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  public BusinessException(String code, String message, Object... msgArgs) {
    super();
    this.code = code;
    this.message = String.format(message, msgArgs);
    this.originCode = code;
    this.transmitSystems = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    this.exceptions = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    this.requestParams = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  public BusinessException(String code, String message, BusinessException e) {
    super();
    this.code = code;
    this.message = message;
    this.originCode = e.getOriginCode();
    this.transmitSystems = e.getTransmitSystems();
    this.exceptions = e.getExceptions();
    this.requestParams = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getCode() {
    return code;
  }


  public String getOriginCode() {
    return originCode;
  }

  public List<String> getTransmitSystems() {
    return transmitSystems;
  }

  public List<StringWriter> getExceptions() {
    return exceptions;
  }

  public List<Map<String, Object>> getRequestParams() {
    return requestParams;
  }
}
