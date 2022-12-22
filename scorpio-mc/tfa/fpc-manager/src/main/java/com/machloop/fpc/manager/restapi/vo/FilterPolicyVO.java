package com.machloop.fpc.manager.restapi.vo;

import java.util.Map;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2021年6月30日, fpc-manager
 */
public class FilterPolicyVO {

  @Length(min = 1, max = 30, message = "过滤规则名称不能为空，最多可输入30个字符")
  private String name;
  @Digits(integer = 1, fraction = 0, message = "不合法的会话应用识别前的流量存储策略")
  @Range(min = 0, max = 1, message = "不合法的会话应用识别前的流量存储策略")
  @NotEmpty(message = "会话应用识别前的流量存储策略不能为空")
  private String exceptFlow;
  private int defaultAction;
  private Map<String, Integer> exceptApplication;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "FilterPolicyVO [name=" + name + ", exceptFlow=" + exceptFlow + ", defaultAction="
        + defaultAction + ", exceptApplication=" + exceptApplication + ", description="
        + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExceptFlow() {
    return exceptFlow;
  }

  public void setExceptFlow(String exceptFlow) {
    this.exceptFlow = exceptFlow;
  }

  public int getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(int defaultAction) {
    this.defaultAction = defaultAction;
  }

  public Map<String, Integer> getExceptApplication() {
    return exceptApplication;
  }

  public void setExceptApplication(Map<String, Integer> exceptApplication) {
    this.exceptApplication = exceptApplication;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
