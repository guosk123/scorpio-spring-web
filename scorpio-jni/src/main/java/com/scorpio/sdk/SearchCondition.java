package com.machloop.iosp.sdk;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author mazhiyuan
 *
 * create at 2020年12月3日, sdk-jni
 */
public class SearchCondition {

  private int startTime;
  private int endTime;
  private int timeout;
  private String zone;
  private String site;
  private String label;

  private SearchCondition(Builder builder) {
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.timeout = builder.timeout;
    this.zone = builder.zone;
    this.site = builder.site;
    this.label = builder.label;
  }

  @Override
  public String toString() {
    return "SearchCondition [startTime=" + startTime + ", endTime=" + endTime + ", timeout="
        + timeout + ", zone=" + zone + ", site=" + site + ", label=" + label + "]";
  }

  public int getStartTime() {
    return startTime;
  }

  public Date getStartTimeDate() {
    return new Date((long) startTime * 1000);
  }

  public int getEndTime() {
    return endTime;
  }

  public Date getEndTimeDate() {
    return new Date((long) endTime * 1000);
  }

  public int getTimeout() {
    return timeout;
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

  public static Builder create(String zone, Date startTime, Date endTime) {
    return new Builder(zone, startTime, endTime);
  }

  public static class Builder {

    private final String zone;
    private final int startTime;
    private final int endTime;

    private int timeout = 0;

    private String site;
    private String label;

    public Builder(String zone, Date startTime, Date endTime) {
      if (zone == null || "".equals(zone)) {
        throw new IllegalArgumentException("zone must not be null.");
      }
      if (startTime == null || endTime == null) {
        throw new IllegalArgumentException("start time or end time must not be null.");
      }
      if (zone.getBytes(StandardCharsets.UTF_8).length >= Constants.ZONE_LENGTH) {
        throw new IllegalArgumentException("zone is too long.");
      }
      this.zone = zone;
      this.startTime = (int) (startTime.getTime() / 1000);
      this.endTime = (int) (endTime.getTime() / 1000);
    }

    public Builder setTimeout(int timeout) {
      if (timeout < 0) {
        throw new IllegalArgumentException("timeout must be positive.");
      }
      this.timeout = timeout;
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

    public SearchCondition build() {
      return new SearchCondition(this);
    }
  }
}

