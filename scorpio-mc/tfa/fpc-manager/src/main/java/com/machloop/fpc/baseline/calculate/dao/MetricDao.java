package com.machloop.fpc.baseline.calculate.dao;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import reactor.util.function.Tuple2;

/**
 * @author fengtianyou
 * 
 * create at 2021年9月13日, fpc-baseline
 */
public interface MetricDao {

  long[] queryMetrics(String indexName, String field, Map<String, Object> params,
      List<Tuple2<Date, Date>> timeRanges) throws IOException;
}
