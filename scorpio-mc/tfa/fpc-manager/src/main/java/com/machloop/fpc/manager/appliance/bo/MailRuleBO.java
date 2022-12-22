package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月27日, fpc-manager
 */
public class MailRuleBO implements LogAudit {

  private String id;
  private String mailAddress;
  private String countryId;
  private String provinceId;
  private String cityId;
  private String startTime;
  private String endTime;
  private String action;
  private String period;
  private String state;

  private String createTime;
  private String updateTime;

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
        builder.append("添加邮件规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改邮件规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除邮件规则：");
        break;
      default:
        return "";
    }
    builder.append("邮件地址=").append(mailAddress).append("；");
    builder.append("国家ID=").append(countryId).append("；");
    builder.append("省份ID=").append(provinceId).append("；");
    builder.append("城市ID=").append(cityId).append("；");
    builder.append("开始时间=").append(startTime).append("；");
    builder.append("结束时间=").append(endTime).append("；");
    builder.append("动作=").append(action).append("；");
    builder.append("每周生效时间=").append(period).append("；");
    builder.append("是否启用=").append(state).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "MailRuleBO [id=" + id + ", mailAddress=" + mailAddress + ", countryId=" + countryId
        + ", provinceId=" + provinceId + ", cityId=" + cityId + ", startTime=" + startTime
        + ", endTime=" + endTime + ", action=" + action + ", period=" + period + ", state=" + state
        + ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

}
