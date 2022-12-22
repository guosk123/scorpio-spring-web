package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class ExternalReceiverDO extends BaseOperateDO {


  private String id;

  private String name;
  private String receiverContent;
  private String receiverType;
  private String assignId;

  @Override
  public String toString() {
    return "ExternalReceiverDO{" + "id='" + id + '\'' + ", name='" + name + '\''
        + ", receiverContent='" + receiverContent + '\'' + ", receiverType='" + receiverType + '\''
        + ", assignId='" + assignId + '\'' + '}';
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReceiverContent() {
    return receiverContent;
  }

  public void setReceiverContent(String receiverContent) {
    this.receiverContent = receiverContent;
  }

  public String getReceiverType() {
    return receiverType;
  }

  public void setReceiverType(String receiverType) {
    this.receiverType = receiverType;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }
}
