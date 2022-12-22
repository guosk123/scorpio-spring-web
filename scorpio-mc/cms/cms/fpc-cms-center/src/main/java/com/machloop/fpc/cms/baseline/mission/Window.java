package com.machloop.fpc.cms.baseline.mission;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class Window {
  private int period;
  private int precision;
  private int bucketSeconds;
  private int bucketGapSeconds;
  private long[] deltaBuckets;

  public Window(int size, int precision, int bucketSeconds, int bucketGapSconds) {
    this.precision = precision;
    this.period = precision;
    this.bucketSeconds = bucketSeconds;
    this.bucketGapSeconds = bucketGapSconds;

    deltaBuckets = new long[size];
    long deltaMillis = 0;
    for (int i = size - 1; i >= 0; i--) {
      deltaMillis = deltaMillis + bucketGapSconds * 1000;
      deltaBuckets[i] = deltaMillis;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    getRanges().forEach(range -> sb.append("(").append(DateUtils.toStringISO8601(range.getT1()))
        .append(" ").append(DateUtils.toStringISO8601(range.getT2())).append("),"));
    return sb.toString();
  }

  public int getBucketSeconds() {
    return bucketSeconds;
  }

  public int getBucketGapSeconds() {
    return bucketGapSeconds;
  }

  public int getPeriod() {
    return period;
  }

  public List<Tuple2<Date, Date>> getRanges() {
    long precisionCurrent = getConrrentWindowTime().getTime();
    List<Tuple2<Date, Date>> ranges = Lists.newArrayListWithCapacity(deltaBuckets.length);
    for (long bucket : deltaBuckets) {
      long end = precisionCurrent - bucket;
      long start = end - bucketSeconds * 1000;
      ranges.add(Tuples.of(new Date(start), new Date(end)));
    }
    return ranges;
  }

  public Date getConrrentWindowTime() {
    long current = System.currentTimeMillis();
    long precisionCurrent = current / (precision * TimeUnit.SECONDS.toMillis(1))
        * (precision * TimeUnit.SECONDS.toMillis(1));
    return new Date(precisionCurrent);
  }

  /**
   * 分钟环比
   * @param size 窗口大小
   * @return 
   */
  public static Window newMinuteOnMinuteWindow(int size) {
    return new Window(size, Constants.ONE_MINUTE_SECONDS, Constants.ONE_MINUTE_SECONDS,
        Constants.ONE_MINUTE_SECONDS);
  }

  /**
   * 5分钟环比
   * @param size 窗口大小
   * @return
   */
  public static Window newFiveMinuteOnFiveMinuteWindow(int size) {
    return new Window(size, Constants.FIVE_MINUTE_SECONDS, Constants.FIVE_MINUTE_SECONDS,
        Constants.FIVE_MINUTE_SECONDS);
  }

  /**
   * 小时环比
   * @param size 窗口大小
   * @return 
   */
  public static Window newHourOnHourWindow(int size) {
    return new Window(size, Constants.ONE_HOUR_SECONDS, Constants.ONE_HOUR_SECONDS,
        Constants.ONE_HOUR_SECONDS);
  }

  /**
   * 分钟天同比
   * @param size 窗口大小
   * @return 
   */
  public static Window newMinuteOnDayWindow(int size) {
    return new Window(size, Constants.ONE_MINUTE_SECONDS, Constants.ONE_MINUTE_SECONDS,
        Constants.ONE_DAY_SECONDS);
  }

  /**
   * 5分钟天同比
   * @param size 窗口大小
   * @return 
   */
  public static Window newFiveMinuteOnDayWindow(int size) {
    return new Window(size, Constants.FIVE_MINUTE_SECONDS, Constants.FIVE_MINUTE_SECONDS,
        Constants.ONE_DAY_SECONDS);
  }

  /**
   * 小时天同比
   * @param size 窗口大小
   * @return 
   */
  public static Window newHourOnDayWindow(int size) {
    return new Window(size, Constants.ONE_HOUR_SECONDS, Constants.ONE_HOUR_SECONDS,
        Constants.ONE_DAY_SECONDS);
  }

  /**
   * 分钟周同比
   * @param size
   * @return
   */
  public static Window newMinuteOnWeekWindow(int size) {
    return new Window(size, Constants.ONE_MINUTE_SECONDS, Constants.ONE_MINUTE_SECONDS,
        (int) TimeUnit.DAYS.toSeconds(7));
  }

  /**
   * 5分钟周同比
   * @param size
   * @return
   */
  public static Window newFiveMinuteOnWeekWindow(int size) {
    return new Window(size, Constants.FIVE_MINUTE_SECONDS, Constants.FIVE_MINUTE_SECONDS,
        (int) TimeUnit.DAYS.toSeconds(7));
  }

  /**
   * 小时周同比
   * @param size
   * @return
   */
  public static Window newHourOnWeekWindow(int size) {
    return new Window(size, Constants.ONE_HOUR_SECONDS, Constants.ONE_HOUR_SECONDS,
        (int) TimeUnit.DAYS.toSeconds(7));
  }

  /**
   * 根据windowModel创建窗口
   * @param size
   * @param windowModel
   * @return
   */
  public static Window createWindow(int size, WindowModelEnum windowModel) {
    Window window = null;
    switch (windowModel) {
      case MINUTE_OF_DAY:
        window = newMinuteOnDayWindow(size);
        break;
      case FIVE_MINUTE_OF_DAY:
        window = newFiveMinuteOnDayWindow(size);
        break;
      case HOUR_OF_DAY:
        window = newHourOnDayWindow(size);
        break;
      case MINUTE_OF_WEEK:
        window = newMinuteOnWeekWindow(size);
        break;
      case FIVE_MINUTE_OF_WEEK:
        window = newFiveMinuteOnWeekWindow(size);
        break;
      case HOUR_OF_WEEK:
        window = newHourOnWeekWindow(size);
        break;
      case LAST_N_MINUTES:
        window = newMinuteOnMinuteWindow(size);
        break;
      case LAST_N_FIVE_MINUTES:
        window = newFiveMinuteOnFiveMinuteWindow(size);
        break;
      case LAST_N_HOURS:
        window = newHourOnHourWindow(size);
        break;
      default:
        throw new IllegalArgumentException("unsupport window model, failed to create window");
    }
    return window;
  }
}
