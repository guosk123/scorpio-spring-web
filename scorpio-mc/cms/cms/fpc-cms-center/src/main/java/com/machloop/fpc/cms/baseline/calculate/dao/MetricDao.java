package com.machloop.fpc.cms.baseline.calculate.dao;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import reactor.util.function.Tuple2;

/**
 * 
 * @author guosk
 *
 * create at 2021年9月16日, fpc-cms-center
 */
public interface MetricDao {

  long[] queryMetrics(String tableName, String field, Map<String, Object> params,
      List<Tuple2<Date, Date>> timeRanges) throws IOException;
}
