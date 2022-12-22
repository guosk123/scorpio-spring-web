package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
public class NetworkNetifDO extends BaseDO {

  private String id;
  private String networkId;
  private String netifName;
  private int specification;
  private String direction;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkNetifDO [id=" + id + ", networkId=" + networkId + ", netifName=" + netifName
        + ", specification=" + specification + ", direction=" + direction + ", timestamp="
        + timestamp + ", operatorId=" + operatorId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public int getSpecification() {
    return specification;
  }

  public void setSpecification(int specification) {
    this.specification = specification;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
