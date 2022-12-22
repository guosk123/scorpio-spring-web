package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class ExternalReceiverQueryVO {


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
