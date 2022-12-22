package com.machloop.fpc.manager.appliance.vo;

import org.hibernate.validator.constraints.Length;

/**
 * @author chenxiao
 * create at 2022/7/11
 */
public class IpConversationsHistoryCreationVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;

  private String data;

  @Override
  public String toString() {
    return "IpConversationsHistoryCreationVO{" + "name='" + name + '\'' + ", data='" + data + '\''
        + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
