package com.machloop.iosp.sdk.subscribe;

public class ConsumeContext {

  private int currentConsumerSize;
  private boolean hasNext;

  @Override
  public String toString() {
    return "ConsumeContext [currentConsumerSize=" + currentConsumerSize + ", hasNext=" + hasNext
        + "]";
  }

  public int getCurrentConsumerSize() {
    return currentConsumerSize;
  }

  public void setCurrentConsumerSize(int currentConsumerSize) {
    this.currentConsumerSize = currentConsumerSize;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

}
