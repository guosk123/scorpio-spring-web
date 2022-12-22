package com.machloop.fpc.manager.appliance.bo;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class StorageSpaceBO {

  private String id;
  private String spaceType;
  private long capacity;
  private String updateTime;

  @Override
  public String toString() {
    return "StorageSpaceBO [id=" + id + ", spaceType=" + spaceType + ", capacity=" + capacity
        + ", updateTime=" + updateTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSpaceType() {
    return spaceType;
  }

  public void setSpaceType(String spaceType) {
    this.spaceType = spaceType;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

}
