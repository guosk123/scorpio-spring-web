package com.machloop.fpc.manager.statistics.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.statistics.dao.RrdDao;
import com.machloop.fpc.manager.statistics.dao.TimeseriesDao;
import com.machloop.fpc.manager.statistics.data.RrdDO;
import com.machloop.fpc.manager.statistics.data.TimeseriesDO;
import com.machloop.fpc.manager.statistics.service.RrdService;
import com.machloop.fpc.manager.system.data.MetricNetworkTraffic;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class RrdServiceImpl implements RrdService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RrdServiceImpl.class);

  @Autowired
  private RrdDao rrdDao;

  @Autowired
  private TimeseriesDao timeseriesDao;

  /**
   * @see com.machloop.fpc.manager.statistics.service.RrdService#queryTimeseriesDataPoint(java.util.List, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public Map<String, Long> queryTimeseriesDataPoint(List<String> rrdNameList, String interval,
      Date startTime, Date endTime) {
    Map<String, Long> rrdMap = Maps.newHashMapWithExpectedSize(rrdNameList.size());

    for (String rrdName : rrdNameList) {

      // dataPoint返回两个值分别是开始时间和结束时间对应值，这里获取结束时间对应值
      TimeseriesBO timeseries = queryRrdHistogram(rrdName,
          String.valueOf(Constants.ONE_MINUTE_SECONDS), startTime, endTime);
      long value = 0L;
      if (timeseries.getDataPoint() != null && timeseries.getDataPoint().length >= 2) {
        value = (long) timeseries.getDataPoint()[1];
      }

      // byte转换成bit
      if (StringUtils.contains(rrdName, FpcConstants.STAT_NETIF_RRD_RX_BYTEPS)
          || StringUtils.contains(rrdName, FpcConstants.STAT_NETIF_RRD_TX_BYTEPS)) {
        value = value * Constants.BYTE_BITS;
      }

      rrdMap.put(rrdName, value);
    }

    return rrdMap;
  }

  /**
   * @see com.machloop.fpc.manager.statistics.service.RrdService#queryRrdHistogram(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public TimeseriesBO queryRrdHistogram(String rrdName, String interval, Date startTime,
      Date endTime) {

    int gap = Integer.parseInt(interval) / Constants.ONE_MINUTE_SECONDS;

    TimeseriesBO timeseries = new TimeseriesBO();

    // 查询当前RRD信息，计算结果所在的cellNum
    RrdDO rrd = rrdDao.queryRrdByName(rrdName);

    LOGGER.debug(
        "start to query rrd histogram, rrd name: {}, query start time: {}, query end time: {}, rrd info: {}",
        rrdName, startTime, endTime, rrd);

    if (StringUtils.isBlank(rrd.getId())) {
      return timeseries;
    }

    // 计算起止时间相对于当前时间的距离
    int startGobackNum = (int) (rrd.getLastTime().getTime() - startTime.getTime()) / 1000
        / Constants.ONE_MINUTE_SECONDS;
    int endGobackNum = (int) (rrd.getLastTime().getTime() - endTime.getTime()) / 1000
        / Constants.ONE_MINUTE_SECONDS;

    LOGGER.debug("rrd name: {}, start time goback num: {}, end time goback num: {}", rrdName,
        startGobackNum, endGobackNum);

    if (startGobackNum < endGobackNum || startGobackNum < 0 || endGobackNum >= RRD_CAPACITY) {
      return timeseries;
    }

    // 计算起止cellNum
    Tuple2<Integer,
        Date> start = computeTruePosition(rrd.getLastTime(), rrd.getLastPosition(), startGobackNum);
    Tuple2<Integer,
        Date> end = computeTruePosition(rrd.getLastTime(), rrd.getLastPosition(), endGobackNum);

    timeseries.setStartPoint(start.getT1());
    timeseries.setStartTime(start.getT2());
    timeseries.setEndPoint(end.getT1());
    timeseries.setEndTime(end.getT2());
    LOGGER.debug(
        "rrd name: {}, timeseries start point: {}, start time: {}, end point: {}, end time: {}",
        rrdName, timeseries.getStartPoint(), timeseries.getStartTime(), timeseries.getEndPoint(),
        timeseries.getEndTime());

    // 查询起止Cell中的全部点
    List<TimeseriesDO> timeseriesList = queryTimeseries(rrdName, timeseries.getStartPoint(),
        timeseries.getEndPoint());

    // 根据起止point从起止cell中取相关点
    timeseries.setDataPoint(new double[timeseries.getDataPointNum(RRD_CAPACITY)]);

    LOGGER.debug("rrd name: {}, timeseries list size: {}, datapoint array size: {}", rrdName,
        timeseriesList.size(), timeseries.getDataPoint().length);

    int cursor = 0;
    for (int i = 0; i < timeseriesList.size(); i++) {
      // 此时timeseriesList的排序应该是按照startCell -> endCell排序
      TimeseriesDO timeseriesDO = timeseriesList.get(i);
      int beginCursor = 0;
      int endCursor = POINT_SIZE;
      if (i == 0) {
        // startCell
        int positionInCell = getPositionInCell(timeseries.getStartPoint());
        beginCursor = positionInCell - 1;
      }
      if (i == timeseriesList.size() - 1) {
        // endCell
        int positionInCell = getPositionInCell(timeseries.getEndPoint());
        endCursor = positionInCell;
      }
      int length = endCursor - beginCursor;

      try {
        System.arraycopy(timeseriesDO.getDataPoint(), beginCursor, timeseries.getDataPoint(),
            cursor, length);
        LOGGER.debug(
            "rrd name: {}, current index: {}, begin cursor: {}, end cursor: {}, length: {}",
            rrdName, i, beginCursor, endCursor, length);
      } catch (IndexOutOfBoundsException | ArrayStoreException e) {
        LOGGER.warn("startPoint: {}, endPoint: {}, timeseries: {}, timeseriesDO: {}",
            timeseries.getStartPoint(), timeseries.getEndPoint(), timeseries, timeseriesDO);
        LOGGER.warn("copy rrd failed.", e);
      }
      cursor += length;
    }

    if (gap != 1) {
      List<Double> dataPointNumList = Lists
          .newArrayListWithCapacity(timeseries.getDataPoint().length);
      int count = 0;
      double sum = 0;
      for (double dataPointNum : timeseries.getDataPoint()) {
        count++;
        sum += dataPointNum;
        if (count == gap) {
          count = 0;
          dataPointNumList.add(sum / gap);
          sum = 0;
        }
      }

      double[] dataPoints = new double[dataPointNumList.size()];
      for (int i = 0; i < dataPointNumList.size(); i++) {
        dataPoints[i] = dataPointNumList.get(i);
      }
      timeseries.setDataPoint(dataPoints);
    }
    return timeseries;
  }

  /**
   * 
   * @param rrdName
   * @param startCell
   * @param endCell
   * @return
   */
  @Override
  public List<TimeseriesDO> queryTimeseries(String rrdName, int startPoint, int endPoint) {
    List<TimeseriesDO> timeseriesList;

    int startCell = getCellNum(startPoint);
    int endCell = getCellNum(endPoint);

    boolean circle = startPoint > endPoint;

    if (startCell <= endCell && !circle) {

      List<Integer> cellNumList = Lists.newArrayListWithCapacity(endCell - startCell + 1);
      int cellNum = startCell;
      while (cellNum <= endCell) {
        cellNumList.add(cellNum);
        cellNum++;
      }
      timeseriesList = timeseriesDao.queryTimeseries(rrdName, cellNumList);

    } else {

      List<Integer> cellNumList = Lists
          .newArrayListWithCapacity(endCell + CELL_SIZE - startCell + 1);
      int cellNum = 1;
      while (cellNum <= endCell) {
        cellNumList.add(cellNum);
        cellNum++;
      }
      cellNum = startCell;
      while (cellNum <= CELL_SIZE) {
        cellNumList.add(cellNum);
        cellNum++;
      }
      timeseriesList = timeseriesDao.queryTimeseries(rrdName, cellNumList);
      timeseriesList = reOrderTimeseries(startCell, timeseriesList);

      // startPoint>endPoint，并且在同一Cell，追加结束Cell
      if (startCell == endCell) {
        timeseriesList.add(timeseriesList.get(0));
      }
    }
    return timeseriesList;
  }

  /**
   * @see com.machloop.fpc.manager.statistics.service.RrdService#produceRrdData(
   *                          com.machloop.fpc.manager.system.data.MetricNetworkTraffic, 
   *                          com.machloop.fpc.manager.system.data.MetricNetworkTraffic, 
   *                          long, java.lang.String)
   */
  @Override
  public int produceRrdData(MetricNetworkTraffic metricStatElement,
      MetricNetworkTraffic previousStatElement, long currentTime, String rrdName) {
    LOGGER.debug("start produce rrd data, rrd name: {}", rrdName);

    int update = 0;

    // 计算当前时间与last_time的差值
    long previousTime = previousStatElement.getLastTime().getTime() / 1000;
    int lastPosition = previousStatElement.getLastPosition();

    LOGGER.debug("rrd name: {}, previous time: {} ,last position: {}", rrdName,
        new Date(previousTime * 1000), lastPosition);

    // 检查前次统计记录是否存在，并检查前次统计时间是否有效
    if (currentTime - previousTime == Constants.ONE_MINUTE_SECONDS) {
      LOGGER.debug("correct interval time, continue!");

      // 计算当前data point的cell和position
      int currentPos = (lastPosition + 1) % RRD_CAPACITY;
      metricStatElement.setLastPosition(currentPos);

      // 计算当前统计值与前值的差值平均，并保存到TS_DB
      int cellNum = getCellNum(metricStatElement.getLastPosition());
      int positionInCell = getPositionInCell(metricStatElement.getLastPosition());

      update += timeseriesDao.updateTimeseriesDataPoint(rrdName, cellNum, positionInCell,
          metricStatElement.getValue());

      LOGGER.debug(
          "current position: {} , position cell num: {}, position in cell num:{}, rrd value: {}",
          currentPos, cellNum, positionInCell, metricStatElement.getValue());
    } else {
      LOGGER.debug(
          "incorrect interval time, service has bean restarted or time has bean changed, clean rrd");

      // 本次数据写入时间与上次写入时间不匹配，需要清理可能存在的垃圾数据
      update += cleanRrdDataIfNecessary(rrdName, metricStatElement, currentTime, previousTime,
          lastPosition);
    }

    // 记录当前时间和位置到RRD表
    RrdDO rrdDO = new RrdDO();
    rrdDO.setName(rrdName);
    rrdDO.setLastTime(metricStatElement.getLastTime());
    rrdDO.setLastPosition(metricStatElement.getLastPosition());
    rrdDao.updateRrd(rrdDO);

    LOGGER.debug("update rrd info success, current rrd: {}", rrdDO);
    return update;
  }

  /**
  * @see com.machloop.fpc.manager.statistics.service.RrdService#buildMetricStatElem(java.lang.String)
  */
  @Override
  public MetricNetworkTraffic buildMetricStatElem(String rrdName) {
    RrdDO rrd = rrdDao.queryRrdByName(rrdName);
    if (StringUtils.isBlank(rrd.getId())) {

      LOGGER.info("Rebuild rrd object.");

      // 初次使用，建立表结构
      rrd.setName(rrdName);
      rrd.setLastPosition(1);
      rrd.setLastTime(DateUtils.now());
      rrd = rrdDao.saveRrd(rrd);

      // 删除timeseries表
      timeseriesDao.deleteTimeseries(rrdName);

      // 重建timeseries表
      int cellNum = 1;
      while (cellNum <= CELL_SIZE) {
        timeseriesDao.saveEmptyTimeseries(rrdName, cellNum);
        cellNum++;
      }
    }

    Date lastTime = rrd.getLastTime();
    int lastPosition = rrd.getLastPosition();

    long value = timeseriesDao.queryTimeseries(rrdName, getCellNum(lastPosition),
        getPositionInCell(lastPosition));
    MetricNetworkTraffic element = new MetricNetworkTraffic(value, lastTime, lastPosition);
    LOGGER.info("Rebuild network stat element.");
    return element;
  }

  /**
   * @param startCell
   * @param timeseriesList
   * @return
   */
  private List<TimeseriesDO> reOrderTimeseries(int startCell, List<TimeseriesDO> timeseriesList) {
    List<
        TimeseriesDO> reOrderTimeseriesList = Lists.newArrayListWithCapacity(timeseriesList.size());
    List<TimeseriesDO> appendTimeseriesList = Lists.newArrayListWithCapacity(timeseriesList.size());
    for (TimeseriesDO timeseriesDO : timeseriesList) {
      if (timeseriesDO.getCellNumber() < startCell) {
        appendTimeseriesList.add(timeseriesDO);
      } else {
        reOrderTimeseriesList.add(timeseriesDO);
      }
    }
    reOrderTimeseriesList.addAll(appendTimeseriesList);
    return reOrderTimeseriesList;
  }

  /**
   * @param lastPosition
   * @param gobackNum
   */
  private Tuple2<Integer, Date> computeTruePosition(Date lastTime, int lastPosition,
      int gobackNum) {
    int point = 0;
    Date time = null;
    if (gobackNum < RRD_CAPACITY) {
      if (gobackNum < 0) {
        point = lastPosition;
      } else if (gobackNum < lastPosition) {
        point = lastPosition - gobackNum;
      } else if (gobackNum == lastPosition) {
        point = RRD_CAPACITY;
      } else {
        point = RRD_CAPACITY - (gobackNum - lastPosition);
      }
      time = new Date(lastTime.getTime() - Constants.ONE_MINUTE_SECONDS * 1000 * gobackNum);
    } else {
      point = lastPosition + 1;
      point = point > RRD_CAPACITY ? (point % RRD_CAPACITY) : point;
      time = new Date(
          lastTime.getTime() - Constants.ONE_MINUTE_SECONDS * 1000 * (RRD_CAPACITY - 1));
    }
    return Tuples.of(point, time);
  }


  /**
   * @param metricStat
   * @param currentTime
   * @param rrdName
   * @param previousTime
   * @param lastPosition
   * @return
   */
  private int cleanRrdDataIfNecessary(String rrdName, MetricNetworkTraffic metricStat,
      long currentTime, long previousTime, int lastPosition) {
    int update = 0;

    // 如果上次统计时间早于当前统计时间一个时间片，则视为无效，需要处理数据库中可能存在的脏数据
    if (currentTime > previousTime) {

      /*
       * 如果当前时间晚于last_time，需要将last_time与当前时间之间的data_point填充为0
       */

      update += cleanFowardPoint(rrdName, metricStat, currentTime, previousTime, lastPosition);

    } else if (currentTime < previousTime) {
      /*
       * 如果当前时间早于last_time（时间发生向前跳变）
       */

      update += cleanBackwardPoint(rrdName, metricStat, currentTime, previousTime, lastPosition);
    }
    // 本次统计时间与前次统计时间一致，不需要做任何事

    return update;
  }

  /**
   * @param rrdName
   * @param metricStat
   * @param currentTime
   * @param previousTime
   * @param lastPosition
   * @return
   */
  private int cleanFowardPoint(String rrdName, MetricNetworkTraffic metricStat, long currentTime,
      long previousTime, int lastPosition) {
    int update = 0;

    LOGGER.debug("start to clean foward point, rrd name: {}, current time: {}, previous time: {}",
        rrdName, new Date(currentTime * 1000), new Date(previousTime * 1000));

    int fowardPointNum = (int) ((currentTime - previousTime) / Constants.ONE_MINUTE_SECONDS);
    if (fowardPointNum >= RRD_CAPACITY) {
      // 回退超过一个循环，需要将数据清空
      update += timeseriesDao.clearTimeseries(rrdName);

      // 可以从1号位置开始使用
      metricStat.setLastPosition(1);
    } else {

      int lastCellNum = getCellNum(lastPosition);

      int currentPos = lastPosition + fowardPointNum;
      if (currentPos > RRD_CAPACITY) {
        // 当前位置发生翻转，取模计算实际位置
        currentPos = currentPos % RRD_CAPACITY;
        int currentCellNum = getCellNum(currentPos);

        clearCellAndPosition(rrdName, lastPosition, lastCellNum, currentPos, currentCellNum);

        metricStat.setLastPosition(currentPos);
      } else {
        /*
         * currentPos == RRD_CAPACITY时，当前位置在最后一个元素位置
         */

        int currentCellNum = getCellNum(currentPos);
        if (currentCellNum == lastCellNum) {
          // 清理行内数据
          clearPositionBetween(rrdName, lastCellNum, lastPosition, currentPos);
        } else if (currentCellNum > lastCellNum) {
          clearCellAndPosition(rrdName, lastPosition, lastCellNum, currentPos, currentCellNum);
        }

        metricStat.setLastPosition(currentPos);
      }
    }
    return update;
  }

  /**
   * @param rrdName
   * @param metricStat
   * @param currentTime
   * @param previousTime
   * @param lastPosition
   * @return
   */
  private int cleanBackwardPoint(String rrdName, MetricNetworkTraffic metricStat, long currentTime,
      long previousTime, int lastPosition) {
    int update = 0;

    LOGGER.debug("start to clean backward point, rrd name:{}, perviousTime: {}, currentTime: {}",
        rrdName, new Date(previousTime * 1000), new Date(currentTime * 1000));

    int currentPosition = 0;
    int lastCellNum = getCellNum(lastPosition);

    int gobackPointNum = (int) (previousTime - currentTime) / Constants.ONE_MINUTE_SECONDS;
    if (gobackPointNum > RRD_CAPACITY) {

      // 回退超过一个循环，需要将数据清空
      update += timeseriesDao.clearTimeseries(rrdName);

      // 可以从1号位置开始使用
      metricStat.setLastPosition(1);
    } else if (gobackPointNum < lastPosition) {

      /*
       * 回退在1~lastPosition之间，不转圈圈
       */

      currentPosition = lastPosition - gobackPointNum;
      int currentCellNum = getCellNum(currentPosition);

      if (currentCellNum == lastCellNum) {
        clearPositionBetween(rrdName, lastCellNum, currentPosition, lastPosition);
      } else if (currentCellNum < lastCellNum) {
        clearCellAndPosition(rrdName, currentPosition, currentCellNum, lastPosition, lastCellNum);
      }
      metricStat.setLastPosition(currentPosition);
    } else {

      /*
       * 如果gobackPointNum == lastPosition，当前位置在RRD的最后一个元素
       */

      currentPosition = RRD_CAPACITY - (gobackPointNum - lastPosition);
      int currentCellNum = getCellNum(currentPosition);

      clearCellAndPosition(rrdName, currentPosition, currentCellNum, lastPosition, lastCellNum);

      metricStat.setLastPosition(currentPosition);

    }
    return update;
  }

  /**
   * 计算指定position的cell number
   * @param position
   * @return
   */
  private int getCellNum(int position) {
    // 行号从1开始，所以需要整除后+1
    // 位置从1开始，position为POINT_SIZE时在第一行
    return (position - 1) / POINT_SIZE + 1;
  }

  /**
   * 计算指定position对应的行内下标
   * @param position
   * @return
   */
  private int getPositionInCell(int position) {
    int posInCell = position % POINT_SIZE;
    if (posInCell == 0) {
      posInCell = POINT_SIZE;
    }
    return posInCell;
  }

  /**
   * @param rrdName
   * @param startPosition
   * @param startCellNum
   * @param endPosition
   * @param endCellNum
   */
  private int clearCellAndPosition(String rrdName, int startPosition, int startCellNum,
      int endPosition, int endCellNum) {

    int update = 0;

    // 清理至当前行尾
    update += clearToCellEnd(rrdName, startPosition, startCellNum);

    // currentCell到lastCell需要整体刷新为0
    update += clearBetweenCell(rrdName, startCellNum, endCellNum);

    // currentCell当前point前的point设置为0
    update += clearFromCellHead(rrdName, endPosition, endCellNum);
    return update;
  }

  /**
   * 
   * @param rrdName
   * @param startCellNum
   * @param endCellNum
   */
  private int clearBetweenCell(String rrdName, int startCellNum, int endCellNum) {

    int update = 0;

    if (startCellNum < endCellNum) {
      int clearCellNum = startCellNum + 1;
      while (clearCellNum < endCellNum) {
        update += timeseriesDao.clearTimeseries(rrdName, clearCellNum);
        clearCellNum++;
      }
    } else if (startCellNum > endCellNum) {
      int clearCellNum = startCellNum + 1;
      while (clearCellNum <= CELL_SIZE) {
        update += timeseriesDao.clearTimeseries(rrdName, clearCellNum);
        clearCellNum++;
      }
      clearCellNum = 1;
      while (clearCellNum < endCellNum) {
        update += timeseriesDao.clearTimeseries(rrdName, clearCellNum);
        clearCellNum++;
      }
    }
    return update;
  }

  /**
   * @param rrdName
   * @param position
   * @param cellNum
   */
  private int clearFromCellHead(String rrdName, int position, int cellNum) {
    TimeseriesDO timeseries = timeseriesDao.queryTimeseries(rrdName, cellNum);
    int clearPos = 1;
    while (clearPos <= getPositionInCell(position)) {
      timeseries.getDataPoint()[clearPos - 1] = 0D; // 数组下标从0开始，但是PG的数组从1开始，所以这里要减1
      clearPos++;
    }
    return timeseriesDao.updateTimeseries(timeseries);
  }

  /**
   * @param rrdName
   * @param cellNum
   * @param startPosition
   * @param endPosition
   */
  private int clearPositionBetween(String rrdName, int cellNum, int startPosition,
      int endPosition) {
    TimeseriesDO timeseries = timeseriesDao.queryTimeseries(rrdName, cellNum);
    int clearPos = getPositionInCell(startPosition) + 1;
    while (clearPos <= getPositionInCell(endPosition)) {
      timeseries.getDataPoint()[clearPos - 1] = 0D;
      clearPos++;
    }
    return timeseriesDao.updateTimeseries(timeseries);
  }

  /**
   * @param rrdName
   * @param position
   * @param cellNum
   */
  private int clearToCellEnd(String rrdName, int position, int cellNum) {
    TimeseriesDO timeseries = timeseriesDao.queryTimeseries(rrdName, cellNum);
    int clearPos = getPositionInCell(position) + 1;
    while (clearPos <= POINT_SIZE) {
      timeseries.getDataPoint()[clearPos - 1] = 0D;
      clearPos++;
    }
    return timeseriesDao.updateTimeseries(timeseries);
  }
}
