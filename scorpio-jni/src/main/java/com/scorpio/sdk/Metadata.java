package com.machloop.iosp.sdk;

import java.util.Date;

public class Metadata {

  private int createTime;
  private int ingestTime;

  private int objectSize;
  private String objectId;
  private String objectName;

  private String zone;
  private String site;
  private String label;

  @Override
  public String toString() {
    return "Metadata [createTime=" + createTime + ", ingestTime=" + ingestTime + ", objectSize="
        + objectSize + ", objectId=" + objectId + ", objectName=" + objectName + ", zone=" + zone
        + ", site=" + site + ", label=" + label + "]";
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

}
