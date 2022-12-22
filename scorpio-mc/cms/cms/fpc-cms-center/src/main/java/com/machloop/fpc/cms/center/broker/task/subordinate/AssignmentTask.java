package com.machloop.fpc.cms.center.broker.task.subordinate;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author liyongjun
 *
 * create at 2020年1月2日, fpc-cms-center
 */
@Component
public class AssignmentTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentTask.class);

  @Autowired
  private AssignmentService assignmentService;

  @PostConstruct
  private void init() {
    new Thread(() -> {
      while (!GracefulShutdownHelper.isShutdownNow()) {
        assignmentService.assignmentTask();
      }
    }).start();
  }

  @Scheduled(fixedRateString = "${task.assignment.execute.schedule.fixedrate.ms}")
  private void run() {
    LOGGER.debug("start execute assignment task.");

    // 执行任务下发
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    LOGGER.debug("end execute assignment task.");
  }
}
