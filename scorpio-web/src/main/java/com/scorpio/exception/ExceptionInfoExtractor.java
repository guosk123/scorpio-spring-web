package com.scorpio.exception;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scorpio.Constants;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExceptionInfoExtractor {

  private ExceptionInfoExtractor() {
    throw new IllegalStateException("Utility class");
  }

  public static Map<String, Object> getExceptionInfo(Throwable ex) {

    String code = null;
    String originCode = null;

    if (ex instanceof BusinessException) {
      BusinessException businessRuntimeException = (BusinessException) ex;
      code = businessRuntimeException.getCode();
      originCode = code;
    } else {

      code = ErrorCode.COMMON_BASE_EXCEPTION;
      originCode = code;
    }

    Map<String, Object> result = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("code", code);
    result.put("originCode", originCode);

    return result;
  }

  public static Map<String, Object> getExceptionInfoWithMessage(Throwable ex) {

    String code = null;
    String message = null;
    String originCode = null;

    if (ex instanceof BusinessException) {
      BusinessException businessRuntimeException = (BusinessException) ex;
      code = businessRuntimeException.getCode();
      message = businessRuntimeException.getMessage();
      originCode = code;
    } else if (ex instanceof ConstraintViolationException) {
      ConstraintViolationException violationException = (ConstraintViolationException) ex;
      code = ErrorCode.COMMON_BASE_FORMAT_INVALID;
      Set<ConstraintViolation<?>> set = violationException.getConstraintViolations();
      List<String> msgList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      set.forEach(msg -> msgList.add(msg.getMessageTemplate()));
      message = String.join(",", msgList);
      originCode = code;
    } else if (ex instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException argumentException = (MethodArgumentNotValidException) ex;
      code = ErrorCode.COMMON_BASE_FORMAT_INVALID;
      message = argumentException.getBindingResult().getFieldError().getDefaultMessage();
      originCode = code;
    } else if (ex instanceof BindException) {
      BindException bindException = (BindException) ex;
      code = ErrorCode.COMMON_BASE_FORMAT_INVALID;
      message = bindException.getBindingResult().getFieldError().getDefaultMessage();
      originCode = code;
    } else {

      code = ErrorCode.COMMON_BASE_EXCEPTION;
      message = ex.getMessage();
      originCode = code;
    }

    Map<String, Object> result = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("code", code);
    result.put("message", message);
    result.put("originCode", originCode);

    return result;
  }

}
