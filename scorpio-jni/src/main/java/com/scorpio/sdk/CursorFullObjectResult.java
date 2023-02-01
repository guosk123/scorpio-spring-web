package com.machloop.iosp.sdk;

import java.util.Arrays;

/**
 * @author mazhiyuan
 *
 * create at 2020年12月10日, sdk-jni
 */
public class CursorFullObjectResult {

  private FullObject[] objects;
  private String datanodeMsg;
  private int cursor;

  @Override
  public String toString() {
    return "CursorFullObjectResult [objects=" + Arrays.toString(objects) + ", msg=" + datanodeMsg
        + ", cursor=" + cursor + "]";
  }

  public FullObject[] getObjects() {
    return objects;
  }

  public String getDatanodeMsg() {
    return datanodeMsg;
  }

  public int getCursor() {
    return cursor;
  }
}
