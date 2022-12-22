package com.machloop.fpc.cms.center.central.bo;

public class CentralSystemBO {

  private String id;

  private String deviceType;
  private String monitoredSerialNumber;
  private int cpuMetric;
  private int memoryMetric;
  private int systemFsMetric;
  private int indexFsMetric;
  private int metadataFsMetric;
  private int metadataHotFsMetric;
  private int packetFsMetric;

  private long fsDataTotalByte;
  private int fsDataUsedPct;
  private long fsCacheTotalByte;
  private int fsCacheUsedPct;
  private long dataOldestTime;
  private long dataLast24TotalByte;
  private long dataPredictTotalDay;
  private long cacheFileAvgByte;
  private long fsStoreTotalByte;
  private long fsSystemTotalByte;
  private long fsIndexTotalByte;
  private long fsMetadataTotalByte;
  private long fsMetadataHotTotalByte;
  private long fsPacketTotalByte;

  private String metricTime;

  @Override
  public String toString() {
    return "CentralSystemBO [id=" + id + ", deviceType=" + deviceType + ", monitoredSerialNumber="
        + monitoredSerialNumber + ", cpuMetric=" + cpuMetric + ", memoryMetric=" + memoryMetric
        + ", systemFsMetric=" + systemFsMetric + ", indexFsMetric=" + indexFsMetric
        + ", metadataFsMetric=" + metadataFsMetric + ", metadataHotFsMetric=" + metadataHotFsMetric
        + ", packetFsMetric=" + packetFsMetric + ", fsDataTotalByte=" + fsDataTotalByte
        + ", fsDataUsedPct=" + fsDataUsedPct + ", fsCacheTotalByte=" + fsCacheTotalByte
        + ", fsCacheUsedPct=" + fsCacheUsedPct + ", dataOldestTime=" + dataOldestTime
        + ", dataLast24TotalByte=" + dataLast24TotalByte + ", dataPredictTotalDay="
        + dataPredictTotalDay + ", cacheFileAvgByte=" + cacheFileAvgByte + ", fsStoreTotalByte="
        + fsStoreTotalByte + ", fsSystemTotalByte=" + fsSystemTotalByte + ", fsIndexTotalByte="
        + fsIndexTotalByte + ", fsMetadataTotalByte=" + fsMetadataTotalByte
        + ", fsMetadataHotTotalByte=" + fsMetadataHotTotalByte + ", fsPacketTotalByte="
        + fsPacketTotalByte + ", metricTime=" + metricTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public int getCpuMetric() {
    return cpuMetric;
  }

  public void setCpuMetric(int cpuMetric) {
    this.cpuMetric = cpuMetric;
  }

  public int getMemoryMetric() {
    return memoryMetric;
  }

  public void setMemoryMetric(int memoryMetric) {
    this.memoryMetric = memoryMetric;
  }

  public int getSystemFsMetric() {
    return systemFsMetric;
  }

  public void setSystemFsMetric(int systemFsMetric) {
    this.systemFsMetric = systemFsMetric;
  }

  public int getIndexFsMetric() {
    return indexFsMetric;
  }

  public void setIndexFsMetric(int indexFsMetric) {
    this.indexFsMetric = indexFsMetric;
  }

  public int getMetadataFsMetric() {
    return metadataFsMetric;
  }

  public void setMetadataFsMetric(int metadataFsMetric) {
    this.metadataFsMetric = metadataFsMetric;
  }

  public int getMetadataHotFsMetric() {
    return metadataHotFsMetric;
  }

  public void setMetadataHotFsMetric(int metadataHotFsMetric) {
    this.metadataHotFsMetric = metadataHotFsMetric;
  }

  public int getPacketFsMetric() {
    return packetFsMetric;
  }

  public void setPacketFsMetric(int packetFsMetric) {
    this.packetFsMetric = packetFsMetric;
  }

  public long getFsDataTotalByte() {
    return fsDataTotalByte;
  }

  public void setFsDataTotalByte(long fsDataTotalByte) {
    this.fsDataTotalByte = fsDataTotalByte;
  }

  public int getFsDataUsedPct() {
    return fsDataUsedPct;
  }

  public void setFsDataUsedPct(int fsDataUsedPct) {
    this.fsDataUsedPct = fsDataUsedPct;
  }

  public long getFsCacheTotalByte() {
    return fsCacheTotalByte;
  }

  public void setFsCacheTotalByte(long fsCacheTotalByte) {
    this.fsCacheTotalByte = fsCacheTotalByte;
  }

  public int getFsCacheUsedPct() {
    return fsCacheUsedPct;
  }

  public void setFsCacheUsedPct(int fsCacheUsedPct) {
    this.fsCacheUsedPct = fsCacheUsedPct;
  }

  public long getDataOldestTime() {
    return dataOldestTime;
  }

  public void setDataOldestTime(long dataOldestTime) {
    this.dataOldestTime = dataOldestTime;
  }

  public long getDataLast24TotalByte() {
    return dataLast24TotalByte;
  }

  public void setDataLast24TotalByte(long dataLast24TotalByte) {
    this.dataLast24TotalByte = dataLast24TotalByte;
  }

  public long getDataPredictTotalDay() {
    return dataPredictTotalDay;
  }

  public void setDataPredictTotalDay(long dataPredictTotalDay) {
    this.dataPredictTotalDay = dataPredictTotalDay;
  }

  public long getCacheFileAvgByte() {
    return cacheFileAvgByte;
  }

  public void setCacheFileAvgByte(long cacheFileAvgByte) {
    this.cacheFileAvgByte = cacheFileAvgByte;
  }

  public long getFsStoreTotalByte() {
    return fsStoreTotalByte;
  }

  public void setFsStoreTotalByte(long fsStoreTotalByte) {
    this.fsStoreTotalByte = fsStoreTotalByte;
  }

  public long getFsSystemTotalByte() {
    return fsSystemTotalByte;
  }

  public void setFsSystemTotalByte(long fsSystemTotalByte) {
    this.fsSystemTotalByte = fsSystemTotalByte;
  }

  public long getFsIndexTotalByte() {
    return fsIndexTotalByte;
  }

  public void setFsIndexTotalByte(long fsIndexTotalByte) {
    this.fsIndexTotalByte = fsIndexTotalByte;
  }

  public long getFsMetadataTotalByte() {
    return fsMetadataTotalByte;
  }

  public void setFsMetadataTotalByte(long fsMetadataTotalByte) {
    this.fsMetadataTotalByte = fsMetadataTotalByte;
  }

  public long getFsMetadataHotTotalByte() {
    return fsMetadataHotTotalByte;
  }

  public void setFsMetadataHotTotalByte(long fsMetadataHotTotalByte) {
    this.fsMetadataHotTotalByte = fsMetadataHotTotalByte;
  }

  public long getFsPacketTotalByte() {
    return fsPacketTotalByte;
  }

  public void setFsPacketTotalByte(long fsPacketTotalByte) {
    this.fsPacketTotalByte = fsPacketTotalByte;
  }

  public String getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(String metricTime) {
    this.metricTime = metricTime;
  }

}
