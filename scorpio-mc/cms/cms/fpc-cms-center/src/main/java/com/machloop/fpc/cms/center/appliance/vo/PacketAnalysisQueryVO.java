package com.machloop.fpc.cms.center.appliance.vo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年6月7日, fpc-manager
 */
public class PacketAnalysisQueryVO implements LogAudit {

  private String queryId;
  private String startTime;
  private String endTime;
  private String networkId;
  private String serviceId;
  private String bpf;
  private String tuple;
  private int limit = 100;

  private String fpcSerialNumber;

  private String fileType = FpcCmsConstants.PACKET_FILE_TYPE_PCAP;

  public Tuple2<String, Map<String, Object>> toParamUrlWithPlaceholder()
      throws UnsupportedEncodingException {
    String paramUrl = "?queryId={queryId}&startTime={startTime}&endTime={endTime}"
        + "&networkId={networkId}&serviceId={serviceId}"
        + "&bpf={bpf}&tuple={tuple}&limit={limit}&fileType={fileType}";

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    paramMap.put("queryId", queryId);
    paramMap.put("startTime", URLEncoder.encode(startTime, StandardCharsets.UTF_8.name()));
    paramMap.put("endTime", URLEncoder.encode(endTime, StandardCharsets.UTF_8.name()));
    paramMap.put("networkId", StringUtils.defaultIfBlank(networkId, "ALL"));
    paramMap.put("serviceId", StringUtils.defaultIfBlank(serviceId, ""));
    paramMap.put("bpf", StringUtils.defaultIfBlank(bpf, ""));
    paramMap.put("tuple", StringUtils.defaultIfBlank(tuple, ""));
    paramMap.put("limit", limit);
    paramMap.put("fileType", StringUtils.defaultIfBlank(fileType, ""));

    return Tuples.of(paramUrl, paramMap);
  }

  public String toParamUrl() throws UnsupportedEncodingException {
    return "?queryId=" + queryId + "&startTime="
        + URLEncoder.encode(startTime, StandardCharsets.UTF_8.name()) + "&endTime="
        + URLEncoder.encode(endTime, StandardCharsets.UTF_8.name()) + "&networkId="
        + StringUtils.defaultIfBlank(networkId, "ALL") + "&serviceId="
        + StringUtils.defaultIfBlank(serviceId, "") + "&bpf="
        + URLEncoder.encode(StringUtils.defaultIfBlank(bpf, ""), StandardCharsets.UTF_8.name())
        + "&tuple="
        + URLEncoder.encode(StringUtils.defaultIfBlank(tuple, ""), StandardCharsets.UTF_8.name())
        + "&fileType=" + StringUtils.defaultIfBlank(fileType, "") + "&limit=" + limit;
  }

  @Override
  public String toString() {
    return "PacketAnalysisQueryVO [queryId=" + queryId + ", startTime=" + startTime + ", endTime="
        + endTime + ", networkId=" + networkId + ", serviceId=" + serviceId + ", bpf=" + bpf
        + ", tuple=" + tuple + ", limit=" + limit + ", fpcSerialNumber=" + fpcSerialNumber
        + ", fileType=" + fileType + "]";
  }

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_QUERY:
        builder.append("查询数据包，");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DOWNLOAD:
        builder.append("下载PCAP，");
        break;
      default:
        return "";
    }

    builder.append("查询条件：");
    if (StringUtils.isNotBlank(bpf)) {
      builder.append(bpf).append("；");
    }
    if (StringUtils.isNotBlank(tuple)) {
      builder.append(tuple).append("；");
    }
    builder.append("网络ID=").append(networkId).append("；");
    builder.append("开始时间=").append(startTime).append("；");
    builder.append("结束时间=").append(endTime).append("；");
    builder.append("文件类型=").append(fileType).append("。");

    return builder.toString();
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
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

  public String getBpf() {
    return bpf;
  }

  public void setBpf(String bpf) {
    this.bpf = bpf;
  }

  public String getTuple() {
    return tuple;
  }

  public void setTuple(String tuple) {
    this.tuple = tuple;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

}
