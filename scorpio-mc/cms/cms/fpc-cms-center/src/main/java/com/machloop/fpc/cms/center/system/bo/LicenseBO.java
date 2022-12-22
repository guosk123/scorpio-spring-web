package com.machloop.fpc.cms.center.system.bo;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

public class LicenseBO implements LogAudit {

  private List<LicenseItemBO> licenseItemList;

  private String collectTime;

  private String signTime;

  private String expiryTime;

  private String licenseType;

  private int version;

  private String fileName;

  private String localSerialNo;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (CollectionUtils.isEmpty(licenseItemList)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("导入授权文件：");
        break;
      default:
        return "";
    }

    builder.append("授权文件名称=").append(fileName).append(";");
    builder.append("截止日期=").append(expiryTime).append(";");
    builder.append("类型=").append(licenseType).append(";");
    builder.append("版本号=").append(version).append(";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "LicenseBO [licenseItemList=" + licenseItemList + ", collectTime=" + collectTime
        + ", signTime=" + signTime + ", expiryTime=" + expiryTime + ", licenseType=" + licenseType
        + ", version=" + version + ", fileName=" + fileName + ", localSerialNo=" + localSerialNo
        + ", toString()=" + super.toString() + "]";
  }

  public List<LicenseItemBO> getLicenseItemList() {
    return licenseItemList;
  }

  public void setLicenseItemList(List<LicenseItemBO> licenseItemList) {
    this.licenseItemList = licenseItemList;
  }

  public String getCollectTime() {
    return collectTime;
  }

  public void setCollectTime(String collectTime) {
    this.collectTime = collectTime;
  }

  public String getSignTime() {
    return signTime;
  }

  public void setSignTime(String signTime) {
    this.signTime = signTime;
  }

  public String getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(String expiryTime) {
    this.expiryTime = expiryTime;
  }

  public String getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(String licenseType) {
    this.licenseType = licenseType;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getLocalSerialNo() {
    return localSerialNo;
  }

  public void setLocalSerialNo(String localSerialNo) {
    this.localSerialNo = localSerialNo;
  }
}
