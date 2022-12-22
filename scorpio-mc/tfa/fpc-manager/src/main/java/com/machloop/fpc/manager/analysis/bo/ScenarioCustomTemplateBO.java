package com.machloop.fpc.manager.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
public class ScenarioCustomTemplateBO implements LogAudit {
  private String id;

  private String name;
  private String dataSource;
  private String filterDsl;
  private String filterSpl;
  private String function;
  private String groupBy;
  private int avgTimeInterval;
  private int sliceTimeInterval;
  private String description;

  private String createTime;
  private String updateTime;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加场景分析自定义模板：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除场景分析自定义模板：");
        break;
      default:
        return "";
    }
    builder.append("模板名称=").append(name).append(";");
    builder.append("数据源=").append(dataSource).append(";");
    builder.append("查询条件=").append(filterDsl).append(";");
    builder.append("计算方法=").append(function).append(";");
    builder.append("按时间平均=").append(avgTimeInterval).append(";");
    builder.append("按时间切片=").append(sliceTimeInterval).append(";");
    builder.append("分组=").append(groupBy).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ScenarioCustomTemplateBO [id=" + id + ", name=" + name + ", dataSource=" + dataSource
        + ", filterDsl=" + filterDsl + ", filterSpl=" + filterSpl + ", function=" + function
        + ", groupBy=" + groupBy + ", avgTimeInterval=" + avgTimeInterval + ", sliceTimeInterval="
        + sliceTimeInterval + ", description=" + description + ", createTime=" + createTime
        + ", updateTime=" + updateTime + "]";
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

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
