package com.machloop.iosp.sdk.subscribe;

import java.util.Arrays;

import com.machloop.iosp.sdk.FullObject;

public class SubscribeObjectResult {

  private FullObject[] objects;
  private int hasNext;

  @Override
  public String toString() {
    return "SubscribeObjectResult [objects=" + Arrays.toString(objects) + ", hasNext=" + hasNext
        + "]";
  }

  public FullObject[] getObjects() {
    return objects;
  }

  public int getHasNext() {
    return hasNext;
  }

}
