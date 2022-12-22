package com.machloop.fpc.cms.center.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.global.data.CounterQuery;
import com.machloop.fpc.cms.center.handler.QueryTaskRejectedExecutionHandler;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.service.FlowLogDataService;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月16日, fpc-manager
 */
@Service
public class FlowLogDataServiceImpl implements FlowLogDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogDataServiceImpl.class);

  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 100, 3,
      TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new BasicThreadFactory.Builder()
          .namingPattern("count-protocol-task").uncaughtExceptionHandler((t, e) -> {
            LOGGER.warn("failed to execute count protocol record task, thread is: {}", t.getId(),
                e);
          }).build(),
      new QueryTaskRejectedExecutionHandler());

  @Autowired
  private Map<String, LogRecordDao<?>> logRecordDaoMap;

  @Autowired
  private CounterDao counterDao;

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.FlowLogDataService#countFlowLogDataGroupByProtocol(com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO)
   */
  @Override
  public Map<String, Long> countFlowLogDataGroupByProtocol(LogCountQueryVO queryVO) {
    Map<String, Long> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(queryVO.getSrcIp())
        || StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)) {
      List<Future<Map<String, Long>>> futures = Lists
          .newArrayListWithCapacity(logRecordDaoMap.size());
      for (Entry<String, LogRecordDao<?>> logRecordDao : logRecordDaoMap.entrySet()) {
        Future<Map<String, Long>> future = executor.submit(new Callable<Map<String, Long>>() {
          @Override
          public Map<String, Long> call() throws Exception {
            return logRecordDao.getValue().countLogRecords(queryVO);
          }
        });
        futures.add(future);
      }

      futures.forEach(future -> {
        try {
          resultMap.putAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          LOGGER.warn("failed to execute count protocol record task", e);
        }
      });
    } else {
      CounterQuery counterQuery = new CounterQuery();
      BeanUtils.copyProperties(queryVO, counterQuery);
      resultMap.putAll(counterDao.countProtocolLogRecords(counterQuery));
    }

    resultMap.put("TOTAL", resultMap.values().stream().mapToLong(item -> item).sum());

    return resultMap;
  }

}
