package com.machloop.fpc.cms.center.central.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年12月7日, fpc-cms-center
 */
public class CentralDiskIODO extends BaseDO {

  private String deviceType;
  private String monitoredSerialNumber;
  private String partitionName;
  private long readByteps;
  private long readBytepsPeak;
  private long writeByteps;
  private long writeBytepsPeak;
  private Date metricTime;

  @Override
  public String toString() {
    return "CentralDiskIODO [deviceType=" + deviceType + ", monitoredSerialNumber="
        + monitoredSerialNumber + ", partitionName=" + partitionName + ", readByteps=" + readByteps
        + ", readBytepsPeak=" + readBytepsPeak + ", writeByteps=" + writeByteps
        + ", writeBytepsPeak=" + writeBytepsPeak + ", metricTime=" + metricTime + "]";
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getMonitoredSerialNumber() {
    return monitoredSerialNumber;
  }

  public void setMonitoredSerialNumber(String monitoredSerialNumber) {
    this.monitoredSerialNumber = monitoredSerialNumber;
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

  public Date getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(Date metricTime) {
    this.metricTime = metricTime;
  }

}
