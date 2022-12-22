package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class CustomTimeDO extends BaseOperateDO {

  private String id;
  private String customTimeInCmsId;
  private String name;
  private String type;
  private String period;
  private String customTimeSetting;

  @Override
  public String toString() {
    return "CustomTimeDO [id=" + id + ", customTimeInCmsId=" + customTimeInCmsId + ", name=" + name
        + ", type=" + type + ", period=" + period + ", customTimeSetting=" + customTimeSetting
        + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCustomTimeInCmsId() {
    return customTimeInCmsId;
  }

  public void setCustomTimeInCmsId(String customTimeInCmsId) {
    this.customTimeInCmsId = customTimeInCmsId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getCustomTimeSetting() {
    return customTimeSetting;
  }

  public void setCustomTimeSetting(String customTimeSetting) {
    this.customTimeSetting = customTimeSetting;
  }

}
