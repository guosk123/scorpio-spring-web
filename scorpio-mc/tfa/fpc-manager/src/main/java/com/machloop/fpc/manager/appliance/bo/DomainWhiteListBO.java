package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/8 1:42 PM,cms
 * @version 1.0
 */
public class DomainWhiteListBO implements LogAudit {

  private String id;
  private String domainWhiteListInCmsId;
  private String name;
  private String domain;
  private String description;
  private String createTime;
  private String updateTime;
  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("新建域名白名单：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改域名白名单：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除域名白名单：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("域名白名单=").append(domain).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "DomainWhiteListBO{" + "id='" + id + '\'' + ", domainWhiteListInCmsId='"
        + domainWhiteListInCmsId + '\'' + ", name='" + name + '\'' + ", domain='" + domain + '\''
        + ", description='" + description + '\'' + ", createTime='" + createTime + '\''
        + ", updateTime='" + updateTime + '\'' + ", operatorId='" + operatorId + '\'' + '}';
  }

  public String getDomainWhiteListInCmsId() {
    return domainWhiteListInCmsId;
  }

  public void setDomainWhiteListInCmsId(String domainWhiteListInCmsId) {
    this.domainWhiteListInCmsId = domainWhiteListInCmsId;
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
