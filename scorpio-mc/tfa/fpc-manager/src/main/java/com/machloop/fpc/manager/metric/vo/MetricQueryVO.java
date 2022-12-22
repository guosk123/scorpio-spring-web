package com.machloop.fpc.manager.metric.vo;

import java.util.Date;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2020年12月9日, fpc-manager
 */
public class MetricQueryVO implements LogAudit {

  private String queryId;

  private String sourceType;// 数据源
  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;
  private String networkId;
  private String serviceId;
  private int interval;
  private String dsl;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = false;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = true;

  @Range(min = 10, max = 1000, message = "topN的有效范围是[10,1000]")
  private int count = 10;

  private String drilldown = "0";// 是否为下钻查询
  private String realTime = "0";// 是否为实时统计

  private String packetFileId;// 离线数据包文件ID

  private int timePrecision = 3; // 时间戳精确到毫秒

  // 定义查询哪些列
  @Pattern(regexp = WebappConstants.TABLE_COLUMNS_PATTERN, message = "指定列名不合法,且长度需要大于一个字符")
  private String columns = "*";

  /*
   * 新增字段用于区分服务类型： 分为总体服务:"totalService",内网服务:"intranetService",外网服务:"internetService"
   */
  private String serviceType = "totalService";

  private String entry;// 入口页面标记（中文）

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(entry)) {
      entry = "网络分析";
    }
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_QUERY:
        builder.append("查询").append(entry).append("数据, ");
        break;
      case LogHelper.AUDIT_LOG_ACTION_EXPORT:
        builder.append("导出查询后的").append(entry).append("数据， ");
        break;
      default:
        return "";
    }
    builder.append("数据源：").append(sourceType).append("；");
    builder.append("查询条件：").append(dsl).append("；");
    builder.append("开始时间=").append(startTime).append("；");
    builder.append("结束时间=").append(endTime).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "MetricQueryVO [queryId=" + queryId + ", sourceType=" + sourceType + ", startTime="
        + startTime + ", endTime=" + endTime + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", networkId=" + networkId + ", serviceId=" + serviceId + ", interval="
        + interval + ", dsl=" + dsl + ", includeStartTime=" + includeStartTime + ", includeEndTime="
        + includeEndTime + ", count=" + count + ", drilldown=" + drilldown + ", realTime="
        + realTime + ", packetFileId=" + packetFileId + ", timePrecision=" + timePrecision
        + ", columns=" + columns + ", serviceType=" + serviceType + "]";
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public Date getStartTimeDate() {
    return startTimeDate;
  }

  public void setStartTimeDate(Date startTimeDate) {
    this.startTimeDate = startTimeDate;
  }

  public Date getEndTimeDate() {
    return endTimeDate;
  }

  public void setEndTimeDate(Date endTimeDate) {
    this.endTimeDate = endTimeDate;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
  }

  public boolean getIncludeStartTime() {
    return includeStartTime;
  }

  public void setIncludeStartTime(boolean includeStartTime) {
    this.includeStartTime = includeStartTime;
  }

  public boolean getIncludeEndTime() {
    return includeEndTime;
  }

  public void setIncludeEndTime(boolean includeEndTime) {
    this.includeEndTime = includeEndTime;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getDrilldown() {
    return drilldown;
  }

  public void setDrilldown(String drilldown) {
    this.drilldown = drilldown;
  }

  public String getRealTime() {
    return realTime;
  }

  public void setRealTime(String realTime) {
    this.realTime = realTime;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
  }

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
  }

  public String getColumns() {
    return columns;
  }

  public void setColumns(String columns) {
    this.columns = columns;
  }
}
