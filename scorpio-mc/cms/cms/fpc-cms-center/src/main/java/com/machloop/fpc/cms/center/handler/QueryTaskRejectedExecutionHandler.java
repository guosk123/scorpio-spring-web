package com.machloop.fpc.cms.center.handler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;

/**
 * @author guosk
 *
 * create at 2021年10月12日, fpc-manager
 */
public class QueryTaskRejectedExecutionHandler implements RejectedExecutionHandler {

  /**
   * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
   */
  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "当前并发查询任务数过多，请稍后重试");
  }

}
