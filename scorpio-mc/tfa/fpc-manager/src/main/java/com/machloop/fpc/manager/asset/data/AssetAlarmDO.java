package com.machloop.fpc.manager.asset.data;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月8日, fpc-manager
 */
public class AssetAlarmDO {

  private String id;
  private String ipAddress;
  private String type;
  private String baseline;
  private String current;
  private Date alarmTime;

  @Override
  public String toString() {
    return "AssetAlarmDO [id=" + id + ", ipAddress=" + ipAddress + ", type=" + type + ", baseline="
        + baseline + ", current=" + current + ", alarmTime=" + alarmTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBaseline() {
    return baseline;
  }

  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  public String getCurrent() {
    return current;
  }

  public void setCurrent(String current) {
    this.current = current;
  }

  public Date getAlarmTime() {
    return alarmTime;
  }

  public void setAlarmTime(Date alarmTime) {
    this.alarmTime = alarmTime;
  }

}
