package com.machloop.fpc.npm.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2020年11月10日, fpc-manager
 */
public class NetworkDO extends BaseOperateDO {

  private String name;
  private String netifType;
  private String extraSettings;

  private String reportState;
  private String reportAction;

  @Override
  public String toString() {
    return "NetworkDO [name=" + name + ", netifType=" + netifType + ", extraSettings="
        + extraSettings + ", reportState=" + reportState + ", reportAction=" + reportAction + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetifType() {
    return netifType;
  }

  public void setNetifType(String netifType) {
    this.netifType = netifType;
  }

  public String getExtraSettings() {
    return extraSettings;
  }

  public void setExtraSettings(String extraSettings) {
    this.extraSettings = extraSettings;
  }

  public String getReportState() {
    return reportState;
  }

  public void setReportState(String reportState) {
    this.reportState = reportState;
  }

  public String getReportAction() {
    return reportAction;
  }

  public void setReportAction(String reportAction) {
    this.reportAction = reportAction;
  }

}
