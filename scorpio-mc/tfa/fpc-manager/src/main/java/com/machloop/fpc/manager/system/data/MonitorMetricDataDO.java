package com.machloop.fpc.manager.system.data;

import java.util.Date;

/**
 * @author liyongjun
 *
 * create at 2019年9月16日, fpc-manager
 */
public class MonitorMetricDataDO {

  private Date timestamp;
  private int cpuUsedRatio;
  private int memoryUsedRatio;
  private int systemFsUsedRatio;
  private long systemFsFree;
  private int indexFsUsedRatio;
  private long indexFsFree;
  private int metadataFsUsedRatio;
  private long metadataFsFree;
  private int metadataHotFsUsedRatio;
  private long metadataHotFsFree;
  private int packetFsUsedRatio;
  private long packetFsFree;

  @Override
  public String toString() {
    return "MonitorMetricDataDO [timestamp=" + timestamp + ", cpuUsedRatio=" + cpuUsedRatio
        + ", memoryUsedRatio=" + memoryUsedRatio + ", systemFsUsedRatio=" + systemFsUsedRatio
        + ", systemFsFree=" + systemFsFree + ", indexFsUsedRatio=" + indexFsUsedRatio
        + ", indexFsFree=" + indexFsFree + ", metadataFsUsedRatio=" + metadataFsUsedRatio
        + ", metadataFsFree=" + metadataFsFree + ", metadataHotFsUsedRatio="
        + metadataHotFsUsedRatio + ", metadataHotFsFree=" + metadataHotFsFree
        + ", packetFsUsedRatio=" + packetFsUsedRatio + ", packetFsFree=" + packetFsFree + "]";
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public int getCpuUsedRatio() {
    return cpuUsedRatio;
  }

  public void setCpuUsedRatio(int cpuUsedRatio) {
    this.cpuUsedRatio = cpuUsedRatio;
  }

  public int getMemoryUsedRatio() {
    return memoryUsedRatio;
  }

  public void setMemoryUsedRatio(int memoryUsedRatio) {
    this.memoryUsedRatio = memoryUsedRatio;
  }

  public int getSystemFsUsedRatio() {
    return systemFsUsedRatio;
  }

  public void setSystemFsUsedRatio(int systemFsUsedRatio) {
    this.systemFsUsedRatio = systemFsUsedRatio;
  }

  public long getSystemFsFree() {
    return systemFsFree;
  }

  public void setSystemFsFree(long systemFsFree) {
    this.systemFsFree = systemFsFree;
  }

  public int getIndexFsUsedRatio() {
    return indexFsUsedRatio;
  }

  public void setIndexFsUsedRatio(int indexFsUsedRatio) {
    this.indexFsUsedRatio = indexFsUsedRatio;
  }

  public long getIndexFsFree() {
    return indexFsFree;
  }

  public void setIndexFsFree(long indexFsFree) {
    this.indexFsFree = indexFsFree;
  }

  public int getMetadataFsUsedRatio() {
    return metadataFsUsedRatio;
  }

  public void setMetadataFsUsedRatio(int metadataFsUsedRatio) {
    this.metadataFsUsedRatio = metadataFsUsedRatio;
  }

  public long getMetadataFsFree() {
    return metadataFsFree;
  }

  public void setMetadataFsFree(long metadataFsFree) {
    this.metadataFsFree = metadataFsFree;
  }

  public int getMetadataHotFsUsedRatio() {
    return metadataHotFsUsedRatio;
  }

  public void setMetadataHotFsUsedRatio(int metadataHotFsUsedRatio) {
    this.metadataHotFsUsedRatio = metadataHotFsUsedRatio;
  }

  public long getMetadataHotFsFree() {
    return metadataHotFsFree;
  }

  public void setMetadataHotFsFree(long metadataHotFsFree) {
    this.metadataHotFsFree = metadataHotFsFree;
  }

  public int getPacketFsUsedRatio() {
    return packetFsUsedRatio;
  }

  public void setPacketFsUsedRatio(int packetFsUsedRatio) {
    this.packetFsUsedRatio = packetFsUsedRatio;
  }

  public long getPacketFsFree() {
    return packetFsFree;
  }

  public void setPacketFsFree(long packetFsFree) {
    this.packetFsFree = packetFsFree;
  }

}
