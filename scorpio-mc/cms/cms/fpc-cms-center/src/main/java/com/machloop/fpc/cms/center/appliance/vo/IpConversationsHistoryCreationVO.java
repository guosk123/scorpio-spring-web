package com.machloop.fpc.cms.center.appliance.vo;

import org.hibernate.validator.constraints.Length;

/**
 * @author ChenXiao
 * create at 2022/10/11
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
