package com.machloop.fpc.cms.center.knowledge.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

public class SaCustomApplicationModificationVO {

  @NotEmpty(message = "名称不能为空")
  private String name;

  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "类型格式不正确")
  @NotEmpty(message = "类型不能为空")
  private String categoryId;

  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "子类型格式不正确")
  @NotEmpty(message = "子类型不能为空")
  private String subCategoryId;

  @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "应用层协议不正确")
  @NotEmpty(message = "应用层协议不能为空")
  private String l7ProtocolId;

  private String rule;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "SaCustomApplicationModificationVO [name=" + name + ", categoryId=" + categoryId
        + ", subCategoryId=" + subCategoryId + ", l7ProtocolId=" + l7ProtocolId + ", rule=" + rule
        + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getSubCategoryId() {
    return subCategoryId;
  }

  public void setSubCategoryId(String subCategoryId) {
    this.subCategoryId = subCategoryId;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
