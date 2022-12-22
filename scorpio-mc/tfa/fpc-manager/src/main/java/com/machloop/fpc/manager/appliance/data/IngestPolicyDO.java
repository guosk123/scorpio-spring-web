package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class IngestPolicyDO extends BaseOperateDO {

  private String name;
  private String defaultAction;
  private String deduplication;
  private String exceptBpf;
  private String exceptTuple;
  private String ingestPolicyInCmsId;
  private String description;

  @Override
  public String toString() {
    return "IngestPolicyDO [name=" + name + ", defaultAction=" + defaultAction + ", deduplication="
        + deduplication + ", exceptBpf=" + exceptBpf + ", exceptTuple=" + exceptTuple
        + ", ingestPolicyInCmsId=" + ingestPolicyInCmsId + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  public String getDeduplication() {
    return deduplication;
  }

  public void setDeduplication(String deduplication) {
    this.deduplication = deduplication;
  }

  public String getExceptBpf() {
    return exceptBpf;
  }

  public void setExceptBpf(String exceptBpf) {
    this.exceptBpf = exceptBpf;
  }

  public String getExceptTuple() {
    return exceptTuple;
  }

  public void setExceptTuple(String exceptTuple) {
    this.exceptTuple = exceptTuple;
  }

  public String getIngestPolicyInCmsId() {
    return ingestPolicyInCmsId;
  }

  public void setIngestPolicyInCmsId(String ingestPolicyInCmsId) {
    this.ingestPolicyInCmsId = ingestPolicyInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
