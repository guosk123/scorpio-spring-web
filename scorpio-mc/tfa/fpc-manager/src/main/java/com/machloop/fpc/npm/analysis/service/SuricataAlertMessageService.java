package com.machloop.fpc.npm.analysis.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.npm.analysis.bo.SuricataAlertMessageBO;
import com.machloop.fpc.npm.analysis.bo.SuricataRuleRelationBO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author guosk
 * <p>
 * create at 2022年4月11日, fpc-manager
 */
public interface SuricataAlertMessageService {

    Page<SuricataAlertMessageBO> querySuricataAlerts(Pageable page, SuricataRuleQueryVO queryVO);

    void exportSuricataAlerts(SuricataRuleQueryVO queryVO, String fileType, Sort sort,
                              OutputStream out, int count) throws IOException;

    Page<Map<String, Object>> querySuricataAlertMessagesAsGraph(SuricataRuleQueryVO queryVO,
                                                                PageRequest page);

    List<SuricataRuleRelationBO> queryAlterMessagesRelation(String destIp, String srcIp, int sid,
                                                            Date startTimeDate, Date endTimeDate);


    List<Object> queryTopHundredSuricataFlowId(Integer sid, Date startTimeDate, Date endTimeDate);

    Map<String, Object> fetchFlowLogPacketFileUrls(String queryId, String fileType,
                                                   SuricataRuleQueryVO queryVO, Sort sort);

    Map<String, Object> querySuricataAlertMessagesStatistics(SuricataRuleQueryVO queryVO);
}
