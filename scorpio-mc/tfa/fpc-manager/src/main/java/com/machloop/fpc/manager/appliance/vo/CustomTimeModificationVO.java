package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2020年11月2日, fpc-manager
 */
public class CustomTimeModificationVO {
  
  private String id;
  @NotEmpty(message = "自定义时间名称不能为空")
  private String name;
  @Range(min = 0, max = 1, message = "类型格式错误")
  private String type;
  private String period;
  private String customTimeSetting;

  @Override
  public String toString() {
    return "CustomTimeCreationVO [id=" + id + ", name=" + name + ", type=" + type + ", period="
        + period + ", customTimeSetting=" + customTimeSetting + "]";
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
