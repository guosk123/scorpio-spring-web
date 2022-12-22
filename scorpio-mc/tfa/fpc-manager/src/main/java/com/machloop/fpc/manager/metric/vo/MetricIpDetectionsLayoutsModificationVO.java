package com.machloop.fpc.manager.metric.vo;

/**
 * @author ChenXiao
 * create at 2022/11/15
 */
public class MetricIpDetectionsLayoutsModificationVO {


  private String layouts;


  @Override
  public String toString() {
    return "MetricIpDetectionsLayoutsModificationVO{" + "layouts='" + layouts + '\'' + '}';
  }

  public String getLayouts() {
    return layouts;
  }

  public void setLayouts(String layouts) {
    this.layouts = layouts;
  }
}
