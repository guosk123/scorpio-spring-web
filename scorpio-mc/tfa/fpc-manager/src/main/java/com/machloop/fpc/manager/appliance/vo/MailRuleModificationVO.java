package com.machloop.fpc.manager.appliance.vo;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月27日, fpc-manager
 */
public class MailRuleModificationVO {

  private String mailAddress;
  private String countryId;
  private String provinceId;
  private String cityId;
  private String startTime;
  private String endTime;
  private String action;
  private String period;
  private String state;

  @Override
  public String toString() {
    return "MailRuleCreationVO [mailAddress=" + mailAddress + ", countryId=" + countryId
        + ", provinceId=" + provinceId + ", cityId=" + cityId + ", startTime=" + startTime
        + ", endTime=" + endTime + ", action=" + action + ", period=" + period + ", state=" + state
        + "]";
  }

  public String getMailAddress() {
    return mailAddress;
  }

  public void setMailAddress(String mailAddress) {
    this.mailAddress = mailAddress;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(String provinceId) {
    this.provinceId = provinceId;
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
