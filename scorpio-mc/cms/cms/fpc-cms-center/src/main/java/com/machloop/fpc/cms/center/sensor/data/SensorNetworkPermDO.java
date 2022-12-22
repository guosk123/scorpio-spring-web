package com.machloop.fpc.cms.center.sensor.data;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
public class SensorNetworkPermDO {

  private String id;
  private String userId;
  private String networkId;
  private String networkGroupId;

  @Override
  public String toString() {
    return "SensorNetworkPermDO [id=" + id + ", userId=" + userId + ", networkId=" + networkId
        + ", networkGroupId=" + networkGroupId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }

}
