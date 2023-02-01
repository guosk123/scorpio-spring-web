import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.machloop.iosp.sdk.Constants.Sort;
import com.machloop.iosp.sdk.Constants.SubscribeType;
import com.machloop.iosp.sdk.CursorFullObjectResult;
import com.machloop.iosp.sdk.CursorMetadataResult;
import com.machloop.iosp.sdk.FullObject;
import com.machloop.iosp.sdk.Metadata;
import com.machloop.iosp.sdk.SearchCondition;
import com.machloop.iosp.sdk.WriteFullObject;
import com.machloop.iosp.sdk.highlevel.AnalysResult;
import com.machloop.iosp.sdk.highlevel.IospConnection;
import com.machloop.iosp.sdk.subscribe.*;

public class IospSDKTest {
  public static void main(String[] args) throws IOException {

    // Datanode IP
    String ip = "10.0.0.1";
    short port = 1100;
    String clientId = "C1";
    String clientToken = "xiwoiqehoiasdhasd";

    IospConnection conn = new IospConnection(ip, port, clientId, clientToken, false);
    // 使用DirectByteBuffer方式
    // IospConnection conn = new IospConnection(ip, port, clientId, clientToken, true);

    // 连接IOSP
    conn.connect();

    // 构建写入对象
    byte[] picContent = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    WriteFullObject writeFullObject = WriteFullObject.create("zoneKey", "picture.jpg", picContent)
        .setLabel("label").setSite("site").setCreateTime(new Date()).build();

    // 写入ByteBuffer类型的对象内容
    // ByteBuffer contentByteBuffer = ByteBuffer.allocateDirect(10);
    // WriteFullObject writeFullObject = WriteFullObject
    // .create("zoneKey", "picture.jpg", contentByteBuffer).setLabel("label").setSite("site")
    // .setCreateTime(new Date()).build();

    // 向IOSP写入对象，返回对象ID
    String objectId = conn.write(writeFullObject);

    // 使用对象ID查询对象
    FullObject fullObject = conn.read(objectId);

    // 使用对象ID查询对象元数据
    Metadata metadata = conn.readMetadata(objectId);

    // 使用对象ID修改对象标签
    conn.modifyLabel(objectId, "newLabel");

    // 构建搜索条件
    Date startTime = new Date(1577808000000L);
    Date endTime = new Date(1606752000000L);
    SearchCondition condition = SearchCondition.create("zoneKey", startTime, endTime)
        .setLabel("label").setSite("site").build();

    {
      // 根据条件批量查询对象
      CursorFullObjectResult cursorFullObjectResult = conn.search(condition, Sort.CREATE_TIME_DESC,
          1000, 0);
      int cursor = cursorFullObjectResult.getCursor();
      String datanodeMsg = cursorFullObjectResult.getDatanodeMsg();
      for (FullObject fo : cursorFullObjectResult.getObjects()) {
        // 获取对象ByteBuffer结果。注意：使用DirectByteBuffer模式时，不要保留此对象，处理完本次查询结果后再调用其他接口
        ByteBuffer contentByteBuffer = fo.getContent();
        // 获取对象byte[]结果
        byte[] contentByteArray = fo.getContentByteArray();

        // do something ...
      }
      // 使用返回的cursor继续查询1000条之后的结果
      CursorFullObjectResult continueResult = conn.search(condition, Sort.CREATE_TIME_DESC, 1000,
          cursor);
    }

    {
      // 根据条件批量查询对象元数据
      CursorMetadataResult cursorMetadataResult = conn.searchMetadata(condition,
          Sort.CREATE_TIME_DESC, 1000, 0);
      int cursor = cursorMetadataResult.getCursor();
      String datanodeMsg = cursorMetadataResult.getDatanodeMsg();
      for (Metadata meta : cursorMetadataResult.getMetadatas()) {
        // do something ...
      }
      // 使用返回的cursor继续查询1000条之后的结果
      CursorMetadataResult continueResult = conn.searchMetadata(condition, Sort.CREATE_TIME_DESC,
          1000, cursor);
    }

    {
      // 手动创建订阅任务，并消费
      long taskId = conn.createSubscribeTask(condition, SubscribeType.SUB_TYPE_META, 0);
      // long taskId = conn.createSubscribeTask(condition, SubscribeType.SUB_TYPE_OBJECT, 0);

      // 消费任务
      int hasNext = 0; // 0:任务未结束，仍可消费；1：已消费完成，没有可消费内容
      while (hasNext == 0) {
        // 根据创建的任务类型，选择消费对象或元数据
        SubscribeMetadataResult metadataResult = conn.consumerMetadata(taskId);
        // SubscribeObjectResult objectResult = conn.consumerObject(taskId);

        // 获取消费结果
        Metadata[] metadatas = metadataResult.getMetadatas();
        // 订阅任务是否还可继续消费
        hasNext = metadataResult.getHasNext();
      }

      // 销毁任务
      conn.destorySubscribeTask();
    }

    {
      // 创建自动订阅任务(订阅对象全部信息)
      conn.subscribe(condition, 0, new ObjectListener() {

        @Override
        public void consume(FullObject[] objects, ConsumeContext context) {

          for (FullObject item : objects) {
            // do something ...
          }

          // 获取本次消费数量
          context.getCurrentConsumerSize();
          // 订阅任务是否还可继续消费
          context.isHasNext();
        }
      }).start();
    }

    {
      // 创建自动订阅任务(订阅对象元数据)
      conn.subscribeMetadata(condition, 0, new MetadataListener() {

        @Override
        public void consume(Metadata[] metadatas, ConsumeContext context) {

          for (Metadata item : metadatas) {
            // do something ...
          }

          // 获取本次消费数量
          context.getCurrentConsumerSize();
          // 订阅任务是否还可继续消费
          context.isHasNext();
        }
      }).start();
    }

    // 根据查询条件，查询符合条件的统计数据
    AnalysResult analysResult = conn.searchAnalys(condition);
    String analysMsg = analysResult.getAnalysMsg();

    // 根据查询条件，将查询结果对象写入指定目录
    conn.searchAndWriteDisk(condition, "/tmp/iosp_out", 1000);

    // 断开连接
    conn.close();
  }
}
