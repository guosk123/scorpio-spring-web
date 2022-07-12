package com.scorpio.metric.system.data;

public class MonitorProcess {

  private String processName;
  private String cpuMetric;
  private String memoryMetric;

  private boolean leader;

  public MonitorProcess() {
  }

  public MonitorProcess(String processName) {
    super();
    this.processName = processName;
    this.cpuMetric = "0";
    this.memoryMetric = "0";
    this.leader = false;
  }

  @Override
  public String toString() {
    return "MonitorProcess [processName=" + processName + ", leader=" + leader + ", cpuMetric="
        + cpuMetric + ", memoryMetric=" + memoryMetric + "]";
  }

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public boolean isLeader() {
    return leader;
  }

  public void setLeader(boolean leader) {
    this.leader = leader;
  }

  public String getCpuMetric() {
    return cpuMetric;
  }

  public void setCpuMetric(String cpuMetric) {
    this.cpuMetric = cpuMetric;
  }

  public String getMemoryMetric() {
    return memoryMetric;
  }

  public void setMemoryMetric(String memoryMetric) {
    this.memoryMetric = memoryMetric;
  }

}
