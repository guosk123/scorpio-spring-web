package com.machloop.fpc.npm.appliance.bo;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;

/**
 * @author guosk
 *
 * create at 2020年11月10日, fpc-manager
 */
public class NetworkBO implements LogAudit {

  private String id;
  private String name;
  private String netifType;
  private String extraSettings;
  private String createTime;

  private String insideIpAddress;
  private String ingestPolicyId;
  private String filterRuleIds;
  private String sendPolicyIds;
  private List<NetworkNetifDO> netif;

  private String netifTypeText;
  private long netifCount;
  private long totalBandwidth;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除网络：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("流量方向=")
        .append(
            StringUtils.equals(netifType, FpcConstants.APPLIANCE_NETWORK_UNIDIRECTION_FLOW) ? "单向流量"
                : "双向流量")
        .append(";");
    builder.append("业务接口=").append(JsonHelper.serialize(netif)).append(";");
    builder.append("额外配置=").append(extraSettings).append(";");
    builder.append("内网ip配置=").append(insideIpAddress).append(";");
    builder.append("捕获规则id=").append(ingestPolicyId).append(";");
    builder.append("应用规则id=").append(filterRuleIds).append(";");
    builder.append("外发策略id=").append(sendPolicyIds).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "NetworkBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", netifType='"
        + netifType + '\'' + ", extraSettings='" + extraSettings + '\'' + ", createTime='"
        + createTime + '\'' + ", insideIpAddress='" + insideIpAddress + '\'' + ", ingestPolicyId='"
        + ingestPolicyId + '\'' + ", filterRuleIds='" + filterRuleIds + '\'' + ", sendPolicyIds='"
        + sendPolicyIds + '\'' + ", netif=" + netif + ", netifTypeText='" + netifTypeText + '\''
        + ", netifCount=" + netifCount + ", totalBandwidth=" + totalBandwidth + '}';
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

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getInsideIpAddress() {
    return insideIpAddress;
  }

  public void setInsideIpAddress(String insideIpAddress) {
    this.insideIpAddress = insideIpAddress;
  }

  public String getIngestPolicyId() {
    return ingestPolicyId;
  }

  public void setIngestPolicyId(String ingestPolicyId) {
    this.ingestPolicyId = ingestPolicyId;
  }

  public String getFilterRuleIds() {
    return filterRuleIds;
  }

  public void setFilterRuleIds(String filterRuleIds) {
    this.filterRuleIds = filterRuleIds;
  }

  public List<NetworkNetifDO> getNetif() {
    return netif;
  }

  public void setNetif(List<NetworkNetifDO> netif) {
    this.netif = netif;
  }

  public String getNetifTypeText() {
    return netifTypeText;
  }

  public void setNetifTypeText(String netifTypeText) {
    this.netifTypeText = netifTypeText;
  }

  public long getNetifCount() {
    return netifCount;
  }

  public void setNetifCount(long netifCount) {
    this.netifCount = netifCount;
  }

  public long getTotalBandwidth() {
    return totalBandwidth;
  }

  public void setTotalBandwidth(long totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
