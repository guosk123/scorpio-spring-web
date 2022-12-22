package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author chenxiao
 * create at 2022/7/11
 */
public class IpConversationsHistoryDO extends BaseOperateDO {

  private String name;

  private String data;

  @Override
  public String toString() {
    return "IpConversationsHistoryDO{" + "name='" + name + '\'' + ", data='" + data + '\'' + '}';
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
