package com.machloop.fpc.manager.analysis.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class ScenarioTaskCreationVO {
  @Length(min = 1, max = 30, message = "名称长度不在可允许范围内")
  private String name;

  private String analysisStartTime;
  private String analysisEndTime;
  @NotEmpty(message = "场景类型不能为空")
  private String type;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "ScenarioTaskCreationVO [name=" + name + ", analysisStartTime=" + analysisStartTime
        + ", analysisEndTime=" + analysisEndTime + ", type=" + type + ", description=" + description
        + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAnalysisStartTime() {
    return analysisStartTime;
  }

  public void setAnalysisStartTime(String analysisStartTime) {
    this.analysisStartTime = analysisStartTime;
  }

  public String getAnalysisEndTime() {
    return analysisEndTime;
  }

  public void setAnalysisEndTime(String analysisEndTime) {
    this.analysisEndTime = analysisEndTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
