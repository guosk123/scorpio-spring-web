package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
public class ExternalStorageCreationVO {

  @NotEmpty(message = "名称不能为空")
  private String name;
  @Digits(integer = 1, fraction = 0, message = "启用选项格式不正确")
  @Range(min = 0, max = 1, message = "启用选项格式不正确")
  @NotEmpty(message = "启用状态不能为空")
  private String state;
  @NotEmpty(message = "服务器用途不能为空")
  private String usage;
  @NotEmpty(message = "服务器类型不能为空")
  private String type;
  @NotEmpty(message = "IP地址不能为空")
  private String ipAddress;
  @Range(min = 0, max = 65535, message = "端口范围不合法")
  private int port;
  @NotEmpty(message = "用户名不能为空")
  private String username;
  private String password;
  @NotEmpty(message = "存储目录不能为空")
  private String directory;
  private long capacity;

  @Override
  public String toString() {
    return "ExternalStorageCreationVO [name=" + name + ", state=" + state + ", usage=" + usage
        + ", type=" + type + ", ipAddress=" + ipAddress + ", port=" + port + ", username="
        + username + ", password=" + password + ", directory=" + directory + ", capacity="
        + capacity + "]";
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

}
