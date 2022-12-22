package com.machloop.fpc.cms.center.central.bo;

import java.util.List;

/**
 * @author guosk
 * @apiNote 当前CMS所管理的设备</br> 
 *
 * create at 2021年11月12日, fpc-cms-center
 */
public class CentralDeviceBO {

  private String deviceSerialNumber;
  private String deviceName;
  private String deviceType;
  private String owner;
  private String sensorType;// 探针类型
  private List<CentralDeviceBO> child;

  @Override
  public String toString() {
    return "CentralDeviceBO [deviceSerialNumber=" + deviceSerialNumber + ", deviceName="
        + deviceName + ", deviceType=" + deviceType + ", owner=" + owner + ", sensorType="
        + sensorType + ", child=" + child + "]";
  }

  public String getDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  public void setDeviceSerialNumber(String deviceSerialNumber) {
    this.deviceSerialNumber = deviceSerialNumber;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getSensorType() {
    return sensorType;
  }

  public void setSensorType(String sensorType) {
    this.sensorType = sensorType;
  }

  public List<CentralDeviceBO> getChild() {
    return child;
  }

  public void setChild(List<CentralDeviceBO> child) {
    this.child = child;
  }

}
