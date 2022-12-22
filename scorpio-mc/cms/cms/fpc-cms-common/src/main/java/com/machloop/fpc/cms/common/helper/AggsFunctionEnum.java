package com.machloop.fpc.cms.common.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * @author guosk
 *
 * create at 2021年5月26日, fpc-common
 */
public enum AggsFunctionEnum {

  MIN("MIN"),
  MAX("MAX"),
  MEAN("MEAN"),
  MEDIAN("MEDIAN"),
  SUM("SUM"),
  COUNT("COUNT"),
  AVG("AVG"),
  PLUS("plus"),
  DIVIDE("divide"),
  UNIQ_ARRAY("uniqArray");

  private final String operation;

  AggsFunctionEnum(String operation) {
    this.operation = operation;
  }

  public String getOperation() {
    return operation;
  }

  @Override
  public String toString() {
    return operation;
  }

  @Nullable
  public static AggsFunctionEnum getEnumByValue(String operation) {
    for (AggsFunctionEnum item : values()) {
      if (StringUtils.equals(operation, item.operation)) {
        return item;
      }
    }

    throw new IllegalArgumentException("No enum constant " + operation);
  }
}
