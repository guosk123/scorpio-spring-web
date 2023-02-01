package com.machloop.iosp.sdk.highlevel;

import java.io.IOException;

import com.machloop.iosp.sdk.Constants.Sort;
import com.machloop.iosp.sdk.Constants.SubscribeType;
import com.machloop.iosp.sdk.Core;
import com.machloop.iosp.sdk.CursorFullObjectResult;
import com.machloop.iosp.sdk.CursorMetadataResult;
import com.machloop.iosp.sdk.FullObject;
import com.machloop.iosp.sdk.Metadata;
import com.machloop.iosp.sdk.SearchCondition;
import com.machloop.iosp.sdk.WriteFullObject;
import com.machloop.iosp.sdk.subscribe.MetadataListener;
import com.machloop.iosp.sdk.subscribe.ObjectListener;
import com.machloop.iosp.sdk.subscribe.SubscribeMetadataResult;
import com.machloop.iosp.sdk.subscribe.SubscribeMetadataTask;
import com.machloop.iosp.sdk.subscribe.SubscribeObjectResult;
import com.machloop.iosp.sdk.subscribe.SubscribeObjectTask;

public class IospConnection implements AutoCloseable {

  static {
    String logPath = System.getProperty("iosp.sdk.logpath");
    Core.globalInit(logPath);
  }

  private boolean closed = false;
  private Object closeLock = new Object();

  private boolean useDirectByteBuffer = false;

  private String ip;
  private short port;
  private String clientId;
  private String clientToken;

  private long pLake = 0;

  public IospConnection(String ip, String clientId, String clientToken) {
    this(ip, (short) 1100, clientId, clientToken, false);
  }

  public IospConnection(String ip, short port, String clientId, String clientToken) {
    this(ip, port, clientId, clientToken, false);
  }

  public IospConnection(String ip, short port, String clientId, String clientToken,
      boolean useDirectByteBuffer) {
    this.ip = ip;
    this.port = port;
    this.clientId = clientId;
    this.clientToken = clientToken;
    this.useDirectByteBuffer = useDirectByteBuffer;
  }

  /**
   * 与IOSP服务端建立连接
   * @return 
   * @throws IOException
   */
  public void connect() throws IOException {
    this.pLake = Core.syncInit((char) 0, this.ip, this.port, this.clientId, this.clientToken);
    this.closed = false;
  }

  /**
   * 根据查询条件搜索对象（包含对象元数据及对象内容）
   * @param searchCondition 查询条件
   * @param sortType 排序方式
   * @param searchNum 一次查询返回最大结果数量
   * @param cursor 游标
   * @return 对象元数据及内容的搜索结果
   * @throws IOException
   */
  public synchronized CursorFullObjectResult search(SearchCondition searchCondition, Sort sortType,
      int searchNum, int cursor) throws IOException {
    return Core.search(this.pLake, searchCondition, sortType.value(), searchNum, cursor,
        this.useDirectByteBuffer);
  }

  /**
   * 根据查询条件搜索对象元数据（仅包含对象元数据，不包含对象内容）
   * @param searchCondition 查询条件
   * @param sortType 排序方式
   * @param searchNum 一次查询返回最大结果数量
   * @param cursor 游标
   * @return 对象元数据的搜索结果
   * @throws IOException
   */
  public synchronized CursorMetadataResult searchMetadata(SearchCondition searchCondition,
      Sort sortType, int searchNum, int cursor) throws IOException {
    return Core.searchMetadata(this.pLake, searchCondition, sortType.value(), searchNum, cursor);
  }

  /**
   * 根据查询条件搜索对象（包含对象元数据及对象内容），并将查询结果写入指定目录。
   * 目录下会创建包含对象元数据信息的CSV文件
   * 按照指定的目录最大文件数量将文件分到多个子目录。
   * @param searchCondition 查询条件
   * @param savePath 存放路径
   * @param dirFileNum 单个文件夹中的对象最大数量
   */
  public synchronized void searchAndWriteDisk(SearchCondition searchCondition, String savePath,
      int dirFileNum) {
    Core.searchAndWriteDisk(this.pLake, searchCondition, savePath, dirFileNum);
  }

  /**
   * 根据查询条件查询对象统计信息。
   * @param searchCondition 查询条件
   * @return 统计信息结果
   * @throws IOException
   */
  public synchronized AnalysResult searchAnalys(SearchCondition searchCondition)
      throws IOException {
    String[] result = Core.searchAnalys(this.pLake, searchCondition);
    return new AnalysResult(result[0], result[1]);
  }

  /**
   * 根据对象ID查询完整对象（包含对象元数据及对象内容）
   * @param objectId 对象ID
   * @return 
   * @throws IOException
   */
  public synchronized FullObject read(String objectId) throws IOException {
    return Core.read(this.pLake, objectId, this.useDirectByteBuffer);
  }

  /**
   * 根据对象ID查询对象元数据（仅包含对象元数据，不包含对象内容）
   * @param objectId 对象ID
   * @return
   * @throws IOException
   */
  public synchronized Metadata readMetadata(String objectId) throws IOException {
    return Core.readMetadata(this.pLake, objectId);
  }

  /**
   * 写入对象
   * @param writeFullObject 需要写入的对象元数据及内容
   * @return 写入成功对象的对象ID
   * @throws IOException
   */
  public String write(WriteFullObject writeFullObject) throws IOException {
    return Core.write(this.pLake, writeFullObject);
  }

  /**
   * 根据对象ID修改对象标签信息
   * @param objectId 对象ID
   * @param label 新标签
   * @throws IOException
   */
  public void modifyLabel(String objectId, String label) throws IOException {
    Core.modifyLabel(this.pLake, objectId, label);
  }

  /**
   * 创建订阅任务（单个连接只能创建一个订阅任务）
   * @param searchCondition 查询条件
   * @param type 订阅类型
   * @param taskId 0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重新订阅
   * @return
   * @throws IOException
   */
  public synchronized long createSubscribeTask(SearchCondition searchCondition, SubscribeType type,
      long taskId) throws IOException {
    return Core.createSubscribeTask(this.pLake, searchCondition, type.value(), taskId);
  }

  /**
   * 消费订阅完整数据（订阅任务类型为订阅完整对象）
   * @param taskId 订阅任务ID
   * @return
   * @throws IOException
   */
  public synchronized SubscribeObjectResult consumerObject(long taskId) throws IOException {
    return Core.consumeSubscribeObject(this.pLake, taskId, this.useDirectByteBuffer);
  }

  /**
   * 消费订阅元数据（订阅任务类型为订阅元数据）
   * @param taskId 订阅任务ID
   * @return
   * @throws IOException
   */
  public synchronized SubscribeMetadataResult consumerMetadata(long taskId) throws IOException {
    return Core.consumeSubscribeMetadata(this.pLake, taskId);
  }

  /**
   * 销毁当前连接存在的订阅任务
   * @throws IOException
   */
  public synchronized void destorySubscribeTask() throws IOException {
    Core.destroySubscribe(this.pLake);
  }

  /**
   * 订阅完整对象
   * @param searchCondition 查询条件
   * @param taskId 0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重新订阅
   * @param listener 对象消费监听器
   * @return 订阅任务对象
   * @throws IOException
   */
  public synchronized SubscribeObjectTask subscribe(SearchCondition searchCondition, long taskId,
      ObjectListener listener) throws IOException {

    return new SubscribeObjectTask(this.pLake, searchCondition, taskId, listener,
        this.useDirectByteBuffer);
  }

  /**
   * 订阅对象元数据
   * @param searchCondition 查询条件
   * @param taskId 0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重新订阅
   * @param listener 对象元数据消费监听器
   * @return 订阅任务对象
   * @throws IOException
   */
  public synchronized SubscribeMetadataTask subscribeMetadata(SearchCondition searchCondition,
      long taskId, MetadataListener listener) throws IOException {

    return new SubscribeMetadataTask(this.pLake, searchCondition, taskId, listener);
  }

  /**
   * 释放查询结果
   */
  private void resultRelease() {
    Core.resultRelease(this.pLake);
  }

  /**
   * @return 是否连接
   */
  public boolean isConnected() {
    return this.pLake != 0;
  }

  /**
   * @return 是否关闭
   */
  public boolean isClosed() {
    synchronized (closeLock) {
      return closed;
    }
  }

  /**
   * 断开与IOSP服务端的连接，断开连接后不能再进行操作
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public void close() {
    synchronized (closeLock) {
      if (isClosed()) return;
      if (isConnected()) {
        resultRelease();
        Core.destory(this.pLake);
        this.pLake = 0;
      }
      closed = true;
    }
  }
}
