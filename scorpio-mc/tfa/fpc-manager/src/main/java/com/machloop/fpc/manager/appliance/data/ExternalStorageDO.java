package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
public class ExternalStorageDO extends BaseOperateDO {

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

  @Override
  public String toString() {
    return "ExternalStorageDO [name=" + name + ", state=" + state + ", usage=" + usage + ", type="
        + type + ", ipAddress=" + ipAddress + ", port=" + port + ", username=" + username
        + ", password=" + password + ", directory=" + directory + ", capacity=" + capacity
        + ", description=" + description + "]";
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

}
