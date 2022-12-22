package com.machloop.fpc.manager.appliance.vo;

import org.hibernate.validator.constraints.Length;

public class HostInsideModificationVO {

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "HostInsideModificationVO [description=" + description + ", toString()="
        + super.toString() + "]";
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
