package com.scorpio.security.data;

import java.util.Date;

public class UserDO {

  private String name;
  private String fullname;
  private String password;
  private String needChangePassword = "0";
  private String appKey;
  private String appToken;
  private String userType;
  private String description;
  private String locked = "0";
  private Date passwordUpdateTime;

  @Override
  public String toString() {
    return "UserDO [name=" + name + ", fullname=" + fullname + ", password=" + password
        + ", needChangePassword=" + needChangePassword + ", appKey=" + appKey + ", appToken="
        + appToken + ", userType=" + userType + ", description=" + description + ", locked="
        + locked + ", passwordUpdateTime=" + passwordUpdateTime + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getNeedChangePassword() {
    return needChangePassword;
  }

  public void setNeedChangePassword(String needChangePassword) {
    this.needChangePassword = needChangePassword;
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

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocked() {
    return locked;
  }

  public void setLocked(String locked) {
    this.locked = locked;
  }

  public Date getPasswordUpdateTime() {
    return passwordUpdateTime;
  }

  public void setPasswordUpdateTime(Date passwordUpdateTime) {
    this.passwordUpdateTime = passwordUpdateTime;
  }

}
