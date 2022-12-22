package com.machloop.fpc.manager.global.dao;

import java.util.Map;

import com.machloop.fpc.manager.global.data.CounterQuery;

/**
 * @author guosk
 *
 * create at 2022年3月28日, fpc-manager
 */
public interface CounterDao {

  public static final String FLOW_LOG = "flow_log";
  public static final String DHCP = "DHCP";
  public static final String ICMP = "ICMP";
  public static final String MAIL = "MAIL";

  boolean onlyBaseFilter(String sourceType, String dsl, String dataType);

  long countFlowLogs(String queryId, CounterQuery queryVO);

  long countProtocolLogRecord(CounterQuery queryVO, String protocol);

  Map<String, Long> countProtocolLogRecords(CounterQuery queryVO);

}
