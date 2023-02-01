package com.machloop.iosp.sdk;

import java.io.IOException;

import com.machloop.iosp.sdk.subscribe.SubscribeMetadataResult;
import com.machloop.iosp.sdk.subscribe.SubscribeObjectResult;

public class Core {

  static {
    System.loadLibrary("sdk_jni");
  }

  public static native int globalInit(String pcLogPath);

  public static native long syncInit(char mode, String ip, short port, String clientId,
      String clientToken) throws IOException;

  public static native CursorFullObjectResult search(long pLake, SearchCondition searchCondition,
      char sortType, int searchNum, int cursor, boolean useDirectByteBuffer) throws IOException;

  public static native CursorMetadataResult searchMetadata(long pLake,
      SearchCondition searchCondition, char sortType, int searchNum, int cursor) throws IOException;

  public static native void searchAndWriteDisk(long pLake, SearchCondition searchCondition,
      String savePath, int dirFileNum);

  public static native String[] searchAnalys(long pLake, SearchCondition searchCondition)
      throws IOException;

  public static native FullObject read(long pLake, String objectId, boolean useDirectByteBuffer)
      throws IOException;

  public static native Metadata readMetadata(long pLake, String objectId) throws IOException;

  public static native String write(long pLake, WriteFullObject writeFullObject) throws IOException;

  public static native void modifyLabel(long pLake, String objectId, String label)
      throws IOException;

  public static native long createSubscribeTask(long pLake, SearchCondition searchCondition,
      char type, long taskId) throws IOException;

  public static native SubscribeObjectResult consumeSubscribeObject(long pLake, long taskId,
      boolean useDirectByteBuffer) throws IOException;

  public static native SubscribeMetadataResult consumeSubscribeMetadata(long pLake, long taskId)
      throws IOException;

  public static native void destroySubscribe(long pLake) throws IOException;

  public static native void resultRelease(long pLake);

  public static native void destory(long pLake);

}
