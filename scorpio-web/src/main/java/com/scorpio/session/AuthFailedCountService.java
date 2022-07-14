package com.scorpio.session;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.scorpio.util.DateUtils;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class AuthFailedCountService {

  private static Map<String, Tuple2<Integer, Long>> authFailedCountMap = Maps.newHashMap();

  public Tuple2<Integer, Long> getFailedCount(String username) {
    return authFailedCountMap.get(username);
  }

  public Tuple2<Integer, Long> authFail(String username) {
    synchronized (this) {
      int count = 1;
      Tuple2<Integer, Long> tuple = authFailedCountMap.get(username);
      if (tuple != null) {
        count = tuple.getT1() + 1;
      }
      tuple = Tuples.of(count, DateUtils.now().getTime());
      authFailedCountMap.put(username, tuple);
      return tuple;
    }
  }

  public void reset(String username) {
    synchronized (this) {
      authFailedCountMap.remove(username);
    }
  }
}
