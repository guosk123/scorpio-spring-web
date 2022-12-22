package com.machloop.fpc.cms.center.central.bo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

public class FpcBO implements LogAudit {

  private String id;
  private String name;
  private String ip;
  private String serialNumber;
  private String version;
  private String type;
  private String licenseStatus;
  private String connectStatus;
  private String appKey;
  private String appToken;
  private String cmsToken;
  private String cmsSerialNumber;
  private String description;
  private String createTime;

  private String operatorId;

  private String cmsName = "本机";

  // 设备系统状态
  private long upTime;
  private int cpuMetric;
  private int memoryMetric;
  private int systemFsMetric;
  private int indexFsMetric;
  private int metadataFsMetric;
  private int metadataHotFsMetric;
  private int packetFsMetric;
  private int alarmCount;

  // 交互
  private String lastLoginTime;
  private String lastInteractiveTime;
  private long lastInteractiveLatency;

  private String licenseStatusText;
  private String connectStatusText;

  private List<CentralRaidBO> raidList;
  private List<CentralNetifBO> netifList;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加探针设备：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改探针设备信息：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除探针设备：");
        break;
      default:
        return "";
    }

    builder.append("设备名称=").append(name).append(";");
    builder.append("设备IP=").append(ip).append(";");
    builder.append("设备序列号=").append(serialNumber).append(";");
    builder.append("设备版本=").append(version).append(";");
    builder.append("连接状态=").append(connectStatusText).append(";");
    builder.append("管理用户appKey=").append(appKey).append(";");
    builder.append("管理用户appToken=").append(appToken).append(";");
    builder.append("cms分配给设备token=").append(cmsToken).append(";");
    builder.append("备注=").append(description).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "FpcBO [id=" + id + ", name=" + name + ", ip=" + ip + ", serialNumber=" + serialNumber
        + ", version=" + version + ", type=" + type + ", licenseStatus=" + licenseStatus
        + ", connectStatus=" + connectStatus + ", appKey=" + appKey + ", appToken=" + appToken
        + ", cmsToken=" + cmsToken + ", cmsSerialNumber=" + cmsSerialNumber + ", description="
        + description + ", createTime=" + createTime + ", operatorId=" + operatorId + ", cmsName="
        + cmsName + ", upTime=" + upTime + ", cpuMetric=" + cpuMetric + ", memoryMetric="
        + memoryMetric + ", systemFsMetric=" + systemFsMetric + ", indexFsMetric=" + indexFsMetric
        + ", metadataFsMetric=" + metadataFsMetric + ", metadataHotFsMetric=" + metadataHotFsMetric
        + ", packetFsMetric=" + packetFsMetric + ", alarmCount=" + alarmCount + ", lastLoginTime="
        + lastLoginTime + ", lastInteractiveTime=" + lastInteractiveTime
        + ", lastInteractiveLatency=" + lastInteractiveLatency + ", licenseStatusText="
        + licenseStatusText + ", connectStatusText=" + connectStatusText + ", raidList=" + raidList
        + ", netifList=" + netifList + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLicenseStatus() {
    return licenseStatus;
  }

  public void setLicenseStatus(String licenseStatus) {
    this.licenseStatus = licenseStatus;
  }

  public String getConnectStatus() {
    return connectStatus;
  }

  public void setConnectStatus(String connectStatus) {
    this.connectStatus = connectStatus;
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

  public String getCmsSerialNumber() {
    return cmsSerialNumber;
  }

  public void setCmsSerialNumber(String cmsSerialNumber) {
    this.cmsSerialNumber = cmsSerialNumber;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getCmsName() {
    return cmsName;
  }

  public void setCmsName(String cmsName) {
    this.cmsName = cmsName;
  }

  public long getUpTime() {
    return upTime;
  }

  public void setUpTime(long upTime) {
    this.upTime = upTime;
  }

  public int getCpuMetric() {
    return cpuMetric;
  }

  public void setCpuMetric(int cpuMetric) {
    this.cpuMetric = cpuMetric;
  }

  public int getMemoryMetric() {
    return memoryMetric;
  }

  public void setMemoryMetric(int memoryMetric) {
    this.memoryMetric = memoryMetric;
  }

  public int getSystemFsMetric() {
    return systemFsMetric;
  }

  public void setSystemFsMetric(int systemFsMetric) {
    this.systemFsMetric = systemFsMetric;
  }

  public int getIndexFsMetric() {
    return indexFsMetric;
  }

  public void setIndexFsMetric(int indexFsMetric) {
    this.indexFsMetric = indexFsMetric;
  }

  public int getMetadataFsMetric() {
    return metadataFsMetric;
  }

  public void setMetadataFsMetric(int metadataFsMetric) {
    this.metadataFsMetric = metadataFsMetric;
  }

  public int getMetadataHotFsMetric() {
    return metadataHotFsMetric;
  }

  public void setMetadataHotFsMetric(int metadataHotFsMetric) {
    this.metadataHotFsMetric = metadataHotFsMetric;
  }

  public int getPacketFsMetric() {
    return packetFsMetric;
  }

  public void setPacketFsMetric(int packetFsMetric) {
    this.packetFsMetric = packetFsMetric;
  }

  public int getAlarmCount() {
    return alarmCount;
  }

  public void setAlarmCount(int alarmCount) {
    this.alarmCount = alarmCount;
  }

  public String getLastInteractiveTime() {
    return lastInteractiveTime;
  }

  public void setLastInteractiveTime(String lastInteractiveTime) {
    this.lastInteractiveTime = lastInteractiveTime;
  }

  public long getLastInteractiveLatency() {
    return lastInteractiveLatency;
  }

  public void setLastInteractiveLatency(long lastInteractiveLatency) {
    this.lastInteractiveLatency = lastInteractiveLatency;
  }

  public String getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(String lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public String getLicenseStatusText() {
    return licenseStatusText;
  }

  public void setLicenseStatusText(String licenseStatusText) {
    this.licenseStatusText = licenseStatusText;
  }

  public String getConnectStatusText() {
    return connectStatusText;
  }

  public void setConnectStatusText(String connectStatusText) {
    this.connectStatusText = connectStatusText;
  }

  public List<CentralRaidBO> getRaidList() {
    return raidList;
  }

  public void setRaidList(List<CentralRaidBO> raidList) {
    this.raidList = raidList;
  }

  public List<CentralNetifBO> getNetifList() {
    return netifList;
  }

  public void setNetifList(List<CentralNetifBO> netifList) {
    this.netifList = netifList;
  }

}
