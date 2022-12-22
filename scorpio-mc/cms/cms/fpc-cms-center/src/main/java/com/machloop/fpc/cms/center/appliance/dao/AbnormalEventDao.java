package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.AbnormalEventDO;
import com.machloop.fpc.cms.center.appliance.vo.AbnormalEventQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public interface AbnormalEventDao {

  Page<AbnormalEventDO> queryAbnormalEvents(Pageable page, AbnormalEventQueryVO queryVO);

  List<AbnormalEventDO> queryAbnormalEvents(AbnormalEventQueryVO queryVO, int size);

  List<Map<String, Object>> countAbnormalEvent(Date startTime, Date endTime, String metricType,
      int count);

}
