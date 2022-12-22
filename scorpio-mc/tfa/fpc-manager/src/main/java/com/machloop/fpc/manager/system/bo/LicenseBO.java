package com.machloop.fpc.manager.system.bo;

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

  private long packetTimeLimit;

  private long packetCapacityLimit;

  private long recvSpeedLimit;

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
    builder.append("数据包最大存储时长（天）=").append(packetTimeLimit).append(";");
    builder.append("数据包最大存储空间（GB）=").append(packetCapacityLimit).append(";");
    builder.append("限速（Byte/s）=").append(recvSpeedLimit).append(";");
    builder.append("版本号=").append(version).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "LicenseBO [licenseItemList=" + licenseItemList + ", collectTime=" + collectTime
        + ", signTime=" + signTime + ", expiryTime=" + expiryTime + ", licenseType=" + licenseType
        + ", packetTimeLimit=" + packetTimeLimit + ", packetCapacityLimit=" + packetCapacityLimit
        + ", recvSpeedLimit=" + recvSpeedLimit + ", version=" + version + ", fileName=" + fileName
        + ", localSerialNo=" + localSerialNo + "]";
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

  public long getPacketTimeLimit() {
    return packetTimeLimit;
  }

  public void setPacketTimeLimit(long packetTimeLimit) {
    this.packetTimeLimit = packetTimeLimit;
  }

  public long getPacketCapacityLimit() {
    return packetCapacityLimit;
  }

  public void setPacketCapacityLimit(long packetCapacityLimit) {
    this.packetCapacityLimit = packetCapacityLimit;
  }

  public long getRecvSpeedLimit() {
    return recvSpeedLimit;
  }

  public void setRecvSpeedLimit(long recvSpeedLimit) {
    this.recvSpeedLimit = recvSpeedLimit;
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
