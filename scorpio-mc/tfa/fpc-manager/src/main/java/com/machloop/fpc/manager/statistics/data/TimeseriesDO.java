package com.machloop.fpc.manager.statistics.data;

import java.util.Arrays;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author liumeng
 *
 * create at 2018年12月19日, fpc-manager
 */
public class TimeseriesDO extends BaseDO {

  private String rrdName;
  private int cellNumber;

  private double[] dataPoint;

  @Override
  public String toString() {
    return "TimeseriesDO [rrdName=" + rrdName + ", cellNumber=" + cellNumber + ", dataPoint="
        + Arrays.toString(dataPoint) + ", toString()=" + super.toString() + "]";
  }

  public String getRrdName() {
    return rrdName;
  }

  public void setRrdName(String rrdName) {
    this.rrdName = rrdName;
  }


  public int getCellNumber() {
    return cellNumber;
  }

  public void setCellNumber(int cellNumber) {
    this.cellNumber = cellNumber;
  }

  public double[] getDataPoint() {
    return dataPoint;
  }

  public void setDataPoint(double[] dataPoint) {
    this.dataPoint = dataPoint;
  }


}
