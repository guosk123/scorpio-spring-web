package com.machloop.fpc.manager.system.data;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
public class MetricDiskIODO {

  private Date timestamp;
  private String partitionName;
  private long readByteps;
  private long readBytepsPeak;
  private long writeByteps;
  private long writeBytepsPeak;

  @Override
  public String toString() {
    return "MetricDiskIODO [timestamp=" + timestamp + ", partitionName=" + partitionName
        + ", readByteps=" + readByteps + ", readBytepsPeak=" + readBytepsPeak + ", writeByteps="
        + writeByteps + ", writeBytepsPeak=" + writeBytepsPeak + "]";
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getPartitionName() {
    return partitionName;
  }

  public void setPartitionName(String partitionName) {
    this.partitionName = partitionName;
  }

  public long getReadByteps() {
    return readByteps;
  }

  public void setReadByteps(long readByteps) {
    this.readByteps = readByteps;
  }

  public long getReadBytepsPeak() {
    return readBytepsPeak;
  }

  public void setReadBytepsPeak(long readBytepsPeak) {
    this.readBytepsPeak = readBytepsPeak;
  }

  public long getWriteByteps() {
    return writeByteps;
  }

  public void setWriteByteps(long writeByteps) {
    this.writeByteps = writeByteps;
  }

  public long getWriteBytepsPeak() {
    return writeBytepsPeak;
  }

  public void setWriteBytepsPeak(long writeBytepsPeak) {
    this.writeBytepsPeak = writeBytepsPeak;
  }

}
