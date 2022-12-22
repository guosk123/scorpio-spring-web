package com.machloop.fpc.npm.analysis.vo;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public class SuricataRuleQueryVO {

  private String sid;
  private String action;
  private String protocol;
  private String srcIp;
  private String srcPort;
  private String direction;
  private String destIp;
  private String destPort;
  private String msg;
  private String content;
  private Integer priority;
  private String classtypeIds;
  private String mitreTacticIds;
  private String mitreTechniqueIds;
  private String cve;
  private String cnnvd;
  private String signatureSeverity;
  private String target;
  private String state;
  private String source;
  private String parseState;

  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;

  private String dsl;
  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;
  // 时间戳精确到毫秒
  private int timePrecision = 3;

  private int count;

  private String networkId;

  private String tag;

  private String basicTag;

  @Override
  public String toString() {
    return "SuricataRuleQueryVO{" + "sid='" + sid + '\'' + ", action='" + action + '\''
        + ", protocol='" + protocol + '\'' + ", srcIp='" + srcIp + '\'' + ", srcPort='" + srcPort
        + '\'' + ", direction='" + direction + '\'' + ", destIp='" + destIp + '\'' + ", destPort='"
        + destPort + '\'' + ", msg='" + msg + '\'' + ", content='" + content + '\'' + ", priority="
        + priority + ", classtypeIds='" + classtypeIds + '\'' + ", mitreTacticIds='"
        + mitreTacticIds + '\'' + ", mitreTechniqueIds='" + mitreTechniqueIds + '\'' + ", cve='"
        + cve + '\'' + ", cnnvd='" + cnnvd + '\'' + ", signatureSeverity='" + signatureSeverity
        + '\'' + ", target='" + target + '\'' + ", state='" + state + '\'' + ", source='" + source
        + '\'' + ", parseState='" + parseState + '\'' + ", startTime='" + startTime + '\''
        + ", endTime='" + endTime + '\'' + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", dsl='" + dsl + '\'' + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + ", timePrecision=" + timePrecision + ", count="
        + count + ", networkId='" + networkId + '\'' + ", tag='" + tag + '\'' + ", basicTag='"
        + basicTag + '\'' + '}';
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
  }

  public String getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(String srcPort) {
    this.srcPort = srcPort;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public String getDestPort() {
    return destPort;
  }

  public void setDestPort(String destPort) {
    this.destPort = destPort;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getClasstypeIds() {
    return classtypeIds;
  }

  public void setClasstypeIds(String classtypeIds) {
    this.classtypeIds = classtypeIds;
  }

  public String getMitreTacticIds() {
    return mitreTacticIds;
  }

  public void setMitreTacticIds(String mitreTacticIds) {
    this.mitreTacticIds = mitreTacticIds;
  }

  public String getMitreTechniqueIds() {
    return mitreTechniqueIds;
  }

  public void setMitreTechniqueIds(String mitreTechniqueIds) {
    this.mitreTechniqueIds = mitreTechniqueIds;
  }

  public String getCve() {
    return cve;
  }

  public void setCve(String cve) {
    this.cve = cve;
  }

  public String getCnnvd() {
    return cnnvd;
  }

  public void setCnnvd(String cnnvd) {
    this.cnnvd = cnnvd;
  }

  public String getSignatureSeverity() {
    return signatureSeverity;
  }

  public void setSignatureSeverity(String signatureSeverity) {
    this.signatureSeverity = signatureSeverity;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getParseState() {
    return parseState;
  }

  public void setParseState(String parseState) {
    this.parseState = parseState;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
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

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getBasicTag() {
    return basicTag;
  }

  public void setBasicTag(String basicTag) {
    this.basicTag = basicTag;
  }
}
