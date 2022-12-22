package com.machloop.fpc.manager.appliance.vo;

import org.hibernate.validator.constraints.Length;

/**
 * @author chenshimiao
 *
 * create at 2022/8/8 6:38 PM,cms
 * @version 1.0
 */
public class FilterRuleCreationVO {

  @Length(min = 1, max = 30, message = "存储过滤规则名称不能为空，最多可输入30个字符")
  private String name;

  private String tuple;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  private String networkId;

  @Override
  public String toString() {
    return "FilterRuleCreationVO [name=" + name + ", tuple=" + tuple + ", description="
        + description + ", networkId" + networkId + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTuple() {
    return tuple;
  }

  public void setTuple(String tuple) {
    this.tuple = tuple;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }
}
