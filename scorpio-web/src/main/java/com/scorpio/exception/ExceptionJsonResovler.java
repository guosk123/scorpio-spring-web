package com.scorpio.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Map;

@ControllerAdvice
public class ExceptionJsonResovler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionJsonResovler.class);

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Throwable.class)
  @ResponseBody
  public Map<String, Object> handleException(HttpServletRequest request, Throwable e) {
    LOGGER.warn("", e);
    return ExceptionInfoExtractor.getExceptionInfo(e);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(BusinessException.class)
  @ResponseBody
  public Map<String, Object> handleBusinessException(HttpServletRequest request, Throwable e) {
    return ExceptionInfoExtractor.getExceptionInfoWithMessage(e);
  }

  /**
   * 拦截参数格式异常
   * @param request
   * @param e
   * @return
   */
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(
      value = {ConstraintViolationException.class, BindException.class,
          MethodArgumentNotValidException.class})
  @ResponseBody
  public Map<String, Object> handleValidationException(HttpServletRequest request, Throwable e) {
    return ExceptionInfoExtractor.getExceptionInfoWithMessage(e);
  }

  /**
   * 拦截权限不足异常
   * @param request
   * @param e
   * @return
   */
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public Map<String, Object> handleAccessDeniedException(HttpServletRequest request, Throwable e) {
    return ExceptionInfoExtractor.getExceptionInfo(e);
  }

}
