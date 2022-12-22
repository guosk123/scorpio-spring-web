package com.machloop.fpc.cms.npm.analysis.data;

import java.util.List;

import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author chenshimiao
 * 
 * create at 2022/10/10 3:02 PM,cms
 * @version 1.0
 */
public class SuricataRuleDO extends BaseOperateDO {

  private String assignId;
  private int sid;
  private String action;
  private String protocol;
  private String srcIp;
  private String srcPort;
  private String direction;
  private String destIp;
  private String destPort;
  private String msg;
  private int rev;
  private String content;
  private int priority;
  private String classtypeId;
  private String mitreTacticId;
  private String mitreTechniqueId;
  private String cve;
  private String cnnvd;
  private String signatureSeverity;
  private String target;
  private String threshold;
  private String rule;
  private String parseState;
  private String parseLog;
  private String state;
  private String source;

  /**
   * 下发用字段
   */
  private List<String> sids;
  private SuricataRuleQueryVO queryVO;

  @Override
  public String toString() {
    return "SuricataRuleDO [assignId=" + assignId + ", sid=" + sid + ", action=" + action
        + ", protocol=" + protocol + ", srcIp=" + srcIp + ", srcPort=" + srcPort + ", direction="
        + direction + ", destIp=" + destIp + ", destPort=" + destPort + ", msg=" + msg + ", rev="
        + rev + ", content=" + content + ", priority=" + priority + ", classtypeId=" + classtypeId
        + ", mitreTacticId=" + mitreTacticId + ", mitreTechniqueId=" + mitreTechniqueId + ", cve="
        + cve + ", cnnvd=" + cnnvd + ", signatureSeverity=" + signatureSeverity + ", target="
        + target + ", threshold=" + threshold + ", rule=" + rule + ", parseState=" + parseState
        + ", parseLog=" + parseLog + ", state=" + state + ", source=" + source + "]";
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
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

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public int getRev() {
    return rev;
  }

  public void setRev(int rev) {
    this.rev = rev;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getClasstypeId() {
    return classtypeId;
  }

  public void setClasstypeId(String classtypeId) {
    this.classtypeId = classtypeId;
  }

  public String getMitreTacticId() {
    return mitreTacticId;
  }

  public void setMitreTacticId(String mitreTacticId) {
    this.mitreTacticId = mitreTacticId;
  }

  public String getMitreTechniqueId() {
    return mitreTechniqueId;
  }

  public void setMitreTechniqueId(String mitreTechniqueId) {
    this.mitreTechniqueId = mitreTechniqueId;
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

  public String getThreshold() {
    return threshold;
  }

  public void setThreshold(String threshold) {
    this.threshold = threshold;
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public String getParseState() {
    return parseState;
  }

  public void setParseState(String parseState) {
    this.parseState = parseState;
  }

  public String getParseLog() {
    return parseLog;
  }

  public void setParseLog(String parseLog) {
    this.parseLog = parseLog;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  /**
   *  下发用字段
   */
  public List<String> getSids() {
    return sids;
  }

  public void setSids(List<String> sids) {
    this.sids = sids;
  }

  public SuricataRuleQueryVO getQueryVO() {
    return queryVO;
  }

  public void setQueryVO(SuricataRuleQueryVO queryVO) {
    this.queryVO = queryVO;
  }
}
