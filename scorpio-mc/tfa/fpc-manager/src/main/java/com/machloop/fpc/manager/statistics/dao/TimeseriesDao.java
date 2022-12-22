package com.machloop.fpc.manager.statistics.dao;

import java.util.List;

import com.machloop.fpc.manager.statistics.data.TimeseriesDO;

/**
 * @author liumeng
 *
 * create at 2018年12月19日, fpc-manager
 */
public interface TimeseriesDao {

  TimeseriesDO queryTimeseries(String rrdName, int cellNum);

  List<TimeseriesDO> queryTimeseries(String rrdName, List<Integer> cellNumList);

  long queryTimeseries(String rrdName, int cellNum, int position);

  TimeseriesDO saveEmptyTimeseries(String rrdName, int cellNum);

  int updateTimeseries(TimeseriesDO timeseriesDO);

  int updateTimeseriesDataPoint(String rrdName, int cellNum, int posInCell, long value);

  int clearTimeseries(String rrdName);

  int clearTimeseries(String rrdName, int cellNum);

  int deleteTimeseries(String rrdName);
}
