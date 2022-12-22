package com.machloop.fpc.cms.center.central.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年12月15日, fpc-cms-center
 */
public class CmsDO extends BaseOperateDO {

  private String name;
  private String ip;
  private String serialNumber;
  private String version;
  private String appKey;
  private String appToken;
  private String cmsToken;
  private String superiorCmsSerialNumber;
  private String description;

  private String reportState;
  private String reportAction;

  @Override
  public String toString() {
    return "CmsDO [name=" + name + ", ip=" + ip + ", serialNumber=" + serialNumber + ", version="
        + version + ", appKey=" + appKey + ", appToken=" + appToken + ", cmsToken=" + cmsToken
        + ", superiorCmsSerialNumber=" + superiorCmsSerialNumber + ", description=" + description
        + ", reportState=" + reportState + ", reportAction=" + reportAction + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getAppKey() {
    return appKey;
  }

  public void setAppKey(String appKey) {
    this.appKey = appKey;
  }

  public String getAppToken() {
    return appToken;
  }

  public void setAppToken(String appToken) {
    this.appToken = appToken;
  }

  public String getCmsToken() {
    return cmsToken;
  }

  public void setCmsToken(String cmsToken) {
    this.cmsToken = cmsToken;
  }

  public String getSuperiorCmsSerialNumber() {
    return superiorCmsSerialNumber;
  }

  public void setSuperiorCmsSerialNumber(String superiorCmsSerialNumber) {
    this.superiorCmsSerialNumber = superiorCmsSerialNumber;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
