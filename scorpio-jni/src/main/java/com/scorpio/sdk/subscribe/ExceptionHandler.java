package com.machloop.iosp.sdk.subscribe;

public interface ExceptionHandler {

  /**
   * 异常处理
   * @param e 运行期间异常
   * @return true:中断线程，停止订阅；false：继续执行
   */
  boolean onRuningFailure(Exception e);

}
