package com.machloop.iosp.sdk.subscribe;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.machloop.iosp.sdk.Constants.SubscribeType;
import com.machloop.iosp.sdk.Core;
import com.machloop.iosp.sdk.FullObject;
import com.machloop.iosp.sdk.SearchCondition;

public class SubscribeObjectTask {

  private static final ThreadPoolExecutor defaultExecutor = new ThreadPoolExecutor(0, 1, 3,
      TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

  private volatile boolean finished = false;

  private long pLake = 0;
  private SearchCondition searchCondition = null;
  private SubscribeType type = SubscribeType.SUB_TYPE_OBJECT;
  private long taskId = 0;
  private boolean useDirectByteBuffer = false;

  private ObjectListener listener;
  private ExceptionHandler handler;
  private ThreadPoolExecutor executor;

  public SubscribeObjectTask(long pLake, SearchCondition searchCondition, long taskId,
      ObjectListener listener, boolean useDirectByteBuffer) {
    this(pLake, searchCondition, taskId, listener, defaultExecutor, useDirectByteBuffer);
  }

  public SubscribeObjectTask(long pLake, SearchCondition searchCondition, long taskId,
      ObjectListener listener, ThreadPoolExecutor executor, boolean useDirectByteBuffer) {
    this.pLake = pLake;
    this.searchCondition = searchCondition;
    this.taskId = taskId;
    this.listener = listener;
    this.useDirectByteBuffer = useDirectByteBuffer;
    this.executor = executor;
  }

  /**
   * 自定义线程池，调用者可使用己方线程池
   * @param executor
   */
  public void setExecutor(ThreadPoolExecutor executor) {
    this.executor = executor;
  }

  /**
   * 自定义异常处理
   * @param handler
   */
  public void setExceptionHandler(ExceptionHandler handler) {
    this.handler = handler;
  }

  /**
   * 开始订阅
   * @throws IOException
   */
  public void start() throws IOException {

    taskId = Core.createSubscribeTask(pLake, searchCondition, type.value(), taskId);
    finished = false;

    executor.execute(new Runnable() {

      @Override
      public void run() {
        Thread.currentThread().setName("subscribeObjectTask");

        while (!finished && !Thread.currentThread().isInterrupted()) {
          try {
            SubscribeObjectResult subscribeObjectResult = Core.consumeSubscribeObject(pLake, taskId,
                useDirectByteBuffer);

            ConsumeContext context = new ConsumeContext();

            int hasNext = subscribeObjectResult.getHasNext();
            if (hasNext == 0) {
              finished = false;
              FullObject[] objects = subscribeObjectResult.getObjects();
              if (objects.length == 0) {
                continue;
              }

              context.setCurrentConsumerSize(objects.length);
              context.setHasNext(true);

              listener.consume(objects, context);
            } else if (hasNext == 1) {
              shutdown();
              listener.consume(new FullObject[]{}, context);
              break;
            }
          } catch (IOException e) {
            // 异常处理
            if (handler == null) {
              try {
                TimeUnit.SECONDS.sleep(3);
              } catch (InterruptedException e1) {
              }
            } else {
              boolean isInterrupted = handler.onRuningFailure(e);
              if (isInterrupted) {
                finished = true;
                break;
              }
            }
          }
        }
      }
    });

  }

  /**
   * 关闭订阅
   * @throws IOException
   */
  public void shutdown() throws IOException {
    Core.destroySubscribe(pLake);
    finished = true;
  }

  /**
   * 任务是否已结束
   * @return
   */
  public boolean isfinished() {
    return !finished;
  }

}
