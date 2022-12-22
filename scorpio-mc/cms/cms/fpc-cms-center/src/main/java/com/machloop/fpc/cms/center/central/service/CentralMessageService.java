package com.machloop.fpc.cms.center.central.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.webapp.system.bo.AlarmCountBO;
import com.machloop.alpha.webapp.system.bo.LogBO;
import com.machloop.alpha.webapp.system.bo.AlarmBO;
import com.machloop.alpha.webapp.system.data.RoleDO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;

public interface CentralMessageService {

  Page<LogBO> queryLogs(Pageable page, List<RoleDO> roleList, LogQueryVO query, String nodeType);

  Page<AlarmBO> queryAlarms(Pageable page, AlarmQueryVO query, String nodeType);

  List<AlarmCountBO> countAlarmsGroupByLevelWithoutCms();

}
