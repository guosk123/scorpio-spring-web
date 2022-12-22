package com.machloop.fpc.cms.center.sensor.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月22日, fpc-cms-center
 */
public class SensorNetworkBO implements LogAudit {

  private String id;
  private String name;
  private int bandwidth;
  private String sensorId;
  private String sensorName;
  private String sensorType;
  private String networkInSensorId;
  private String networkInSensorName;
  private String owner;
  private String deviceSerialNumber;
  private String description;
  private String operatorId;

  // 网络所在的的集群节点状态、详情
  private String status;
  private String statusDetail;

  private String sendPolicyIds;

  @Override
  public String toString() {
    return "SensorNetworkBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", bandwidth="
        + bandwidth + ", sensorId='" + sensorId + '\'' + ", sensorName='" + sensorName + '\''
        + ", sensorType='" + sensorType + '\'' + ", networkInSensorId='" + networkInSensorId + '\''
        + ", networkInSensorName='" + networkInSensorName + '\'' + ", owner='" + owner + '\''
        + ", deviceSerialNumber='" + deviceSerialNumber + '\'' + ", description='" + description
        + '\'' + ", operatorId='" + operatorId + '\'' + ", status='" + status + '\''
        + ", statusDetail='" + statusDetail + '\'' + ", sendPolicyIds='" + sendPolicyIds + '\''
        + '}';
  }

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加探针网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改探针网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除探针网络：");
        break;
      default:
        return "";
    }
    builder.append("网络名=").append(sensorName).append(";");
    builder.append("网络ID=").append(networkInSensorId).append(";");
    builder.append("探针名=").append(sensorName).append(";");
    builder.append("探针ID=").append(sensorName).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
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

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  public String getSensorName() {
    return sensorName;
  }

  public void setSensorName(String sensorName) {
    this.sensorName = sensorName;
  }

  public String getSensorType() {
    return sensorType;
  }

  public void setSensorType(String sensorType) {
    this.sensorType = sensorType;
  }

  public String getNetworkInSensorId() {
    return networkInSensorId;
  }

  public void setNetworkInSensorId(String networkInSensorId) {
    this.networkInSensorId = networkInSensorId;
  }

  public String getNetworkInSensorName() {
    return networkInSensorName;
  }

  public void setNetworkInSensorName(String networkInSensorName) {
    this.networkInSensorName = networkInSensorName;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  public void setDeviceSerialNumber(String deviceSerialNumber) {
    this.deviceSerialNumber = deviceSerialNumber;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusDetail() {
    return statusDetail;
  }

  public void setStatusDetail(String statusDetail) {
    this.statusDetail = statusDetail;
  }

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
