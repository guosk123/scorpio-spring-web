package com.scorpio.metric;

public final class MetricConstants {

  public static final int METRIC_TASK_INTERVAL_SEC = 30;

  public static final String DEVICE_DISK_STATE_ONLINE = "0";
  public static final String DEVICE_DISK_STATE_HOTSPARE = "1";
  public static final String DEVICE_DISK_STATE_REBUILD = "2";
  public static final String DEVICE_DISK_STATE_UNCONFIGURED_GOOD = "3";
  public static final String DEVICE_DISK_STATE_UNCONFIGURED_BAD = "4";
  public static final String DEVICE_DISK_STATE_COPYBACK = "5";
  public static final String DEVICE_DISK_STATE_FAILED = "6";
  public static final String DEVICE_DISK_STATE_ERROR = "7";
  
  public static final String DEVICE_DISK_FOREIGN_STATE_NONE = "0";
  public static final String DEVICE_DISK_FOREIGN_STATE_FOREIGN = "1";
  
  public static final String DEVICE_RAID_STATE_OPTIMAL = "0";
  public static final String DEVICE_RAID_STATE_PARTIALLY_DEGRADED = "1";
  public static final String DEVICE_RAID_STATE_DEGRADED = "2";
  public static final String DEVICE_RAID_STATE_OFFLINE = "3";
  public static final String DEVICE_RAID_STATE_FAULT = "4";

  public static final String DEVICE_DISK_MEDIUM_HDD = "0";
  public static final String DEVICE_DISK_MEDIUM_SSD = "1";
  public static final String DEVICE_DISK_MEDIUM_UNKNOWN = "2";

  private MetricConstants() {
    throw new IllegalStateException("Utility class");
  }
}
