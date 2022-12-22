package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.AlertMessageDO;
import com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO;

/**
 * @author guosk
 * <p>
 * create at 2020年10月29日, fpc-manager
 */
public interface AlertMessageDao {

    Page<AlertMessageDO> queryAlertMessages(Pageable page, AlertMessageQueryVO queryVO,
                                            List<String> solverIds);

    List<AlertMessageDO> queryAlertMessages(AlertMessageQueryVO queryVO);

    long countAlertMessages(Date startTime, Date endTime, String networkId, String serviceId);

    AlertMessageDO queryAlertMessage(String id);

    int updateAlertMessageStatus(AlertMessageDO alertMessage, String status, String reason,
                                 String solverId);

    List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO);
}
