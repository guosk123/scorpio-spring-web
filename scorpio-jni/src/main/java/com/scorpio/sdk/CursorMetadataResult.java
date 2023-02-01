package com.machloop.iosp.sdk;

import java.util.Arrays;

public class CursorMetadataResult {

  private Metadata[] metadatas;
  private String datanodeMsg;
  private int cursor;

  @Override
  public String toString() {
    return "CursorMetadataResult [objects=" + Arrays.toString(metadatas) + ", datanodeMsg="
        + datanodeMsg + ", cursor=" + cursor + "]";
  }

  public Metadata[] getMetadatas() {
    return metadatas;
  }

  public String getDatanodeMsg() {
    return datanodeMsg;
  }

  public int getCursor() {
    return cursor;
  }

}
