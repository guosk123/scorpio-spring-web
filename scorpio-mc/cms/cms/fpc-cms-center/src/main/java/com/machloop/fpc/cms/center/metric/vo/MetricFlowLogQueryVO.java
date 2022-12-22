package com.machloop.fpc.cms.center.metric.vo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Pattern;

import com.machloop.alpha.webapp.WebappConstants;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2021年5月26日, fpc-manager
 */
public class MetricFlowLogQueryVO {

  private String queryId;

  private int count = 10;

  // 数据源
  private String sourceType;
  private Date startTimeDate;
  private Date endTimeDate;
  private String networkId;
  private String networkGroupId;
  private String serviceId;
  private int interval;
  private String dsl;
  // 离线数据包文件ID
  private String packetFileId;

  private Integer countryId;
  private Integer provinceId;
  private Integer cityId;
  private Integer categoryId;
  private Integer subCategoryId;
  private Integer applicationId;
  private Integer l7ProtocolId;
  private Integer port;
  private String ipProtocol;
  private String ipAddress;
  private String hostgroupId;
  private String macAddress;
  private Integer ethernetType;
  private Integer ipLocality;

  private Integer ipLocalityInitiator;
  private Integer ipLocalityResponder;

  private List<String> networkIds;
  private List<Tuple2<String, String>> serviceNetworkIds;

  // 下钻时不同的统计对应不同的过滤类型
  private String filterType = "";

  // 定义查询哪些列
  @Pattern(regexp = WebappConstants.TABLE_COLUMNS_PATTERN, message = "指定列名不合法,且长度需要大于一个字符")
  private String columns = "*";

  private String startTime;

  private String endTime;

  private String ipInitiator;
  private String ipResponder;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;

  @Override
  public String toString() {
    return "MetricFlowLogQueryVO{" + "queryId='" + queryId + '\'' + ", count=" + count
        + ", sourceType='" + sourceType + '\'' + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", networkId='" + networkId + '\'' + ", networkGroupId='"
        + networkGroupId + '\'' + ", serviceId='" + serviceId + '\'' + ", interval=" + interval
        + ", dsl='" + dsl + '\'' + ", packetFileId='" + packetFileId + '\'' + ", countryId="
        + countryId + ", provinceId=" + provinceId + ", cityId=" + cityId + ", categoryId="
        + categoryId + ", subCategoryId=" + subCategoryId + ", applicationId=" + applicationId
        + ", l7ProtocolId=" + l7ProtocolId + ", port=" + port + ", ipProtocol='" + ipProtocol + '\''
        + ", ipAddress='" + ipAddress + '\'' + ", hostgroupId='" + hostgroupId + '\''
        + ", macAddress='" + macAddress + '\'' + ", ethernetType=" + ethernetType + ", ipLocality="
        + ipLocality + ", ipLocalityInitiator=" + ipLocalityInitiator + ", ipLocalityResponder="
        + ipLocalityResponder + ", networkIds=" + networkIds + ", serviceNetworkIds="
        + serviceNetworkIds + ", filterType='" + filterType + '\'' + ", columns='" + columns + '\''
        + ", startTime='" + startTime + '\'' + ", endTime='" + endTime + '\'' + ", ipInitiator='"
        + ipInitiator + '\'' + ", ipResponder='" + ipResponder + '\'' + ", includeStartTime="
        + includeStartTime + ", includeEndTime=" + includeEndTime + '}';
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
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

  public Integer getCountryId() {
    return countryId;
  }

  public void setCountryId(Integer countryId) {
    this.countryId = countryId;
  }

  public Integer getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(Integer provinceId) {
    this.provinceId = provinceId;
  }

  public Integer getCityId() {
    return cityId;
  }

  public void setCityId(Integer cityId) {
    this.cityId = cityId;
  }

  public Integer getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
  }

  public Integer getSubCategoryId() {
    return subCategoryId;
  }

  public void setSubCategoryId(Integer subCategoryId) {
    this.subCategoryId = subCategoryId;
  }

  public Integer getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  public Integer getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(Integer l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getHostgroupId() {
    return hostgroupId;
  }

  public void setHostgroupId(String hostgroupId) {
    this.hostgroupId = hostgroupId;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public Integer getEthernetType() {
    return ethernetType;
  }

  public void setEthernetType(Integer ethernetType) {
    this.ethernetType = ethernetType;
  }

  public Integer getIpLocality() {
    return ipLocality;
  }

  public void setIpLocality(Integer ipLocality) {
    this.ipLocality = ipLocality;
  }

  public Integer getIpLocalityInitiator() {
    return ipLocalityInitiator;
  }

  public void setIpLocalityInitiator(Integer ipLocalityInitiator) {
    this.ipLocalityInitiator = ipLocalityInitiator;
  }

  public Integer getIpLocalityResponder() {
    return ipLocalityResponder;
  }

  public void setIpLocalityResponder(Integer ipLocalityResponder) {
    this.ipLocalityResponder = ipLocalityResponder;
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

  public String getFilterType() {
    return filterType;
  }

  public void setFilterType(String filterType) {
    this.filterType = filterType;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
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

  public String getIpInitiator() {
    return ipInitiator;
  }

  public void setIpInitiator(String ipInitiator) {
    this.ipInitiator = ipInitiator;
  }

  public String getIpResponder() {
    return ipResponder;
  }

  public void setIpResponder(String ipResponder) {
    this.ipResponder = ipResponder;
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

}
