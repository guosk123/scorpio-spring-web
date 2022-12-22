package com.machloop.fpc.cms.center.restapi.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author guosk
 *
 * create at 2021年6月23日, fpc-manager
 */
public class RestAPIResultVO {

  private int code;
  private String msg;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object result;

  public static RestAPIResultVO resultSuccess(Object result) {
    return new Builder(WebappConstants.REST_RESULT_SUCCESS_CODE).msg("SUCCESS").result(result)
        .build();
  }

  public static RestAPIResultVO resultFailed(BusinessException exception) {
    int code = StringUtils.equals(exception.getCode(), ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND)
        ? FpcCmsConstants.OBJECT_NOT_FOUND_CODE
        : FpcCmsConstants.ILLEGAL_PARAMETER_CODE;
    return new Builder(code).msg(exception.getMessage()).build();
  }

  public RestAPIResultVO() {
  }

  private RestAPIResultVO(Builder builder) {
    this.code = builder.code;
    this.msg = builder.msg;
    this.result = builder.result;
  }

  @Override
  public String toString() {
    return "RestAPIResultVO [code=" + code + ", msg=" + msg + ", result=" + result + "]";
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public static class Builder {

    private int code;
    private String msg;
    private Object result;

    public Builder(int code) {
      this.code = code;
    }

    public Builder msg(String msg) {
      this.msg = msg;
      return this;
    }

    public Builder result(Object result) {
      this.result = result;
      return this;
    }

    public RestAPIResultVO build() {
      return new RestAPIResultVO(this);
    }

  }

}
