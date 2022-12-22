package com.machloop.fpc.manager.appliance.vo;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
public class NatConfigModificationVO {

  private String natAction;

  @Override
  public String toString() {
    return "NatConfigModificationVO{" + "natAction='" + natAction + '\'' + '}';
  }

  public String getNatAction() {
    return natAction;
  }

  public void setNatAction(String natAction) {
    this.natAction = natAction;
  }
}
