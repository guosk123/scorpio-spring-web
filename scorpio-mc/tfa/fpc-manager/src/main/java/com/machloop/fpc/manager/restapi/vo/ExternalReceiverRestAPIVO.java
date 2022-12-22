package com.machloop.fpc.manager.restapi.vo;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/10/28
 */
public class ExternalReceiverRestAPIVO {

  @NotEmpty(message = "外发服务器名称不能为空")
  private String name;
  private Map<String, Object> receiverContent;
  private String receiverType;


  @Override
  public String toString() {
    return "ExternalReceiverRestAPIVO{" + "name='" + name + '\'' + ", receiverContent="
        + receiverContent + ", receiverType='" + receiverType + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getReceiverContent() {
    return receiverContent;
  }

  public void setReceiverContent(Map<String, Object> receiverContent) {
    this.receiverContent = receiverContent;
  }

  public String getReceiverType() {
    return receiverType;
  }

  public void setReceiverType(String receiverType) {
    this.receiverType = receiverType;
  }
}
