package com.scorpio.rest.vo;

import java.util.List;

public class TransmitTaskVO {

  private String name;
  private String filterStartTime;
  private String filterEndTime;
  private String filterIngestNetif;
  private String filterConditionType;
  private String filterTuple;
  private String filterBpf;
  private Integer mode;
  private Integer replayRateUnit;
  private String replayNetif;
  private Integer replayRate;
  private String description;

  private List<SixTuple> filterTupleArray;

  @Override
  public String toString() {
    return "TransmitTaskVO [name=" + name + ", filterStartTime=" + filterStartTime
        + ", filterEndTime=" + filterEndTime + ", filterIngestNetif=" + filterIngestNetif
        + ", filterConditionType=" + filterConditionType + ", filterTuple=" + filterTuple
        + ", filterBpf=" + filterBpf + ", mode=" + mode + ", replayRateUnit=" + replayRateUnit
        + ", replayNetif=" + replayNetif + ", replayRate=" + replayRate + ", description="
        + description + ", filterTupleArray=" + filterTupleArray + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilterStartTime() {
    return filterStartTime;
  }

  public void setFilterStartTime(String filterStartTime) {
    this.filterStartTime = filterStartTime;
  }

  public String getFilterEndTime() {
    return filterEndTime;
  }

  public void setFilterEndTime(String filterEndTime) {
    this.filterEndTime = filterEndTime;
  }

  public String getFilterIngestNetif() {
    return filterIngestNetif;
  }

  public void setFilterIngestNetif(String filterIngestNetif) {
    this.filterIngestNetif = filterIngestNetif;
  }

  public String getFilterConditionType() {
    return filterConditionType;
  }

  public void setFilterConditionType(String filterConditionType) {
    this.filterConditionType = filterConditionType;
  }

  public String getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(String filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterBpf() {
    return filterBpf;
  }

  public void setFilterBpf(String filterBpf) {
    this.filterBpf = filterBpf;
  }

  public Integer getMode() {
    return mode;
  }

  public void setMode(Integer mode) {
    this.mode = mode;
  }

  public Integer getReplayRateUnit() {
    return replayRateUnit;
  }

  public void setReplayRateUnit(Integer replayRateUnit) {
    this.replayRateUnit = replayRateUnit;
  }

  public String getReplayNetif() {
    return replayNetif;
  }

  public void setReplayNetif(String replayNetif) {
    this.replayNetif = replayNetif;
  }

  public Integer getReplayRate() {
    return replayRate;
  }

  public void setReplayRate(Integer replayRate) {
    this.replayRate = replayRate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<SixTuple> getFilterTupleArray() {
    return filterTupleArray;
  }

  public void setFilterTupleArray(List<SixTuple> filterTupleArray) {
    this.filterTupleArray = filterTupleArray;
  }

}
