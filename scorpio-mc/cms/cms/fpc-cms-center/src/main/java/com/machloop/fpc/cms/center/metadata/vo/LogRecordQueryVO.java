package com.machloop.fpc.cms.center.metadata.vo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月22日, fpc-manager
 */
public class LogRecordQueryVO implements LogAudit {
  private String id;
  private String keyword;
  private String dsl;
  private String sourceType;// 数据源
  private String packetFileId;// 离线数据包文件ID

  private String networkId;
  private String networkGroupId;
  private String serviceId;

  private List<String> networkIds;
  private List<Tuple2<String, String>> serviceNetworkIds;

  private Boolean decrypted;

  private Date startTimeDate;
  private Date endTimeDate;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;

  private String entry;// 入口页面标记（中文）

  private boolean hasAgingTime = false;

  private int timePrecision = 9;

  // 定义查询哪些列
  @Pattern(regexp = WebappConstants.TABLE_COLUMNS_PATTERN, message = "指定列名不合法,且长度需要大于一个字符")
  private String columns = "*";

  private String startTime;

  private String endTime;

  private int count = 10;

  private String srcIp;
  private String destIp;

  private int interval;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(entry)) {
      entry = "应用层协议分析";
    }
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_QUERY:
        builder.append("查询").append(entry).append("，");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DOWNLOAD:
        builder.append("导出查询后的").append(entry).append("，");
        break;
      default:
        return "";
    }
    builder.append("查询条件：").append(dsl).append("；");
    builder.append("开始时间=").append(startTimeDate).append("；");
    builder.append("结束时间=").append(endTimeDate).append("；");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "LogRecordQueryVO{" + "id='" + id + '\'' + ", keyword='" + keyword + '\'' + ", dsl='"
        + dsl + '\'' + ", sourceType='" + sourceType + '\'' + ", packetFileId='" + packetFileId
        + '\'' + ", networkId='" + networkId + '\'' + ", networkGroupId='" + networkGroupId + '\''
        + ", serviceId='" + serviceId + '\'' + ", networkIds=" + networkIds + ", serviceNetworkIds="
        + serviceNetworkIds + ", decrypted=" + decrypted + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + ", entry='" + entry + '\'' + ", hasAgingTime="
        + hasAgingTime + ", timePrecision=" + timePrecision + ", columns='" + columns + '\''
        + ", startTime='" + startTime + '\'' + ", endTime='" + endTime + '\'' + ", count=" + count
        + ", srcIp='" + srcIp + '\'' + ", destIp='" + destIp + '\'' + ", interval=" + interval
        + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public List<String> getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(List<String> networkIds) {
    this.networkIds = networkIds;
  }

  public List<Tuple2<String, String>> getServiceNetworkIds() {
    return serviceNetworkIds;
  }

  public void setServiceNetworkIds(List<Tuple2<String, String>> serviceNetworkIds) {
    this.serviceNetworkIds = serviceNetworkIds;
  }

  public Boolean getDecrypted() {
    return decrypted;
  }

  public void setDecrypted(Boolean decrypted) {
    this.decrypted = decrypted;
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

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public boolean getHasAgingTime() {
    return hasAgingTime;
  }

  public void setHasAgingTime(boolean hasAgingTime) {
    this.hasAgingTime = hasAgingTime;
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

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }
}
