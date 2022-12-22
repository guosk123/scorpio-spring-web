package com.machloop.fpc.cms.center.appliance.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.bo.AlertMessageBO;
import com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO;

/**
 * @author guosk
 * <p>
 * create at 2020年10月29日, fpc-manager
 */
public interface AlertMessageService {

    Page<AlertMessageBO> queryAlertMessages(Pageable page, AlertMessageQueryVO queryVO);

    List<AlertMessageBO> queryAlertMessages(AlertMessageQueryVO queryVO);

    List<Map<String, Object>> analysisAlertMessage(Date startTime, Date endTime, int interval,
                                                   String metrics, String sourceType, String sourceValue, String networkId, String serviceId);

    long countAlertMessages(AlertMessageQueryVO queryVO);

    AlertMessageBO queryAlertMessage(String id);

    AlertMessageBO solveAlertMessage(String id, String reason, String operatorId);

    List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO);
}
