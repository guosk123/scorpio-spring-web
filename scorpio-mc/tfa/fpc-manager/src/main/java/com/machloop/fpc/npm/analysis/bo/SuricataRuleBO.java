package com.machloop.fpc.npm.analysis.bo;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
public class SuricataRuleBO implements LogAudit {

  private String id;
  private String suricataRuleInCmsId;
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
  private Integer priority;
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
  private String createTime;
  private String updateTime;

  private String signatureSeverityText;
  private String parseStateText;
  private String sourceText;

  /**
   * 接收下发所用
   */
  private List<String> sids;
  private SuricataRuleQueryVO queryVO;

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加安全分析规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改安全分析规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除安全分析规则：");
        break;
      default:
        return "";
    }
    builder.append("sid=").append(sid).append(";");
    builder.append("动作=").append(action).append(";");
    builder.append("协议=").append(protocol).append(";");
    builder.append("源IP（组）=").append(srcIp).append(";");
    builder.append("源端口（组）=").append(srcPort).append(";");
    builder.append("方向=").append(direction).append(";");
    builder.append("目的IP（组）=").append(destIp).append(";");
    builder.append("目的端口（组）=").append(destPort).append(";");
    builder.append("描述信息=").append(msg).append(";");
    builder.append("规则正文=").append(content).append(";");
    builder.append("优先级=").append(priority).append(";");
    builder.append("规则分类=").append(classtypeId).append(";");
    builder.append("战术分类=").append(mitreTacticId).append(";");
    builder.append("技术分类=").append(mitreTechniqueId).append(";");
    builder.append("CVE编号=").append(cve).append(";");
    builder.append("CNNVD编号=").append(cnnvd).append(";");
    builder.append("严重级别=").append(signatureSeverity).append(";");
    builder.append("受害方=").append(target).append(";");
    builder.append("频率=").append(threshold).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "SuricataRuleBO [id=" + id + ", suricataRuleInCmsId=" + suricataRuleInCmsId + ", sid="
        + sid + ", action=" + action + ", protocol=" + protocol + ", srcIp=" + srcIp + ", srcPort="
        + srcPort + ", direction=" + direction + ", destIp=" + destIp + ", destPort=" + destPort
        + ", msg=" + msg + ", rev=" + rev + ", content=" + content + ", priority=" + priority
        + ", classtypeId=" + classtypeId + ", mitreTacticId=" + mitreTacticId
        + ", mitreTechniqueId=" + mitreTechniqueId + ", cve=" + cve + ", cnnvd=" + cnnvd
        + ", signatureSeverity=" + signatureSeverity + ", target=" + target + ", threshold="
        + threshold + ", rule=" + rule + ", parseState=" + parseState + ", parseLog=" + parseLog
        + ", state=" + state + ", source=" + source + ", createTime=" + createTime + ", updateTime="
        + updateTime + ", signatureSeverityText=" + signatureSeverityText + ", parseStateText="
        + parseStateText + ", sourceText=" + sourceText + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSuricataRuleInCmsId() {
    return suricataRuleInCmsId;
  }

  public void setSuricataRuleInCmsId(String suricataRuleInCmsId) {
    this.suricataRuleInCmsId = suricataRuleInCmsId;
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

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
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

  public String getParseStateText() {
    return parseStateText;
  }

  public void setParseStateText(String parseStateText) {
    this.parseStateText = parseStateText;
  }

  public String getSourceText() {
    return sourceText;
  }

  public void setSourceText(String sourceText) {
    this.sourceText = sourceText;
  }

  public String getSignatureSeverityText() {
    return signatureSeverityText;
  }

  public void setSignatureSeverityText(String signatureSeverityText) {
    this.signatureSeverityText = signatureSeverityText;
  }

  /**
   * 接收下发规则
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
