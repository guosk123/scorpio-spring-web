package com.machloop.fpc.manager.system.service;

import java.util.List;

import com.machloop.alpha.common.metric.system.data.MonitorRaid;

public interface DeviceRaidService {

  List<MonitorRaid> monitorRaidState();
  
}
