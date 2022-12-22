package com.machloop.fpc.manager.global.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.global.dao.SlowQueryDao;
import com.machloop.fpc.manager.global.service.SlowQueryService;


/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
@Service
public class SlowQueryServiceImpl implements SlowQueryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlowQueryServiceImpl.class);

  private static final Map<String, Long> heartbeatMap = Maps.newConcurrentMap();

  @Value("${global.death.query.expire.ms}")
  private int expire;

  @Autowired
  private SlowQueryDao slowQueryDao;

  /**
   * @see com.machloop.fpc.manager.global.service.SlowQueryService#heartbeat(java.util.List)
   */
  @Override
  public void heartbeat(List<String> queryIds) {
    long current = System.currentTimeMillis();
    for (String id : queryIds) {
      if (StringUtils.isNotBlank(id)) {
        Long old = heartbeatMap.get(id);
        // 查询已正常完成，忽略本次心跳
        if (old != null && old == -1) {
          continue;
        }
        heartbeatMap.put(id, current);
        LOGGER.debug("success to heartbeat query, query id: [{}]", id);
      }
    }
  }

  /**
   * @see com.machloop.fpc.manager.global.service.SlowQueryService#finish(java.lang.String)
   */
  @Override
  public void finish(String queryId) {
    if (StringUtils.isNotBlank(queryId)) {
      heartbeatMap.put(queryId, -1L);
      LOGGER.debug("success to finish query, query id: [{}]", queryId);
    }
  }

  /**
   * @see com.machloop.fpc.manager.global.service.SlowQueryService#cancelDeathQueries()
   */
  @Override
  public void cancelQueries(List<String> queryIds) {
    long current = System.currentTimeMillis();

    if (CollectionUtils.isNotEmpty(queryIds)) {
      slowQueryDao.cancelQueries(queryIds);
      // 页面主动发起的取消查询，取消当前查询后不立即移除心跳，由心跳超时检测任务移除心跳（可避免相同queryId包含多个有先后顺序的查询，导致后续查询无法被取消，消耗资源）
      queryIds.forEach(id -> {
        if (StringUtils.isNotBlank(id)) {
          heartbeatMap.put(id, current - expire);
        }
      });
      LOGGER.debug("success to cancel query, query id: [{}]", StringUtils.join(queryIds, ", "));
    }
  }

  /**
   * @see com.machloop.fpc.manager.global.service.SlowQueryService#cancelDeathQueries()
   */
  @Override
  public void cancelDeathQueries() {
    long current = System.currentTimeMillis();
    List<String> deathQueryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (Entry<String, Long> entry : heartbeatMap.entrySet()) {
      if (current - entry.getValue() > expire) {
        // 长时间未心跳
        deathQueryIds.add(entry.getKey());
      } else if (entry.getValue() == -1) {
        // 已正常完成的查询，从map中清除
        heartbeatMap.remove(entry.getKey());
      }
    }
    if (CollectionUtils.isNotEmpty(deathQueryIds)) {
      slowQueryDao.cancelQueries(deathQueryIds);
      deathQueryIds.forEach(id -> {
        if (StringUtils.isNotBlank(id)) {
          heartbeatMap.remove(id);
        }
      });
      LOGGER.info("found death query, finish to kill, query id: [{}]",
          StringUtils.join(deathQueryIds, ", "));
    }
  }
}
