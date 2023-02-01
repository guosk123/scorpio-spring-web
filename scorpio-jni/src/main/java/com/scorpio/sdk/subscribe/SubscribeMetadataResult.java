package com.machloop.iosp.sdk.subscribe;

import java.util.Arrays;

import com.machloop.iosp.sdk.Metadata;

public class SubscribeMetadataResult {

  private Metadata[] metadatas;
  private int hasNext;

  @Override
  public String toString() {
    return "SubscribeMetadataResult [metadatas=" + Arrays.toString(metadatas) + ", hasNext="
        + hasNext + "]";
  }

  public Metadata[] getMetadatas() {
    return metadatas;
  }

  public int getHasNext() {
    return hasNext;
  }

}
