package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author liyongjun
 *
 * create at 2020年3月5日, fpc-manager
 */
public class IngestPolicyCreationVO {

  @Length(min = 1, max = 30, message = "策略名称不能为空，最多可输入30个字符")
  private String name;
  @Digits(integer = 1, fraction = 0, message = "策略选项格式不正确")
  @Range(min = 0, max = 1, message = "策略选项格式不正确")
  @NotEmpty(message = "策略不能为空")
  private String defaultAction;
  @Length(max = 1024, message = "bpf长度不在可允许范围内")
  private String exceptBpf;
  private String exceptTuple;
  @Digits(integer = 1, fraction = 0, message = "报文去重选项格式不正确")
  @Range(min = 0, max = 1, message = "报文去重选项格式不正确")
  @NotEmpty(message = "报文去重选项不能为空")
  private String deduplication;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "IngestPolicyCreationVO [name=" + name + ", defaultAction=" + defaultAction
        + ", exceptBpf=" + exceptBpf + ", exceptTuple=" + exceptTuple + ", deduplication="
        + deduplication + ", description=" + description + "]";
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

  public String getDeduplication() {
    return deduplication;
  }

  public void setDeduplication(String deduplication) {
    this.deduplication = deduplication;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
