package com.machloop.fpc.manager.appliance.data;

import java.util.Date;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
public class NatConfigDO {

  private String id;

  private String natAction;

  private Date updateTime;

  private String operatorId;

  @Override
  public String toString() {
    return "NatConfigDO{" + "id='" + id + '\'' + ", natAction='" + natAction + '\''
        + ", updateTime=" + updateTime + ", operatorId='" + operatorId + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNatAction() {
    return natAction;
  }

  public void setNatAction(String natAction) {
    this.natAction = natAction;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
