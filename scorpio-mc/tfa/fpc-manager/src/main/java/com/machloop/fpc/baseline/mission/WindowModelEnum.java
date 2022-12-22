package com.machloop.fpc.baseline.mission;

import org.apache.commons.lang3.StringUtils;

/**
 * @author guosk
 *
 * create at 2021年5月13日, fpc-manager
 */
public enum WindowModelEnum {

  MINUTE_OF_DAY("minute_of_day"),
  FIVE_MINUTE_OF_DAY("five_minute_of_day"),
  HOUR_OF_DAY("hour_of_day"),
  MINUTE_OF_WEEK("minute_of_week"),
  FIVE_MINUTE_OF_WEEK("five_minute_of_week"),
  HOUR_OF_WEEK("hour_of_week"),
  LAST_N_MINUTES("last_n_minutes"),
  LAST_N_FIVE_MINUTES("last_n_five_minutes"),
  LAST_N_HOURS("last_n_hours"),
  LAST_N_HOURS_ALERT("last_n_hours_alert");

  private String value;

  WindowModelEnum(String value) {
    this.value = value;
  }

  public static WindowModelEnum getEnumByValue(String value) {
    for(WindowModelEnum item :WindowModelEnum.values()) {
      if(StringUtils.equals(value, item.getValue())) {
        return item;
      }
    }

    throw new IllegalArgumentException("No enum constant " + value);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
