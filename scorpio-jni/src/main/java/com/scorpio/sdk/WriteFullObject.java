package com.machloop.iosp.sdk;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class WriteFullObject {

  private String objectName;
  private String zone;

  private int createTime;

  private String site;
  private String label;

  private ByteBuffer content;

  private WriteFullObject(Builder builder) {
    this.objectName = builder.objectName;
    this.zone = builder.zone;
    this.createTime = builder.createTime;
    this.site = builder.site;
    this.label = builder.label;
    this.content = builder.content;
  }

  @Override
  public String toString() {
    return "WriteFullObject [objectName=" + objectName + ", zone=" + zone + ", createTime="
        + createTime + ", site=" + site + ", label=" + label + ", content=" + content + "]";
  }

  public String getObjectName() {
    return objectName;
  }

  public String getZone() {
    return zone;
  }

  public int getCreateTime() {
    return createTime;
  }

  public Date getCreateTimeDate() {
    return new Date((long) createTime * 1000);
  }

  public String getSite() {
    return site;
  }

  public String getLabel() {
    return label;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public static Builder create(String zone, String objectName, ByteBuffer content) {
    // 传入的ByteBuffer不是DirectByteBuffer时需要进行拷贝
    if (!content.isDirect()) {
      ByteBuffer buffer = ByteBuffer.allocateDirect(content.capacity());
      buffer.put(content);
      content = buffer;
    }
    return new Builder(zone, objectName, content);
  }

  public static Builder create(String zone, String objectName, byte[] content) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
    buffer.put(content);
    return new Builder(zone, objectName, buffer);
  }

  public static class Builder {
    private final String zone;
    private final String objectName;
    private final ByteBuffer content;

    private int createTime;

    private String site;
    private String label;

    private Builder(String zone, String objectName, ByteBuffer content) {
      if (zone == null || "".equals(zone)) {
        throw new IllegalArgumentException("zone must not be null.");
      }
      if (zone.getBytes(StandardCharsets.UTF_8).length >= Constants.ZONE_LENGTH) {
        throw new IllegalArgumentException("zone is too long.");
      }
      if (zone == null || "".equals(objectName)) {
        throw new IllegalArgumentException("object name must not be null.");
      }
      if (objectName.getBytes(StandardCharsets.UTF_8).length >= Constants.NAME_LENGTH) {
        throw new IllegalArgumentException("object name is too long.");
      }
      if (content == null || content.capacity() <= 0) {
        throw new IllegalArgumentException("object content must not empty.");
      }

      this.zone = zone;
      this.objectName = objectName;
      this.content = content;

      this.createTime = (int) (new Date().getTime() / 1000);
    }

    public Builder setCreateTime(Date createTime) {
      if (createTime == null) {
        throw new IllegalArgumentException("create time must not be null.");
      }
      this.createTime = (int) (createTime.getTime() / 1000);
      return this;
    }

    public Builder setSite(String site) {
      if (site != null && site.getBytes(StandardCharsets.UTF_8).length >= Constants.SITES_LENGTH) {
        throw new IllegalArgumentException("site is too long.");
      }
      this.site = site;
      return this;
    }

    public Builder setLabel(String label) {
      if (label != null
          && label.getBytes(StandardCharsets.UTF_8).length >= Constants.LABEL_LENGTH) {
        throw new IllegalArgumentException("label is too long.");
      }
      this.label = label;
      return this;
    }

    public WriteFullObject build() {
      return new WriteFullObject(this);
    }
  }
}
