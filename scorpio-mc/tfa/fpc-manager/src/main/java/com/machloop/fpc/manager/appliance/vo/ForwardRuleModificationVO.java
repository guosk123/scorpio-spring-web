package com.machloop.fpc.manager.appliance.vo;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 15:49,IntelliJ IDEA
 *
 */
public class ForwardRuleModificationVO {

  @Length(min = 1, max = 30, message = "转发规则名称不能为空，最多可输入30个字符")
  private String name;

  @Digits(integer = 1, fraction = 0, message = "策略选项格式不正确")
  @Range(min = 0, max = 1, message = "策略选项格式不正确")
  @NotEmpty(message = "策略不能为空")
  private String defaultAction;

  @Length(max = 1024, message = "bpf长度不在可允许范围内")
  private String exceptBpf;

  private String exceptTuple;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override public String toString() {
    return "ForwardRuleModificationVO{" + "name='" + name + '\'' + ", defaultAction='"
        + defaultAction + '\'' + ", exceptBpf='" + exceptBpf + '\'' + ", exceptTuple='"
        + exceptTuple + '\'' + ", description='" + description + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  public String getExceptBpf() {
    return exceptBpf;
  }

  public void setExceptBpf(String exceptBpf) {
    this.exceptBpf = exceptBpf;
  }

  public String getExceptTuple() {
    return exceptTuple;
  }

  public void setExceptTuple(String exceptTuple) {
    this.exceptTuple = exceptTuple;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
