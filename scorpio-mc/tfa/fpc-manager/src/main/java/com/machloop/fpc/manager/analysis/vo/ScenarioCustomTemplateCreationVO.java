package com.machloop.fpc.manager.analysis.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class ScenarioCustomTemplateCreationVO {
  @Length(min = 1, max = 30, message = "名称长度不在可允许范围内")
  private String name;
  @NotEmpty(message = "场景模板名称不能为空")
  private String dataSource;
  @NotEmpty(message = "过滤条件表达式不能为空")
  private String filterSpl;
  @NotEmpty(message = "计算方法不能为空")
  private String function;
  private String groupBy;
  private int avgTimeInterval;
  private int sliceTimeInterval;
  private String description;

  @Override
  public String toString() {
    return "ScenarioCustomTemplateCreationVO [name=" + name + ", dataSource=" + dataSource
        + ", filterSpl=" + filterSpl + ", function=" + function + ", groupBy=" + groupBy
        + ", avgTimeInterval=" + avgTimeInterval + ", sliceTimeInterval=" + sliceTimeInterval
        + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public String getFilterSpl() {
    return filterSpl;
  }

  public void setFilterSpl(String filterSpl) {
    this.filterSpl = filterSpl;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public String getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public int getAvgTimeInterval() {
    return avgTimeInterval;
  }

  public void setAvgTimeInterval(int avgTimeInterval) {
    this.avgTimeInterval = avgTimeInterval;
  }

  public int getSliceTimeInterval() {
    return sliceTimeInterval;
  }

  public void setSliceTimeInterval(int sliceTimeInterval) {
    this.sliceTimeInterval = sliceTimeInterval;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
