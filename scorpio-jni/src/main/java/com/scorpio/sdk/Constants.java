package com.machloop.iosp.sdk;

public final class Constants {

  static final int ZONE_LENGTH = (32 + 1);
  static final int SITES_LENGTH = (33 * 128);
  static final int LABEL_LENGTH = (16 * 33);
  static final int NAME_LENGTH = 128;

  static final int MAX_SYNC_SEARCH_NUM = 1000;
  static final int MIN_SYNC_SEARCH_NUM = -1000;

  public enum Sort {

    UNSORT((char) 0), CREATE_TIME_ASC((char) 1), CREATE_TIME_DESC((char) 2);

    private char value;

    Sort(char value) {
      this.value = value;
    }

    public char value() {
      return this.value;
    }
  }

  public enum SubscribeType {

    SUB_TYPE_META((char) 0), // 只订阅元数据
    SUB_TYPE_OBJECT((char) 1); // 订阅元数据+data

    private char value;

    SubscribeType(char value) {
      this.value = value;
    }

    public char value() {
      return this.value;
    }
  }

}
