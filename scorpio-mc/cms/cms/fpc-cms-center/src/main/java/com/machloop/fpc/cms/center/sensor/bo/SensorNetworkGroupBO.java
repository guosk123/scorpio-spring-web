package com.machloop.fpc.cms.center.sensor.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorNetworkGroupBO implements LogAudit {

  private String id;
  private String name;
  private int bandwidth;
  private String networkInSensorIds;
  private String description;
  private String operatorId;

  private String networkInSensorNames;

  // 网络组所在的的集群节点状态、详情
  private String status;
  private String statusDetail;

  private String sendPolicyIds;

  @Override
  public String toString() {
    return "SensorNetworkGroupBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", bandwidth="
        + bandwidth + ", networkInSensorIds='" + networkInSensorIds + '\'' + ", description='"
        + description + '\'' + ", operatorId='" + operatorId + '\'' + ", networkInSensorNames='"
        + networkInSensorNames + '\'' + ", status='" + status + '\'' + ", statusDetail='"
        + statusDetail + '\'' + ", sendPolicyIds='" + sendPolicyIds + '\'' + '}';
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
    builder.append("网络组名=").append(name).append(";");
    builder.append("包含的网络id=").append(networkInSensorIds).append(";");
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

  public String getNetworkInSensorIds() {
    return networkInSensorIds;
  }

  public void setNetworkInSensorIds(String networkInSensorIds) {
    this.networkInSensorIds = networkInSensorIds;
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

  public String getNetworkInSensorNames() {
    return networkInSensorNames;
  }

  public void setNetworkInSensorNames(String networkInSensorNames) {
    this.networkInSensorNames = networkInSensorNames;
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
