package com.machloop.fpc.cms.center.broker.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public class FpcNetworkDO extends BaseOperateDO {

  private String fpcNetworkId;
  private String fpcNetworkName;
  private int bandwidth;
  private String fpcSerialNumber;
  private String reportState;
  private String reportAction;

  @Override
  public String toString() {
    return "FpcNetworkDO [fpcNetworkId=" + fpcNetworkId + ", fpcNetworkName=" + fpcNetworkName
        + ", bandwidth=" + bandwidth + ", fpcSerialNumber=" + fpcSerialNumber + ", reportState="
        + reportState + ", reportAction=" + reportAction + "]";
  }

  public String getFpcNetworkId() {
    return fpcNetworkId;
  }

  public void setFpcNetworkId(String fpcNetworkId) {
    this.fpcNetworkId = fpcNetworkId;
  }

  public String getFpcNetworkName() {
    return fpcNetworkName;
  }

  public void setFpcNetworkName(String fpcNetworkName) {
    this.fpcNetworkName = fpcNetworkName;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
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
