package com.machloop.fpc.manager.analysis.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
public class ScenarioCustomTemplateDO extends BaseOperateDO {
  private String name;
  private String dataSource;
  private String filterDsl;
  private String filterSpl;
  private String function;
  private String groupBy;
  private int avgTimeInterval;
  private int sliceTimeInterval;
  private String description;

  @Override
  public String toString() {
    return "ScenarioCustomTemplateDO [name=" + name + ", dataSource=" + dataSource + ", filterDsl="
        + filterDsl + ", filterSpl=" + filterSpl + ", function=" + function + ", groupBy=" + groupBy
        + ", avgTimeInterval=" + avgTimeInterval + ", sliceTimeInterval=" + sliceTimeInterval
        + ", description=" + description + ", toString()=" + super.toString() + "]";
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

  public String getFilterDsl() {
    return filterDsl;
  }

  public void setFilterDsl(String filterDsl) {
    this.filterDsl = filterDsl;
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

  public String getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(String groupBy) {
    this.groupBy = groupBy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
