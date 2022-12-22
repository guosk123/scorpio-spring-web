package com.machloop.fpc.cms.npm.analysis.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataAlertMessageBO;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleRelationBO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface SuricataAlertMessageService {

  Page<SuricataAlertMessageBO> querySuricataAlerts(Pageable page, SuricataRuleQueryVO queryVO);

  void exportSuricataAlerts(SuricataRuleQueryVO queryVO, String fileType, Sort sort,
      OutputStream out, int count) throws IOException;

  Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
      PageRequest page);

  List<SuricataRuleRelationBO> queryAlterMessagesRelation(String destIp, String srcIp, int sid,
      Date startTimeDate, Date endTimeDate);

  List<Object> queryTopHundredSuricataFlowId(String sid, Date startTimeDate, Date endTimeDate);

  Map<String, Object> querySuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO);
}
