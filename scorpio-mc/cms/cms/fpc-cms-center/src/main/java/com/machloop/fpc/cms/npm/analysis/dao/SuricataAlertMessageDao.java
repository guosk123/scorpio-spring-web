package com.machloop.fpc.cms.npm.analysis.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.npm.analysis.data.SuricataAlertMessageDO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface SuricataAlertMessageDao {

  Page<SuricataAlertMessageDO> querySuricataAlerts(Pageable page, SuricataRuleQueryVO queryVO);

  List<Map<String, Object>> statisticsMitreAttack(SuricataRuleQueryVO queryVO);

  Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
      PageRequest page);

  List<Map<String, Object>> queryAlterMessagesRelation(String ip, Date startTimeDate,
      Date endTimeDate);

  List<Map<String, Object>> querySuricataAlertsWithoutTotal(PageRequest page,
      SuricataRuleQueryVO queryVO);

  List<Object> querySuricataAlertFlowIds(SuricataRuleQueryVO queryVO, Sort sort, int size);

  List<Object> querySuricataTopHundredFlowIds(String sid, Date startTimeDate, Date endTimeDate);

  long SuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO);
}
