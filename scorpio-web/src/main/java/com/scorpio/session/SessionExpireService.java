package com.scorpio.session;

import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class SessionExpireService {

  private final ConcurrentMap<String, Long> sessionExpireMap = Maps.newConcurrentMap();

  public long getLastOperationTimestamp(String sessionId) {
    return sessionExpireMap.get(sessionId);
  }

  public boolean isSessionExpire(String sessionId, long currentTimestamp,
      int sessionExpiredSecond) {
    return sessionExpireMap.containsKey(sessionId)
        && currentTimestamp > (sessionExpireMap.get(sessionId) + sessionExpiredSecond * 1000L);
  }

  public void refreshLastOperationTime(String sessionId, long lastOperationTime) {
    sessionExpireMap.put(sessionId, lastOperationTime);
  }

  public void remove(String sessionId) {
    sessionExpireMap.remove(sessionId);
  }
}
