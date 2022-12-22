package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/10/11
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
