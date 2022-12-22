package com.machloop.fpc.cms.center.central.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.center.central.service.ClusterService;

/**
 * @author guosk
 *
 * create at 2022年2月9日, fpc-cms-center
 */
@Component
public class FpcSyncRemoteServerTask {

  @Autowired
  private ClusterService clusterService;

  @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {
    clusterService.queryAbnormalNodesAndRefresh();
  }

}
