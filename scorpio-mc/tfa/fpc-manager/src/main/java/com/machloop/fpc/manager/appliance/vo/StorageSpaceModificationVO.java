package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class StorageSpaceModificationVO {

  @NotEmpty(message = "存储空间类型不能为空")
  private String spaceType;
  private long capacity;

  @Override
  public String toString() {
    return "StorageSpaceModificationVO [spaceType=" + spaceType + ", capacity=" + capacity + "]";
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

}
