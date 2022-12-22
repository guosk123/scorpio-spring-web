package com.machloop.fpc.cms.baseline.mission;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
public class Metric {

  private boolean ratio;

  private String numeratorMetric;
  private String numeratorSourceType;
  private String numeratorSourceValue;

  private String denominatorMetric;
  private String denominatorSourceType;
  private String denominatorSourceValue;

  // 常量分母 与 (denominatorMetric,denominatorSourceType,denominatorSourceValue) 互斥
  private Integer denominatorConstants;

  public Metric(boolean ratio, String numeratorMetric, String numeratorSourceType,
      String numeratorSourceValue, String denominatorMetric, String denominatorSourceType,
      String denominatorSourceValue, Integer denominatorConstants) {
    super();
    this.ratio = ratio;
    this.numeratorMetric = numeratorMetric;
    this.numeratorSourceType = numeratorSourceType;
    this.numeratorSourceValue = numeratorSourceValue;
    this.denominatorMetric = denominatorMetric;
    this.denominatorSourceType = denominatorSourceType;
    this.denominatorSourceValue = denominatorSourceValue;
    this.denominatorConstants = denominatorConstants;
  }

  @Override
  public String toString() {
    return "Metric [ratio=" + ratio + ", numeratorMetric=" + numeratorMetric
        + ", numeratorSourceType=" + numeratorSourceType + ", numeratorSourceValue="
        + numeratorSourceValue + ", denominatorMetric=" + denominatorMetric
        + ", denominatorSourceType=" + denominatorSourceType + ", denominatorSourceValue="
        + denominatorSourceValue + ", denominatorConstants=" + denominatorConstants + "]";
  }

  public boolean isRatio() {
    return ratio;
  }

  public String getNumeratorMetric() {
    return numeratorMetric;
  }

  public String getNumeratorSourceType() {
    return numeratorSourceType;
  }

  public String getNumeratorSourceValue() {
    return numeratorSourceValue;
  }

  public String getDenominatorMetric() {
    return denominatorMetric;
  }

  public String getDenominatorSourceType() {
    return denominatorSourceType;
  }

  public String getDenominatorSourceValue() {
    return denominatorSourceValue;
  }

  public Integer getDenominatorConstants() {
    return denominatorConstants;
  }

}
