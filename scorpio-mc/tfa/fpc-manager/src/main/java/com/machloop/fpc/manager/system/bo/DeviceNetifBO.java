package com.machloop.fpc.manager.system.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class DeviceNetifBO implements LogAudit {

  private String id;
  private String name;
  private String state;
  private String type;
  private String typeText;
  private String category;
  private String categoryText;
  private String ipv4Address;
  private String ipv4Gateway;
  private String ipv6Address;
  private String ipv6Gateway;
  private int specification;
  private double bandwidth;
  private String description;
  private String updateTime;
  private String metricTime;

  private String useMessage;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    if (auditLogAction != LogHelper.AUDIT_LOG_ACTION_UPDATE) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    builder.append("接口名=").append(name).append(";");
    builder.append("接口类型=").append(categoryText).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();

  }

  @Override
  public String toString() {
    return "DeviceNetifBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", state='" + state
        + '\'' + ", type='" + type + '\'' + ", typeText='" + typeText + '\'' + ", category='"
        + category + '\'' + ", categoryText='" + categoryText + '\'' + ", ipv4Address='"
        + ipv4Address + '\'' + ", ipv4Gateway='" + ipv4Gateway + '\'' + ", ipv6Address='"
        + ipv6Address + '\'' + ", ipv6Gateway='" + ipv6Gateway + '\'' + ", specification="
        + specification + ", bandwidth=" + bandwidth + ", description='" + description + '\''
        + ", updateTime='" + updateTime + '\'' + ", metricTime='" + metricTime + '\''
        + ", useMessage='" + useMessage + '\'' + '}';
  }

  public String getIpv4Address() {
    return ipv4Address;
  }

  public void setIpv4Address(String ipv4Address) {
    this.ipv4Address = ipv4Address;
  }

  public String getIpv4Gateway() {
    return ipv4Gateway;
  }

  public void setIpv4Gateway(String ipv4Gateway) {
    this.ipv4Gateway = ipv4Gateway;
  }

  public String getIpv6Address() {
    return ipv6Address;
  }

  public void setIpv6Address(String ipv6Address) {
    this.ipv6Address = ipv6Address;
  }

  public String getIpv6Gateway() {
    return ipv6Gateway;
  }

  public void setIpv6Gateway(String ipv6Gateway) {
    this.ipv6Gateway = ipv6Gateway;
  }


  public String getTypeText() {
    return typeText;
  }

  public void setTypeText(String typeText) {
    this.typeText = typeText;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategoryText() {
    return categoryText;
  }

  public void setCategoryText(String categoryText) {
    this.categoryText = categoryText;
  }

  public int getSpecification() {
    return specification;
  }

  public void setSpecification(int specification) {
    this.specification = specification;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public double getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(double bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(String metricTime) {
    this.metricTime = metricTime;
  }

  public String getUseMessage() {
    return useMessage;
  }

  public void setUseMessage(String useMessage) {
    this.useMessage = useMessage;
  }
}
