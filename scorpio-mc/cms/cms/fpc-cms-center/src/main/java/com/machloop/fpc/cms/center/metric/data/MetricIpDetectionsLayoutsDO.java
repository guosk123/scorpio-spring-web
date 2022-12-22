package com.machloop.fpc.cms.center.metric.data;

import java.util.Date;

/**
 * @author ChenXiao
 * create at 2022/11/25
 */
public class MetricIpDetectionsLayoutsDO {

  private String id;

  private String layouts;

  private Date createTime;

  private Date updateTime;

  private String operatorId;


  @Override
  public String toString() {
    return "MetricIpDetectionsLayoutsDO{" + "id='" + id + '\'' + ", layouts='" + layouts + '\''
        + ", createTime=" + createTime + ", updateTime=" + updateTime + ", operatorId='"
        + operatorId + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLayouts() {
    return layouts;
  }

  public void setLayouts(String layouts) {
    this.layouts = layouts;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
