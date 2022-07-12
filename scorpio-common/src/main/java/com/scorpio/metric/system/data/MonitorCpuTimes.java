package com.scorpio.metric.system.data;

public class MonitorCpuTimes {

  private final long userMillis;
  private final long systemMillis;
  private final long idleMillis;

  public MonitorCpuTimes(long userMillis, long systemMillis, long idleMillis) {
    this.userMillis = userMillis;
    this.systemMillis = systemMillis;
    this.idleMillis = idleMillis;
  }

  public String getCpuUsagePct(MonitorCpuTimes previous) {
    double cpuUsage = getCpuUsage(previous);
    return Math.round(cpuUsage * 100) + "%";
  }

  public long getCpuUsagePctLong(MonitorCpuTimes previous) {
    double cpuUsage = getCpuUsage(previous);
    return Math.round(cpuUsage * 100);
  }

  /**
   * Gets the CPU usage given a previous snapshot of CPU times.
   *
   * @param previous a CpuTimes snapshot taken previously.
   * @return the proportion of time between the previous snapshot and this snapshot
   * that the CPUs have spent working. 1 represents 100% usage, 0 represents 0% usage.
   */
  public double getCpuUsage(MonitorCpuTimes previous) {
    if (getTotalMillis() == previous.getTotalMillis()) {
      return 0F;
    }
    if (getIdleMillis() == previous.getIdleMillis()) {
      return 1F;
    }
    return 1 - ((double) (getIdleMillis() - previous.getIdleMillis()))
        / (double) (getTotalMillis() - previous.getTotalMillis());
  }

  @Override
  public String toString() {
    return "CpuTimes [userMillis=" + userMillis + ", systemMillis=" + systemMillis + ", idleMillis="
        + idleMillis + "]";
  }

  public long getTotalMillis() {
    return userMillis + systemMillis + idleMillis;
  }

  public long getUserMillis() {
    return userMillis;
  }

  public long getSystemMillis() {
    return systemMillis;
  }

  public long getIdleMillis() {
    return idleMillis;
  }

}
