package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class ExternalReceiverCreationVO {

  @NotEmpty(message = "外发服务器名称不能为空")
  private String name;
  private String receiverContent;
  private String receiverType;


  @Override
  public String toString() {
    return "ExternalReceiverQueryVO{" + "name='" + name + '\'' + ", receiverContent='"
        + receiverContent + '\'' + ", receiverType='" + receiverType + '\'' + '}';
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
}
