package com.machloop.fpc.npm.analysis.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.npm.analysis.data.SuricataAlertMessageDO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author guosk
 * <p>
 * create at 2022年4月8日, fpc-manager
 */
public interface SuricataAlertMessageDao {

    Page<SuricataAlertMessageDO> querySuricataAlerts(Pageable page, SuricataRuleQueryVO queryVO);

    List<Map<String, Object>> querySuricataAlertsWithoutTotal(Pageable page,
                                                              SuricataRuleQueryVO queryVO);

    List<Map<String, Object>> statisticsMitreAttack(SuricataRuleQueryVO queryVO);


    Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
                                                                PageRequest page);

    List<Map<String, Object>> queryAlterMessagesRelation(String ip, Date startTimeDate,
                                                         Date endTimeDate);

    List<Object> querySuricataTopHundredFlowIds(Integer sid, Date startTimeDate, Date endTimeDate);

    List<Object> querySuricataAlertFlowIds(SuricataRuleQueryVO queryVO, Sort sort, int size);

    long SuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO);
}
