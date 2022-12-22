package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
public class ExternalStorageBO implements LogAudit {

  private String id;
  private String name;
  private String state;
  private String usage;
  private String type;
  private String ipAddress;
  private int port;
  private String username;
  private String password;
  private String directory;
  private long capacity;
  private String description;

  private String updateTime;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加存储服务器：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改存储服务器：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除存储服务器：");
        break;
      default:
        return "";
    }

    builder.append("服务器名称=").append(name).append(";");
    builder.append("是否启用=").append(StringUtils.equals(Constants.BOOL_YES, state) ? "启用" : "关闭")
        .append(";");
    builder.append("用途=").append(usage).append(";");
    builder.append("服务器类型=").append(type).append(";");
    builder.append("IP地址=").append(ipAddress).append(";");
    builder.append("端口=").append(port).append(";");
    builder.append("用户名=").append(username).append(";");
    builder.append("存储目录=").append(directory).append(";");
    builder.append("最大可用容量(byte)=").append(capacity).append(";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ExternalStorageBO [id=" + id + ", name=" + name + ", state=" + state + ", usage="
        + usage + ", type=" + type + ", ipAddress=" + ipAddress + ", port=" + port + ", username="
        + username + ", password=" + password + ", directory=" + directory + ", capacity="
        + capacity + ", description=" + description + ", updateTime=" + updateTime + "]";
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

  public String getUsage() {
    return usage;
  }

  public void setUsage(String usage) {
    this.usage = usage;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
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

}
