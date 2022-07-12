package com.scorpio.metric.system.data;

public class MonitorMemory {

  private final long free;
  private final long buffers;
  private final long cached;
  private final long slab;
  private final long total;

  public MonitorMemory(long free, long buffers, long cached, long slab, long total) {
    this.free = free;
    this.buffers = buffers;
    this.cached = cached;
    this.slab = slab;
    this.total = total;
  }

  public String getMemUsagePct() {
    return getMemUsagePctLong() + "%";
  }

  public long getMemUsagePctLong() {
    if (total == 0) {
      return 0;
    }
    // used = MemTotal - MemFree - Buffers - Cached - Slab
    // https://access.redhat.com/solutions/406773
    double usagePct = ((double) (total - free - buffers - cached - slab)) / ((double) total);
    return Math.round(usagePct * 100);
  }

  @Override
  public String toString() {
    return "MonitorMemory [free=" + free + ", buffers=" + buffers + ", cached=" + cached + ", slab="
        + slab + ", total=" + total + "]";
  }

  public long getFree() {
    return free;
  }

  public long getBuffers() {
    return buffers;
  }

  public long getCached() {
    return cached;
  }

  public long getSlab() {
    return slab;
  }

  public long getTotal() {
    return total;
  }
}
