package com.machloop.fpc.cms.center.sensor.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorNetworkGroupDO extends BaseOperateDO {

  private String id;
  private String name;
  private String networkInSensorIds;
  private String description;

  @Override
  public String toString() {
    return "SensorNetworkGroupDO [id=" + id + ", name=" + name + ", networkInSensorIds="
        + networkInSensorIds + ", description=" + description + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkInSensorIds() {
    return networkInSensorIds;
  }

  public void setNetworkInSensorIds(String networkInSensorIds) {
    this.networkInSensorIds = networkInSensorIds;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
