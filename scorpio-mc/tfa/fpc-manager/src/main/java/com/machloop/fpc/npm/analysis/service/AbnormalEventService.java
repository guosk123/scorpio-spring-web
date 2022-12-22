package com.machloop.fpc.npm.analysis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventBO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public interface AbnormalEventService {

  Page<AbnormalEventBO> queryAbnormalEvents(Pageable page, AbnormalEventQueryVO queryVO);

  List<AbnormalEventBO> queryAbnormalEvents(AbnormalEventQueryVO queryVO);

  List<Map<String, Object>> countAbnormalEvent(Date startTime, Date endTime, String metricType,
      int count);

}
