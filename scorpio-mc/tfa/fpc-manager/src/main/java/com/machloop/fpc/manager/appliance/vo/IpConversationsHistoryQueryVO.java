package com.machloop.fpc.manager.appliance.vo;

/**
 * @author chenxiao
 * create at 2022/7/11
 */
public class IpConversationsHistoryQueryVO {

  private String name;


  @Override
  public String toString() {
    return "IpConversationsHistoryQueryVO{" + "name='" + name + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
