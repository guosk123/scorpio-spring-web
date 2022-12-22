package com.machloop.fpc.cms.center.appliance.vo;

/**
 * @author ChenXiao
 * create at 2022/10/11
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
