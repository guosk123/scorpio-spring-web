package com.machloop.iosp.sdk;

import java.nio.ByteBuffer;
import java.util.Date;

public class FullObject {

  private int createTime;
  private int ingestTime;

  private int objectSize;
  private String objectId;
  private String objectName;

  private String zone;
  private String site;
  private String label;

  private ByteBuffer content;

  @Override
  public String toString() {
    return "FullObject [createTime=" + createTime + ", ingestTime=" + ingestTime + ", objectSize="
        + objectSize + ", objectId=" + objectId + ", objectName=" + objectName + ", zone=" + zone
        + ", site=" + site + ", label=" + label + ", content=" + content + "]";
  }

  public int getCreateTime() {
    return createTime;
  }

  public Date getCreateTimeDate() {
    return new Date((long) createTime * 1000);
  }

  public int getIngestTime() {
    return ingestTime;
  }

  public Date getIngestTimeDate() {
    return new Date((long) ingestTime * 1000);
  }

  public int getObjectSize() {
    return objectSize;
  }

  public String getObjectId() {
    return objectId;
  }

  public String getObjectName() {
    return objectName;
  }

  public String getZone() {
    return zone;
  }

  public String getSite() {
    return site;
  }

  public String getLabel() {
    return label;
  }

  /**
   * WARNING: 使用DirectByteBuffer模式时不要持有此ByteBuffer，调用释放接口后，再使用此ByteBuffer会获取未知结果
   * @return 返回对象内容ByteBuffer，
   */
  public ByteBuffer getContent() {
    return content == null ? null : content.asReadOnlyBuffer();
  }

  public byte[] getContentByteArray() {
    if (content == null) {
      return null;
    }
    content.clear();
    byte[] buffer = new byte[objectSize];
    content.get(buffer, 0, objectSize);
    content.clear();
    return buffer;
  }
}
