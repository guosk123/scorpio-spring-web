package com.machloop.fpc.cms.center.knowledge.bo;

import java.util.Date;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月21日, fpc-manager
 */
public class SaKnowledgeInfoBO implements LogAudit {
  private String version;
  private Date releaseDate;
  private Date importDate;

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("导入SA规则库文件：");
        break;
      default:
        return "";
    }

    builder.append("导入时间=").append(DateUtils.toStringISO8601(importDate)).append(";");
    builder.append("发布时间=").append(DateUtils.toStringISO8601(releaseDate)).append(";");
    builder.append("版本号=").append(version).append(";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "SaKnowledgeBO [version=" + version + ", releaseDate=" + releaseDate + ", importDate="
        + importDate + "]";
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public Date getImportDate() {
    return importDate;
  }

  public void setImportDate(Date importDate) {
    this.importDate = importDate;
  }
}
